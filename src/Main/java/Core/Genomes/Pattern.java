package Core.Genomes;

import Core.CogInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a pattern consisting of characters
 **/
public class Pattern {
    /**
     * e.g. COG1234 COG5234
     */
    private String pattern;
    /**
     * Each cell contains the COG id, e.g. [COG1234, COG5234]
     */
    private String[] patternArr;
    private String[] reverseComplimentPatternArr;

    private int patternId;
    private int instanceCount;

    private int length;
    private List<Instance> instances;

    private int exactInstanceCount;

    private double score;

    private String mainFunctionalCategory;

    private String familyId;

    private Map<Integer, PatternLocationsInGenome> genomeToInstanceLocations;

    public Pattern(int patternId, String pattern, String[] patternArr, int instanceCount, int exactInstanceCount){

        this.patternId = patternId;
        this.pattern = pattern;
        this.patternArr = patternArr;
        this.length = patternArr.length;
        this.instances = instances;
        this.instanceCount = instanceCount;
        this.exactInstanceCount = exactInstanceCount;
        score = 0;
        mainFunctionalCategory = "";
        reverseComplimentPatternArr = reverseComplimentPattern(patternArr);

        genomeToInstanceLocations = new HashMap<>();
    }

    public Pattern(int patternId, String pattern, String[] patternArr){
        this(patternId, pattern, patternArr, 0, 0);
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

                PatternLocationsInGenome patternLocations = genomeToInstanceLocations.get(patternLocation.getGenomeId());
                if (patternLocations == null){
                    patternLocations = new PatternLocationsInGenome();
                }
                patternLocations.addLocation(patternLocation);
                genomeToInstanceLocations.put(patternLocation.getGenomeId(), patternLocations);
            }
        }
    }


    public Map<Integer, PatternLocationsInGenome> getPatternLocations(){
        return genomeToInstanceLocations;
    }

    public int getLength(){
        return length;
    }

    public void setPattern(String pattern){
        this.pattern = pattern;
    }
    public String getPattern(){
        return pattern;
    }

    public int getInstanceCount(){
        return instanceCount;
    }

    public String[] getPatternArr() {
        return patternArr;
    }

    public int getExactInstanceCount() {
        return exactInstanceCount;
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void calculateMainFunctionalCategory(CogInfo cogInfo, boolean nonDirectons){
        if (mainFunctionalCategory.length() ==0 && cogInfo.cogInfoExists()) {

            Map<String, Integer> functional_letter_count = new HashMap<>();
            Map<String, String> functional_letter_to_desc = new HashMap<>();
            for (String cog_id : patternArr) {
                if (nonDirectons){
                    cog_id = cog_id.substring(0, cog_id.length()-1);
                }
                COG cog = cogInfo.getCog(cog_id);
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
     * Returns the reverse compliment of a pattern. e.g. [COG1+, COG2-] -> [COG2+, COG1-]
     * @param pattern
     * @return
     */
    private static String[] reverseComplimentPattern(String[] pattern){
        String[] reversed_pattern = new String[pattern.length];
        int j = 0;
        for (int i = pattern.length-1; i >= 0; i--) {
            String gene = pattern[i];
            String prefix = gene.substring(0, gene.length()-1);
            String reversed_strand = reverseStrand(gene.substring(gene.length()-1));
            reversed_pattern[j++] = prefix + reversed_strand;
        }
        return reversed_pattern;
    }

    public String[] getReverseComplimentPatternArr() {
        return reverseComplimentPatternArr;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
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

