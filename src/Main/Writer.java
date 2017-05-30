package Main;

import SuffixTrees.Edge;
import SuffixTrees.OccurrenceNode;
import Utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import Utils.COG;


/**
 * Created by Boris on 19/05/2017.
 */
public class Writer {
    private DecimalFormat df;
    private PrintWriter catalog_file;
    private PrintWriter catalog_occs_file;
    private int max_error;
    private int max_deletion;
    private int max_insertion;
    private int count_printed_motifs;
    private boolean debug;

    public Writer(int max_error, int max_motif_gap, int max_deletion, int max_insertion, int quorum1, int quorum2,
                  int min_motif_length, String dataset_name, boolean debug){
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);

        this.max_error = max_error;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        this.debug = debug;
        count_printed_motifs = 0;

        createFiles(max_error, max_motif_gap, max_deletion, max_insertion, quorum1, quorum2, min_motif_length,
                dataset_name);

    }

    private void createFiles(int max_error, int max_motif_gap, int max_deletion, int max_insertion,
                             int quorum1, int quorum2, int min_motif_length, String dataset_name){

        String parameters = "_err" + max_error + "_wc" + max_motif_gap + "_del" + max_deletion +
                "_ins" + max_insertion + "_q1_" + quorum1 + "_q2_" + quorum2 + "_l" + min_motif_length;

        String catalog_path = "output/motif_catalog_" + dataset_name + parameters;
        String catalog_occs_path = catalog_path + "_occs";

        catalog_file = createOutputFile(catalog_path);
        catalog_occs_file = createOutputFile(catalog_occs_path);

        if (catalog_file != null) {
            String header = "motif_id\tlength\tmain_category\tscore\toccurrences\toccs_ratio\texact_occs\tmotif_cogs\tmotif\n";
            catalog_file.write(header);
        }
    }

    public int getCountPrintedMotifs(){
        return  count_printed_motifs;
    }

    public void closeFiles(){
        catalog_file.close();
        catalog_occs_file.close();
    }

    public void printMotif(Motif motif, Utils utils){
        count_printed_motifs ++;

        if (!debug) {

            catalog_occs_file.print("motif_" + motif.getMotif_id() + "\t");

            HashMap<Integer, String> occ_seq_and_location = new HashMap<>();
            for (Occurrence occ : motif.get_occs()) {
                OccurrenceNode occ_node = occ.getNodeOcc();
                if (occ.getEdge() != null) {
                    Edge edge = occ.getEdge();
                    occ_node = (OccurrenceNode) edge.getDest();
                }
                int occ_length = occ.getLength();
                for (Map.Entry<Integer, ArrayList<String>> entry : occ_node.getResults().entrySet()) {
                    int seq_id = entry.getKey();
                    String word_id = entry.getValue().get(0);
                    word_id += "_l" + occ_length;
                    occ_seq_and_location.put(seq_id, word_id);

                }
            }
            for (Map.Entry<Integer, String> entry : occ_seq_and_location.entrySet()) {
                int seq = entry.getKey();
                String word_id = entry.getValue();
                catalog_occs_file.print("seq" + seq + "_" + word_id + "\t");
            }
            catalog_occs_file.print("\n");

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

            catalog_file.write(motifs_catalog_line + "\n");
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

    private PrintWriter createOutputFile(String path){

        try {
            new File("output").mkdir();
            try {

                PrintWriter output_file = new PrintWriter(path + ".txt", "UTF-8");
                return output_file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }catch (SecurityException e){
            System.out.println("The directory 'output' could not be created, therefore no output is printed. " +
                    "Please create a directory named 'output' in the following path: " + System.getProperty("user.dir"));
        }
        return null;
    }
}
