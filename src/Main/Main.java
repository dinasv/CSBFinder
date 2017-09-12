package Main;

import PostProcess.Family;
import PostProcess.FamilyClustering;
import SuffixTrees.GeneralizedSuffixTree;
import SuffixTrees.Trie;
import Utils.Utils;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;


/**
 * Created by Dina on 6/6/2016.
 */
public class Main {

    @Parameter(names={"--mismatch", "-err"}, description = "maximal number of mismatches allowed")
    int max_error = 0;
    @Parameter(names={"--deletion", "-del"}, description = "maximal number of deletions allowed")
    int max_deletion = 0;
    @Parameter(names={"--insertion", "-ins"}, description = "maximal number of insertions allowed")
    int max_insertion = 0;
    @Parameter(names={"--wildcard", "-wc"}, description = "maximal number of wildcards allowed")
    int max_wildcards = 0;
    @Parameter(names={"--quorum1", "-q1"}, description = "exact instance quorum")
    int quorum1 = 1;
    @Parameter(names={"--quorum2", "-q2"}, description = "approximate instance quorum")
    int quorum2 = -1;
    @Parameter(names={"--minlength", "-l"}, description = "minimal motif length")
    int min_motif_length = -1;
    @Parameter(names={"--maxlength", "-lmax"}, description = "maximal motif length")
    int max_motif_length = Integer.MAX_VALUE;
    @Parameter(names={"--boolean-count", "-bcount"}, description = "if true, count one instance per input string",
            arity = 1)
    boolean bool_count = true;
    @Parameter(names={"--datasetname", "-ds"}, description = "dataset name")
    String dataset_name = "dataset1";
    @Parameter(names={"--input", "-in"}, description = "input file name", required = true)
    String input_file_name = "";
    @Parameter(names={"--motifs", "-m"}, description = "input motifs file name")
    String input_motifs_file_name = null;
    @Parameter(names={"--cog-info"}, description = "cog info file name")
    String cog_info_file_name = null;

    @Parameter(names={"--threshold", "-t"}, description = "threshold for family clustering")
    double threshold = 0.8;

    @Parameter(names = "-debug", description = "Debug mode")
    private boolean debug = false;
    @Parameter(names = "-mem", description = "Memory Saving Mode")
    private boolean memory_saving_mode = false;
    @Parameter(names = "--help", help = true)
    private boolean help;

    public static void main(String [ ] args) throws Exception {
        Main main = new Main();
        new JCommander(main, args);
        main.run();
    }

    public void run() throws Exception {
        if (help){
            System.out.println("-ins [input file name]");
        }else {
            Utils utils = new Utils("input/"+cog_info_file_name);

            try {
                if (!debug) {//disable logging information printed to screen
                    LogManager.getLogManager().reset();
                }
                FileHandler fh = new FileHandler("OGMFinder.log");
                utils.logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            long startTime = System.nanoTime();
            if (min_motif_length < 2) {
                min_motif_length = 2 + max_error;
            }

            findMotifs(utils);

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            if (debug) {
                if (max_error >0) {
                    System.out.println("q2=" + quorum2 + " err=" + max_error + " time: " + estimatedTime);
                }
                if (max_insertion >0) {
                    System.out.println("q2=" + quorum2 + " ins=" + max_insertion + " time: " + estimatedTime);
                }
            }

        }
    }

    /**
     * Finds the motifs using OGMFinder and prints them
     *
     * @param utils
     * @return
     * @throws Exception
     */

    public void findMotifs(Utils utils)
            throws Exception {

        //wild card
        int wc_char = 0;
        utils.cog_to_index.put("*", wc_char);
        utils.index_to_cog.add("*");

        //gap
        int gap_char = 1;
        utils.cog_to_index.put("_", gap_char);
        utils.index_to_cog.add("_");

        //unkown cog
        int unknown_char = 2;
        utils.cog_to_index.put("X", unknown_char);
        utils.index_to_cog.add("X");

        long startTime = System.nanoTime();

        GeneralizedSuffixTree dataset_suffix_tree = new GeneralizedSuffixTree();
        utils.logger.info("Building Data tree");
        System.out.println("Building Data tree");


        int number_of_genomes = utils.read_and_build_cog_words_tree(input_file_name, dataset_suffix_tree);
        if (quorum2 == -1) {
            quorum2 = number_of_genomes / 2;
        }

        Trie motif_tree = null;
        if (input_motifs_file_name != null) {
            motif_tree = new Trie("motif");
            String path = "input/" + input_motifs_file_name + ".txt";
            utils.buildMotifsTreeFromFile(path, motif_tree);
        }

        String parameters = "_ins" + max_insertion + "_q1_" + quorum1 + "_q2_" + quorum2 + "_l" + min_motif_length;

        String catalog_path = "output/motif_catalog_" + dataset_name + parameters;
        String motif_instances_path = catalog_path + "_instances";

        boolean include_families = true;
        if (memory_saving_mode){
            include_families = false;
        }

        Writer writer = new Writer(max_error, max_deletion, max_insertion, debug, catalog_path, motif_instances_path,
                include_families);

        System.out.println("Extracting motifs from " + number_of_genomes + " genomes. " +
                "Parameters: quorum="+ quorum2 + ", k=" + max_insertion + ", min-length=" + min_motif_length);

        OGMFinder ogmFinder = new OGMFinder(max_error, max_deletion, max_deletion, max_insertion, quorum1, quorum2,
                min_motif_length, max_motif_length, gap_char, wc_char, unknown_char, dataset_suffix_tree, motif_tree,
                bool_count, utils, memory_saving_mode, writer, debug);

        if (input_motifs_file_name == null) {
            if (!debug && !memory_saving_mode) {
                ogmFinder.removeRedundantMotifs();
                System.out.println("Removing redundant motifs");
            }
        }

        if (!debug) {
            if (!memory_saving_mode) {
                ArrayList<Motif> motifs = ogmFinder.getMotifs();

                for (Motif motif: motifs){
                    motif.calculateScore(utils, max_insertion, max_error, max_deletion);
                    motif.calculateMainFunctionalCategory(utils);
                }

                System.out.println("Clustering to families");
                ArrayList<Family> families = FamilyClustering.Cluster(motifs, 0.8, utils);

                for (Family family: families) {
                    writer.printFilteredMotif(family.getMotifs().get(0), utils, family.getFamilyId());
                    for (Motif motif : family.getMotifs()) {
                        writer.printMotif(motif, utils, family.getFamilyId());
                    }
                }
            }
        }
        writer.closeFiles();

        /*
        if (!debug) {
            System.out.println("Clustering to families");
            postProcess(catalog_path);
        }*/

        System.out.println(writer.getCountPrintedMotifs() + " motifs found");

        float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
        utils.logger.info("Took " + estimatedTime + " seconds");

        System.out.println("Took " + estimatedTime + " seconds");

        if (debug) {
            try {
                if (max_insertion > 0) {
                    PrintWriter output = new PrintWriter(new FileOutputStream(new File("OGMFinder.txt"), true));
                    output.println("err=" + max_insertion + "\t" + "q=" + quorum2);
                    output.println("Time" + "\t" + estimatedTime);
                    output.println("T_M nodes" + "\t" + ogmFinder.count_nodes_in_motif_tree);
                    output.println("T_D nodes" + "\t" + ogmFinder.count_nodes_in_data_tree);
                    output.println("Motifs" + "\t" + writer.getCountPrintedMotifs());

                    output.close();
                }
                if (max_error > 0) {
                    PrintWriter output = new PrintWriter(new FileOutputStream(new File("substitutions.txt"), true));
                    output.println("err=" + max_error + "\t" + "q=" + quorum2);
                    output.println("Time" + "\t" + estimatedTime);
                    output.println("T_M nodes" + "\t" + ogmFinder.count_nodes_in_motif_tree);
                    output.println("T_D nodes" + "\t" + ogmFinder.count_nodes_in_data_tree);
                    output.println("Motifs" + "\t" + writer.getCountPrintedMotifs());
                    output.close();
                }
            } catch (IOException e) {
                // do something
            }
        }
    }

    private void postProcess(String catalog_path){
        try {
            String[] command = new String[4];
            if (cog_info_file_name != null){
                command = new String[6];
                command[4] = "-f";
                command[5] = "input/" + cog_info_file_name;
            }

            command[0] = "GreedyFamCluster.exe";
            command[1] = catalog_path;
            command[2] = "-t";
            command[3] = Double.toString(threshold);

            System.out.println("Executing " + String.join(" ", command));

            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
        } catch (IOException e) {
            System.out.println("The file \"GreedyFamCluster.exe\" was not found. In order to further cluster OGMs \n" +
                    "to families and to output the results in an .xslx format, download \"GreedyFamCluster.exe\" \n" +
                    "from: https://www.cs.bgu.ac.il/~negevcb/OGMFinder, put it in the same directory with this \n" +
                    "file and run OGMFinder again.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
