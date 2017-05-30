package Utils;

import COGAlphabet.WordArray;
import Main.MotifReader;
import SuffixTrees.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Contain static methods for building suffix trees
 */
public class Utils {
    public ArrayList<String> index_to_cog;
    public HashMap<String, Integer> cog_to_index;

    /**
     * Size (number of genomic elements) of each dataset
     */
    public ArrayList<Integer> datasets_size;

    /**
     * accession number to tax key
     */
    public HashMap<Integer, Integer> accs_index_to_tax_key;

    public ArrayList<Integer> dataset_length_sum ;

    public int min_genome_size;
    public int max_genome_size;

    public Logger logger;

    /**
     * For each dataset, for each cog - saves the average number of cog occs per genome (excluding genomes in which cog doesn't appear)
     */
    public ArrayList<HashMap<String, Integer>> dataset_cog_homolog_num = new ArrayList<>();
    /**
     * for each cog, a set of genomes (bac_index) in which the cog appears
     */
    public HashMap<String, HashSet<Integer>> cog_to_containing_genomes;

    public HashMap<Integer, HashMap<String, Integer>> genome_to_cog_paralog_count;

    public ArrayList<HashMap<String, Integer>> genus_count_by_dataset;

    public HashMap<String, Integer> genus_to_index;

    public ArrayList<String> index_to_genus;

    public ArrayList<HashMap<String, Integer>> class_count_by_dataset;

    public HashMap<String, Integer> class_to_index;

    public ArrayList<String> index_to_class;

    public ArrayList<HashMap<String, Integer>> phylum_count_by_dataset;

    public HashMap<String, Integer> phylum_to_index;

    public ArrayList<String> index_to_phylum;

    public HashSet<String> bacs_with_plasmid;

    public HashMap<String, COG> cog_info;

    //memoization of computed q_val - it is the same for each motif length
    public double[] q_val;

    private MotifReader reader;

    public Utils(){
        index_to_cog = new ArrayList<String>();
        cog_to_index = new HashMap<String, Integer>();

        datasets_size = new ArrayList<>();

        accs_index_to_tax_key = new HashMap<>();

        dataset_length_sum = new ArrayList<>();

        min_genome_size = Integer.MAX_VALUE;
        max_genome_size = 0;

        logger = Logger.getLogger("MyLog");

        dataset_cog_homolog_num = new ArrayList<>();

        cog_to_containing_genomes = new HashMap<>();

        genome_to_cog_paralog_count = new HashMap<>();

        genus_count_by_dataset = new ArrayList<>();

        genus_to_index = new HashMap<>();

        index_to_genus = new ArrayList<>();

        class_count_by_dataset = new ArrayList<>();
        class_to_index = new HashMap<>();

        index_to_class = new ArrayList<>();

        phylum_count_by_dataset = new ArrayList<>();

        phylum_to_index = new HashMap<>();

        index_to_phylum = new ArrayList<>();

        bacs_with_plasmid = new HashSet<>();

        cog_info = new HashMap<String, COG>();

        q_val = new double[200];

        reader = new MotifReader();
    }


    /**
     *
     * @param input_file_name
     * @param words_suffix_tree
     * @return
     * @throws Exception
     */
    public int read_and_build_cog_words_tree(String input_file_name, GeneralizedSuffixTree words_suffix_tree) throws Exception {
        String file_name = "input/"+input_file_name+".txt";
        //For each cog, how many genes belong to that cog in the dataset
        HashMap<String, Integer> genes_in_cog_count = new HashMap<>();

        HashSet<Integer> bac_indexes = new HashSet<>();

        BufferedReader br = new BufferedReader(new FileReader(file_name));
        int length_sum = 0;
        int genome_size = 0;

        try {
            String line = br.readLine();
            int word_counter = 1;

            int longest_cog_word = 0;
            //String last_bac_id = "";
            int last_bac_index = -1;

            while (line != null) {
                String[] cogs = line.split("\t");
                if (cogs.length - 1 > longest_cog_word) {
                    longest_cog_word = cogs.length - 1;
                }

                //e.g. 1#NC_009927#-1#Acaryochloris_marina_MBIC11017_uid58167#494
                String[] word_name = cogs[0].split("#");
                //e.g. Acaryochloris_marina_MBIC11017_uid58167
                String bac_name = word_name[3];

                //the last number (after the last #) indicates the id of the bacterial strain
                int curr_bac_index = Integer.parseInt(word_name[word_name.length - 1]);

                //new bacteria
                if (curr_bac_index != last_bac_index) {

                    if (genome_size < min_genome_size && genome_size!= 0){
                        min_genome_size = genome_size;
                    }
                    if (genome_size > max_genome_size){
                        max_genome_size = genome_size;
                    }

                    genome_size = 0;

                    last_bac_index = curr_bac_index;
                    bac_indexes.add(curr_bac_index);
                }

                int word_size = cogs.length - 1 - StringUtils.countMatches(line, "X");
                length_sum += word_size;
                genome_size += word_size;

                //the index of the directon
                int word_id = Integer.parseInt(word_name[0]);

                WordArray cog_word = create_word_array(cogs, 1, cogs.length);

                words_suffix_tree.put(cog_word, curr_bac_index, word_id);

                for (int i = 1; i < cogs.length; i++) {
                    String curr_cog = cogs[i];

                    HashMap<String, Integer> curr_genome_paralogs_count = genome_to_cog_paralog_count.get(curr_bac_index);
                    if (curr_genome_paralogs_count == null){
                        curr_genome_paralogs_count = new HashMap<>();
                        genome_to_cog_paralog_count.put(curr_bac_index, curr_genome_paralogs_count);
                    }

                    int curr_cog_paralog_count = 1;
                    if (curr_genome_paralogs_count.containsKey(curr_cog)){
                        curr_cog_paralog_count += curr_genome_paralogs_count.get(curr_cog);
                    }
                    curr_genome_paralogs_count.put(curr_cog, curr_cog_paralog_count);


                    HashSet<Integer> genomes = cog_to_containing_genomes.get(curr_cog);
                    if (genomes == null) {
                        genomes = new HashSet<>();
                        cog_to_containing_genomes.put(curr_cog, genomes);
                    }
                    genomes.add(curr_bac_index);

                }

                word_counter++;
                line = br.readLine();
            }

            dataset_length_sum.add(length_sum);

            /*
            for (Map.Entry<String, Integer> entry : genes_in_cog_count.entrySet()) {
                String cog = entry.getKey();
                float genome_num = cog_to_containing_genomes.get(cog).size();
                int average_paralog_num = Math.round(entry.getValue()/genome_num);
                genes_in_cog_count.put(cog, average_paralog_num);
            }
            */
            logger.info("Min genome size=" + min_genome_size);
            logger.info("Max genome size=" + max_genome_size);
            logger.info("Number of cog words: " + word_counter);
            logger.info("Longest cog word: " + longest_cog_word);
            logger.info("Average cog word length: " + (double) length_sum / word_counter);
            logger.info("Average genome size: " + length_sum / bac_indexes.size());

            logger.info("Number of bacs " + bac_indexes.size());
            logger.info("Number of cogs " + cog_to_index.size());
            datasets_size.add(bac_indexes.size());
            //dataset_cog_homolog_num.add(genes_in_cog_count);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return length_sum / bac_indexes.size();
    }

    //TODO: Change hashmaps to arraylists for children?
    /**
     * Goes over the suffix_tree and simultaneously adding nodes to the trie
     * @param trie the new motifs trie
     * @param suffix_tree the Data Tree
     * @param q the suffix_tree node is added only if it occurs in at least q sequences
     */
    public void buildMotifTreeFromDataTree(Trie trie, GeneralizedSuffixTree suffix_tree, int q){
        suffix_tree.computeCount();
        OccurrenceNode data_tree_node = (OccurrenceNode)suffix_tree.getRoot();
        MotifNode trie_node = trie.getRoot();
        //add the nodes recursively
        addMotifNode(trie, data_tree_node, trie_node, q);
    }

    private void addMotifNode(Trie trie, OccurrenceNode data_tree_src_node, MotifNode trie_src_node, int q){
        HashMap<Integer, Edge> outgoing_edges = data_tree_src_node.getEdges();
        MotifNode trie_target_node;
        for (Edge edge : outgoing_edges.values()) {
            OccurrenceNode data_tree_target_node = (OccurrenceNode) edge.getDest();
            if (data_tree_target_node.getCount_by_keys() >= q) {
                WordArray edge_label = edge.getLabel();
                String label = (edge_label.to_string(this));
                trie_target_node = trie.put(edge_label, trie_src_node, false, this);
                addMotifNode(trie, data_tree_target_node, trie_target_node, q);
            }
        }
    }

    public void buildMotifsTreeFromFile(String input_motifs_file_name, Trie motif_tree) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(input_motifs_file_name));
        try {
            String line = br.readLine();

            while (line != null) {
                String[] line_arr = line.split("\t");
                if (line_arr.length > 1) {
                    int motif_id = Integer.parseInt(line_arr[0].substring(6));
                    WordArray word = create_word_array(line_arr, 1, line_arr.length);
                    //motif_tree.put(word, motif_id, null);
                    motif_tree.put(word, this, motif_id);
                    line = br.readLine();
                }
            }

        } catch (IOException e) {
        e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Convert string array with cog indexes to wordArray, using cog_encoding
     * @param cog_string array of strings, each cell contains a cog
     * @param start_index the index in cog_string where the string starts at
     * @param end_index the index in cog_string where the string ends at (not including)
     * @return
     */
     public WordArray create_word_array(String[] cog_string, int start_index, int end_index) throws Exception {
        int[] word = new int[end_index - start_index];
        for (int i = 0; i < word.length; i++) {

            String cog = cog_string[i + start_index];
            int cog_index = -1;
            if (cog_to_index.get(cog) == null) {
                cog_index = index_to_cog.size();
                index_to_cog.add(cog);
                cog_to_index.put(cog, cog_index);
            } else {
                cog_index = cog_to_index.get(cog);
            }

            word[i] = cog_index;
        }

        return new WordArray(word);
    }

    /**
     * Read COG_INFO_TABLE.txt and fill cog_info. For each cog that is used in our data, save information of functional category
     * @throws FileNotFoundException
     */
    public void read_cog_info_table() throws FileNotFoundException {
        cog_info = reader.read_cog_info_table(cog_to_index);
    }


    public double computeMotifPval(String[] motif_cogs, int max_insertions, int max_error, int max_deletions,
                                   int dataset_index, int motif_occs_keys_size, int motif_id){
        int genomes_count = datasets_size.get(dataset_index);
        int avg_genome_size = dataset_length_sum.get(dataset_index)/genomes_count;

        HashSet<Integer> intersection_of_genomes_with_motif_cogs = new HashSet<Integer>(cog_to_containing_genomes.get(motif_cogs[0]));
        for (int i = 1; i < motif_cogs.length; i++) {
            intersection_of_genomes_with_motif_cogs.retainAll(cog_to_containing_genomes.get(motif_cogs[i]));
        }

        int paralog_count_product_sum = 0;
        int paralog_count_product;
        for (int seq_key: intersection_of_genomes_with_motif_cogs) {

            HashMap<String, Integer> curr_seq_paralog_count = genome_to_cog_paralog_count.get(seq_key);
            paralog_count_product = 1;
            for (String cog : motif_cogs) {
                int curr_cog_paralog_count = curr_seq_paralog_count.get(cog);
                paralog_count_product *= curr_cog_paralog_count;
            }
            paralog_count_product_sum += paralog_count_product;
        }

        int average_paralog_count = paralog_count_product_sum/intersection_of_genomes_with_motif_cogs.size();



        String error_type = "mismatch";
        if (max_insertions > 0){
            error_type = "insert";
        }else if (max_error > 0){
            error_type = "mismatch";
        }else if(max_deletions > 0){
            error_type = "deletion";
        }


        return Formulas.pval_cross_genome(avg_genome_size/*min_genome_size*/, motif_cogs.length, max_insertions,
                average_paralog_count, genomes_count, motif_occs_keys_size, error_type, q_val, motif_id);
    }
}