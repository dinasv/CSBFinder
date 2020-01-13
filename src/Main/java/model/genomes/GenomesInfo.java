package model.genomes;

import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class GenomesInfo {

    /**
     * Accession number to tax key
     */
    private Map<Integer, Genome> genomesById;
    private Map<String, Genome> genomesByName;

    private double[][] distancesBetweenGenomes;

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

        distancesBetweenGenomes = new double[0][0];

        maxGenomeSize = 0;
        countReplicons = 0;

        datasetLengthSum = 0;

        cogToContainingGenomes = new HashMap<>();

        genomeToCogParalogCount = new HashMap<>();

        alphabet = new Alphabet();
    }

    public WordArray createWordArray(List<Gene> genes){
        return alphabet.createWordArray(genes);
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

    public Iterator<Gene> getAlphabetLetters(){
        return alphabet.getAlphabetLetters();
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

        replicon.getGenes()
                .forEach(gene -> {
                    alphabet.addLetter(new Gene(gene.getCogId().intern(), Strand.FORWARD));
                    alphabet.addLetter(new Gene(gene.getCogId().intern(), Strand.REVERSE));
                    alphabet.addLetter(new Gene(gene.getCogId().intern(), Strand.INVALID));
                });

    }


    public int getMaxGenomeSize(){
        return maxGenomeSize;
    }

    public void countParalogsInSeqs(WordArray word, int currGenomeId){
        for (int ch : word.getWordArray()) {

            Map<Integer, Integer> currGenomeParalogsCount = genomeToCogParalogCount
                    .computeIfAbsent(currGenomeId, k -> new HashMap<>());

            int currCogParalogCount = 1;
            if (currGenomeParalogsCount.containsKey(ch)) {
                currCogParalogCount += currGenomeParalogsCount.get(ch);
            }
            currGenomeParalogsCount.put(ch, currCogParalogCount);

            Set<Integer> genomes = cogToContainingGenomes.computeIfAbsent(ch, k -> new HashSet<>());
            genomes.add(currGenomeId);
        }
    }

    public int getDatasetLengthSum() {
        return datasetLengthSum;
    }

    public void setDatasetLengthSum(int dataset_length_sum) {
        this.datasetLengthSum = dataset_length_sum;
    }

    public void computeDistancesBetweenGenomesAllVsAll(){

        if (distancesBetweenGenomes.length == genomesById.size()){
            return;
        }

        distancesBetweenGenomes = new double[genomesById.size()][genomesById.size()];

        List<Genome> genomes = new ArrayList<>(genomesById.values());

        for (int i = 0; i < genomes.size(); i++) {
            Genome genome1 = genomes.get(i);

            //the distance between a genome and itself is 1
            distancesBetweenGenomes[i][i] = 1;

            for (int j = i+1; j < genomes.size(); j++) {
                Genome genome2 = genomes.get(j);

                computeDistancesBetweenGenomes(genome1, genome2);
            }
        }
    }

    private void computeDistancesBetweenGenomes(Genome genome1, Genome genome2){

        Set<Gene> genes1 = getGeneSet(genome1);
        Set<Gene> genes2 = getGeneSet(genome2);

        Set<Gene> union = new HashSet<>(genes1);
        union.addAll(genes2);
        Set<Gene> intersection = new HashSet<>(genes1);
        intersection.retainAll(genes2);

        double distance = (double)intersection.size()/union.size();

        distancesBetweenGenomes[genome1.getId()][genome2.getId()] = distance;
        distancesBetweenGenomes[genome2.getId()][genome1.getId()] = distance;
    }

    private Set<Gene> getGeneSet(Genome genome){
        Set<Gene> genes = new HashSet<>();

        for (Replicon replicon: genome.getReplicons()){
            genes.addAll(replicon.getGenes().stream().filter(gene -> !gene.getCogId().equals(Alphabet.UNK_CHAR))
                    .map(gene -> new Gene(gene.getCogId(), Strand.INVALID)).collect(Collectors.toList()));
        }

        return genes;
    }

    public double getGenomesDistance(int genomeId1, int genomeId2){

        if (genomeId1 >= distancesBetweenGenomes.length || genomeId2 >= distancesBetweenGenomes.length
            || genomeId1 < 0 || genomeId2 < 0){
            return -1;
        }
        return distancesBetweenGenomes[genomeId1][genomeId2];
    }
}
