package Core.Genomes;

import java.util.*;

/**
 */
public class GenomesInfo {
    //public List<Gene> indexToChar;
    //public Map<Gene, Integer> charToIndex;

    /*
    public static final int WC_CHAR_INDEX = 0;
    public static final String WC_CHAR = "*";
    public static final int GAP_CHAR_INDEX = 1;
    public static final String GAP_CHAR = "_";
    public static final int UNK_CHAR_INDEX = 2;
    public static final String UNK_CHAR = "X";
*/
    /**
     * Accession number to tax key
     */
    private Map<Integer, Genome> genomesById;
    private Map<String, Genome> genomesByName;

    private int countReplicons;

    private int datasetLengthSum;

    /**
     * for each cog, a set of genomes (indexes) in which the cog appears
     */
    public Map<Integer, Set<Integer>> cogToContainingGenomes;

    public Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount;

    private int maxGenomeSize;

    private Alphabet alphabet;

    public GenomesInfo(){
        genomesByName = new HashMap<>();
        genomesById = new HashMap<>();

        maxGenomeSize = 0;
        countReplicons = 0;

        datasetLengthSum = 0;

        cogToContainingGenomes = new HashMap<>();

        genomeToCogParalogCount = new HashMap<>();

        alphabet = new Alphabet();
        //initAlphabet();
    }

    public WordArray createWordArray(List<Gene> genes, boolean nonDirectons){
        return alphabet.createWordArray(genes, nonDirectons);
    }

    public Gene getLetter(int index){
        return alphabet.getLetter(index);
    }

    public int getLetter(Gene gene){
        return alphabet.getLetter(gene);
    }

    public int alphabetSize() {
        return alphabet.alphabetSize();
    }

    public int getNumberOfGenomes(){
        return genomesByName.size();
    }

    public int getNumberOfReplicons(){
        return countReplicons;
    }

    public boolean genomeExists(String genomeId){
        return genomesByName.get(genomeId) != null;
    }

    public void addGenome(Genome genome){

        genomesById.put(genome.getId(), genome);
        genomesByName.put(genome.getName(), genome);

        maxGenomeSize = genome.getGenomeSize() > maxGenomeSize ? genome.getGenomeSize() : maxGenomeSize;

    }

    public Genome getGenome(String genomeName){
        return genomesByName.get(genomeName);
    }

    public Genome getGenome(int genomeId){
        return genomesById.get(genomeId);
    }

    public Collection<Genome> getGenomes(){
        return genomesByName.values();
    }

    public Map<String, Genome> getGenomesByName() {
        return this.genomesByName;
    }

    public String getGenomeName(int id){
        if (!genomesById.containsKey(id)){
            return null;
        }
        return genomesById.get(id).getName();
    }

    public int getGenomeId(String name){
        return genomesByName.get(name).getId();
    }


    public void addReplicon(Replicon replicon){

        countReplicons++;

        datasetLengthSum += replicon.size();
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
