package Model.PostProcess;

import Model.Genomes.Gene;
import Model.Genomes.GenomesInfo;
import Model.Patterns.Pattern;
import Model.Genomes.Strand;

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

    private int longestPattern;
    /**
     * Score of the highest scoring member.
     * Updated after calling  {@link #sortPatternsAndSetScore() sortPatternsAndSetScore} method
     */
    private double score;
    private GenomesInfo genomesInfo;

    public Family(int familyId, Pattern firstPattern, GenomesInfo genomesInfo){
        charSet = new HashSet<>();

        this.genomesInfo = genomesInfo;
        this.score = -1;
        this.familyId = familyId;

        patterns = new ArrayList<>();
        patterns.add(firstPattern);

        longestPattern = firstPattern.getLength();

        addCharsToCharsSet(firstPattern);
    }

    public Family(int familyId, GenomesInfo genomesInfo, List<Pattern> patterns) {
        this.patterns = new ArrayList<>(patterns);

        this.genomesInfo = genomesInfo;
        this.familyId = familyId;

        setLongestPattern();

        sortPatternsAndSetScore();
    }

    public Family(Family family, List<Pattern> patterns) {
        this.patterns = new ArrayList<>(patterns);

        genomesInfo = family.genomesInfo;
        familyId = family.familyId;

        setLongestPattern();
        sortPatternsAndSetScore();
    }

    public Family(Family family) {

        this.patterns = new ArrayList<>(family.getPatterns());
        genomesInfo = family.genomesInfo;
        familyId = family.familyId;
        longestPattern = family.longestPattern;

    }


    private void addCharsToCharsSet(Pattern pattern){
        for (Gene gene: pattern.getPatternGenes()) {
            int cogIndex = genomesInfo.getLetter(new Gene(gene.getCogId().intern(), Strand.INVALID));

            if (cogIndex != -1){
                charSet.add(cogIndex);
            }
        }
    }

    private void setLongestPattern(){
        longestPattern = this.patterns.stream()
                .map(Pattern::getLength)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public HashSet<Integer> getGeneSet(){
        return charSet;
    }

    public void addPattern(Pattern pattern){
        addCharsToCharsSet(pattern);
        patterns.add(pattern);

        if (pattern.getLength() > longestPattern){
            longestPattern = pattern.getLength();
        }
    }

    public int getFamilyId(){
        return familyId;
    }

    public int getLongestPattern(){
        return longestPattern;
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
