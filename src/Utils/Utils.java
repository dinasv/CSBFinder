package Utils;

import Main.Readers;
import Words.WordArray;
import SuffixTrees.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Contain static methods for building suffix trees
 */
public class Utils {
    public ArrayList<String> index_to_char;
    public HashMap<String, Integer> char_to_index;

    public static final int WC_CHAR_INDEX = 0;
    public static final String WC_CHAR = "*";
    public static final int GAP_CHAR_INDEX = 1;
    public static final String GAP_CHAR = "_";
    public static final int UNK_CHAR_INDEX = 2;
    public static final String UNK_CHAR = "X";

    /**
     * Size (number of genomic elements)
     */
    public int number_of_genomes;

    /**
     * accession number to tax key
     */
    public HashMap<String, Integer> genome_name_to_key;
    public HashMap<Integer, String> genome_key_to_name;

    public int dataset_length_sum ;

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

    public HashMap<Integer, Integer>  words_per_genome;

    //memoization of computed q_val - it is the same for each pattern length
    public double[] q_val;

    private Readers reader;

    public HashMap<String, COG> cog_info;

    public Utils(HashMap<String, COG> cog_info){
        index_to_char = new ArrayList<String>();
        char_to_index = new HashMap<String, Integer>();

        number_of_genomes = 0;

        genome_name_to_key = new HashMap<>();
        genome_key_to_name = new HashMap<>();

        dataset_length_sum = 0;

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

        words_per_genome = new HashMap<>();

        this.cog_info = cog_info;


        q_val = new double[200];

        //reader = new Readers();
    }

    private void countParalogsInSeqs(ArrayList<Gene> directon, int curr_seq_index){
        for (Gene gene : directon) {

            HashMap<String, Integer> curr_genome_paralogs_count = genome_to_cog_paralog_count.get(curr_seq_index);
            if (curr_genome_paralogs_count == null) {
                curr_genome_paralogs_count = new HashMap<>();
                genome_to_cog_paralog_count.put(curr_seq_index, curr_genome_paralogs_count);
            }

            int curr_cog_paralog_count = 1;
            if (curr_genome_paralogs_count.containsKey(gene.getCog_id())) {
                curr_cog_paralog_count += curr_genome_paralogs_count.get(gene.getCog_id());
            }
            curr_genome_paralogs_count.put(gene.getCog_id(), curr_cog_paralog_count);

            HashSet<Integer> genomes = cog_to_containing_genomes.get(gene.getCog_id());
            if (genomes == null) {
                genomes = new HashSet<>();
                cog_to_containing_genomes.put(gene.getCog_id(), genomes);
            }
            genomes.add(curr_seq_index);
        }
    }

    private ArrayList<Gene> remove_x_from_end(ArrayList<Gene> directon){
        int i = directon.size()-1;
        while (i>=0 && directon.get(i).getCog_id().equals(UNK_CHAR)){
            i--;
        }
        directon = new ArrayList<Gene>(directon.subList(0, i+1));
        return directon;
    }

    private int split_replicon_to_directons(ArrayList<Gene> regulon_genes, GeneralizedSuffixTree words_suffix_tree,
                                            int curr_seq_index, String regulon_id) {

        ArrayList<Gene> directon = new ArrayList<>();
        int directons_length_sum = 0;
        int gene_index = 0;
        for (Gene gene : regulon_genes) {
            //end directon if it is the last gene in the regulon, or if next gene is on different strand
            boolean end_directon = (gene_index == regulon_genes.size()-1) ||
                    !(gene.getStrand().equals(regulon_genes.get(gene_index+1).getStrand()));
            if (directon.size() == 0) {
                if (!gene.getCog_id().equals(UNK_CHAR) && !end_directon) {
                    directon.add(gene);
                }
            } else {
                directon.add(gene);

                if (end_directon){
                    int directon_start_index = gene_index-directon.size()+1;
                    int reverse = 1;
                    directon = remove_x_from_end(directon);

                    if (directon.get(0).getStrand().equals("-")) {
                        Collections.reverse(directon);
                        reverse = -1;
                        directon_start_index = gene_index;
                    }

                    //add directon to suffix tree
                    if (directon.size() > 1) {
                        WordArray cog_word = create_word_array(directon);
                        words_suffix_tree.put(cog_word, curr_seq_index, regulon_id, directon_start_index, reverse);

                        countParalogsInSeqs(directon, curr_seq_index);
                        directons_length_sum += directon.size();
                    }

                    directon = new ArrayList<>();
                }
            }
            gene_index ++;
        }

        return directons_length_sum;
    }

    private boolean updateGenomes(String curr_genome_name, int genome_size, int curr_genome_index,
                                  HashSet<Integer> genomes_indexes){
        boolean is_updated = false;
        if (!genome_name_to_key.containsKey(curr_genome_name)) {
            if (genome_size > 0 && curr_genome_index != -1) {
                genomes_indexes.add(curr_genome_index);
                genome_key_to_name.put(curr_genome_index, curr_genome_name);
                genome_name_to_key.put(curr_genome_name, curr_genome_index);
                is_updated = true;
            }
        }
        return is_updated;
    }

    /**
     *
     * @param input_file_path
     * @param dataset_gst
     * @return
     * @throws Exception
     */
    public int read_and_build_dataset_tree(String input_file_path, GeneralizedSuffixTree dataset_gst) {
        String file_name = input_file_path;

        HashSet<Integer> genomes_indexes = new HashSet<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file_name));

            int length_sum = 0;
            int genome_size = 0;

            try {
                String line = br.readLine();

                String replicon_id = "";
                int curr_genome_index = -1;
                String curr_genome_name = "";

                ArrayList<Gene> replicon_genes = new ArrayList<>();

                while (line != null) {
                    if (line.startsWith(">")) {

                        if (curr_genome_index != -1) {
                            int replicon_length = split_replicon_to_directons(replicon_genes, dataset_gst,
                                    curr_genome_index, replicon_id);
                            length_sum += replicon_length;
                            genome_size += replicon_length;
                        }

                        replicon_genes = new ArrayList<>();

                        line = line.substring(1); //remove ">"
                        //e.g. Acaryochloris_marina_MBIC11017_uid58167|NC_009927
                        String[] word_desc = line.trim().split("\\|");

                        if (word_desc.length > 0) {
                            String next_genome_name = word_desc[0];

                            updateGenomes(curr_genome_name, genome_size, curr_genome_index, genomes_indexes);

                            if (!next_genome_name.equals(curr_genome_name)) {
                                curr_genome_index++;
                                genome_size = 0;
                            }

                            curr_genome_name = next_genome_name;
                            if (word_desc.length > 1) {
                                replicon_id = word_desc[1];
                            }
                        }
                    } else {
                        String[] split_line = line.trim().split("\t");
                        if (split_line.length > 1) {
                            String gene_family = split_line[0];
                            String strand = split_line[1];
                            Gene gene = new Gene(gene_family, strand);
                            replicon_genes.add(gene);
                        }
                    }

                    line = br.readLine();
                }

                int replicon_length = split_replicon_to_directons(replicon_genes, dataset_gst,
                        curr_genome_index, replicon_id);
                length_sum += replicon_length;
                genome_size += replicon_length;

                updateGenomes(curr_genome_name, genome_size, curr_genome_index, genomes_indexes);

                dataset_length_sum = length_sum;


                //logger.info("Min genome size=" + min_genome_size);
                //logger.info("Max genome size=" + max_genome_size);
                //logger.info("Number of cog words: " + word_counter);
                //logger.info("Longest cog word: " + longest_cog_word);
                //logger.info("Average cog word length: " + (double) length_sum / word_counter);
                logger.info("Average genome size: " + length_sum / genomes_indexes.size());

                logger.info("Number of genomes " + genomes_indexes.size());
                logger.info("Number of cogs " + char_to_index.size());
                number_of_genomes = genomes_indexes.size();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return genomes_indexes.size();
        } catch (FileNotFoundException e) {
            System.out.println("File " + file_name + " was not found.");
        }
        return -1;
    }


    public void buildPatternsTreeFromFile(String input_patterns_file_name, Trie pattern_tree) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(input_patterns_file_name));
        try {
            //read header
            String line = br.readLine();
            if (line != null){
                line = br.readLine();
            }

            while (line != null) {
                String[] line_arr = line.split("\t");
                if (line_arr.length > 1) {
                    int pattern_id = Integer.parseInt(line_arr[0]);
                    String[] wordStr = line_arr[line_arr.length-1].split("-");
                    WordArray word = create_word_array_from_str(wordStr);
                    pattern_tree.put(word, this, pattern_id);
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
     * Convert a directon to wordArray, using char_to_index
     * @param directon contains the Genes comprising this directon, all in the same strand, without intervening
     *                 gene in the opposite strand
     * @return
     */
     public WordArray create_word_array(ArrayList<Gene> directon){
        int[] word = new int[directon.size()];
        int i = 0;
        for(Gene gene: directon){
            int char_index = -1;
            if (char_to_index.containsKey(gene.getCog_id())) {
                char_index = char_to_index.get(gene.getCog_id());
            } else {
                char_index = index_to_char.size();
                index_to_char.add(gene.getCog_id());
                char_to_index.put(gene.getCog_id(), char_index);
            }

            word[i] = char_index;
            i++;
        }

        return new WordArray(word);
    }

    /**
     * Converts an array of strings to wordArray, using char_to_index
     * @param str contains the characters comprising this str
     * @return
     */
    public WordArray create_word_array_from_str(String[] str){
        int[] word = new int[str.length];
        int i = 0;
        for(String ch: str){
            int char_index = -1;
            if (char_to_index.containsKey(ch)) {
                char_index = char_to_index.get(ch);
            } else {
                char_index = index_to_char.size();
                index_to_char.add(ch);
                char_to_index.put(ch, char_index);
            }

            word[i] = char_index;
            i++;
        }

        return new WordArray(word);
    }


    public double computePatternScore(String[] pattern_chars, int max_insertions, int max_error, int max_deletions,
                                      int pattern_occs_keys_size, int pattern_id){
        int genomes_count = number_of_genomes;
        int avg_genome_size = dataset_length_sum/genomes_count;

        HashSet<Integer> intersection_of_genomes_with_pattern_chars = new HashSet<>(cog_to_containing_genomes.get(pattern_chars[0]));
        for (int i = 1; i < pattern_chars.length; i++) {
            intersection_of_genomes_with_pattern_chars.retainAll(cog_to_containing_genomes.get(pattern_chars[i]));
        }

        int paralog_count_product_sum = 0;
        int paralog_count_product;
        for (int seq_key: intersection_of_genomes_with_pattern_chars) {

            HashMap<String, Integer> curr_seq_paralog_count = genome_to_cog_paralog_count.get(seq_key);
            paralog_count_product = 1;
            for (String cog : pattern_chars) {
                int curr_cog_paralog_count = curr_seq_paralog_count.get(cog);
                paralog_count_product *= curr_cog_paralog_count;
            }
            paralog_count_product_sum += paralog_count_product;
        }

        int average_paralog_count = paralog_count_product_sum/intersection_of_genomes_with_pattern_chars.size();

        String error_type = "mismatch";
        if (max_insertions > 0){
            error_type = "insert";
        }else if (max_error > 0){
            error_type = "mismatch";
        }else if(max_deletions > 0){
            error_type = "deletion";
        }

        return Formulas.pval_cross_genome(avg_genome_size/*min_genome_size*/, pattern_chars.length, max_insertions,
                average_paralog_count, genomes_count, pattern_occs_keys_size, error_type, q_val, pattern_id);
    }
}