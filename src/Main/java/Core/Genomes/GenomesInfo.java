package Core.Genomes;

import java.util.*;

/**
 */
public class GenomesInfo {
    public List<String> indexToChar;
    public Map<String, Integer> charToIndex;

    public static final int WC_CHAR_INDEX = 0;
    public static final String WC_CHAR = "*";
    public static final int GAP_CHAR_INDEX = 1;
    public static final String GAP_CHAR = "_";
    public static final int UNK_CHAR_INDEX = 2;
    public static final String UNK_CHAR = "X";

    /**
     * Accession number to tax key
     */
    private Map<String, Integer> genomeNameToId;
    private Map<Integer, String> genomeIdToName;
    private Map<Integer, String> repliconIdToName;

    private int datasetLengthSum;

    /**
     * for each cog, a set of genomes (indexes) in which the cog appears
     */
    public Map<String, Set<Integer>> cogToContainingGenomes;

    public Map<Integer, Map<String, Integer>> genomeToCogParalogCount;


    private Map<String, Genome> genomesMap;

    private int maxGenomeSize;

    public GenomesInfo(){
        genomesMap = new HashMap<>();

        maxGenomeSize = 0;

        genomeNameToId = new HashMap<>();
        genomeIdToName = new HashMap<>();
        repliconIdToName = new HashMap<>();

        datasetLengthSum = 0;

        cogToContainingGenomes = new HashMap<>();

        genomeToCogParalogCount = new HashMap<>();

        initAlphabet();
    }



    public int getNumberOfGenomes(){
        return genomesMap.size();
    }

    public int getNumberOfReplicons(){
        return repliconIdToName.size();
    }


    public Genome addGenome(String genome_name){
        Genome genome = genomesMap.get(genome_name);
        if (genome == null){
            int genome_id = genomesMap.size();
            genome = new Genome(genome_name, genome_id);
            genomeNameToId.put(genome_name, genome_id);
            genomeIdToName.put(genome_id, genome_name);
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
        return genomeIdToName.get(id);
    }

    public int getGenomeId(String name){
        return genomeNameToId.get(name);
    }

    public String getRepliconName(int id){
        return repliconIdToName.get(id);
    }

    public void addReplicon(Replicon replicon, Genome genome){
        genome.addReplicon(replicon);
        repliconIdToName.put(replicon.getId(), replicon.getName());

        maxGenomeSize = genome.getGenomeSize() > maxGenomeSize ? genome.getGenomeSize() : maxGenomeSize;
        datasetLengthSum += replicon.size();
    }

    private void initAlphabet(){
        indexToChar = new ArrayList<String>();
        charToIndex = new HashMap<String, Integer>();

        //wild card
        charToIndex.put(WC_CHAR, WC_CHAR_INDEX);
        indexToChar.add(WC_CHAR);

        //gap
        charToIndex.put(GAP_CHAR, GAP_CHAR_INDEX);
        indexToChar.add(GAP_CHAR);

        //unkown cog
        charToIndex.put(UNK_CHAR, UNK_CHAR_INDEX);
        indexToChar.add(UNK_CHAR);
        //if the sequence is not segmented to directons
        charToIndex.put("X+", UNK_CHAR_INDEX);
        charToIndex.put("X-", UNK_CHAR_INDEX);

    }

    public int getMaxGenomeSize(){
        return maxGenomeSize;
    }

    public void countParalogsInSeqs(String[] directon, int curr_seq_index){
        for (String gene : directon) {

            Map<String, Integer> curr_genome_paralogs_count = genomeToCogParalogCount.get(curr_seq_index);
            if (curr_genome_paralogs_count == null) {
                curr_genome_paralogs_count = new HashMap<>();
                genomeToCogParalogCount.put(curr_seq_index, curr_genome_paralogs_count);
            }

            int curr_cog_paralog_count = 1;
            if (curr_genome_paralogs_count.containsKey(gene)) {
                curr_cog_paralog_count += curr_genome_paralogs_count.get(gene);
            }
            curr_genome_paralogs_count.put(gene, curr_cog_paralog_count);

            Set<Integer> genomes = cogToContainingGenomes.get(gene);
            if (genomes == null) {
                genomes = new HashSet<>();
                cogToContainingGenomes.put(gene, genomes);
            }
            genomes.add(curr_seq_index);
        }
    }

    public int getDatasetLengthSum() {
        return datasetLengthSum;
    }

    public void setDatasetLengthSum(int dataset_length_sum) {
        this.datasetLengthSum = dataset_length_sum;
    }
}
