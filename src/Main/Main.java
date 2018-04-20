package Main;

import PostProcess.Family;
import PostProcess.FamilyClustering;
import SuffixTrees.GeneralizedSuffixTree;
import SuffixTrees.TreeType;
import SuffixTrees.Trie;
import Utils.*;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Created by Dina on 6/6/2016.
 */
public class Main {
    private static CommandLineArgs cla;
    public static void main(String [ ] args) throws Exception{
        Main main = new Main();
        JCommander jcommander = null;
        try {
            cla = new CommandLineArgs();

            jcommander = JCommander.newBuilder().addObject(cla).build();
            jcommander.parse(args);

        }catch (ParameterException e){
            System.err.println(e.getMessage());

            jcommander = new JCommander(cla);
            jcommander.setProgramName("java -jar OGBFinder.jar");
            jcommander.usage();
            System.exit(1);
        }
        main.run();
    }

    public void run() throws Exception {

        String INPUT_PATH = "input/";

        HashMap<String, COG> cog_info = null;
        if (cla.cog_info_file_name != null) {
            cog_info = Readers.read_cog_info_table(INPUT_PATH + cla.cog_info_file_name);
        }

        Utils utils = new Utils(cog_info);

        try {
            if (!cla.debug) {//disable logging information printed to screen
                LogManager.getLogManager().reset();
            }
            FileHandler fh = new FileHandler("OGBFinder.log");
            utils.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.nanoTime();
        if (cla.min_pattern_length < 2) {
            cla.min_pattern_length = 2 + cla.max_error;
        }

        pipeline(utils, INPUT_PATH);

        float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);

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
     * Executes OGBFinder and prints colinear synteny blocks
     *
     * @param utils
     * @return
     * @throws Exception
     */

    private void pipeline(Utils utils, String INPUT_PATH)
            throws Exception {

        //wild card
        utils.char_to_index.put(utils.WC_CHAR, utils.WC_CHAR_INDEX);
        utils.index_to_char.add(utils.WC_CHAR);

        //gap
        utils.char_to_index.put(utils.GAP_CHAR, utils.GAP_CHAR_INDEX);
        utils.index_to_char.add(utils.GAP_CHAR);

        //unkown cog
        int unknown_char = 2;
        utils.char_to_index.put(utils.UNK_CHAR, utils.UNK_CHAR_INDEX);
        utils.index_to_char.add(utils.UNK_CHAR);

        long startTime = System.nanoTime();

        GeneralizedSuffixTree dataset_suffix_tree = new GeneralizedSuffixTree();
        utils.logger.info("Building Data tree");
        System.out.println("Building Data tree");


        int number_of_genomes = utils.read_and_build_dataset_tree(INPUT_PATH+cla.input_file_name,
                                                                    dataset_suffix_tree);
        if (number_of_genomes != -1) {

            //read patterns from a file if a file is given, and put them in a suffix trie
            Trie pattern_tree = buildPatternsTree(INPUT_PATH, utils);

            String parameters = "_ins" + cla.max_insertion + "_q1_" + cla.quorum1 + "_q2_" + cla.quorum2 + "_l" +
                    cla.min_pattern_length;

            String catalog_path = "output/Catalog_" + cla.dataset_name + parameters;
            String instances_path = catalog_path + "_instances";

            boolean include_families = true;
            if (cla.memory_saving_mode) {
                include_families = false;
            }

            Writer writer = new Writer(cla.max_error, cla.max_deletion, cla.max_insertion, cla.debug, catalog_path,
                    instances_path,
                    include_families, cla.output_file_type, utils.cog_info != null);

            System.out.println("Extracting OGBs from " + number_of_genomes + " genomes. " +
                    "Parameters: quorum=" + cla.quorum2 + ", k=" + cla.max_insertion + ", min-length="
                    + cla.min_pattern_length);

            OGBFinder OGBFinder = new OGBFinder(cla.max_error, cla.max_wildcards, cla.max_deletion, cla.max_insertion,
                    cla.quorum1, cla.quorum2,
                    cla.min_pattern_length, cla.max_pattern_length, utils.GAP_CHAR_INDEX, utils.WC_CHAR_INDEX,
                    unknown_char,
                    dataset_suffix_tree, pattern_tree, cla.bool_count, utils, cla.memory_saving_mode, writer,
                    cla.debug);

            if (cla.input_patterns_file_name == null) {
                if (!cla.memory_saving_mode) {
                    System.out.println("Removing redundant OGBs");
                    utils.logger.info("OGBs found: " + OGBFinder.getPatternsCount());
                    OGBFinder.removeRedundantPatterns();
                    utils.logger.info("OGBs left after removing redundant OGBs: " + OGBFinder.getPatternsCount());
                }
            }

            if (!cla.memory_saving_mode) {
                ArrayList<Pattern> patterns = OGBFinder.getPatterns();

                for (Pattern pattern : patterns) {
                    pattern.calculateScore(utils, cla.max_insertion, cla.max_error, cla.max_deletion);
                    pattern.calculateMainFunctionalCategory(utils);
                }

                System.out.println("Clustering to families");
                ArrayList<Family> families = FamilyClustering.Cluster(patterns, cla.threshold, cla.cluster_by, utils);

                System.out.println("Writing to files");
                for (Family family : families) {
                    writer.printFilteredOGB(family.getPatterns().get(0), utils, family.getFamilyId());
                    for (Pattern pattern : family.getPatterns()) {
                        writer.printPattern(pattern, utils, family.getFamilyId());
                    }
                }

            }
            writer.closeFiles();

            System.out.println(writer.getCountPrintedPatterns() + " OGBs found");

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            utils.logger.info("Took " + estimatedTime + " seconds");

            System.out.println("Took " + estimatedTime + " seconds");

        }
    }
}



