package Core.Genomes;

import java.util.*;

/**
 */
public class GenomesInfo {

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
    }

    public WordArray createWordArray(List<Gene> genes/*, boolean nonDirectons*/){
        return alphabet.createWordArray(genes/*, nonDirectons*/);
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

    public void countParalogsInSeqs(WordArray word, int currGenomeId){
        for (int ch : word.getWordArray()) {

            Map<Integer, Integer> currGenomeParalogsCount = genomeToCogParalogCount.get(currGenomeId);
            if (currGenomeParalogsCount == null) {
                currGenomeParalogsCount = new HashMap<>();
                genomeToCogParalogCount.put(currGenomeId, currGenomeParalogsCount);
            }

            int currCogParalogCount = 1;
            if (currGenomeParalogsCount.containsKey(ch)) {
                currCogParalogCount += currGenomeParalogsCount.get(ch);
            }
            currGenomeParalogsCount.put(ch, currCogParalogCount);

            Set<Integer> genomes = cogToContainingGenomes.get(ch);
            if (genomes == null) {
                genomes = new HashSet<>();
                cogToContainingGenomes.put(ch, genomes);
            }
            genomes.add(currGenomeId);
        }
    }

    public int getDatasetLengthSum() {
        return datasetLengthSum;
    }

    public void setDatasetLengthSum(int dataset_length_sum) {
        this.datasetLengthSum = dataset_length_sum;
    }
}
