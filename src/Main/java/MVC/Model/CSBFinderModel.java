package MVC.Model;

import Core.Genomes.*;
import Core.OrthologyGroups.COG;
import Core.OrthologyGroups.CogInfo;
import Core.Patterns.InstanceLocation;
import Core.Patterns.Pattern;
import Core.Patterns.PatternLocationsInGenome;
import Core.Patterns.PatternLocationsInReplicon;
import Core.SuffixTreePatternFinder.SuffixTreeAlgorithm;
import IO.*;
import MVC.Common.*;
import Core.*;
import Core.PostProcess.Family;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CSBFinderModel {

    private CSBFinderDoneListener csbFinderDoneListener;

    private Parameters params;
    private CSBFinderWorkflow workflow;
    private List<Family> families;

    private GenomesInfo gi;
    private CogInfo cogInfo;

    private String arguments;
    private String inputGenomesPath;

    public CSBFinderModel() {

        params = new Parameters();
        workflow = null;
        families = new ArrayList<>();

        gi = new GenomesInfo();
        cogInfo = new CogInfo();
        arguments = "";
        inputGenomesPath = "";
    }

    public String getUNKchar(){
        return Alphabet.UNK_CHAR;
    }

    public String getInputGenomesPath() {
        return inputGenomesPath;
    }

    public String loadInputGenomesFile(String path) {

        String msg = "";
        try {
            gi = Parsers.parseGenomesFile(path);
            inputGenomesPath = path;
            msg = "Loaded " + gi.getNumberOfGenomes() + " genomes.";
            workflow = new CSBFinderWorkflow(gi);
        }catch(Exception e){
            gi = new GenomesInfo();
            msg = e.getMessage();
        }

        return msg;
    }

    public String loadSessionFile(String path) throws IOException {

        String msg = "";
        try {
            gi = new GenomesInfo();
            families = new ArrayList<>();
            String[] args = Parsers.parseSessionFile(families, path, gi);
            parseArgs(args);

            workflow = new CSBFinderWorkflow(gi);
            workflow.setParameters(params);
            workflow.setPatterns(families.stream().map(Family::getPatterns).flatMap(List::stream)
                    .collect(Collectors.toList()));

            csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
            msg = "Loaded session file.";
        }catch(Exception e){
            msg = e.getMessage();
            e.printStackTrace();
        }
        return msg;
    }


    private JCommander parseArgs(String[] args){
        JCommander jcommander;
        try {
            params = new Parameters();

            jcommander = JCommander.newBuilder().addObject(params).build();
            jcommander.parse(args);

            arguments = String.join(" ", args);

            return jcommander;

        } catch (ParameterException e){
            System.err.println(e.getMessage());

            return null;
        }
    }

    public String findCSBs(CSBFinderRequest request) {
        String[] args = request.toArgArray();

        return findCSBs(args);
    }

    public String findCSBs(String[] args) {
        JCommander jcommander = parseArgs(args);
        if (jcommander != null){
            return this.findCSBs();
        }
        return "";
    }


    /**
     * Need to load genomes first
     */
    private String findCSBs() {

        String msg = "";

        if (gi == null || gi.getNumberOfGenomes() == 0){
            msg = "Need to read genomes first.";
            return msg;
        }else if(workflow == null){
            msg = "CSBFinder workflow was not created yet.";
            return msg;
        }

        long startTime = System.nanoTime();

        List<Pattern> patternsFromFile = new ArrayList<>();
        try {
            patternsFromFile = readPatternsFromFile();
        }catch (Exception e){
            msg = e.getMessage();
            return msg;
        }

        workflow.setPatternsFromFile(patternsFromFile);

        Algorithm algorithm = new SuffixTreeAlgorithm();
        workflow.setAlgorithm(algorithm);

        System.out.println("Extracting CSBs from " + gi.getNumberOfGenomes() + " input sequences.");

        families = workflow.run(params);

        calculateMainFunctionalCategory();

        msg += workflow.getPatternsCount() + " CSBs found";

        System.out.println(msg);
        System.out.println("Took " + String.valueOf((System.nanoTime() - startTime) / Math.pow(10, 9)) + " seconds");

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));

        return msg;
    }

    public String clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator){
        String msg = "";
        try{
            families = workflow.clusterToFamilies(threshold, clusterBy, clusterDenominator);
            msg = "Clustered to " + families.size() + " families";
        }catch (Exception e){
            msg = "Something went wrong";
        }

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));

        return msg;
    }

    public String loadCogInfo(String path){
        cogInfo = new CogInfo();

        String msg = "";
        Map<String, COG> cogInfoTable = null;

        boolean cogInfoExists = (path != null);
        if (cogInfoExists) {
            try {
                cogInfoTable = Parsers.parseCogInfoTable(path);
                this.cogInfo.setCogInfo(cogInfoTable);
                calculateMainFunctionalCategory();
                msg = "Loaded orthology information table";
            }catch (Exception e){
                msg = e.getMessage() + "\n";
            }
        }
        return msg;
    }

    private void calculateMainFunctionalCategory(){
        if (cogInfo.cogInfoExists() && families != null){
            families.forEach(family -> family.getPatterns()
                    .forEach(pattern -> pattern.calculateMainFunctionalCategory(cogInfo)));
        }
    }

    private Writer createWriter(boolean cogInfoExists, OutputType outputType, List<Family> families){

        String outputPath = createOutputPath();

        String parameters = "_ins" + params.maxInsertion + "_q" + params.quorum2;
        String catalogFileName = "Catalog_" + params.datasetName + parameters;
        String instancesFileName = catalogFileName + "_instances";

        String catalogPath = outputPath + catalogFileName;
        //TODO: add as input parameter
        boolean includeFamilies = true;

        PatternsWriter patternsWriter = null;

        switch (outputType){
            case TXT:
                patternsWriter = new TextWriter(cogInfoExists, includeFamilies, catalogPath);
                break;
            case XLSX:
                patternsWriter = new ExcelWriter(cogInfoExists, includeFamilies, catalogPath);
                break;
            case EXPORT:
                SessionWriter sessionWriter = new SessionWriter(includeFamilies, catalogPath, gi);
                sessionWriter.writeHeader(arguments);
                sessionWriter.writeGenomes(gi.getGenomesByName());
                patternsWriter = sessionWriter;
                break;
        }

        Writer writer = new Writer(params.debug,
                instancesFileName, includeFamilies, cogInfoExists,
                outputPath, patternsWriter);

        if (outputType != OutputType.EXPORT){
            writer.printInstances(families, gi);
            writer.writeHeader(createHeader(includeFamilies));
        }

        writer.printFamilies(families, cogInfo);
        writer.closeFiles();

        return writer;
    }

    private String createOutputPath(){
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("dd_MM_yyyy_hh_mm_ss_a");

        String path = params.outputDir;
        Writer.createOutputDirectory(path);
        path += "/"+ft.format(dNow)+"/";
        Writer.createOutputDirectory(path);

        return path;
    }

    public String saveOutputFiles(OutputType outputFileType, String outputDir, String datasetName,
                                  List<Family> families) {
        System.out.println("Writing to files");

        String msg = "";
        try {
            params.outputDir = outputDir;
            params.datasetName = datasetName;
            Writer writer = createWriter(cogInfo.cogInfoExists(), outputFileType, families);
            msg = "CSBs written to files.";
        }catch (Exception e){
            msg = e.getMessage();
        }

        return msg;
    }

    private String createHeader(boolean include_families){

        String header = "CSB_ID\tLength\tScore\tInstance_Count\tCSB";
        if (cogInfo.cogInfoExists()){
            header += "\tMain_Category";
        }
        if (include_families){
            header += "\tFamily_ID";
        }

        return header;
    }

    /**
     * Read patterns from a file if a file is given, and putWithSuffix them in a suffix trie
     * @return
     */
    private List<Pattern> readPatternsFromFile() throws Exception{
        List<Pattern> patterns = new ArrayList<>();
        if (params.inputPatternsFilePath != null) {
            //these arguments are not valid when input patterns are given
            //params.minPatternLength = 2;
            //params.maxPatternLength = Integer.MAX_VALUE;

            String path = params.inputPatternsFilePath;
            patterns = Parsers.parsePatternsFile(path);
        }
        return patterns;
    }

    public List<Family> getFamilies() {
        return families;
    }

    public void setCSBFinderDoneListener(CSBFinderDoneListener csbFinderDoneListener) {
        this.csbFinderDoneListener = csbFinderDoneListener;
    }

    public List<COG> getCogInfo(List<Gene> genes) {
        List<COG> currCogInfo = new ArrayList<COG>();
        if (cogInfo.cogInfoExists()) {
            genes.forEach(gene -> {
                COG c = cogInfo.getCog(gene.getCogId());
                if (c != null) {
                    currCogInfo.add(c);
                }
            });
        }

        return currCogInfo;
    }

    public Set<COG> getInsertedGenes(Pattern pattern, List<COG> patternCOGs) {

        Set<COG> insertedGenes = new HashSet<>();

        if (params.maxInsertion > 0) {
            Set<COG> patternGenesSet = new HashSet<>();
            patternGenesSet.addAll(patternCOGs);

            for (PatternLocationsInGenome instancesMap : pattern.getPatternLocations().values()) {
                for (PatternLocationsInReplicon patternLocationsInReplicon : instancesMap.getRepliconToLocations().values()) {
                    for (InstanceLocation instance : patternLocationsInReplicon.getSortedLocations()) {
                        List<COG> instanceGenes = getCogInfo(getGenes(instance));
                        Set<COG> instanceGenesSet = new HashSet<>();
                        instanceGenesSet.addAll(instanceGenes);
                        instanceGenesSet.removeAll(patternGenesSet);
                        insertedGenes.addAll(instanceGenesSet);
                    }
                }
            }
        }
        return insertedGenes;
    }

    private List<Gene> getGenes(InstanceLocation instance){
        Genome genome = gi.getGenome(instance.getGenomeId());
        Replicon replicon = genome.getReplicon(instance.getRepliconId());

        List<Gene> instanceList = new ArrayList<>();
        List<Gene> repliconGenes = replicon.getGenes();

        if (repliconGenes != null) {
            instanceList = repliconGenes.subList(instance.getActualStartIndex(), instance.getActualEndIndex());
        }

        return instanceList;
    }

    public int getNumberOfGenomes() {
        return gi.getNumberOfGenomes();
    }

    public GenomesInfo getGenomeInfo(){
        return gi;
    }

    public Genome getGenome(String genomeName){
        return gi.getGenome(genomeName);
    }

    public int getMaxGenomeSize(){
        return gi.getMaxGenomeSize();
    }
}
