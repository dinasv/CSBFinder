package Genomes;

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
    private Map<String, Integer> genome_name_to_id;
    private Map<Integer, String> genome_id_to_name;
    private Map<Integer, String> replicon_id_to_name;

    private int dataset_length_sum ;

    /**
     * for each cog, a set of genomes (indexes) in which the cog appears
     */
    public Map<String, Set<Integer>> cog_to_containing_genomes;

    public Map<Integer, Map<String, Integer>> genome_to_cog_paralog_count;


    private Map<String, Genome> genomesMap;

    private int max_genome_size;

    public GenomesInfo(){
        genomesMap = new HashMap<>();

        max_genome_size = 0;

        genome_name_to_id = new HashMap<>();
        genome_id_to_name = new HashMap<>();
        replicon_id_to_name = new HashMap<>();

        dataset_length_sum = 0;

        cog_to_containing_genomes = new HashMap<>();

        genome_to_cog_paralog_count = new HashMap<>();

        initAlphabet();
    }



    public int getNumberOfGenomes(){
        return genomesMap.size();
    }

    public int getNumberOfReplicons(){
        return replicon_id_to_name.size();
    }


    public Genome addGenome(String genome_name){
        Genome genome = genomesMap.get(genome_name);
        if (genome == null){
            int genome_id = genomesMap.size();
            genome = new Genome(genome_name, genome_id);
            genome_name_to_id.put(genome_name, genome_id);
            genome_id_to_name.put(genome_id, genome_name);
            genomesMap.put(genome_name, genome);
        }

        return genome;
    }

    public Collection<Genome> getGenomes(){
        return genomesMap.values();
    }

    public Map<String, Genome> getGenomesMap() {
        return this.genomesMap;
    }

    public String getGenomeName(int id){
        return genome_id_to_name.get(id);
    }

    public int getGenomeId(String name){
        return genome_name_to_id.get(name);
    }

    public String getRepliconName(int id){
        return replicon_id_to_name.get(id);
    }

    public void addReplicon(Replicon replicon, Genome genome){
        genome.addReplicon(replicon);
        replicon_id_to_name.put(replicon.getId(), replicon.getName());

        max_genome_size = genome.getGenomeSize() > max_genome_size ? genome.getGenomeSize() : max_genome_size;
        dataset_length_sum += replicon.size();
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

    public int getDatasetLengthSum() {
        return dataset_length_sum;
    }

    public void setDatasetLengthSum(int dataset_length_sum) {
        this.dataset_length_sum = dataset_length_sum;
    }
}
