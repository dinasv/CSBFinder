package Main;

import SuffixTrees.GeneralizedSuffixTree;
import SuffixTrees.Trie;
import Utils.COG;
import Utils.Utils;
import Utils.Formulas;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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
    @Parameter(names={"--quorum1", "-q1"}, description = "exact occurrences quorum")
    int quorum1 = 1;
    @Parameter(names={"--quorum2", "-q2"}, description = "approximate occurrences quorum")
    int quorum2 = 1;
    @Parameter(names={"--minlength", "-l"}, description = "minimal motif length")
    int min_motif_length = 2;
    @Parameter(names={"--keys", "-keys"}, description = "if true, count by sequence keys", arity = 1)
    boolean count_by_keys = true;
    @Parameter(names={"--datasetname", "-ds"}, description = "dataset name")
    String dataset_name = "dataset1";
    @Parameter(names={"--input", "-i"}, description = "input file name", required = true)
    String input_file_name = "";
    @Parameter(names={"--motifs", "-m"}, description = "input motifs file name")
    String input_motifs_file_name = null;

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
            System.out.println("-i [input file name]");
        }else {
            Utils utils = new Utils();

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
            min_motif_length = 2 + max_error;
            //System.out.println(min_motif_length);
            MotifFinder mf = new MotifFinder();
            mf.findMotifs(max_error, max_wildcards, max_deletion, max_insertion, quorum1, quorum2, min_motif_length,
                    count_by_keys, dataset_name, input_file_name, input_motifs_file_name, memory_saving_mode, utils, debug);

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

}
