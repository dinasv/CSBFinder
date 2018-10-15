package Genomes;

import IO.MyLogger;

import java.util.*;

/**
 */
public class GenomesInfo {
    public List<String> index_to_char;
    public Map<String, Integer> char_to_index;

    public static final int WC_CHAR_INDEX = 0;
    public static final String WC_CHAR = "*";
    public static final int GAP_CHAR_INDEX = 1;
    public static final String GAP_CHAR = "_";
    public static final int UNK_CHAR_INDEX = 2;
    public static final String UNK_CHAR = "X";

    /**
     * Accession number to tax key
     */
    public Map<String, Integer> genome_name_to_id;
    public Map<Integer, String> genome_id_to_name;
    public Map<Integer, String> replicon_id_to_name;

    public int dataset_length_sum ;

    /**
     * for each cog, a set of genomes (indexes) in which the cog appears
     */
    public Map<String, Set<Integer>> cog_to_containing_genomes;

    public Map<Integer, Map<String, Integer>> genome_to_cog_paralog_count;

    public Map<String, COG> cog_info;

    public Map<String, Map<String, Replicon>> genomeToRepliconsMap;

    public int max_genome_size;

    public GenomesInfo(){
        genomeToRepliconsMap = new HashMap<>();

        //number_of_genomes = 0;
        max_genome_size = 0;

        genome_name_to_id = new HashMap<>();
        genome_id_to_name = new HashMap<>();
        replicon_id_to_name = new HashMap<>();

        dataset_length_sum = 0;

        cog_to_containing_genomes = new HashMap<>();

        genome_to_cog_paralog_count = new HashMap<>();

        this.cog_info = cog_info;

        initAlphabet();
    }

    public int getNumberOfGenomes(){
        return genome_id_to_name.size();
    }

    private void initAlphabet(){
        index_to_char = new ArrayList<String>();
        char_to_index = new HashMap<String, Integer>();

        //wild card
        char_to_index.put(WC_CHAR, WC_CHAR_INDEX);
        index_to_char.add(WC_CHAR);

        //gap
        char_to_index.put(GAP_CHAR, GAP_CHAR_INDEX);
        index_to_char.add(GAP_CHAR);

        //unkown cog
        char_to_index.put(UNK_CHAR, UNK_CHAR_INDEX);
        index_to_char.add(UNK_CHAR);
        //if the sequence is not segmented to directons
        char_to_index.put("X+", UNK_CHAR_INDEX);
        char_to_index.put("X-", UNK_CHAR_INDEX);

    }

    public void setCogInfo(Map<String, COG> cog_info) {
        this.cog_info = cog_info;
    }

    public Map<String, COG> getCogInfo() {
        return this.cog_info;
    }

    public Map<String, Map<String, Replicon>> getGenomeToRepliconsMap() {
        return this.genomeToRepliconsMap;
    }

    public int getMaxGenomeSize(){
        return max_genome_size;
    }

    public void countParalogsInSeqs(String[] directon, int curr_seq_index){
        for (String gene : directon) {

            Map<String, Integer> curr_genome_paralogs_count = genome_to_cog_paralog_count.get(curr_seq_index);
            if (curr_genome_paralogs_count == null) {
                curr_genome_paralogs_count = new HashMap<>();
                genome_to_cog_paralog_count.put(curr_seq_index, curr_genome_paralogs_count);
            }

            int curr_cog_paralog_count = 1;
            if (curr_genome_paralogs_count.containsKey(gene)) {
                curr_cog_paralog_count += curr_genome_paralogs_count.get(gene);
            }
            curr_genome_paralogs_count.put(gene, curr_cog_paralog_count);

            Set<Integer> genomes = cog_to_containing_genomes.get(gene);
            if (genomes == null) {
                genomes = new HashSet<>();
                cog_to_containing_genomes.put(gene, genomes);
            }
            genomes.add(curr_seq_index);
        }
    }
}
