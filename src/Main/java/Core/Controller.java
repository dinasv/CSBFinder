package Core;

import IO.MyLogger;
import IO.Readers;
import IO.Writer;
import Core.PostProcess.Family;
import Core.PostProcess.FamilyClustering;
import Core.SuffixTrees.GeneralizedSuffixTree;
import Core.SuffixTrees.TreeType;
import Core.SuffixTrees.Trie;
import Genomes.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Controller {

    private Parameters cla;
    private MyLogger logger;
    private String output_path;
    private Writer writer;
    private Utils utils;
    private String INPUT_PATH = "input/";


    public Controller(String [ ] args){
        JCommander jcommander = null;
        try {
            cla = new Parameters();

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

        output_path = createOutputPath();
        logger = new MyLogger(output_path, cla.debug);

        boolean cog_info_exists = (cla.cog_info_file_name != null);
        writer = createWriter(cog_info_exists);

        Map<String, COG> cog_info = null;

        if (cog_info_exists) {
            cog_info = Readers.read_cog_info_table(INPUT_PATH + cla.cog_info_file_name);
        }

        utils = new Utils(cog_info, logger);

        if (cla.min_pattern_length < 2) {
            cla.min_pattern_length = 2 + cla.max_error;
        }

        run();
    }

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar MainAlgorithm.jar");
        jcommander.usage();
        System.exit(exitStatus);
    }


    private Writer createWriter(boolean cog_info_exists){
        String parameters = "_ins" + cla.max_insertion + "_q" + cla.quorum2;
        String catalog_file_name = "Catalog_" + cla.dataset_name + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;

        Writer writer = new Writer(cla.max_error, cla.max_deletion, cla.max_insertion, cla.debug, catalog_file_name,
                instances_file_name,
                include_families, cla.output_file_type, cog_info_exists, cla.non_directons, output_path);

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

    public void run() {

        pipeline();

        if (cla.debug){
            utils.measureMemory();
            long actualMemUsed = utils.currMem - utils.initialMem;

            System.out.println(actualMemUsed);
            logger.writeLogger("Memory used: " + actualMemUsed);

        }

    }

    /**
     * Read patterns from a file if a file is given, and put them in a suffix trie
     * @return the Trie with the patterns, null if patterns file not given ot building the tree was unsuccessful
     */
    private Trie buildPatternsTree(GenomesInfo gi) {
        Trie pattern_tree = null;
        if (cla.input_patterns_file_name != null) {
            //these arguments are not valid when input patterns are give
            cla.min_pattern_length = 2;
            cla.max_pattern_length = Integer.MAX_VALUE;

            pattern_tree = new Trie(TreeType.STATIC);
            String path = INPUT_PATH + cla.input_patterns_file_name;
            if (!DatasetTreeBuilder.buildPatternsTreeFromFile(path, pattern_tree, gi)){
                pattern_tree = null;//if tree building wasn't successful
            }
        }
        return pattern_tree;
    }

    /**
     * Executes MainAlgorithm and prints colinear synteny blocks
     *
     * @return
     * @throws Exception
     */

    private void pipeline(){

        long startTime = System.nanoTime();

        GeneralizedSuffixTree dataset_suffix_tree = new GeneralizedSuffixTree();

        logger.writeLogger("Building Data tree");
        System.out.println("Building Data tree");

        GenomesInfo gi = new GenomesInfo();
        GenomesReader reader = new GenomesReader(gi, logger);
        String genomes_file_path = INPUT_PATH+cla.input_file_name;
        int number_of_genomes = reader.readGenomes(genomes_file_path);

        DatasetTreeBuilder.buildTree(dataset_suffix_tree, cla.non_directons, gi);

        utils.measureMemory();

        if (number_of_genomes != -1) {

            //read patterns from a file if a file is given, and put them in a suffix trie
            Trie pattern_tree = buildPatternsTree(gi);

            System.out.println("Extracting CSBs from " + number_of_genomes + " input sequences.");

            MainAlgorithm MainAlgorithm = new MainAlgorithm(cla, dataset_suffix_tree, pattern_tree, gi, utils, cla.debug);

            utils.measureMemory();

            if (cla.input_patterns_file_name == null) {
                System.out.println("Removing redundant CSBs");
                logger.writeLogger("CSBs found: " + MainAlgorithm.getPatternsCount());

                MainAlgorithm.removeRedundantPatterns();
                if (cla.debug) {
                    utils.measureMemory();
                    logger.writeLogger("CSBs left after removing redundant CSBs: " + MainAlgorithm.getPatternsCount());
                }

            }

            PatternScore pattern_score = new PatternScore(gi.max_genome_size, number_of_genomes, gi.dataset_length_sum,
                    gi.cog_to_containing_genomes, gi.genome_to_cog_paralog_count);

            List<Pattern> patterns = MainAlgorithm.getPatterns();

            for (Pattern pattern : patterns) {
                double score = utils.computePatternScore(pattern_score, pattern.getPatternArr(), cla.max_insertion, cla.max_error,
                        cla.max_deletion, pattern.getInstanceCount());
                pattern.setScore(score);
                //pattern.calculateScore(gi, cla.max_insertion, cla.max_error, cla.max_deletion);
                pattern.calculateMainFunctionalCategory(gi, cla.non_directons);
            }
            utils.measureMemory();

            System.out.println("Clustering to families");
            List<Family> families = FamilyClustering.Cluster(patterns, cla.threshold, cla.cluster_by, gi,
                    cla.non_directons);

            utils.measureMemory();

            System.out.println("Writing to files");
            for (Family family : families) {
                writer.printFilteredCSB(family.getPatterns().get(0), gi, family.getFamilyId());
                for (Pattern pattern : family.getPatterns()) {
                    writer.printPattern(pattern, gi, family.getFamilyId());
                }
            }
            utils.measureMemory();


            writer.closeFiles();

            System.out.println(writer.getCountPrintedPatterns() + " CSBs found");

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            logger.writeLogger("Took " + estimatedTime + " seconds");

            System.out.println("Took " + estimatedTime + " seconds");

        }else{
            System.out.println("Could not read input sequences");
            logger.writeLogger("Could not read input sequences");
            System.exit(1);
        }
    }
}



