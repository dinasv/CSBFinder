package PostProcess;

import Utils.Pattern;
import Utils.Utils;

import java.util.*;

/**
 * Created by Dina on 23/08/2017.
 * Represents a family of patterns that share characters
 */
public class Family {

    private String family_id;
    //members of the family
    private List<Pattern> patterns;
    //contains the union of all characters of all family members
    private HashSet<Integer> char_set;
    /**
     * Score of the highest scoring member.
     * Updated after calling  {@link #sortPatternsAndSetScore() sortPatternsAndSetScore} method
     */
    private double score;
    private Utils utils;
    private boolean non_directons;

    public Family(String family_id, Pattern first_pattern, Utils utils, boolean non_directons){
        this.utils = utils;
        score = -1;
        this.family_id = family_id;
        patterns = new ArrayList<>();
        patterns.add(first_pattern);
        char_set = new HashSet<>();
        this.non_directons = non_directons;
        addCharsToCharsSet(first_pattern);
    }

    private void addCharsToCharsSet(Pattern pattern){
        addCharsToCharsSet(pattern.getPatternArr());
        if (non_directons){
            addCharsToCharsSet(pattern.getReversePatternArr());
        }
    }

    private void addCharsToCharsSet(String[] pattern_arr){
        for (String cog: pattern_arr) {
            int cog_index = utils.char_to_index.get(cog);
            char_set.add(cog_index);
        }
    }

    public HashSet<Integer> getGeneSet(){
        return char_set;
    }

    public void addPattern(Pattern pattern){
        addCharsToCharsSet(pattern);
        patterns.add(pattern);
    }

    public String getFamilyId(){
        return family_id;
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void sortPatternsAndSetScore(){
        Collections.sort(patterns, new Pattern.ScoreComparator());
        score = patterns.get(0).getScore();
    }

    public List<Pattern> getPatterns(){
        return patterns;
    }

    public static class ScoreComparator implements Comparator<Family> {
        @Override
        public int compare(Family o1, Family o2) {
            if (o2.getScore() < o1.getScore()) return -1;
            if (o2.getScore() > o1.getScore()) return 1;
            return 0;
        }
    }
}
