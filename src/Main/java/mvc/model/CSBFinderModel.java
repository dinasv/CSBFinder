package mvc.model;

import mvc.view.events.UpdateFamiliesEvent;
import mvc.view.graphics.GeneColors;
import mvc.view.listeners.UpdateFamiliesListener;
import mvc.view.requests.CSBFinderRequest;
import model.genomes.*;
import model.cogs.COG;
import model.cogs.CogInfo;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;
import io.*;
import model.*;
import model.postprocess.Family;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CSBFinderModel {

    private UpdateFamiliesListener updateFamiliesListener;

    private Parameters params;
    private CSBFinderWorkflow workflow;

    private GenomesInfo gi;
    private CogInfo cogInfo;
    private Map<String,Taxon> genomeToTaxa;
    private Map<String,Object[]> genomeToMetadata;
    private String[] genomeMetadataColumnNames;

    private String arguments;
    private String inputGenomesPath;

    private GeneColors colors;

    public CSBFinderModel() {

        params = new Parameters();
        workflow = null;

        gi = new GenomesInfo();
        cogInfo = new CogInfo();
        arguments = "";
        inputGenomesPath = "";
        genomeToTaxa = new HashMap<>();
        genomeToMetadata = new HashMap<>();
        genomeMetadataColumnNames = new String[0];

        colors = new GeneColors();
    }

    public void setGeneColors(GeneColors colors){
        this.colors = colors;
    }

    public String getUNKchar(){
        return Alphabet.UNK_CHAR;
    }

    public String getInputGenomesPath() {
        return inputGenomesPath;
    }

    public void loadInputGenomesFile(String path) throws IOException {

        try {
            gi = Parsers.parseGenomesFile(path, params.circular);
            inputGenomesPath = path;
            workflow = new CSBFinderWorkflow(gi);
        }catch(Exception e){
            gi = new GenomesInfo();
            throw e;
        }
    }

    public void loadSessionFile(String path) throws Exception {

        String msg = "";
        gi = new GenomesInfo();
        workflow = null;

        String[] args = Parsers.parseSessionFileFirstLine(path);
        JCommander jCommander = parseArgs(args);

        if (jCommander == null){
            throw new IOException(String.format("The first line in the file %s should contain valid arguments", path));
        }

        List<Family> families = new ArrayList<>();
        Parsers.parseSessionFile(families, path, gi, colors, params.circular);

        workflow = new CSBFinderWorkflow(gi);
        workflow.setParameters(params);
        workflow.setFamilies(families);

        updateFamiliesListener.UpdateFamiliesOccurred(new UpdateFamiliesEvent(families));
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

        long startTime = System.nanoTime();

        List<Pattern> patternsFromFile;

        //could throw exception
        patternsFromFile = readPatternsFromFile();

        workflow.setPatternsFromFile(patternsFromFile);

        Algorithm algorithm = params.algorithmType.getAlgorithm();
        workflow.setAlgorithm(algorithm);

        System.out.println("Extracting CSBs from " + gi.getNumberOfGenomes() + " input sequences.");

        workflow.run(params);

        System.out.println("Took " + (System.nanoTime() - startTime) / Math.pow(10, 9) + " seconds");

        updateFamiliesListener.UpdateFamiliesOccurred(new UpdateFamiliesEvent(workflow.getFamilies()));

    }

    public void clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator)
            throws ParameterException{

        try{
            workflow.clusterToFamilies(threshold, clusterBy, clusterDenominator);
            updateFamiliesListener.UpdateFamiliesOccurred(new UpdateFamiliesEvent(workflow.getFamilies()));
        }catch (Exception e){
            throw new ParameterException("Something went wrong");
        }

    }

    public void computeScores(double threshold) throws ParameterException{
        try{
            workflow.computeScores(threshold);
            updateFamiliesListener.UpdateFamiliesOccurred(new UpdateFamiliesEvent(workflow.getFamilies()));
        }catch (Exception e){
            throw new ParameterException("Something went wrong");
        }
    }


    public void loadCogInfo(String path) throws Exception {
        cogInfo = new CogInfo();

        Map<String, COG> cogInfoTable = null;

        boolean cogInfoExists = (path != null);
        if (cogInfoExists) {
            cogInfoTable = Parsers.parseCogInfoTable(path);
            this.cogInfo.setCogInfo(cogInfoTable);
            calculateMainFunctionalCategory();
        }
    }


    public void loadTaxa(String path) throws IOException, IllegalArgumentException {

        genomeToTaxa = new HashMap<>();

        if (path != null) {

            genomeToTaxa = Parsers.parseTaxaFile(path);

        }
    }

    public void loadMetadata(String path) throws IOException, IllegalArgumentException {

        genomeToMetadata = new HashMap<>();

        if (path != null) {

            genomeMetadataColumnNames = Parsers.parseMetadataFileHeader(path);
            genomeToMetadata = Parsers.parseMetadataFile(path);

        }
    }

    public void calculateMainFunctionalCategory(){
        if (cogInfo.cogInfoExists() && workflow.getFamilies() != null){
            workflow.getFamilies().forEach(family -> family.getPatterns()
                    .forEach(pattern -> pattern.calculateMainFunctionalCategory(cogInfo)));
        }
    }

    public void saveSession(List<Family> families, File currentSession, GeneColors colors){
        Writer writer = WriteUtils.saveSessionFile(families, gi,  cogInfo, params, arguments, colors, currentSession);
        //this.families = families;
        workflow.setFamilies(families);
        //updateFamiliesListener.UpdateFamiliesOccurred(new UpdateFamiliesEvent(families));
    }


    public void exportFiles(OutputType outputFileType, String outputDir, String datasetName,
                            List<Family> families) {

        System.out.println("Writing to files");

        params.outputDir = outputDir;
        params.exportFileName = datasetName;
        params.outputFileType = outputFileType;

        Writer writer = WriteUtils.writeExportFiles(families, gi,  cogInfo, params, arguments);
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

    public void setCSBFinderDoneListener(UpdateFamiliesListener updateFamiliesListener) {
        this.updateFamiliesListener = updateFamiliesListener;
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
    public Map<String, Object[]> getGenomeToMetadata() {
        return genomeToMetadata;
    }

    public String[] getGenomeMetadataColumnNames() {
        return genomeMetadataColumnNames;
    }
}
