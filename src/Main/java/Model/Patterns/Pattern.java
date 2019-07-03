package Model.Patterns;

import Model.OrthologyGroups.COG;
import Model.OrthologyGroups.CogInfo;
import Model.Genomes.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a pattern consisting of characters
 **/
public class Pattern {

    private static final String GENES_DELIMITER = ",";

    private Gene[] patternGenes;
    private Gene[] reverseComplimentPatternArr;

    private String patternId;

    private int length;

    private double score;

    private String mainFunctionalCategory;

    private int familyId;

    private Locations locations;

    private int instancesPerGenomeCount;

    public Pattern(){
        this(null, new Gene[0]);
    }

    public Pattern(Gene[] patternGenes) { this(null, patternGenes); }

    public Pattern(String patternId, List<Gene> patternGenes){
        this(patternId, patternGenes.toArray(new Gene[0]));
    }
    public Pattern(String patternId, Gene[] patternGenes){

        this.patternId = patternId;
        this.patternGenes = Arrays.copyOf(patternGenes, patternGenes.length);
        this.length = patternGenes.length;

        score = 0;
        mainFunctionalCategory = "";
        reverseComplimentPatternArr = reverseComplimentPattern(patternGenes);

        locations = new Locations();

        instancesPerGenomeCount = -1;
    }


    public void setPatternId(String pattern_id){
        this.patternId = pattern_id;
    }

    public String getPatternId(){
        return patternId;
    }

    public void addInstanceLocations(List<Instance> instances){

        for (Instance instance: instances){
            Collection<List<InstanceLocation>> instanceLocations = instance.getInstanceLocations().values();

            List<InstanceLocation> flatInstanceLocations = instanceLocations.stream()
                                                            .flatMap(List::stream)
                                                            .collect(Collectors.toList());

            for (InstanceLocation instanceLocation: flatInstanceLocations){

                InstanceLocation patternLocation = new InstanceLocation(instanceLocation);
                patternLocation.setInstanceLength(instance.getLength());

                addInstanceLocation(patternLocation);
            }
        }

        instancesPerGenomeCount = -1;
    }


    public void addInstanceLocation(InstanceLocation patternLocation){
        locations.addLocation(patternLocation);
        instancesPerGenomeCount = -1;
    }


    public Locations getPatternLocations(){
        return locations;
    }

    public int getLength(){
        return length;
    }

    public Gene[] getPatternGenes() {
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

            StringBuilder main_categories = new StringBuilder();
            if (functional_letter_count.size() > 0) {
                List<Map.Entry<String, Integer>> list = new ArrayList<>(functional_letter_count.entrySet());

                list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

                main_categories = new StringBuilder(functional_letter_to_desc.get(list.get(0).getKey()));
                int max_count = list.get(0).getValue();
                list.remove(0);
                for (Map.Entry<String, Integer> entry : list) {
                    String letter = entry.getKey();
                    int count = entry.getValue();
                    if (count == max_count) {
                        main_categories.append("/").append(functional_letter_to_desc.get(letter));
                    }
                }
            }

            mainFunctionalCategory = main_categories.toString();
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
    private static Gene[] reverseComplimentPattern(Gene[] pattern){

        Gene[] reversedPattern = Arrays.stream(pattern).map(gene -> {
                Gene copy = new Gene(gene.getCogId().intern(), Gene.reverseStrand(gene.getStrand()));
                return copy;
            }).toArray(Gene[]::new);

        reverseArray(reversedPattern);

        return reversedPattern;
    }

    private static <T> void reverseArray(T[] array){
        for(int i=0; i < array.length/2; i++){
            T temp = array[i];
            array[i] = array[array.length-i-1];
            array[array.length-i-1] = temp;
        }
    }

    public Gene[] getReverseComplimentPattern() {
        return reverseComplimentPatternArr;
    }

    public int getFamilyId() {
        return familyId;
    }

    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }

    public Collection<Integer> getInstanceGenomeIds(){
        return locations.getInstanceLocations().stream()
                .collect(Collectors.groupingBy(InstanceLocation::getGenomeId)).keySet();
    }

    public int getInstancesPerGenomeCount(){

        if (instancesPerGenomeCount == -1){
            instancesPerGenomeCount = getInstanceGenomeIds().size();
        }

        return instancesPerGenomeCount;
    }

    public String toString(){
        return toString(patternGenes);
    }

    public static String toString(Gene[] genes){
        String str = Arrays.stream(genes)
                .map(gene -> gene.getCogId() + gene.getStrand())
                .collect(Collectors.joining(GENES_DELIMITER));
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pattern))
            return false;
        if (obj == this)
            return true;

        Pattern other = (Pattern) obj;
        return (Arrays.equals(other.patternGenes, this.patternGenes) ||
                Arrays.equals(other.patternGenes, this.reverseComplimentPatternArr));
    }

}

