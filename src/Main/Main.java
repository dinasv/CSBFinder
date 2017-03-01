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
    int min_motif_length = 2 + max_error;
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

            MotifFinder mf = new MotifFinder();
            mf.findMotifs(max_error, max_wildcards, max_deletion, max_insertion, quorum1, quorum2, min_motif_length,
                    count_by_keys, dataset_name, input_file_name, input_motifs_file_name, memory_saving_mode, utils);

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            if (debug) {
                System.out.println("q2=" + quorum2 + " err=" + max_error + " time: " + estimatedTime);
            }

        }

    }

    /*
    //TODO: add argument parser (JCommander). Check if mismatch/indel parameters are valid (min word length)
    private void runProgram(String[] args) throws Exception {





        if (args.length > 0) {
            try {
                FileHandler fh = new FileHandler("MotifFinder_log.log");
                Utils.logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            }catch (SecurityException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            //Utils.load_tax_similarity();

            //mf = new MotifFinder(logger, Utils.index_to_cog, Utils.cog_to_index);
            //String arg = args[0];

            //HashMap<String, COG> cog_info = null;
            /*
            if(arg.equals("compare")){

                int number_of_datasets = Integer.parseInt(args[1]);
                datasets_names = new String[number_of_datasets];
                String[] datasets_names = new String[number_of_datasets];
                for (int i = 0; i < number_of_datasets; i++) {
                    datasets_names[i] = args[i + 2];
                    datasets_names[i] = args[i + 2];
                }
                int index = 2 + number_of_datasets;

                //Utils.sort_motifs_by = args[index++];

                //Utils.read_tax_count_file();
                //Utils.read_bacs_with_plasmid_file();

                ArrayList<GeneralizedSuffixTree> datasets_suffix_trees = new ArrayList<>();

                GeneralizedSuffixTree dataset_words_suffix_tree;
                for (int i = 0; i < number_of_datasets; i++) {
                    logger.info("Building "+datasets_names[i]+" tree");
                    dataset_words_suffix_tree = new GeneralizedSuffixTree();

                    Utils.read_and_build_cog_words_tree(datasets_names[i], dataset_words_suffix_tree, logger);

                    datasets_suffix_trees.add(dataset_words_suffix_tree);
                }

                cog_info = Utils.read_cog_info_table();


                //Utils.buildMotifTreeFromDataTree("input/cog_words_" + datasets_names[0] + ".txt", motifs_suffix_tree, logger, cog_to_index, 1, index_to_cog);
                //Trie motifs_suffix_tree = new Trie("enumeration");
                //Utils.buildMotifTreeFromDataTree(motifs_suffix_tree, datasets_suffix_trees.get(0), quorum1);

                //ArrayList<MotifNode> found_motifs = mf.findMotifs(0, 0, 0, 0, quorum1, min_motif_length, count_by_keys, 0, datasets_names[0], cog_info, datasets_suffix_trees.get(0), motifs_suffix_tree);

                logger.info("Comparing motifs started");
                long startTime = System.nanoTime();
                MotifCompare mc = new MotifCompare(number_of_datasets, max_error, max_wildcards, max_deletion, max_insertion, quorum1, quorum2, min_motif_length, count_by_keys, logger, Utils.cog_to_index, Utils.index_to_cog);
                mc.compareDatasets(datasets_suffix_trees, datasets_names, cog_info);
                float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);

                logger.info("Comparing motifs finished, took: " + estimatedTime + " seconds");
                System.out.println("ins=" + max_insertion + " q="+ quorum2 +": finding motifs finished, took: " + estimatedTime + " seconds");*/
                /*PrintWriter genus_file = null;
                try {
                    genus_file = new PrintWriter("output/genus_count.txt", "UTF-8");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                genus_file.println("genus\tbac\tplasmid");
                HashMap<String, Integer> genus_count_bac = Utils.genus_count_by_dataset.get(0);
                HashMap<String, Integer> genus_count_plasmid = Utils.genus_count_by_dataset.get(1);
                for (Map.Entry<String, Integer> entry : genus_count_bac.entrySet()) {
                    String genus = entry.getKey();
                    int count_bac = entry.getValue();
                    int count_plasmid = 0;
                    if( genus_count_plasmid.get(genus)!=null) {
                        count_plasmid = genus_count_plasmid.get(genus);
                    }
                    genus_file.println(genus + "\t" + count_bac + "\t" + count_plasmid);
                }
                genus_file.close();*/
/*
            }
            else if (arg.equals("find")) {
                if (args.length >= 7) {
                    int index = 1;
                    String dataset1_name = args[index++];
                    /*
                    max_error = Integer.parseInt(args[index++]);
                    max_wildcards = Integer.parseInt(args[index++]);
                    max_deletion = Integer.parseInt(args[index++]);
                    max_insertion = Integer.parseInt(args[index++]);
                    quorum1 = Integer.parseInt(args[index++]);
                    min_motif_length = Integer.parseInt(args[index++]);
                    count_by_keys = false;
                    if (args[index++].equals("T")){
                        count_by_keys = true;
                    }
                    */
                    //build dataset1 words suffix tree
                   /* GeneralizedSuffixTree dataset_words_suffix_tree = new GeneralizedSuffixTree();

                    Utils.read_and_build_cog_words_tree(dataset1_name, dataset_words_suffix_tree, logger);

                    cog_info = Utils.read_cog_info_table();

                    long startTime = System.nanoTime();
                    logger.info("Building enumeration tree started");
                    /*
                    Trie motifs_suffix_tree = new Trie("enumeration");
                    //Utils.buildMotifTreeFromDataTree("input/cog_words_" + dataset1_name + ".txt", motifs_suffix_tree, logger, cog_to_index, 1, index_to_cog);
                    Utils.buildMotifTreeFromDataTree(motifs_suffix_tree, dataset_words_suffix_tree, quorum1);

                    float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
                    logger.info("Building enumeration tree finished, took: " + estimatedTime + " seconds");

                    //ArrayList<MotifNode> found_motifs = mf.findMotifs(0, 0, 0, 0, quorum1, min_motif_length, count_by_keys, 3, dataset1_name, cog_info, dataset_words_suffix_tree, motifs_suffix_tree);
                    Trie motifs_trie = new Trie("motif");
                    Utils.buildMotifTreeFromDataTree(motifs_trie, dataset_words_suffix_tree, quorum1);
                    //Utils.build_motifs_trie(found_motifs, motifs_trie, cog_to_index, index_to_cog);
                    mf.findMotifs(max_error, max_wildcards, max_deletion, max_insertion, quorum1, quorum2, min_motif_length, count_by_keys, 3, dataset1_name, cog_info, dataset_words_suffix_tree, motifs_trie);
                    */
              /*  }else{
                    System.out.println("Arguments should be one of the following: find/[compare] dataset1_name [dataset2_name] max_error max_wildcards max_deletion quorum1 min_motif_length");
                }
            }else{
                System.out.println("Arguments should be one of the following: find/[compare] dataset1_name [dataset2_name] max_error max_wildcards max_deletion quorum1 min_motif_length");
            }

        }else{
            System.out.println("Arguments should be one of the following: find/compare dataset1_name dataset2_name max_error max_wildcards max_deletion quorum1 min_motif_length");
        }

    }*/


/*
    static private void compare_from_file(String dataset1_name, String dataset2_name) throws Exception {

        GeneralizedSuffixTree bac_words_suffix_tree = buildDataset2Tree(dataset2_name);

        logger.info("Comparing motifs started");
        long startTime = System.nanoTime();

        MotifCompare mc = new MotifCompare(null, bac_words_suffix_tree, max_error, max_wildcards, max_deletion, quorum1, min_motif_length, cog_encoding, logger, cog_to_index, bitset_to_cog, index_to_cog);
        mc.findMotifsFromFile(dataset1_name, dataset2_name);

        float estimatedTime = (float)(System.nanoTime() - startTime)/(float)Math.pow(10, 9);
        logger.info("Comparing motifs finished, took: " + estimatedTime +" seconds");

    }
*/




    static private void test() throws Exception {
        //System.out.println(Formulas.q_insert(2200, 25, 1));
        //System.out.println(Formulas.q_homologs(2200, 20, 1, 2000));
        //n, w, error, Psi, G(total genomes), g(appears in at least g genomes)
        //System.out.println(Formulas.pval_cross_genome(2000, 15, 1, 500, 1485, 500, "mismatch"));
/*
        BigDecimal result = Formulas.pval_cross_genome(10, 3, 0, 1, 1000, 500);
        System.out.println(result.toString());
        System.out.println(result.doubleValue());*/
    }
    /*
    private static void printAllSuffixes(Trie trie, WordArray word_bitset){
        Trie.Pair<MotifNode, Integer> pair = trie.search(word_bitset);
        MotifNode suffix = pair.getFirst();
        while (suffix != null){
            System.out.println(suffix.getSubstring());
            suffix = (MotifNode)suffix.getSuffix();
        }
    }
    */
}
