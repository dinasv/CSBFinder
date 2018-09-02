package MVC.Model;

import IO.MyLogger;
import IO.Readers;
import IO.Writer;
import MVC.Common.*;
import CLI.*;
import PostProcess.Family;
import PostProcess.FamilyClustering;
import SuffixTrees.*;
import Utils.Utils;
import Utils.Gene;
import Utils.COG;
import Utils.Pattern;
import Utils.Instance;
import Utils.InstanceLocation;
import Utils.Replicon;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.text.SimpleDateFormat;
import java.util.*;

public class CSBFinderModel {

    private GenomesLoadedListener genomesLoadedListener;
    private CSBFinderDoneListener csbFinderDoneListener;

    private CommandLineArgs cla;
    private Utils utils;
    private GeneralizedSuffixTree dataset_suffix_tree;
    private List<Family> families;

    private int number_of_genomes;

    public CSBFinderModel() {

    }

    public MyLogger logger = new MyLogger("",true);
    public void init() {
        this.utils = new Utils(null, logger);
    }

    public void loadInputGenomesFile(String path) {
        this.init();
        dataset_suffix_tree = new GeneralizedSuffixTree();
        number_of_genomes = utils.readAndBuildDatasetTree(path, dataset_suffix_tree, false);


    }

    private JCommander parseArgs(String[] args){
        JCommander jcommander = null;
        try {
            cla = new CommandLineArgs();

            jcommander = JCommander.newBuilder().addObject(cla).build();
            jcommander.parse(args);
            return jcommander;

        } catch (ParameterException e){
            System.err.println(e.getMessage());

            return null;
            //jcommander = JCommander.newBuilder().addObject(cla).build();
//            printUsageAndExit(jcommander, 1);
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

        if (dataset_suffix_tree == null){
            System.out.println("dataset_suffix_tree does not exist");
            return;
        }

        long startTime = System.nanoTime();

        Map<String, COG> cog_info = null;
        boolean cog_info_exists = (cla.cog_info_file_name != null);
        if (cog_info_exists) {
            cog_info = Readers.read_cog_info_table(cla.cog_info_file_name);
        }

        utils.setCogInfo(cog_info);

        Trie pattern_tree = buildPatternsTree(utils);

        System.out.println("Extracting CSBs from " + number_of_genomes + " input sequences.");

        CSBFinderCore csbFinderCore = new CSBFinderCore(cla, dataset_suffix_tree, pattern_tree, utils, cla.debug);

        if (cla.input_patterns_file_name == null) {
            csbFinderCore.removeRedundantPatterns();
        }

        List<Pattern> patterns = csbFinderCore.getPatterns();

        for (Pattern pattern : patterns) {
            pattern.calculateScore(utils, cla.max_insertion, cla.max_error, cla.max_deletion);
            pattern.calculateMainFunctionalCategory(utils, cla.non_directons);
        }

        System.out.println("Clustering to families");
        families = FamilyClustering.Cluster(patterns, cla.threshold, cla.cluster_by, utils,
                cla.non_directons);

        long patternCount = 0;
        for (Family family : families) {
            patternCount += family.getPatterns().stream().filter(pattern -> pattern != null).count();
        }

        System.out.println(patternCount + " CSBs found");
        System.out.println("Took " + String.valueOf((System.nanoTime() - startTime) / Math.pow(10, 9)) + " seconds");

        csbFinderDoneListener.CSBFinderDoneOccurred(new CSBFinderDoneEvent(families));
    }

    private Writer createWriter(boolean cog_info_exists, CommandLineArgs.OutputType outputType){
        String parameters = "_ins" + cla.max_insertion + "_q" + cla.quorum2;
        String catalog_file_name = "Catalog_" + cla.dataset_name + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;

        Writer writer = new Writer(cla.max_error, cla.max_deletion, cla.max_insertion, cla.debug, catalog_file_name,
                instances_file_name,
                include_families, outputType, cog_info_exists, cla.non_directons, createOutputPath());

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

         Writer writer = createWriter(cla.cog_info_file_name != null && !"".equals(cla.cog_info_file_name),
                CommandLineArgs.OutputType.valueOf(outputFileType));

        System.out.println("Writing to files");
        for (Family family : families) {
            writer.printFilteredCSB(family.getPatterns().get(0), utils, family.getFamilyId());
            for (Pattern pattern : family.getPatterns()) {
                writer.printPattern(pattern, utils, family.getFamilyId());
            }
        }
        writer.closeFiles();
    }

    private Trie buildPatternsTree(Utils utils) {
        Trie pattern_tree = null;
        if (cla.input_patterns_file_name != null) {
            //these arguments are not valid when input patterns are give
            cla.min_pattern_length = 2;
            cla.max_pattern_length = Integer.MAX_VALUE;

            pattern_tree = new Trie(TreeType.STATIC);
            String path = cla.input_patterns_file_name;
            if (!utils.buildPatternsTreeFromFile(path, pattern_tree)){
                pattern_tree = null;//if tree building wasn't successful
            }
        }
        return pattern_tree;
    }

    public Map<String, Map<String, Replicon>> getGenomeRepliconsMap() {
        return utils.getGenomeToRepliconsMap();
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
        List<COG> cogInfo = new ArrayList<COG>();
        if (utils.getCogInfo() != null) {
            cogs.forEach(cog -> {
                COG c = utils.getCogInfo().get(cog);
                if (c != null) {
                    cogInfo.add(c);
                }
            });
        }

        return cogInfo;
    }

     public Map<String, Map<String, List<InstanceInfo>>> getInstances(Pattern pattern){

        Map<String, Map<String, List<InstanceInfo>>> instances = new HashMap<>();
        Map<Integer, Map<Integer, List<InstanceLocation>>> sameSeqInstances = groupSameSeqInstances(pattern);

        for (Map.Entry<Integer, Map<Integer, List<InstanceLocation>>> seq2replicons : sameSeqInstances.entrySet()) {

            String seq_name = utils.genome_id_to_name.get(seq2replicons.getKey());

            Map<Integer, List<InstanceLocation>> repliconInstanceLocations = seq2replicons.getValue();

            Map<String, List<InstanceInfo>> repliconInstances = new HashMap<>();

            for (Map.Entry<Integer, List<InstanceLocation>> replicon2locations : repliconInstanceLocations.entrySet()) {

                List<InstanceInfo> instanceLocations = new ArrayList<>();

                String replicon_name = utils.replicon_id_to_name.get(replicon2locations.getKey());
                List<InstanceLocation> instances_locations = replicon2locations.getValue();

                instances_locations.sort(Comparator.comparing(InstanceLocation::getActualStartIndex));

                for (InstanceLocation instance_location : instances_locations) {
                    instance_location.setRepliconName(replicon_name);
                    List<Gene> genes = getInstanceFromCogList(seq_name, replicon_name, instance_location.getStartIndex(), instance_location.getEndIndex());
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

    private List<Gene> getInstanceFromCogList(String seq_name, String replicon_name, int startIndex, int endIndex) {
        List<Gene> instanceList = null;
        Map<String, Replicon> genomeToRepliconsMap = utils.getGenomeToRepliconsMap().get(seq_name);
        List<Gene> genomeToCogList = genomeToRepliconsMap.get(replicon_name).getGenes();
        if (genomeToCogList != null) {
            if (startIndex >= 0 && startIndex < genomeToCogList.size() &&
                    endIndex >= 0 && endIndex < genomeToCogList.size()) {
                if (startIndex >= endIndex) {
                    int tmp = startIndex;
                    startIndex = endIndex;
                    endIndex = tmp;
                }
                instanceList = genomeToCogList.subList(startIndex, endIndex+1);
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
                    instanceLocation.setEndIndex(instance_length);

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
        return number_of_genomes;
    }

    public Map<String, Map<String, Replicon>> getGenomeMap() {
        return utils.getGenomeToRepliconsMap();
    }

    public int getMaxGenomeSize(){
        return utils.getMaxGenomesSize();
    }
}
