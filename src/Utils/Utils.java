package Utils;

import COGAlphabet.WordArray;
import Main.Taxa;
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

    public String[] datasets_names;
    /**
     * accession number to tax key
     */
    public HashMap<Integer, Integer> accs_index_to_tax_key;
    /**
     * bacterial name to Taxa object
     */
    public HashMap<String, Taxa> bac_name_to_tax;
    /**
     * bacterial key to Taxa object
     */
    public HashMap<Integer, Taxa> key_to_tax;

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

    public Utils(){
        index_to_cog = new ArrayList<String>();
        cog_to_index = new HashMap<String, Integer>();

        datasets_size = new ArrayList<>();

        accs_index_to_tax_key = new HashMap<>();
        bac_name_to_tax = new HashMap<>();
        key_to_tax = new HashMap<>();
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
                //int cog_family_mem_num = genes_in_cog_count.get(cog);
                //double n = Math.pow((double)min_genome_size, 2.0);
                //n = length_sum / bac_indexes.size();
                //double f_i = (cog_family_mem_num/(double)length_sum) * n;
                //System.out.println(f_i);

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

    private static void update_tax_count(HashMap<String, Integer> tax_count, String tax_name) {
        if (tax_count.containsKey(tax_name)) {
            tax_count.put(tax_name, tax_count.get(tax_name) + 1);
        } else {
            tax_count.put(tax_name, 1);
        }
    }

    private static int update_tax_index(String tax_name, int index, HashMap<String, Integer> tax_to_index, ArrayList<String> index_to_tax){
        if (!tax_to_index.containsKey(tax_name)) {
            tax_to_index.put(tax_name, index);
            index_to_tax.add(tax_name);
            index++;
        }
        return index;
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
        OccurrenceNode suffix_tree_node = (OccurrenceNode)suffix_tree.getRoot();
        MotifNode trie_node = trie.getRoot();
        //add the nodes recursively
        addMotifNode(trie, suffix_tree_node, trie_node, q);
    }

    private void addMotifNode(Trie trie, OccurrenceNode suffix_tree_src_node, MotifNode trie_src_node, int q){
        HashMap<Integer, Edge> outgoing_edges = suffix_tree_src_node.getEdges();
        MotifNode trie_target_node;
        for (Edge edge : outgoing_edges.values()) {
            OccurrenceNode suffix_tree_target_node = (OccurrenceNode) edge.getDest();
            if (suffix_tree_target_node.getCount_by_keys() >= q) {
                WordArray edge_label = edge.getLabel();
                String label = (edge_label.to_string(this));
                trie_target_node = trie.put(edge_label, trie_src_node, false, this);
                addMotifNode(trie, suffix_tree_target_node, trie_target_node, q);
            }
        }
    }

    public void buildMotifsTreeFromFile(String input_motifs_file_name, Trie motif_tree) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(input_motifs_file_name));
        try {
            String line = br.readLine();

            while (line != null) {
                String[] line_arr = line.split("\t");
                int motif_id = Integer.parseInt(line_arr[0]);
                WordArray word = create_word_array(line_arr, 1, line_arr.length);
                motif_tree.put(word, motif_id, null);

                line = br.readLine();
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

    public void build_motifs_trie(ArrayList<MotifNode> found_motifs, Trie motif_tree) throws Exception {

        for (Iterator<MotifNode> iterator = found_motifs.iterator(); iterator.hasNext(); ) {
            MotifNode node = iterator.next();

            String str = node.getSubstring();
            String[] str_arr = str.split("\\|");

            WordArray word = create_word_array(str_arr, 0, str_arr.length);

            MotifNode new_node = motif_tree.put(word, node.getMotifKey(), null);
            new_node.setStd(node.getStd());
        }
    }

    static public ArrayList<String[]>  read_gene_annotations(String file_name, HashMap<Integer, String[]> existing_keys) throws FileNotFoundException {
        ArrayList<String[]> cog_words_anots = new ArrayList<String[]>();
        cog_words_anots.add(null); //force indexes start from index 1
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        try {
            String line = br.readLine();
            int counter = 0;

            while (line != null) {
                //4#NC_009926#-1#Acaryochloris_marina_MBIC11017_uid58167	recombinase A 	RecD/TraA family helicase 	1A family penicillin-binding protein
                String[] line_arr = line.split("\t");
                int key = Integer.parseInt(line_arr[0].split("#")[0]);
                if (existing_keys == null) {
                    cog_words_anots.add(line_arr);
                }else{
                    if (existing_keys.containsKey(key)){
                        existing_keys.put(key, line_arr);
                    }
                }

                line = br.readLine();
                counter++;

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cog_words_anots;
    }

    /**
     * Convert string array with cog indexes to wordArray, using cog_encoding
     * @param cog_string array of strings, each cell contains a cog
     * @param start_index the index in cog_string where the string starts at
     * @param end_index the index in cog_string where the string ends at (not including)
     * @param cog_to_index
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
    public HashMap<String, COG> read_cog_info_table() throws FileNotFoundException {

        BufferedReader br = new BufferedReader(new FileReader("input/COG_INFO_TABLE.txt"));
        try {

            int counter = 0;
            String line = br.readLine();
            while (line != null) {

                String[] cog_line = line.split(";");

                String cog_id = cog_line[0];
                cog_id = cog_id.substring(3);

                if (cog_to_index.containsKey(cog_id)) {
                    String letters = cog_line[1];

                    int index = 2;
                    String char_letters = ""+letters.charAt(0);
                    String fun_cats = cog_line[index++];
                    String letter_descs = cog_line[index++].replace("/", "_");
                    letter_descs = letter_descs.replace(":", "");
                    for (int i = 1; i < letters.length(); i++) {
                        char_letters += letters.charAt(i);
                        fun_cats += "_OR_" + cog_line[index++];
                        letter_descs += "_OR_" + cog_line[index++].replace("/", "_").replace(":", "");
                    }

                    String sub_cat_desc = cog_line[index];
                    String cog_type = "";
                    /*
                    if (cog_line.length >=7) {
                        cog_type = cog_line[6];
                    }*/

                    COG cog = new COG(cog_id, char_letters, fun_cats, letter_descs, sub_cat_desc, cog_type);
                    cog_info.put(cog_id, cog);
                }
                line = br.readLine();
                counter ++;
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  cog_info;
    }

    /*
    public void read_bacs_with_plasmid_file() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("input/bacs_with_plasmid.txt"));
        String line = br.readLine();
        while (line != null) {
            String bac_name = line.trim();
            bacs_with_plasmid.add(bac_name);

            line = br.readLine();
        }
    }*/
    /*
    //filter bacs by genus
    public void read_tax_count_file() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader("input/tax_count.txt"));
        HashMap<String, Integer> genus_count_bac = new HashMap<>();
        HashMap<String, Integer> genus_count_plasmid = new HashMap<>();

        try {

            //header
            String line = br.readLine();
            line = br.readLine();
            int counter = 0;
            while (line != null) {

                String[] tax_line = line.split("\t");
                String genus = tax_line[2];
                int bac_count = Integer.parseInt(tax_line[3]);
                int plasmid_count = Integer.parseInt(tax_line[4]);

                boolean is_valid_genus = true;
                if (Utils.datasets_names[0].equals("plasmid")){
                    if (bac_count <5 || plasmid_count < 10) {
                        is_valid_genus = false;
                    }
                }
                if (is_valid_genus) {
                    genus_to_index.put(genus, counter);
                    index_to_genus.add(genus);

                    genus_count_bac.put(genus, bac_count);
                    genus_count_plasmid.put(genus, plasmid_count);
                    counter ++;
                }
                line = br.readLine();

            }
            for (int i = 0; i < datasets_names.length; i++) {
                if (datasets_names[i].equals("plasmid")) {
                    genus_count_by_dataset.add(genus_count_plasmid);
                }else {
                    genus_count_by_dataset.add(genus_count_bac);
                }
            }

            br.close();

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
    */
    private static String unknown_tax_char(String tax){
        if (tax.equals("-") || tax.equals("")){
            return "X";
        }
        return  tax;
    }


    public void read_tax_file() throws FileNotFoundException {

        BufferedReader br = new BufferedReader(new FileReader("input/taxa.txt"));
        try {
            //header
            String line = br.readLine();

            line = br.readLine();
            while (line != null) {

                String[] tax_line = line.split(",");
                String genome_name = tax_line[5];
                int bac_sort_index = Integer.parseInt(tax_line[6]);
                String kingdom = tax_line[0];
                String phylum = tax_line[1];
                String tax_class = tax_line[2];
                String genus = tax_line[3];
                String species = tax_line[4];
                String order = tax_line[8];

                phylum = unknown_tax_char(phylum);
                tax_class = unknown_tax_char(tax_class);
                genus = unknown_tax_char(genus);

                Taxa taxa = new Taxa(kingdom, phylum, tax_class, genus, species, order, genome_name, bac_sort_index);

                bac_name_to_tax.put(genome_name, taxa);
                key_to_tax.put(bac_sort_index, taxa);
                line = br.readLine();
            }
            br.close();

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

    //TODO: change G and n
    public double computeMotifPval(String[] motif_cogs, int max_insertions, int max_error, int max_deletions, int dataset_index, int motif_occs_keys_size, int motif_id){
        int genomes_count = datasets_size.get(dataset_index);
        int avg_genome_size = dataset_length_sum.get(dataset_index)/genomes_count;
        //HashMap<String, Integer> cog_homolog_num  = dataset_cog_homolog_num.get(dataset_index);
        //HashMap<String, HashSet<Integer>> cog_to_containing_genomes = Utils.cog_to_containing_genomes.get(dataset_index);
        HashSet<Integer> intersection_of_genomes_with_motif_cogs = new HashSet<Integer>(cog_to_containing_genomes.get(motif_cogs[0]));
        for (int i = 1; i < motif_cogs.length; i++) {
            intersection_of_genomes_with_motif_cogs.retainAll(cog_to_containing_genomes.get(motif_cogs[i]));
        }

        //int min_cog_genomes_count = genomes_count;
        int cog_genomes_count;
        int paralog_count_product_sum = 0;
        int paralog_count_product;
        int counter = 1;
        for (int seq_key: intersection_of_genomes_with_motif_cogs) {

            HashMap<String, Integer> curr_seq_paralog_count = genome_to_cog_paralog_count.get(seq_key);
            paralog_count_product = 1;
            for (String cog : motif_cogs) {
                int curr_cog_paralog_count = curr_seq_paralog_count.get(cog);
                paralog_count_product *= curr_cog_paralog_count;
            }
            paralog_count_product_sum += paralog_count_product;


            counter++;
            /*cog_genomes_count = cog_to_containing_genomes.get(cog).size();
            if (cog_genomes_count < min_cog_genomes_count){
                min_cog_genomes_count = cog_genomes_count;
            }
                if (cog_homolog_num.get(cog) == null) {
                    paralog_count_product = 0;
                } else {
                    int paralog_num = cog_homolog_num.get(cog);
                    paralog_count_product *= paralog_num;
                }
            }*/
        }
        //System.out.println("Psi="+paralog_count_product);
        String error_type = "mismatch";
        if (max_insertions > 0){
            error_type = "insert";
        }else if (max_error > 0){
            error_type = "mismatch";
        }else if(max_deletions > 0){
            error_type = "deletion";
        }
        int average_paralog_count = paralog_count_product_sum/intersection_of_genomes_with_motif_cogs.size();
        //if (motif_id == 1785){
            //System.out.println(average_paralog_count);
        //}
        //motif_occ_num = Math.min(motif_occ_num, min_cog_genomes_count);
        return Formulas.pval_cross_genome(avg_genome_size/*min_genome_size*/, motif_cogs.length, max_insertions, average_paralog_count, genomes_count, motif_occs_keys_size, error_type, q_val);
    }
}