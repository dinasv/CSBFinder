package MVC.Model;

import Model.Genomes.*;
import Model.OrthologyGroups.COG;
import Model.OrthologyGroups.CogInfo;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import IO.*;
import MVC.Common.*;
import Model.*;
import Model.PostProcess.Family;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CSBFinderModel {

    private CSBFinderDoneListener csbFinderDoneListener;

    private Parameters params;
    private CSBFinderWorkflow workflow;
    private List<Family> families;

    private GenomesInfo gi;
    private CogInfo cogInfo;
    private Map<String,Taxon> genomeToTaxa;

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
        genomeToTaxa = new HashMap<>();
    }

    public String getUNKchar(){
        return Alphabet.UNK_CHAR;
    }

    public String getInputGenomesPath() {
        return inputGenomesPath;
    }

    public void loadInputGenomesFile(String path) throws IOException {

        try {
            gi = Parsers.parseGenomesFile(path);
            inputGenomesPath = path;
            workflow = new CSBFinderWorkflow(gi);
        }catch(Exception e){
            gi = new GenomesInfo();
            throw e;
        }
    }

    public void loadSessionFile(String path) throws IOException {

        String msg = "";
        gi = new GenomesInfo();
        families = new ArrayList<>();
        workflow = null;

        String[] args = Parsers.parseSessionFileFirstLine(path);
        JCommander jCommander = parseArgs(args);

        if (jCommander == null){
            throw new IOException(String.format("The first line in the file %s should contain valid arguments", path));
        }

        Parsers.parseSessionFile(families, path, gi);

        workflow = new CSBFinderWorkflow(gi);
        workflow.setParameters(params);
        workflow.setPatterns(families.stream().map(Family::getPatterns).flatMap(List::stream)
                .collect(Collectors.toList()));

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
        msg = "Loaded session file.";

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

    public void findCSBs(CSBFinderRequest request) throws IOException, IllegalArgumentException {
        String[] args = request.toArgArray();

        findCSBs(args);
    }

    public void findCSBs(String[] args) throws IOException, IllegalArgumentException {
        JCommander jcommander = parseArgs(args);
        if (jcommander != null){
            this.findCSBs();
        }
    }


    /**
     * Need to load genomes first
     */
    private void findCSBs() throws IOException, IllegalArgumentException{

        if (gi == null || gi.getNumberOfGenomes() == 0){
            throw new IllegalStateException("Need to read genomes first.");
        }else if(workflow == null){
            throw new IllegalStateException("CSBFinder workflow was not created yet.");
        }

        families = new ArrayList<>();

        long startTime = System.nanoTime();

        List<Pattern> patternsFromFile;

        //could throw exception
        patternsFromFile = readPatternsFromFile();

        workflow.setPatternsFromFile(patternsFromFile);

        Algorithm algorithm = params.algorithmType.algorithm;
        workflow.setAlgorithm(algorithm);

        System.out.println("Extracting CSBs from " + gi.getNumberOfGenomes() + " input sequences.");

        families = workflow.run(params);

        System.out.println("Took " + (System.nanoTime() - startTime) / Math.pow(10, 9) + " seconds");

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));

    }

    public void clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator)
            throws ParameterException{

        try{
            families = workflow.clusterToFamilies(threshold, clusterBy, clusterDenominator);
            csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
        }catch (Exception e){
            throw new ParameterException("Something went wrong");
        }

    }

    public void computeScores(double threshold) throws ParameterException{
        try{
            workflow.computeScores(threshold);
            families.forEach(Family::sortPatternsAndSetScore);
            csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
        }catch (Exception e){
            throw new ParameterException("Something went wrong");
        }
    }


    public void loadCogInfo(String path) throws IOException {
        cogInfo = new CogInfo();

        Map<String, COG> cogInfoTable = null;

        boolean cogInfoExists = (path != null);
        if (cogInfoExists) {
            cogInfoTable = Parsers.parseCogInfoTable(path);
            this.cogInfo.setCogInfo(cogInfoTable);
            calculateMainFunctionalCategory();
        }
    }


    public void loadTaxa(String path) throws IOException {

        genomeToTaxa = new HashMap<>();

        if (path != null) {

            genomeToTaxa = Parsers.parseTaxaFile(path);

        }
    }

    public void calculateMainFunctionalCategory(){
        if (cogInfo.cogInfoExists() && families != null){
            families.forEach(family -> family.getPatterns()
                    .forEach(pattern -> pattern.calculateMainFunctionalCategory(cogInfo)));
        }
    }


    public void saveOutputFiles(OutputType outputFileType, String outputDir, String datasetName,
                                  List<Family> families) {
        System.out.println("Writing to files");

        params.outputDir = outputDir;
        params.datasetName = datasetName;
        params.outputFileType = outputFileType;

        Writer writer = WriteUtils.writeFamiliesToFiles(families, gi,  cogInfo, params, arguments);
    }

    /**
     * Read patterns from a file if a file is given, and putWithSuffix them in a suffix trie
     * @return
     */
    private List<Pattern> readPatternsFromFile() throws IOException, IllegalArgumentException{
        List<Pattern> patterns = new ArrayList<>();
        if (params.inputPatternsFilePath != null) {

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

    public List<COG> getCogsInfo(Gene[] genes) {
        List<COG> currCogInfo = new ArrayList<>();
        if (cogInfo.cogInfoExists()) {
            Arrays.stream(genes).forEach(gene -> {
                COG c = getCogInfo(gene.getCogId());
                if (c != null) {
                    currCogInfo.add(c);
                }
            });
        }

        return currCogInfo;
    }

    public COG getCogInfo(String cogId){
        return cogInfo.getCog(cogId);
    }

    public Set<COG> getInsertedGenes(Pattern pattern, List<COG> patternCOGs) {

        Set<COG> insertedGenes = new HashSet<>();

        if (params.maxInsertion > 0) {
            Set<COG> patternGenesSet = new HashSet<>(patternCOGs);

            for (InstanceLocation instance : pattern.getPatternLocations().getInstanceLocations()) {
                List<COG> instanceGenes = getCogsInfo(getGenes(instance));
                Set<COG> instanceGenesSet = new HashSet<>(instanceGenes);
                instanceGenesSet.removeAll(patternGenesSet);
                insertedGenes.addAll(instanceGenesSet);
            }

        }
        return insertedGenes;
    }

    private Gene[] getGenes(InstanceLocation instance){
        Genome genome = gi.getGenome(instance.getGenomeId());
        Replicon replicon = genome.getReplicon(instance.getRepliconId());

        List<Gene> instanceList = new ArrayList<>();
        List<Gene> repliconGenes = replicon.getGenes();

        if (repliconGenes != null) {
            instanceList = repliconGenes.subList(instance.getActualStartIndex(), instance.getActualEndIndex());
        }

        Gene[] instanceGenes = new Gene[instanceList.size()];

        return instanceList.toArray(instanceGenes);
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

    public Map<String, Taxon> getGenomeToTaxa() {
        return genomeToTaxa;
    }
}
