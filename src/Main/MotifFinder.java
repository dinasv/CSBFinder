package Main;

import SuffixTrees.*;
import Utils.COG;
import Utils.Utils;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Finds motifs
 */
public class MotifFinder {

    private HashMap<String, String> cog_fun_cat;

    private ArrayList<String> cog_words_ids;

    private boolean count_by_keys;

    public MotifFinder() throws IOException {

        cog_words_ids = null;

        count_by_keys = true;

    }

    /**
     * Finds the motifs using Sagot and prints them
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
                                           boolean memory_saving_mode, Utils utils)
                                           throws Exception {

        this.count_by_keys = count_by_keys;

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

        Trie motif_tree;
        if (input_motifs_file_name == null) {
            motif_tree = new Trie("enumeration");
            utils.buildMotifTreeFromDataTree(motif_tree, dataset_suffix_tree, quorum1);
        }else{
            motif_tree = new Trie("motif");
            String path = "input/"+ input_motifs_file_name + ".txt";
            utils.buildMotifsTreeFromFile(path, motif_tree);
        }

        utils.read_cog_info_table();

        System.out.println("Extracting motifs");
        Sagot s = new Sagot(max_error, max_motif_gap, max_deletion, max_insertion, quorum1, quorum2, min_motif_length,
                gap_char, wc_char, unknown_char, dataset_suffix_tree, motif_tree, count_by_keys,  utils, memory_saving_mode);
        s.removeRedundantMotifs();
        ArrayList<Motif> motifs_nodes = s.getMotifs();
        System.out.println(motifs_nodes.size() + " motifs found");

        printMotifCatalog(dataset_name, max_error, max_motif_gap, max_deletion, max_insertion, quorum1, quorum2,
                min_motif_length, motifs_nodes, utils);

        float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
        utils.logger.info("Took " + estimatedTime + " seconds");
    }


    /********* Printing files **********/

    private void printMotifCatalog(String dataset_name, int max_error, int max_motif_wildcard, int max_deletion,
                                   int max_insertion, int quorum1, int quorum2, int min_motif_length,
                                   ArrayList<Motif> motifs, Utils utils){

        try {
            new File("output").mkdir();
            try {
                String parameters = "_err" + max_error + "_wc" + max_motif_wildcard + "_del" + max_deletion +
                        "_ins" + max_insertion + "_q1_" + quorum1 + "_q2_" + quorum2 + "_l" + min_motif_length;

                PrintWriter motifs_catalog_file = new PrintWriter("output/motif_catalog_" + dataset_name + parameters
                                                + ".txt", "UTF-8");
                PrintWriter motifs_seq_ids_file = new PrintWriter("output/seqIDs_" + dataset_name + parameters
                                                + ".txt", "UTF-8");

                printMotifs(motifs, motifs_catalog_file, motifs_seq_ids_file, max_insertion, max_error, max_deletion, utils);

                motifs_catalog_file.close();
                motifs_seq_ids_file.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (SecurityException e){
            System.out.println("The directory 'output' could not be created, therefore no output is printed. " +
                    "Please create a directory named 'output' in the following path: " + System.getProperty("user.dir"));
        }
    }

    private String[] getMotifMainCat(String[] motif_arr, Utils utils){
        HashMap<String, Integer> count_motif_letter_cat = new HashMap<>();
        HashSet<String> motif_main_cat = new HashSet<String>();

        String motif_str = "";
        String motif_letters = "";

        for (String cog : motif_arr) {
            if (cog.equals("X") || cog.equals("*")) {
                motif_str += cog + " ";
            } else {
                motif_str += "COG" + cog + " ";
            }
            COG cog_obj = utils.cog_info.get(cog);
            if (cog_obj != null){
                String[] cog_main_cats = cog_obj.getFunctional_category_desc().split("_OR_");
                for (String main_cat: cog_main_cats){
                    motif_main_cat.add(main_cat);
                }
                String letters_desc = cog_obj.getLetter_desc();
                String cog_letters = cog_obj.getFunctional_category_letters();
                for (int i = 0; i < cog_letters.length(); i++) {
                    String letter = cog_letters.substring(i, i + 1);
                    if (!motif_letters.contains(letter)) {
                        motif_letters += letter;
                    }
                }
                if (count_motif_letter_cat.containsKey(letters_desc)){
                    count_motif_letter_cat.put(letters_desc, count_motif_letter_cat.get(letters_desc)+1);
                }else{
                    count_motif_letter_cat.put(letters_desc, 1);
                }
            }
        }

        int max_count = 0;
        String max_category = "";
        for (Map.Entry<String, Integer> entry : count_motif_letter_cat.entrySet()) {
            String cat = entry.getKey();
            int count = entry.getValue();
            if (count > max_count){
                max_count = count;
                max_category = cat;
            }
        }
        count_motif_letter_cat.remove(max_category);
        if (count_motif_letter_cat.values().contains(max_count)){
            max_category = "WTF";
        }
        String[] ret = {motif_str, max_category};
        return ret;
        //return motif_str + "#" + max_category;
    }

    private String getCogDesc(String cog, Utils utils) {
        String cog_description = "";
        if (!cog.equals("*") && !cog.equals("X")) {
            COG cog_obj = utils.cog_info.get(cog);
            if (cog_obj == null) {
                //System.out.println("NO description for COG " + cog);
            } else {

                cog_description = cog_obj.getSub_cat_desc();
                String cog_description2 = cog_obj.getLetter_desc();
                cog_description = "COG" + cog + " " + cog_description2 + ": " + cog_description;

                //motifs_file.println(cog_description);
            }
        }
        return cog_description;
    }


    public void printMotifs(ArrayList<Motif> motifs, PrintWriter motifs_catalog_file, PrintWriter motifs_seq_ids_file,
                            int max_insertion, int max_error, int max_deletion, Utils utils){

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);

        String header = "motif_id\tlength\tmain_category\tscore\toccurrences\toccs_ratio\texact_occs\tmotif_cogs\tmotif\n";

        motifs_catalog_file.write(header);

        int count = 0;
        for (Motif motif : motifs) {
            count += 1;

            motifs_seq_ids_file.print("motif_" + motif.getMotif_id() + "\t");
            for (int seq_id : motif.get_occs_keys()){
                motifs_seq_ids_file.print(seq_id + "\t");
            }
            motifs_seq_ids_file.print("\n");

            String[] motif_arr = motif.getMotif_arr();
            String[] ret = getMotifMainCat(motif_arr, utils);
            String motif_str = ret[0];
            String motifMainCat = ret[1];

            String motifs_catalog_line = motif.getMotif_id() + "\t" + motif.getLength() + "\t" +
                    motifMainCat + "\t";

            double motif_pval = utils.computeMotifPval(motif_arr, max_insertion, max_error, max_deletion, 0,
                    motif.getOccCount(), motif.getMotif_id());

            motifs_catalog_line += df.format(motif_pval) + "\t"
                    + motif.getOccCount() + "\t="
                    + motif.getOccCount() + "/" + utils.datasets_size.get(0) + "\t"
                    + motif.getExact_occs_count() + "\t";

            motifs_catalog_line += "=\"" + motif_str + "\"";

            for (String cog : motif_arr) {
                motifs_catalog_line += "& CHAR(10) &\"" + getCogDesc(cog, utils) + "\"";

            }

            motifs_catalog_line += "\t" + motif_str;

            motifs_catalog_file.write(motifs_catalog_line + "\n");
        }
    }

    /**
     * Print motifs to file
     *
     * @param motifs
     * @param max_error
     * @param quorum
     */
    public ArrayList<MotifNode> printMotifs2(ArrayList<MotifNode> motifs, String file_name, int max_error, int max_motif_gap, int max_occ_gap, int quorum, int min_motif_length, String dataset1_name, HashMap<String, COG> cog_info, ArrayList<String[]> cog_words_anots) throws FileNotFoundException {
        ArrayList<MotifNode> bac_motifs = new ArrayList<>();

        PrintWriter motifs_file = null;
        try {
            motifs_file = new PrintWriter("output/" + file_name + ".txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        motifs_file.println("Max error: " + max_error + ", max motif wildcard: " + max_motif_gap + ", max occ gap: " + max_occ_gap + ", quorum1: " + quorum + " min motif length: " + min_motif_length);
        motifs_file.println("Motifs found: " + motifs.size());
        motifs_file.println("Motif_id\tMotif\tocc_count\tstd");
        //bac_motifs_file.println("Max error: " + max_error +", max motif wildcard: "+max_motif_gap+", max occ gap: "+max_deletion+", quorum1: "+ quorum1 +" min motif length: "+min_motif_length);

        /*
        HashMap<String, Integer> category_histogram = new HashMap<String, Integer>(26);
        HashMap<String, Integer> category_histogram_bac = new HashMap<String, Integer>(26);
        fill_category_histogram(category_histogram);
        fill_category_histogram(category_histogram_bac);
        */

        int bac_motif_count = 0;
        int motif_count = 0;

        for (MotifNode motif : motifs) {
            motif_count++;

            String motif_string = printMotif(motif, motifs_file, false, motif_count, cog_info, dataset1_name, cog_words_anots);
        }
            //check if motif contains at list one bacterial cog
            /*
            String cog_motif = checkMotif(motif_string, category_histogram, category_histogram_bac);
            if(cog_motif != ""){
                bac_motifs.add(motif);
                printMotif(motif, bac_motifs_file,  false, motif_count);
                bac_motif_count++;
            }*/



        /*
        bac_motifs_file.println();
        bac_motifs_file.println("Motifs found: "+ bac_motif_count);
        bac_motifs_file.println();
        for (Map.Entry<String, Integer> entry : category_histogram_bac.entrySet()) {
            String cat_letter = entry.getKey();
            int value = entry.getValue();
            if (value > 0) {
                bac_motifs_file.println(value + "\t" + cat_letter + "\t" + cog_fun_cat.get(cat_letter));
            }

        }
        bac_motifs_file.close();

        motifs_file.println();


        motifs_file.println("Viral cogs: " + viral_counter);
        motifs_file.println("Bacterial cogs: " + bacterial_counter);

        motifs_file.println();
        motifs_file.println("num_of_cogs\tcategory");

        for (Map.Entry<String, Integer> entry : category_histogram.entrySet()) {
            String cat_letter = entry.getKey();
            int value = entry.getValue();

            if (value > 0) {
                motifs_file.println(value + "\t" + cat_letter + "\t" + cog_fun_cat.get(cat_letter));
            }

        }

        */
        motifs_file.close();
        //motifs_file_length.close();

        return bac_motifs;
    }

    public String printMotif(MotifNode motif, PrintWriter motifs_file, Boolean print_occs, int count, HashMap<String, COG> cog_info, String dataset1_name, ArrayList<String[]> cog_words_anots) {
        String motif_string = motif.getSubstring();
        String[] cogs = motif_string.split("\\|");


        //motifs_file.println("Motif_"+count+"\t"+motif_string + "\tnumber of occurrences: "+ motif.getOccKeysSize() + "\tstd: "+ motif.getStd());
        /*int wi = motif.getInfix_count();
        int wp = motif.getPrefix_count();
        int ws = motif.getSuffix_count();

        double expected = motif.getExpected();

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);
        String expected_str = df.format(expected);
        String std_str = df.format(motif.getStd());
        */
        String motif_to_print = "";
        for (String cog : cogs) {
            if (cog.equals("*") || cog.equals("X")) {
                motif_to_print += cog + " ";
            }else {
                motif_to_print += "COG" + cog + " ";
            }
        }


        DecimalFormat format = new DecimalFormat("0.##E0");
        String p_val = format.format(motif.getP_val());
        int occ_count;
        if(count_by_keys) {
            occ_count = motif.getOccKeysSize();
        }else{
            occ_count = motif.getOccsIndexCount();
        }

        motifs_file.println("(" + count + ") Motif_" + motif.getMotifKey() + "\t" + motif_to_print + "\t" + occ_count + "\tP_val: " +  motif.getP_val());

        motifs_file.println();
        int count_viral = 0;
        for (String cog : cogs) {
            if (!cog.equals("*") && !cog.equals("X")) {
                COG cog_obj = cog_info.get(cog);
                if ( cog_obj != null) {
                    String cog_description = cog_obj.getSub_cat_desc();
                    String cog_description2 = cog_obj.getLetter_desc();

                    motifs_file.println("COG" + cog + " " + cog_description2 + ": " + cog_description);

                    cog_description = cog_description.toLowerCase();
                    if (cog_description.contains("phage")) {
                        count_viral++;
                    }
                }else{
                    System.out.println("COG" + cog + " not found");
                }
            }
        }
        motifs_file.println();

        if (dataset1_name.equals("prophage")) {
            motifs_file.println(count_viral+"/"+motif.getSubstring_length()+" VIRAL");
        }
        motifs_file.println();

        if (print_occs) {

            printOccs(motifs_file, motif, cog_words_anots);
        }


        return motif_string;
    }

    private void printMotifsTax(ArrayList<MotifNode> motifs, String file_name){
        PrintWriter motifs_file = null;
        try {
            motifs_file = new PrintWriter("output/" + file_name + ".txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (MotifNode motif : motifs) {
            count ++;
            String motif_to_print = "";
            String[] cogs = motif.getSubstring().split("\\|");
            for (String cog : cogs) {
                motif_to_print += "COG" + cog + " ";
            }
            motifs_file.println("(" + count + ") Motif_" + motif.getMotifKey() + "\t" + motif_to_print);
            printOccsTax(motifs_file, motif);
        }
    }
    private void printOccsTax(PrintWriter motifs_file, MotifNode motif){
        ArrayList<Occurrence> occs = motif.getOccs();

        for (Occurrence occ : occs) {

            OccurrenceNode occ_node = occ.getNodeOcc();
            if (occ.getEdge() != null) {
                occ_node = (OccurrenceNode) occ.getEdge().getDest();
            }

            HashMap<Integer, ArrayList<String>> keys = occ_node.getResults();

            for (Map.Entry<Integer, ArrayList<String>> entry : keys.entrySet()) {
                int word_key = entry.getKey();
                //positions in the word
                ArrayList key_indexes = entry.getValue();

                //occ_prophage_id = word_ids.get(prophage_key);
                String word_string = occ.getSubstring();

                String bacteria_name = cog_words_ids.get(word_key);
                motifs_file.println("Word_key: " + word_key + "\t" + key_indexes.toString() + "\t" +bacteria_name);
            }
        }
        motifs_file.println();
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
