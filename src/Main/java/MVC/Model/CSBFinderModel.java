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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CSBFinderModel {

    private GenomesLoadedListener genomesLoadedListener;
    private CSBFinderDoneListener csbFinderDoneListener;

    private Parameters params;
    //private GeneralizedSuffixTree dataset_suffix_tree;
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


    public void loadInputGenomesFile(String path) {
        cogInfo = new CogInfo();
        gi = new GenomesInfo();
        try {
            numberOfGenomes = Parsers.parseGenomesFile(path, gi);
        }catch(IOException e){
            //TODO: show error message
        }
        System.out.println("Loaded " + numberOfGenomes + " genomes.");
        workflow = new CSBFinderWorkflow(gi);

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

    public void findCSBs(CSBFinderRequest request) {
        String[] args = request.toArgArray();

        findCSBs(args);
    }

    public void findCSBs(String[] args) {
        JCommander jcommander = parseArgs(args);
        if (jcommander != null){

            this.findCSBs();
        }
    }


    /**
     * Need to load genomes first
     */
    private void findCSBs() {

        if (gi == null || gi.getNumberOfGenomes() == 0){
            System.out.println("Need to read genomes first.");
            return;
        }else if(workflow == null){
            System.out.println("CSBFinder workflow was not created yet.");
            return;
        }

        long startTime = System.nanoTime();

        Map<String, COG> cog_info = null;
        boolean cog_info_exists = (params.cogInfoFileName != null);
        if (cog_info_exists) {
            cog_info = Parsers.parseCogInfoTable(params.cogInfoFileName);
        }
        cogInfo.setCogInfo(cog_info);


        List<Pattern> patternsFromFile = readPatternsFromFile();

        System.out.println("Extracting CSBs from " + numberOfGenomes + " input sequences.");

        if (patternsFromFile != null){
            families = workflow.run(params, patternsFromFile);
        }else{
            families = workflow.run(params);
        }


        System.out.println(workflow.getPatternsCount() + " CSBs found");
        System.out.println("Took " + String.valueOf((System.nanoTime() - startTime) / Math.pow(10, 9)) + " seconds");

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
    }

    private Writer createWriter(boolean cog_info_exists, Parameters.OutputType outputType){
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

         Writer writer = createWriter(params.cogInfoFileName != null && !"".equals(params.cogInfoFileName),
                Parameters.OutputType.valueOf(outputFileType));

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
    private List<Pattern> readPatternsFromFile() {
        List<Pattern> patterns = null;
        if (params.inputPatternsFileName != null) {
            //these arguments are not valid when input patterns are give
            params.minPatternLength = 2;
            params.maxPatternLength = Integer.MAX_VALUE;

            String path = params.inputPatternsFileName;
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

    public List<COG> getCogInfo(List<String> cogs) {
        List<COG> currCogInfo = new ArrayList<COG>();
        if (cogInfo.cogInfoExists()) {
            cogs.forEach(cog -> {
                COG c = cogInfo.getCog(cog);
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
                        List<COG> instanceGenes = getCogInfo(instance.getGenes().stream().map(gene -> gene.getCogId()).collect(Collectors.toList()));
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
        Map<Integer, Map<Integer, List<InstanceLocation>>> sameSeqInstances = groupSameSeqInstances(pattern);

        for (Map.Entry<Integer, Map<Integer, List<InstanceLocation>>> seq2replicons : sameSeqInstances.entrySet()) {

            String seq_name = gi.getGenomeName(seq2replicons.getKey());

            Map<Integer, List<InstanceLocation>> repliconInstanceLocations = seq2replicons.getValue();

            Map<String, List<InstanceInfo>> repliconInstances = new HashMap<>();

            for (Map.Entry<Integer, List<InstanceLocation>> replicon2locations : repliconInstanceLocations.entrySet()) {

                List<InstanceInfo> instanceLocations = new ArrayList<>();

                int replicon_id = replicon2locations.getKey();
                String replicon_name = gi.getRepliconName(replicon2locations.getKey());
                List<InstanceLocation> instances_locations = replicon2locations.getValue();

                instances_locations.sort(Comparator.comparing(InstanceLocation::getActualStartIndex));

                for (InstanceLocation instance_location : instances_locations) {
                    instance_location.setRepliconName(replicon_name);
                    List<Gene> genes = getInstanceFromCogList(seq_name, replicon_id, instance_location.getActualStartIndex(),
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
                instances.put(seq_name, repliconInstances);
            }
        }

        return instances;
    }

    private List<Gene> getInstanceFromCogList(String seq_name, int replicon_id, int startIndex, int endIndex) {
        List<Gene> instanceList = null;
        Genome genome = getGenomeMap().get(seq_name);
        List<Gene> genomeToCogList = genome.getReplicon(replicon_id).getGenes();
        if (genomeToCogList != null) {
            if (startIndex >= 0 && startIndex < genomeToCogList.size() &&
                    endIndex >= 0 && endIndex <= genomeToCogList.size()) {
                /*if (startIndex >= endIndex) {
                    int tmp = startIndex;
                    startIndex = endIndex;
                    endIndex = tmp;
                }*/
                instanceList = genomeToCogList.subList(startIndex, endIndex);
            } else {
//                writer.writeLogger(String.format("WARNING: replicon is out of bound in sequence %s, start: %s,length: %s",
//                        seq_name, startIndex, instanceLength));
            }
        } else {
//            writer.writeLogger(String.format("WARNING: Genome %s not found", seq_name));
        }

        return instanceList;
    }

    /**
     * Group all instances of pattern that are located in the same sequence
     * @param pattern
     * @return
     */
    private Map<Integer, Map<Integer, List<InstanceLocation>>> groupSameSeqInstances(Pattern pattern){
        Map<Integer, Map<Integer, List<InstanceLocation>>> instance_seq_to_location = new HashMap<>();
        for (Instance instance : pattern.get_instances()) {

            int instance_length = instance.getLength();
            for (Map.Entry<Integer, List<InstanceLocation>> entry : instance.getInstanceLocations().entrySet()) {
                int seq_key = entry.getKey();

                if (!instance_seq_to_location.containsKey(seq_key)) {
                    instance_seq_to_location.put(seq_key, new HashMap<>());
                }
                Map<Integer, List<InstanceLocation>> instancesRepliconsMap = instance_seq_to_location.get(seq_key);

                for (InstanceLocation instanceLocation : entry.getValue()) {
                    instanceLocation.changeInstanceLength(instance_length);

                    if (! instancesRepliconsMap.containsKey(instanceLocation.getRepliconId())){
                        instancesRepliconsMap.put(instanceLocation.getRepliconId(), new ArrayList<>());
                    }
                    List<InstanceLocation> repliconLocations = instancesRepliconsMap.get(instanceLocation.getRepliconId());
                    repliconLocations.add(instanceLocation);
                }
            }
        }
        return instance_seq_to_location;
    }

    public int getNumberOfGenomes() {
        return numberOfGenomes;
    }

    public Map<String, Genome> getGenomeMap() {
        return gi.getGenomesMap();
    }

    public int getMaxGenomeSize(){
        return gi.getMaxGenomeSize();
    }
}
