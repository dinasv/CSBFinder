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
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.text.SimpleDateFormat;
import java.util.*;

public class CSBFinderModel {

    private GenomesLoadedListener genomesLoadedListener;
    private CSBFinderDoneListener csbFinderDoneListener;

    private CommandLineArgs cla;
    private Utils utils;
    private Writer writer;
    private GeneralizedSuffixTree dataset_suffix_tree;
    private List<Family> families;

    private int number_of_genomes;

    public CSBFinderModel() {

    }

    public MyLogger logger = new MyLogger("",true);
    public void init() {
        this.utils = new Utils(null, logger);
    }

    public void loadFile(String path, boolean is_directons) {
        this.init();
        dataset_suffix_tree = new GeneralizedSuffixTree();
        number_of_genomes = utils.readAndBuildDatasetTree(path,
                dataset_suffix_tree, cla.non_directons);
//        number_of_genomes= utils.getGenomeToRepliconsMap().size();
//        genomesLoadedListener.genomesLoadDone(new GenomesLoadEvent(utils.getGenomeToRepliconsMap()));

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
            //writer = createWriter(cla.cog_info_file_name != null && !"".equals(cla.cog_info_file_name));

            this.findCSBs();
        }
    }


    private void findCSBs() {

        writer = createWriter(cla.cog_info_file_name != null && !"".equals(cla.cog_info_file_name));

        long startTime = System.nanoTime();

        Map<String, COG> cog_info = null;
        boolean cog_info_exists = (cla.cog_info_file_name != null);
        if (cog_info_exists) {
            cog_info = Readers.read_cog_info_table(cla.cog_info_file_name);
        }

        utils.setCogInfo(cog_info);

        Trie pattern_tree = buildPatternsTree(utils);

        System.out.println("Extracting CSBs from " + number_of_genomes + " input sequences.");

        CSBFinder csbFinder = new CSBFinder(cla.max_error, cla.max_wildcards, cla.max_deletion, cla.max_insertion,
                cla.quorum1, cla.quorum2,
                cla.min_pattern_length, cla.max_pattern_length, utils.GAP_CHAR_INDEX, utils.WC_CHAR_INDEX,
                dataset_suffix_tree, pattern_tree, cla.mult_count, utils,
                cla.non_directons, cla.debug);

        if (cla.input_patterns_file_name == null) {
            csbFinder.removeRedundantPatterns();
        }

        List<Pattern> patterns = csbFinder.getPatterns();

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

    private Writer createWriter(boolean cog_info_exists){
        String parameters = "_ins" + cla.max_insertion + "_q" + cla.quorum2;
        String catalog_file_name = "Catalog_" + cla.dataset_name + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;

        Writer writer = new Writer(cla.max_error, cla.max_deletion, cla.max_insertion, cla.debug, catalog_file_name,
                instances_file_name,
                include_families, cla.output_file_type, cog_info_exists, cla.non_directons, createOutputPath());

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
        writer.setOutputFileType(CommandLineArgs.OutputType.valueOf(outputFileType));
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

    public Map<String, Map<String, List<Gene>>> getGenomeRepliconsMap() {
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

    public Map<String, String> getCogInfo(List<String> cogs) {
        Map<String, String> cogInfo = new HashMap<>();
        if (utils.getCogInfo() != null) {
            cogs.forEach(cog -> {
                COG c = utils.getCogInfo().get(cog);
                if (c != null) {
                    cogInfo.put(cog, c.getCog_desc());
                }
            });
        }

        return cogInfo;
    }

     public Map<String, List<List<Gene>>> getInstances(Pattern pattern){

        Map<String, List<List<Gene>>> instances = new HashMap<>();

        for (Map.Entry<Integer, List<InstanceLocation>> entry : groupSameSeqInstances(pattern).entrySet()) {

            String seq_name = utils.genome_key_to_name.get(entry.getKey());

            List<List<Gene>> genomeInstances = new ArrayList<>();

            List<InstanceLocation> instances_locations = entry.getValue();
            for (InstanceLocation instance_location : instances_locations){
                String replicon_name = utils.replicon_key_to_name.get(instance_location.getRepliconId());
                List<Gene> genes = getInstanceFromCogList(seq_name, replicon_name, instance_location.getStartIndex(), instance_location.getEndIndex());
                if (genes != null) {
                    genomeInstances.add(genes);
                }
            }

            if (genomeInstances.size() > 0) {
                instances.put(seq_name, genomeInstances);
            }
        }

        return instances;
    }

    private List<Gene> getInstanceFromCogList(String seq_name, String replicon_name, int startIndex, int endIndex) {
        List<Gene> instanceList = null;
        Map<String, List<Gene>> genomeToRepliconsMap = utils.getGenomeToRepliconsMap().get(seq_name);
        List<Gene> genomeToCogList = genomeToRepliconsMap.get(replicon_name);
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
    private Map<Integer, List<InstanceLocation>> groupSameSeqInstances(Pattern pattern){
        Map<Integer, List<InstanceLocation>> instance_seq_to_location = new HashMap<>();
        for (Instance instance : pattern.get_instances()) {

            int instance_length = instance.getLength();
            for (Map.Entry<Integer, List<InstanceLocation>> entry : instance.getInstanceLocations().entrySet()) {
                int seq_key = entry.getKey();

                if (!instance_seq_to_location.containsKey(seq_key)) {
                    instance_seq_to_location.put(seq_key, new ArrayList<InstanceLocation>());
                }
                List<InstanceLocation> instances_locations = instance_seq_to_location.get(seq_key);
                for (InstanceLocation instance_location : entry.getValue()) {
                    instance_location.setEndIndex(instance_length);
                    instances_locations.add(instance_location);
                }
            }
        }
        return instance_seq_to_location;
    }

    public int getNumberOfGenomes() {
        return number_of_genomes;
    }
}
