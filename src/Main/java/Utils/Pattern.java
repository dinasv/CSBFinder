package Utils;

import java.util.*;

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
    private String[] pattern_arr;
    private String[] reverse_pattern_arr;

    private int pattern_id;
    private int instance_count;

    private int length;
    private List<Instance> instances;

    private int exact_instance_count;

    private double score;

    private String main_functional_category;

    private String familyId;

    public Pattern(int pattern_id, String pattern, String[] pattern_arr, int length, Set<Integer> seq_keys,
                   List<Instance> instances, int exact_instance_count){
        this.pattern_id = pattern_id;
        this.pattern = pattern;
        this.pattern_arr = pattern_arr;
        this.length = length;
        this.instances = instances;
        instance_count = seq_keys.size();
        this.exact_instance_count = exact_instance_count;
        score = 0;
        main_functional_category = "";
        reverse_pattern_arr = reverseComplimentPattern(pattern_arr);
    }

    public void setPatternId(int pattern_id){
        this.pattern_id = pattern_id;
    }

    public int getPatternId(){
        return pattern_id;
    }

    public List<Instance> get_instances(){
        return instances;
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
        return instance_count;
    }

    public String[] getPatternArr() {
        return pattern_arr;
    }

    public int getExactInstanceCount() {
        return exact_instance_count;
    }

    public void calculateScore(Utils utils, int max_insertion, int max_error, int max_deletion){

        score = utils.computePatternScore(pattern_arr, max_insertion, max_error, max_deletion, instance_count);
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void calculateMainFunctionalCategory(Utils utils, boolean non_directons){
        if (utils.cog_info != null) {

            Map<String, Integer> functional_letter_count = new HashMap<>();
            Map<String, String> functional_letter_to_desc = new HashMap<>();
            for (String cog_id : pattern_arr) {
                if (non_directons){
                    cog_id = cog_id.substring(0, cog_id.length()-1);
                }
                COG cog = utils.cog_info.get(cog_id);
                if (cog != null) {
                    String[] functional_letters = cog.getFunctional_letters();
                    String[] functional_categories = cog.getFunctional_categories();
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

            main_functional_category = main_categories;
        }
    }

    public String getMainFunctionalCategory() {
        return main_functional_category;
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

    public String[] getReversePatternArr() {
        return reverse_pattern_arr;
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

