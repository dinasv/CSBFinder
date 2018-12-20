package Core.PostProcess;

import Core.Genomes.Gene;
import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;
import Core.Genomes.Strand;

import java.util.*;

/**
 * Represents a family of patterns that share characters
 */
public class Family {

    private int familyId;
    //members of the family
    private List<Pattern> patterns;
    //contains the union of all characters of all family members
    private HashSet<Integer> charSet;
    /**
     * Score of the highest scoring member.
     * Updated after calling  {@link #sortPatternsAndSetScore() sortPatternsAndSetScore} method
     */
    private double score;
    private GenomesInfo genomesInfo;

    public Family(int familyId, Pattern firstPattern, GenomesInfo genomesInfo){
        charSet = new HashSet<>();

        this.genomesInfo = genomesInfo;
        score = -1;
        this.familyId = familyId;
        patterns = new ArrayList<>();
        patterns.add(firstPattern);
        addCharsToCharsSet(firstPattern);
    }

    public Family(Family family) {
        patterns = new ArrayList<>(family.getPatterns());

        score = family.score;
        genomesInfo = family.genomesInfo;
        familyId = family.familyId;
    }

    private void addCharsToCharsSet(Pattern pattern){

        List<Gene> patternGenes = pattern.getPatternGenes();
        addCharsToCharsSet(pattern.getPatternGenes());
        if (patternGenes != null && patternGenes.size()>0 && patternGenes.get(0) != null) {
            if (patternGenes.get(0).getStrand() != Strand.INVALID) {
                addCharsToCharsSet(pattern.getReverseComplimentPattern());
            }
        }
    }

    private void addCharsToCharsSet(List<Gene> patternGenes){
        for (Gene gene: patternGenes) {
            int cogIndex;

            if (genomesInfo.getLetter(gene) != -1){
                cogIndex = genomesInfo.getLetter(gene);
                charSet.add(cogIndex);
            }
        }
    }

    public HashSet<Integer> getGeneSet(){
        return charSet;
    }

    public void addPattern(Pattern pattern){
        addCharsToCharsSet(pattern);
        patterns.add(pattern);
    }

    public int getFamilyId(){
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
        score = getTopScoringPattern().getScore();
    }

    public List<Pattern> getPatterns(){
        return patterns;
    }

    public int size(){
        return patterns.size();
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

    public Pattern getTopScoringPattern(){
        if (patterns.size() > 0){
            return patterns.get(0);
        }
        return null;
    }
}
