package Core.Genomes;

import java.util.*;

/**
 */
public class GenomesInfo {
    public List<Gene> indexToChar;
    public Map<Gene, Integer> charToIndex;

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
    public Map<Integer, Set<Integer>> cogToContainingGenomes;

    public Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount;


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

    public boolean genomeExists(String genomeId){
        return genomesMap.get(genomeId) != null;
    }

    public void addGenome(Genome genome){

        genomeNameToId.put(genome.getName(), genome.getId());
        genomeIdToName.put(genome.getId(), genome.getName());
        genomesMap.put(genome.getName(), genome);

        maxGenomeSize = genome.getGenomeSize() > maxGenomeSize ? genome.getGenomeSize() : maxGenomeSize;

    }

    public Genome getGenome(String genomeId){
        return genomesMap.get(genomeId);
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

    public void addReplicon(Replicon replicon){

        repliconIdToName.put(replicon.getId(), replicon.getName());

        datasetLengthSum += replicon.size();
    }

    private void initAlphabet(){
        indexToChar = new ArrayList<Gene>();
        charToIndex = new HashMap<Gene, Integer>();

        //wild card
        Gene gene = new Gene(WC_CHAR, Strand.FORWARD);
        charToIndex.put(gene, WC_CHAR_INDEX);
        indexToChar.add(gene);

        gene = new Gene(GAP_CHAR, Strand.FORWARD);
        //gap
        charToIndex.put(gene, GAP_CHAR_INDEX);
        indexToChar.add(gene);

        gene = new Gene(UNK_CHAR, Strand.FORWARD);
        //unkown cog
        charToIndex.put(gene, UNK_CHAR_INDEX);
        indexToChar.add(gene);

        gene = new Gene(UNK_CHAR, Strand.REVERSE);
        //if the sequence is not segmented to directons
        //charToIndex.put("X+", UNK_CHAR_INDEX);
        charToIndex.put(gene, UNK_CHAR_INDEX);

    }

    public int getMaxGenomeSize(){
        return maxGenomeSize;
    }

    public void countParalogsInSeqs(WordArray word, int curr_seq_index){
        for (int ch : word.getWordArray()) {

            Map<Integer, Integer> curr_genome_paralogs_count = genomeToCogParalogCount.get(curr_seq_index);
            if (curr_genome_paralogs_count == null) {
                curr_genome_paralogs_count = new HashMap<>();
                genomeToCogParalogCount.put(curr_seq_index, curr_genome_paralogs_count);
            }

            int curr_cog_paralog_count = 1;
            if (curr_genome_paralogs_count.containsKey(ch)) {
                curr_cog_paralog_count += curr_genome_paralogs_count.get(ch);
            }
            curr_genome_paralogs_count.put(ch, curr_cog_paralog_count);

            Set<Integer> genomes = cogToContainingGenomes.get(ch);
            if (genomes == null) {
                genomes = new HashSet<>();
                cogToContainingGenomes.put(ch, genomes);
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
