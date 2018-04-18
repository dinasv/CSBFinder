package PostProcess;

import Main.Pattern;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by Dina on 23/08/2017.
 * Represents a family of patterns that share characters
 */
public class Family {

    private String family_id;
    //private int family_rank;
    //members of the family
    private ArrayList<Pattern> patterns;
    //contains the union of all characters of all family members
    private HashSet<Integer> char_set;
    /**
     * Score of the highest scoring member.
     * Updated after calling  {@link #sortPatternsAndSetScore() sortPatternsAndSetScore} method
     */
    private double score;
    private Utils utils;

    public Family(String family_id, Pattern first_pattern, Utils utils){
        this.utils = utils;
        score = -1;
        this.family_id = family_id;
        patterns = new ArrayList<>();
        patterns.add(first_pattern);
        char_set = new HashSet<>();
        addCharsToCharsSet(first_pattern);
    }

    private void addCharsToCharsSet(Pattern pattern){
        for (String cog: pattern.getPatternArr()) {
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

    public ArrayList<Pattern> getPatterns(){
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
