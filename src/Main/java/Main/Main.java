package Main;

import PostProcess.Family;
import PostProcess.FamilyClustering;
import SuffixTrees.GeneralizedSuffixTree;
import SuffixTrees.TreeType;
import SuffixTrees.Trie;
import Utils.*;

import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Main {

    private static CommandLineArgs cla;

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar CSBFinder.jar");
        jcommander.usage();
        System.exit(exitStatus);
    }

    public static void main(String [ ] args){
        Main main = new Main();
        JCommander jcommander = null;
        try {
            cla = new CommandLineArgs();

            jcommander = JCommander.newBuilder().addObject(cla).build();
            jcommander.parse(args);
            if (cla.help){
                printUsageAndExit(jcommander, 0);
            }

        }catch (ParameterException e){
            System.err.println(e.getMessage());

            jcommander = JCommander.newBuilder().addObject(cla).build();
            printUsageAndExit(jcommander, 1);
        }
        main.run();
    }

    private Writer createWriter(boolean cog_info_exists){
        String parameters = "_ins" + cla.max_insertion + "_q" + cla.quorum2;
        String catalog_file_name = "Catalog_" + cla.dataset_name + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;
        if (cla.memory_saving_mode) {
            include_families = false;
        }

        Writer writer = new Writer(cla.max_error, cla.max_deletion, cla.max_insertion, cla.debug, catalog_file_name,
                instances_file_name,
                include_families, cla.output_file_type, cog_info_exists, cla.is_directons);

        return writer;
    }

    public void run() {

        String INPUT_PATH = "input/";

        HashMap<String, COG> cog_info = null;
        boolean cog_info_exists = (cla.cog_info_file_name != null);
        if (cog_info_exists) {
            cog_info = Readers.read_cog_info_table(INPUT_PATH + cla.cog_info_file_name);
        }

        Writer writer = createWriter(cog_info_exists);

        Utils utils = new Utils(cog_info, writer);

        if (cla.min_pattern_length < 2) {
            cla.min_pattern_length = 2 + cla.max_error;
        }

        pipeline(utils, INPUT_PATH, writer);

        if (cla.debug){
            utils.measureMemory();
            long actualMemUsed = utils.currMem - utils.initiailMem;

            System.out.println(actualMemUsed);
            writer.writeLogger("Genomes:" +utils.number_of_genomes + "," + actualMemUsed);

        }

    }

    /**
     * Read patterns from a file if a file is given, and put them in a suffix trie
     * @param INPUT_PATH
     * @param utils
     * @return the Trie with the patterns, null if patterns file not given ot building the tree was unsuccessful
     */
    private Trie buildPatternsTree(String INPUT_PATH, Utils utils) {
        Trie pattern_tree = null;
        if (cla.input_patterns_file_name != null) {
            //these arguments are not valid when input patterns are give
            cla.min_pattern_length = 2;
            cla.max_pattern_length = Integer.MAX_VALUE;

            pattern_tree = new Trie(TreeType.STATIC);
            String path = INPUT_PATH + cla.input_patterns_file_name;
            if (!utils.buildPatternsTreeFromFile(path, pattern_tree)){
                pattern_tree = null;//if tree building wasn't successful
            }
        }
        return pattern_tree;
    }

    /**
     * Executes CSBFinder and prints colinear synteny blocks
     *
     * @param utils
     * @return
     * @throws Exception
     */

    private void pipeline(Utils utils, String INPUT_PATH, Writer writer){

        //wild card
        utils.char_to_index.put(utils.WC_CHAR, utils.WC_CHAR_INDEX);
        utils.index_to_char.add(utils.WC_CHAR);

        //gap
        utils.char_to_index.put(utils.GAP_CHAR, utils.GAP_CHAR_INDEX);
        utils.index_to_char.add(utils.GAP_CHAR);

        //unkown cog
        utils.char_to_index.put(utils.UNK_CHAR, utils.UNK_CHAR_INDEX);
        utils.index_to_char.add(utils.UNK_CHAR);
        //if the sequence is not segmented to is_directons
        utils.char_to_index.put("X+", utils.UNK_CHAR_INDEX);
        utils.char_to_index.put("X-", utils.UNK_CHAR_INDEX);

        long startTime = System.nanoTime();

        GeneralizedSuffixTree dataset_suffix_tree = new GeneralizedSuffixTree();

        writer.writeLogger("Building Data tree");
        System.out.println("Building Data tree");

        int number_of_genomes = utils.readAndBuildDatasetTree(INPUT_PATH+cla.input_file_name,
                                                                    dataset_suffix_tree, cla.is_directons);

        utils.measureMemory();

        if (number_of_genomes != -1) {

            //read patterns from a file if a file is given, and put them in a suffix trie
            Trie pattern_tree = buildPatternsTree(INPUT_PATH, utils);

            System.out.println("Extracting CSBs from " + number_of_genomes + " input sequences.");

            CSBFinder CSBFinder = new CSBFinder(cla.max_error, cla.max_wildcards, cla.max_deletion, cla.max_insertion,
                    cla.quorum1, cla.quorum2,
                    cla.min_pattern_length, cla.max_pattern_length, utils.GAP_CHAR_INDEX, utils.WC_CHAR_INDEX,
                    dataset_suffix_tree, pattern_tree, cla.bool_count, utils, cla.memory_saving_mode, writer,
                    cla.is_directons, cla.debug);

            utils.measureMemory();

            if (cla.input_patterns_file_name == null) {
                if (!cla.memory_saving_mode) {
                    System.out.println("Removing redundant CSBs");
                    writer.writeLogger("CSBs found: " + CSBFinder.getPatternsCount());

                    CSBFinder.removeRedundantPatterns();
                    if (cla.debug) {
                        utils.measureMemory();
                        writer.writeLogger("CSBs left after removing redundant CSBs: " + CSBFinder.getPatternsCount());
                    }
                }
            }

            if (!cla.memory_saving_mode) {
                ArrayList<Pattern> patterns = CSBFinder.getPatterns();

                for (Pattern pattern : patterns) {
                    pattern.calculateScore(utils, cla.max_insertion, cla.max_error, cla.max_deletion);
                    pattern.calculateMainFunctionalCategory(utils, cla.is_directons);
                }
                utils.measureMemory();

                System.out.println("Clustering to families");
                ArrayList<Family> families = FamilyClustering.Cluster(patterns, cla.threshold, cla.cluster_by, utils,
                        cla.is_directons);

                utils.measureMemory();

                System.out.println("Writing to files");
                for (Family family : families) {
                    writer.printFilteredCSB(family.getPatterns().get(0), utils, family.getFamilyId());
                    for (Pattern pattern : family.getPatterns()) {
                        writer.printPattern(pattern, utils, family.getFamilyId());
                    }
                }
                utils.measureMemory();

            }
            writer.closeFiles();

            System.out.println(writer.getCountPrintedPatterns() + " CSBs found");

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            writer.writeLogger("Took " + estimatedTime + " seconds");

            System.out.println("Took " + estimatedTime + " seconds");

        }else{
            System.out.println("Could not read input sequences");
            writer.writeLogger("Could not read input sequences");
            System.exit(1);
        }
    }
}



