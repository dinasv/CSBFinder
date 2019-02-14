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

    public Family(int familyId, GenomesInfo genomesInfo, List<Pattern> patterns) {
        this.patterns = new ArrayList<>(patterns);

        this.genomesInfo = genomesInfo;
        this.familyId = familyId;

        sortPatternsAndSetScore();
    }

    public Family(Family family, List<Pattern> patterns) {
        this.patterns = new ArrayList<>(patterns);

        genomesInfo = family.genomesInfo;
        familyId = family.familyId;

        sortPatternsAndSetScore();
    }

    private void addCharsToCharsSet(Pattern pattern){
        for (Gene gene: pattern.getPatternGenes()) {
            int cogIndex = genomesInfo.getLetter(new Gene(gene.getCogId(), Strand.INVALID));

            if (cogIndex != -1){
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Family))
            return false;
        if (obj == this)
            return true;

        Family other = (Family) obj;
        return other.patterns.equals(this.patterns);
    }
}
