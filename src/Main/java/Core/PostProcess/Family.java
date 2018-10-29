package Core.PostProcess;

import Core.Genomes.Gene;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;

import java.util.*;

/**
 * Created by Dina on 23/08/2017.
 * Represents a family of patterns that share characters
 */
public class Family {

    private String familyId;
    //members of the family
    private List<Pattern> patterns;
    //contains the union of all characters of all family members
    private HashSet<Integer> charSet;
    /**
     * Score of the highest scoring member.
     * Updated after calling  {@link #sortPatternsAndSetScore() sortPatternsAndSetScore} method
     */
    private double score;
    private GenomesInfo gi;
    private boolean nonDirectons;

    public Family(String familyId, Pattern first_pattern, GenomesInfo gi, boolean nonDirectons){
        this.gi = gi;
        score = -1;
        this.familyId = familyId;
        patterns = new ArrayList<>();
        patterns.add(first_pattern);
        charSet = new HashSet<>();
        this.nonDirectons = nonDirectons;
        addCharsToCharsSet(first_pattern);
    }

    public Family(Family family) {
        patterns = new ArrayList<>(family.getPatterns());
        score = family.score;
        gi = family.gi;
        familyId = family.familyId;
    }

    private void addCharsToCharsSet(Pattern pattern){
        addCharsToCharsSet(pattern.getPatternGenes());
        if (nonDirectons){
            addCharsToCharsSet(pattern.getReverseComplimentPattern());
        }
    }

    private void addCharsToCharsSet(List<Gene> patternGenes){
        for (Gene gene: patternGenes) {
            int cog_index;
            /*
            if (nonDirectons){
                cog_index = gi.charToIndex.get(gene.getCogId()+gene.getStrand());
            }else {
                cog_index = gi.charToIndex.get(gene.getCogId());
            }*/
            cog_index = gi.charToIndex.get(gene);
            charSet.add(cog_index);
        }
    }

    public HashSet<Integer> getGeneSet(){
        return charSet;
    }

    public void addPattern(Pattern pattern){
        addCharsToCharsSet(pattern);
        patterns.add(pattern);
    }

    public String getFamilyId(){
        return familyId;
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

    public void setPatterns(List<Pattern> plist) {
        this.patterns = plist;
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
