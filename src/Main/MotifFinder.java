package Main;

import SuffixTrees.*;
import Utils.Utils;
import java.io.*;
import java.util.*;

/**
 * Finds motifs
 */
public class MotifFinder {

    /**
     * Finds the motifs using OGMFinder and prints them
     *
     * @param max_error
     * @param max_motif_gap
     * @param max_deletion
     * @param quorum1
     * @param min_motif_length
     * @return
     * @throws Exception
     */

    public void findMotifs(int max_error, int max_motif_gap, int max_deletion, int max_insertion,
                                           int quorum1, int quorum2, int min_motif_length, boolean count_by_keys,
                                           String dataset_name, String input_file_name, String input_motifs_file_name,
                                           boolean memory_saving_mode, Utils utils, boolean debug)
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

        utils.read_and_build_cog_words_tree(input_file_name, dataset_suffix_tree);

        Trie motif_tree = null;
        if (input_motifs_file_name == null) {
            if (!memory_saving_mode){
                motif_tree = new Trie("enumeration");
                utils.buildMotifTreeFromDataTree(motif_tree, dataset_suffix_tree, quorum1);
            }
        }else{
            motif_tree = new Trie("motif");
            String path = "input/"+ input_motifs_file_name + ".txt";
            utils.buildMotifsTreeFromFile(path, motif_tree);
        }

        utils.read_cog_info_table();

        Writer writer = new Writer(max_error, max_motif_gap, max_deletion, max_insertion, quorum1, quorum2,
                min_motif_length, dataset_name, debug);

        System.out.println("Extracting motifs");

        OGMFinder ogmFinder = new OGMFinder(max_error, max_motif_gap, max_deletion, max_insertion, quorum1, quorum2, min_motif_length,
                gap_char, wc_char, unknown_char, dataset_suffix_tree, motif_tree, count_by_keys,  utils,
                memory_saving_mode, writer);

        if (input_motifs_file_name == null) {
            if (!debug && !memory_saving_mode) {
                ogmFinder.removeRedundantMotifs();
                System.out.println("Removing redundant motifs");
            }
        }

        if(!debug) {
            if (!memory_saving_mode){
                ArrayList<Motif> motifs_nodes = ogmFinder.getMotifs();

                for (Motif motif : motifs_nodes) {
                    writer.printMotif(motif, utils);
                }
            }

            writer.closeFiles();
        }

        System.out.println(writer.getCountPrintedMotifs() + " motifs found");

        float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
        utils.logger.info("Took " + estimatedTime + " seconds");

        System.out.println("Took " + estimatedTime + " seconds");

        try{
            if (max_insertion>0) {
                PrintWriter output = new PrintWriter(new FileOutputStream(new File("insertions.txt"), true));
                output.println("err=" + max_insertion + "\t" + "q=" + quorum2);
                output.println("Time" + "\t" + estimatedTime);
                output.println("T_M nodes" + "\t" + ogmFinder.count_nodes_in_motif_tree);
                output.println("T_D nodes" + "\t" + ogmFinder.count_nodes_in_data_tree);
                output.println("Motifs" + "\t" + writer.getCountPrintedMotifs() );

                output.close();
            }
            if (max_error > 0) {
                PrintWriter output = new PrintWriter(new FileOutputStream(new File("substitutions.txt"), true));
                output.println("err=" + max_error + "\t" + "q=" + quorum2);
                output.println("Time" + "\t" + estimatedTime);
                output.println("T_M nodes" + "\t" + ogmFinder.count_nodes_in_motif_tree);
                output.println("T_D nodes" + "\t" + ogmFinder.count_nodes_in_data_tree);
                output.println("Motifs" + "\t" + writer.getCountPrintedMotifs() );
                output.close();
            }
        } catch (IOException e) {
            // do something
        }
    }

    private void printOccs(PrintWriter motifs_file, MotifNode motif, ArrayList<String[]> cog_words_anots){
        ArrayList<Occurrence> occs = motif.getOccs();

        motifs_file.println("Gene products:");

        for (Occurrence occ : occs) {

            OccurrenceNode occ_node = occ.getNodeOcc();
            if (occ.getEdge() != null) {
                occ_node = (OccurrenceNode) occ.getEdge().getDest();
            }

            HashMap<Integer, ArrayList<String>> keys = occ_node.getResults();

            for (Map.Entry<Integer, ArrayList<String>> entry : keys.entrySet()) {
                int word_key = entry.getKey();
                //positions in the word
                ArrayList<String> key_indexes = entry.getValue();

                String word_string = occ.getSubstring();
                String[] word = cog_words_anots.get(word_key);

                motifs_file.println("Bac_" + word_key + " " + key_indexes.toString() + "\t" + word_string);
                /*
                for (int index: key_indexes) {
                    int word_start_index = index;

                    int from = word_start_index + 1;
                    int to = from + motif.getSubstring_length() - occ.getDeletions();

                    for (int i = from; i < to; i++) {
                        try {
                            motifs_file.print(word[i] + "\t");
                        } catch (Exception e) {
                            System.out.println("No gi found word_id " + word_key + " word index " + i);
                        }
                    }
                    motifs_file.print("\n");
                }*/
            }
        }
        motifs_file.println();
    }



}
