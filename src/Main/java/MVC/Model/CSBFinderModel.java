package MVC.Model;

import Core.Genomes.*;
import IO.MyLogger;
import IO.Parsers;
import IO.Writer;
import MVC.Common.*;
import Core.*;
import Core.PostProcess.Family;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CSBFinderModel {

    private GenomesLoadedListener genomesLoadedListener;
    private CSBFinderDoneListener csbFinderDoneListener;

    private Parameters params;
    private CSBFinderWorkflow workflow;
    private List<Family> families;

    private int numberOfGenomes;

    private GenomesInfo gi;
    CogInfo cogInfo;

    public CSBFinderModel() {
    }

    public String getUNKchar(){
        return gi.UNK_CHAR;
    }

    public MyLogger logger = new MyLogger("",true);


    public String loadInputGenomesFile(String path) {
        cogInfo = new CogInfo();
        initGenomesInfo();

        String msg = "";
        try {
            numberOfGenomes = Parsers.parseGenomesFile(path, gi);
            msg = "Loaded " + numberOfGenomes + " genomes.";
            workflow = new CSBFinderWorkflow(gi);
        }catch(Exception e){
            msg = e.getMessage();
        }

        return msg;
    }

    private void initGenomesInfo(){
        gi = new GenomesInfo();
        numberOfGenomes = -1;
    }


    private JCommander parseArgs(String[] args){
        JCommander jcommander;
        try {
            params = new Parameters();

            jcommander = JCommander.newBuilder().addObject(params).build();
            jcommander.parse(args);
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
        long startTime = System.nanoTime();
        Map<String, COG> cog_info = null;

        if (gi == null || gi.getNumberOfGenomes() == 0){
            msg = "Need to read genomes first.";
            System.out.println(msg);
            return msg;
        }else if(workflow == null){
            msg = "CSBFinder workflow was not created yet.";
            System.out.println(msg);
            return msg;
        }

        List<Pattern> patternsFromFile = new ArrayList<>();
        try {
            patternsFromFile = readPatternsFromFile();
        }catch (Exception e){
            msg = e.getMessage();
            return msg;
        }

        boolean cog_info_exists = (params.cogInfoFilePath != null);
        if (cog_info_exists) {
            try {
                cog_info = Parsers.parseCogInfoTable(params.cogInfoFilePath);
                cogInfo.setCogInfo(cog_info);
            }catch (Exception e){
                msg = e.getMessage() + "\n";
            }
        }

        System.out.println("Extracting CSBs from " + numberOfGenomes + " input sequences.");

        if (patternsFromFile.size() > 0){
            families = workflow.run(params, patternsFromFile);
        }else{
            families = workflow.run(params);
        }

        msg += workflow.getPatternsCount() + " CSBs found";

        System.out.println(msg);
        System.out.println("Took " + String.valueOf((System.nanoTime() - startTime) / Math.pow(10, 9)) + " seconds");

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));

        return msg;
    }

    private Writer createWriter(boolean cog_info_exists, OutputType outputType){
        String parameters = "_ins" + params.maxInsertion + "_q" + params.quorum2;
        String catalog_file_name = "Catalog_" + params.datasetName + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;

        Writer writer = new Writer(params.maxError, params.maxDeletion, params.maxInsertion, params.debug, catalog_file_name,
                instances_file_name,
                include_families, outputType, cog_info_exists, params.nonDirectons, createOutputPath());

        return writer;
    }

    private static String createOutputPath(){
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("dd_MM_yyyy_hh_mm_ss_a");

        String path = "output";
        Writer.createOutputDirectory(path);
        path += "/"+ft.format(dNow)+"/";
        Writer.createOutputDirectory(path);

        return path;
    }

    public void saveOutputFiles(String outputFileType) {

         Writer writer = createWriter(params.cogInfoFilePath != null && !"".equals(params.cogInfoFilePath),
                OutputType.valueOf(outputFileType));

        System.out.println("Writing to files");
        for (Family family : families) {
            writer.printTopScoringPattern(family.getPatterns().get(0), gi, family.getFamilyId(), cogInfo);
            for (Pattern pattern : family.getPatterns()) {
                writer.printPattern(pattern, gi, family.getFamilyId(), cogInfo);
            }
        }
        writer.closeFiles();
    }

    /**
     * Read patterns from a file if a file is given, and put them in a suffix trie
     * @return
     */
    private List<Pattern> readPatternsFromFile() throws Exception{
        List<Pattern> patterns = new ArrayList<>();
        if (params.inputPatternsFilePath != null) {
            //these arguments are not valid when input patterns are give
            params.minPatternLength = 2;
            params.maxPatternLength = Integer.MAX_VALUE;

            String path = params.inputPatternsFilePath;
            patterns = Parsers.parsePatternsFile(path);
        }
        return patterns;
    }



    public List<Family> getFamilies() {
        return families;
    }

    public void setGenomesLoadedListener(GenomesLoadedListener genomesLoadedListener) {
        this.genomesLoadedListener = genomesLoadedListener;
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

    public Set<COG> getInsertedGenes(Map<String, Map<String, List<InstanceInfo>>> instances, List<COG> patternGenes) {

        Set<COG> insertedGenes = new HashSet<COG>();

        if (params.maxInsertion > 0) {
            Set<COG> patternGenesSet = new HashSet<>();
            patternGenesSet.addAll(patternGenes);

            for (Map<String, List<InstanceInfo>> instancesMap : instances.values()) {
                for (List<InstanceInfo> instancesList : instancesMap.values()) {
                    for (InstanceInfo instance : instancesList) {
                        List<COG> instanceGenes = getCogInfo(instance.getGenes());
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

    public Map<String, Map<String, List<InstanceInfo>>> getInstances(Pattern pattern){

        Map<String, Map<String, List<InstanceInfo>>> instances = new HashMap<>();
        Map<Integer, PatternLocationsInGenome> locationsPerGenome = pattern.getPatternLocations();

        for (Map.Entry<Integer, PatternLocationsInGenome> genomeToRepliconsLocations : locationsPerGenome.entrySet()) {

            String genomeName = gi.getGenomeName(genomeToRepliconsLocations.getKey());

            PatternLocationsInGenome repliconInstanceLocations = genomeToRepliconsLocations.getValue();

            Map<String, List<InstanceInfo>> repliconInstances = new HashMap<>();

            for (Map.Entry<Integer, List<InstanceLocation>> replicon2locations : repliconInstanceLocations.getSortedLocations().entrySet()) {

                List<InstanceInfo> instanceLocations = new ArrayList<>();

                int replicon_id = replicon2locations.getKey();
                String replicon_name = gi.getRepliconName(replicon2locations.getKey());
                List<InstanceLocation> instances_locations = replicon2locations.getValue();

                //instances_locations.sort(Comparator.comparing(InstanceLocation::getActualStartIndex));

                for (InstanceLocation instance_location : instances_locations) {
                    instance_location.setRepliconName(replicon_name);
                    List<Gene> genes = getInstanceFromCogList(genomeName, replicon_id, instance_location.getActualStartIndex(),
                            instance_location.getActualEndIndex());
                    if (genes != null) {
                        instanceLocations.add(new InstanceInfo(instance_location, genes));
                    }
                }
                if (instanceLocations.size() > 0) {
                    repliconInstances.put(replicon_name, instanceLocations);
                }
            }
            if (repliconInstances.size() > 0) {
                instances.put(genomeName, repliconInstances);
            }
        }

        return instances;
    }

    private List<Gene> getInstanceFromCogList(String genomeName, int replicon_id, int startIndex, int endIndex) {
        List<Gene> instanceList = null;
        Genome genome = getGenome(genomeName);
        List<Gene> repliconGenes = genome.getReplicon(replicon_id).getGenes();
        if (repliconGenes != null) {
            if (startIndex >= 0 && startIndex < repliconGenes.size() &&
                    endIndex >= 0 && endIndex <= repliconGenes.size()) {

                instanceList = repliconGenes.subList(startIndex, endIndex);
            } else {
//                writer.writeLogger(String.format("WARNING: replicon is out of bound in sequence %s, start: %s,length: %s",
//                        genomeName, startIndex, instanceLength));
            }
        } else {
//            writer.writeLogger(String.format("WARNING: Genome %s not found", genomeName));
        }

        return instanceList;
    }


    public int getNumberOfGenomes() {
        return numberOfGenomes;
    }

    public Map<String, Genome> getGenomeMap() {
        return gi.getGenomesMap();
    }

    public Genome getGenome(String genomeName){
        return gi.getGenome(genomeName);
    }

    public int getMaxGenomeSize(){
        return gi.getMaxGenomeSize();
    }
}
