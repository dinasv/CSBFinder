package Utils;

import Words.Directon;
import Words.Replicon;
import Words.WordArray;
import SuffixTrees.*;

import java.io.*;
import java.util.*;
import Main.Writer;

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
    public HashMap<Integer, String> replicon_key_to_name;

    public int dataset_length_sum ;

    public int min_genome_size;

    /**
     * for each cog, a set of genomes (bac_index) in which the cog appears
     */
    public HashMap<String, HashSet<Integer>> cog_to_containing_genomes;

    public HashMap<Integer, HashMap<String, Integer>> genome_to_cog_paralog_count;

    //memoization of computed q_val - it is the same for each pattern length
    public double[] q_val;


    public HashMap<String, COG> cog_info;

    public long initiailMem;
    public long currMem;

    Writer writer = null;


    public Utils(HashMap<String, COG> cog_info, Writer writer){
        this.writer = writer;

        index_to_char = new ArrayList<String>();
        char_to_index = new HashMap<String, Integer>();

        number_of_genomes = 0;

        genome_name_to_key = new HashMap<>();
        genome_key_to_name = new HashMap<>();
        replicon_key_to_name = new HashMap<>();

        dataset_length_sum = 0;

        min_genome_size = Integer.MAX_VALUE;

        cog_to_containing_genomes = new HashMap<>();

        genome_to_cog_paralog_count = new HashMap<>();

        this.cog_info = cog_info;

        initiailMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        currMem = initiailMem;

        q_val = new double[200];

    }

    public void measureMemory(){
        long currMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        if (currMem > this.currMem){
            this.currMem = currMem;
        }
        System.out.println(currMem-initiailMem);
    }

    private void countParalogsInSeqs(String[] directon, int curr_seq_index){
        for (String gene : directon) {

            HashMap<String, Integer> curr_genome_paralogs_count = genome_to_cog_paralog_count.get(curr_seq_index);
            if (curr_genome_paralogs_count == null) {
                curr_genome_paralogs_count = new HashMap<>();
                genome_to_cog_paralog_count.put(curr_seq_index, curr_genome_paralogs_count);
            }

            int curr_cog_paralog_count = 1;
            if (curr_genome_paralogs_count.containsKey(gene)) {
                curr_cog_paralog_count += curr_genome_paralogs_count.get(gene);
            }
            curr_genome_paralogs_count.put(gene, curr_cog_paralog_count);

            HashSet<Integer> genomes = cog_to_containing_genomes.get(gene);
            if (genomes == null) {
                genomes = new HashSet<>();
                cog_to_containing_genomes.put(gene, genomes);
            }
            genomes.add(curr_seq_index);
        }
    }


    private static ArrayList<Directon> splitRepliconToDirectons(Replicon replicon) {

        ArrayList<Directon> directons = new ArrayList<>();

        Directon directon = new Directon();

        int gene_index = 0;
        for (Gene gene : replicon.getGenes()) {
            //end directon if it is the last gene in the regulon, or if next gene is on different strand
            boolean end_directon = (gene_index == replicon.size()-1) ||
                    !(gene.getStrand().equals(replicon.getGenes().get(gene_index+1).getStrand()));
            if (directon.size() == 0) {
                if (!gene.getCog_id().equals(UNK_CHAR) && !end_directon) {
                    directon.add(gene);
                }
            } else {
                directon.add(gene);

                if (end_directon){//directon.size()>0

                    if (directon.size() > 1) {

                        directon.setStartIndex(gene_index-directon.size()+1);

                        directon.removeXFromEnd(UNK_CHAR);
                        if (directon.size() > 1) {
                            directon.setStrand();

                            directons.add(directon);
                        }
                    }

                    directon = new Directon();
                }
            }
            gene_index ++;
        }

        return directons;
    }

    private boolean updateGenomes(String curr_genome_name, int genome_size, int curr_genome_index){

        boolean is_updated = false;
        if (!genome_name_to_key.containsKey(curr_genome_name)) {
            if (genome_size > 0 && curr_genome_index != -1) {
                genome_key_to_name.put(curr_genome_index, curr_genome_name);
                genome_name_to_key.put(curr_genome_name, curr_genome_index);
                is_updated = true;
            }
        }
        return is_updated;
    }

    /**
     * Turns a replicon or a directon into an array of genes and puts in in the @dataset_gst
     * @param replicon
     * @param dataset_gst
     * @param curr_genome_index
     */
    private void putWordInDataTree(Replicon replicon, GeneralizedSuffixTree dataset_gst, int curr_genome_index){
        String[] genes = replicon.getGenesIDs();
        WordArray cog_word = createWordArray(genes);
        dataset_gst.put(cog_word, curr_genome_index, Replicon.index, replicon.getStartIndex(), replicon.getStrand());

        countParalogsInSeqs(genes, curr_genome_index);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param is_directons
     * @param replicon
     * @param dataset_gst
     * @param curr_genome_index
     * @return
     */
    private int updateDataTree(boolean is_directons, Replicon replicon, GeneralizedSuffixTree dataset_gst,
                                int curr_genome_index){

        int replicon_length = 0;
        if (is_directons) {
            ArrayList<Directon> directons = splitRepliconToDirectons(replicon);

            for (Directon directon: directons){
                replicon_length += directon.size();
                putWordInDataTree(directon, dataset_gst, curr_genome_index);
            }

        }else{

            putWordInDataTree(replicon, dataset_gst, curr_genome_index);

            //reverse replicon
            replicon.reverse();
            putWordInDataTree(replicon, dataset_gst, curr_genome_index);

            replicon_length += replicon.size() * 2;
        }
        return replicon_length;
    }

    /**
     *
     * @param input_file_path path to input file with input sequences
     * @param dataset_gst the input sequences are inserted to this GST
     * @return number of input sequences that contains at least one valid direction
     */
    public int readAndBuildDatasetTree(String input_file_path, GeneralizedSuffixTree dataset_gst,
                                       boolean is_directons) {
        String file_name = input_file_path;

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

                Replicon replicon = new Replicon(1);

                while (line != null) {
                    if (line.startsWith(">")) {

                        if (curr_genome_index != -1) {

                            int replicon_length = updateDataTree(is_directons, replicon, dataset_gst, curr_genome_index);

                            length_sum += replicon_length;
                            genome_size += replicon_length;
                        }

                        replicon = new Replicon(1);

                        line = line.substring(1); //remove ">"
                        //e.g. Acaryochloris_marina_MBIC11017_uid58167|NC_009927
                        String[] word_desc = line.trim().split("\\|");

                        if (word_desc.length > 0) {
                            String next_genome_name = word_desc[0];

                            updateGenomes(curr_genome_name, genome_size, curr_genome_index);

                            if (!next_genome_name.equals(curr_genome_name)) {
                                curr_genome_index++;
                                genome_size = 0;
                            }

                            curr_genome_name = next_genome_name;

                            Replicon.index ++;

                            if (word_desc.length > 1) {
                                replicon_id = word_desc[1];
                                replicon_key_to_name.put(Replicon.index, replicon_id);
                            }

                        }
                    } else {
                        String[] split_line = line.trim().split("\t");
                        if (split_line.length > 1) {
                            String gene_family = split_line[0];
                            String strand = split_line[1];
                            Gene gene = new Gene(gene_family, strand);
                            replicon.add(gene);
                        }
                    }

                    line = br.readLine();
                }

                int replicon_length = updateDataTree(is_directons, replicon, dataset_gst, curr_genome_index);
                length_sum += replicon_length;
                genome_size += replicon_length;

                updateGenomes(curr_genome_name, genome_size, curr_genome_index);

                dataset_length_sum = length_sum;


                writer.writeLogger("Average genome size: " + length_sum / genome_key_to_name.size());
                writer.writeLogger("Number of genomes " + genome_key_to_name.size());
                writer.writeLogger("Number of cogs " + char_to_index.size());

                number_of_genomes = genome_key_to_name.size();
                if (number_of_genomes == 0){
                    return -1;
                }

            } catch (IOException e) {
                System.out.println("An exception occured while reading " + file_name);
                return -1;
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Cannot close file " + file_name);
                    return -1;
                }
            }

            return number_of_genomes;
        } catch (FileNotFoundException e) {
            System.out.println("File " + file_name + " was not found.");
        }
        return -1;
    }

    /**
     * Builds a Trie of patterns, given in a file.
     * @param input_patterns_file_name path to input file containing patterns
     * @param pattern_tree the patterns are inserted to this GST
     * @return True if succesful, False if exception occurred
     */
    public boolean buildPatternsTreeFromFile(String input_patterns_file_name, Trie pattern_tree) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(input_patterns_file_name));

            String line = br.readLine();

            int pattern_id = 0;
            while (line != null) {
                if (line.charAt(0) == '>'){
                    try {
                        pattern_id = Integer.parseInt(line.substring(1));
                    }catch (NumberFormatException e){
                        System.out.println("Pattern id should be an integer, found " + line);
                        return false;
                    }
                }else {
                    String[] pattern = line.split("-");

                    if (pattern.length > 1) {
                        WordArray word = createWordArray(pattern);
                        pattern_tree.put(word, this, pattern_id);
                    }
                }
                line = br.readLine();
            }

            try {
                br.close();
            } catch (IOException e) {
                System.out.println("A problem occurred while reading file " +input_patterns_file_name);
                return false;
            }

        } catch (IOException e) {
            System.out.println("A problem occurred while reading file " +input_patterns_file_name);
            return false;
        }
        return true;
    }


    /**
     * Converts an array of strings to wordArray, using char_to_index
     * @param str contains the characters comprising this str
     * @return WordArray representing this str
     */
    public WordArray createWordArray(String[] str){
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
                                      int pattern_occs_keys_size){

        int avg_genome_size = 1;
        if (number_of_genomes > 0 ) {
            avg_genome_size = dataset_length_sum / number_of_genomes;
        }

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

        return Formulas.pval_cross_genome(avg_genome_size/*min_genome_size*/, pattern_chars.length, max_insertions,
                average_paralog_count, number_of_genomes, pattern_occs_keys_size, q_val);
    }
}