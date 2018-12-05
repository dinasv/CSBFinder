package Core.Genomes;

import Core.CogInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a pattern consisting of characters
 **/
public class Pattern {

    private List<Gene> patternGenes;
    private List<Gene> reverseComplimentPatternArr;

    private int patternId;

    private int length;
    private List<Instance> instances;

    private double score;

    private String mainFunctionalCategory;

    private String familyId;

    private Map<Integer, PatternLocationsInGenome> genomeToInstanceLocations;

    public Pattern(){
        this(-1, new ArrayList<>());
    }

    public Pattern(int patternId, List<Gene> patternGenes/*, int instanceCount, int exactInstanceCount*/){

        this.patternId = patternId;
        this.patternGenes = new ArrayList<>();
        this.patternGenes.addAll(patternGenes);
        this.length = patternGenes.size();
        this.instances = instances;
        //this.instanceCount = instanceCount;
        //this.exactInstanceCount = exactInstanceCount;
        score = 0;
        mainFunctionalCategory = "";
        reverseComplimentPatternArr = reverseComplimentPattern(patternGenes);

        genomeToInstanceLocations = new HashMap<>();
    }


    public void setPatternId(int pattern_id){
        this.patternId = pattern_id;
    }

    public int getPatternId(){
        return patternId;
    }

    public void addInstanceLocations(List<Instance> instances){

        for (Instance instance: instances){
            Collection<List<InstanceLocation>> instanceLocations = instance.getInstanceLocations().values();

            List<InstanceLocation> flatinstanceLocations = instanceLocations.stream()
                                                            .flatMap(List::stream)
                                                            .collect(Collectors.toList());

            for (InstanceLocation instanceLocation: flatinstanceLocations){

                InstanceLocation patternLocation = new InstanceLocation(instanceLocation);
                patternLocation.setInstanceLength(instance.getLength());

                addInstanceLocation(patternLocation);
            }
        }
    }


    public void addInstanceLocation(InstanceLocation patternLocation){
        PatternLocationsInGenome patternLocations = genomeToInstanceLocations.get(patternLocation.getGenomeId());
        if (patternLocations == null){
            patternLocations = new PatternLocationsInGenome();
        }
        patternLocations.addLocation(patternLocation);
        genomeToInstanceLocations.put(patternLocation.getGenomeId(), patternLocations);
    }


    public Map<Integer, PatternLocationsInGenome> getPatternLocations(){
        return genomeToInstanceLocations;
    }

    public int getLength(){
        return length;
    }

    public List<Gene> getPatternGenes() {
        return patternGenes;
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void calculateMainFunctionalCategory(CogInfo cogInfo){
        if (mainFunctionalCategory.length() == 0 && cogInfo.cogInfoExists()) {

            Map<String, Integer> functional_letter_count = new HashMap<>();
            Map<String, String> functional_letter_to_desc = new HashMap<>();
            for (Gene gene : patternGenes) {
                String cogId = gene.getCogId();
                COG cog = cogInfo.getCog(cogId);
                if (cog != null) {
                    String[] functional_letters = cog.getFunctionalLetters();
                    String[] functional_categories = cog.getFunctionalCategories();
                    for (int i = 0; i < functional_letters.length; i++) {
                        String letter = functional_letters[i];
                        if (!functional_letter_count.containsKey(letter)) {
                            functional_letter_count.put(letter, 0);
                        }
                        functional_letter_count.put(letter, functional_letter_count.get(letter) + 1);
                        functional_letter_to_desc.put(letter, functional_categories[i]);
                    }
                }
            }

            String main_categories = "";
            if (functional_letter_count.size() > 0) {
                List<Map.Entry<String, Integer>> list = new ArrayList<>(functional_letter_count.entrySet());

                Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

                main_categories = functional_letter_to_desc.get(list.get(0).getKey());
                int max_count = list.get(0).getValue();
                list.remove(0);
                for (Map.Entry<String, Integer> entry : list) {
                    String letter = entry.getKey();
                    int count = entry.getValue();
                    if (count == max_count) {
                        main_categories += "/" + functional_letter_to_desc.get(letter);
                    }
                }
            }

            mainFunctionalCategory = main_categories;
        }
    }

    public String getMainFunctionalCategory() {
        return mainFunctionalCategory;
    }

    private static String reverseStrand(String strand){
        return strand.equals("-") ? "+" : "-";
    }

    /**
     * Returns the reverseCompliment compliment of a pattern. e.g. [COG1+, COG2-] -> [COG2+, COG1-]
     * @param pattern
     * @return
     */
    private static List<Gene> reverseComplimentPattern(List<Gene> pattern){

        List<Gene> reversedPattern = pattern.stream().map(gene ->
                {Gene copy = new Gene(gene.getCogId(), Gene.reverseStrand(gene.getStrand()));
                return copy;})
                .collect(Collectors.toList());

        Collections.reverse(reversedPattern);

        return reversedPattern;
    }

    public List<Gene> getReverseComplimentPattern() {
        return reverseComplimentPatternArr;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public int getInstancesPerGenome(){
        return genomeToInstanceLocations.size();
    }

    public String toString(){
        String str = "";
        for (Gene gene: patternGenes){
            str += gene.getCogId() + gene.getStrand() + ",";
        }
        return str;
    }

    public static class LengthComparator implements Comparator<Pattern> {

        @Override
        public int compare(Pattern o1, Pattern o2) {

            // descending order
            return o2.getLength() - o1.getLength();
        }
    }

    public static class ScoreComparator implements Comparator<Pattern> {
        @Override
        public int compare(Pattern o1, Pattern o2) {
            if (o2.getScore() < o1.getScore()) return -1;
            if (o2.getScore() > o1.getScore()) return 1;
            return 0;
        }
    }

}

