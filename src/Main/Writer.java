package Main;

import SuffixTrees.Edge;
import SuffixTrees.InstanceNode;
import Utils.Utils;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dina on 19/05/2017.
 * Writes the output files:
 *      catalog_file: OGMs catalog
 *      motif_instances_file: The strings in which each OGM has an instance
 */
public class Writer {
    //output files
    private PrintWriter catalog_file;
    private PrintWriter motif_instances_file;

    private DecimalFormat df;

    private int max_error;
    private int max_deletion;
    private int max_insertion;
    private int count_printed_motifs;
    private boolean debug;

    public Writer(int max_error, int max_deletion, int max_insertion, boolean debug, String catalog_path,
                  String motif_instances_path){
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);

        this.max_error = max_error;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        this.debug = debug;
        count_printed_motifs = 0;

        createFiles(catalog_path, motif_instances_path);

    }

    private void createFiles(String catalog_path, String motif_instances_path){

        catalog_file = createOutputFile(catalog_path);
        motif_instances_file = createOutputFile(motif_instances_path);

        String header = "motif_id\tlength\tscore\tinstance_count\tinstance_ratio\texact_instance_count" +
                "\tmotif";
        if (catalog_file != null) {
            catalog_file.write(header+ "\n");
        }

    }

    public int getCountPrintedMotifs(){
        return  count_printed_motifs;
    }

    public void closeFiles(){
        if (catalog_file != null) {
            catalog_file.close();
        }
        if (motif_instances_file != null) {
            motif_instances_file.close();
        }
    }

    public void printMotif(Motif motif, Utils utils){
        count_printed_motifs ++;

        if (!debug) {

            motif_instances_file.print("motif_" + motif.getMotif_id() + "\t");

            HashMap<Integer, String> instance_seq_and_location = new HashMap<>();
            for (Instance instance : motif.get_instances()) {
                InstanceNode instance_node = instance.getNodeInstance();
                if (instance.getEdge() != null) {
                    Edge edge = instance.getEdge();
                    instance_node = (InstanceNode) edge.getDest();
                }
                int instance_length = instance.getLength();
                for (Map.Entry<Integer, ArrayList<String>> entry : instance_node.getResults().entrySet()) {
                    int seq_id = entry.getKey();
                    String word_id = entry.getValue().get(0);
                    word_id += "_l" + instance_length;
                    instance_seq_and_location.put(seq_id, word_id);

                }
            }
            for (Map.Entry<Integer, String> entry : instance_seq_and_location.entrySet()) {
                int seq = entry.getKey();
                String word_id = entry.getValue();
                motif_instances_file.print("seq" + seq + "_" + word_id + "\t");
            }
            motif_instances_file.print("\n");

            String[] motif_arr = motif.getMotif_arr();
            //String[] ret = getMotifMainCat(motif_arr, utils);
            //String motif_str = ret[0];
            //String motifMainCat = ret[1];

            String motifs_catalog_line = motif.getMotif_id() + "\t" + motif.getLength() + "\t";
                    //+motifMainCat + "\t";

            double motif_pval = utils.computeMotifPval(motif_arr, max_insertion, max_error, max_deletion, 0,
                    motif.get_instance_count(), motif.getMotif_id());

            motifs_catalog_line += df.format(motif_pval) + "\t"
                    + motif.get_instance_count() + "\t"
                    + df.format(motif.get_instance_count()/(double)utils.datasets_size.get(0)) + "\t"
                    + motif.get_exact_instance_count() + "\t";

            motifs_catalog_line += "\t" + String.join("-", motif_arr);

            catalog_file.write(motifs_catalog_line + "\n");
        }
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
