package MVC.View.Requests;

import MVC.View.Models.Filters.PatternStrand;

/**
 */
public class FilterRequest implements Request{

    private int minCSBLength = 2;
    private int maxCSBLength = Integer.MAX_VALUE;
    private int minScore = 0;
    private int maxScore = Integer.MAX_VALUE;
    private int minInstanceCount = 1;
    private int maxInstanceCount = Integer.MAX_VALUE;
    private PatternStrand patternStrand = PatternStrand.ALL;

    public void setMinCSBLength(int minCSBLength) {
        this.minCSBLength = minCSBLength;
    }

    public void setMaxCSBLength(int maxCSBLength) {
        this.maxCSBLength = maxCSBLength;
    }

    public int getMinCSBLength() {
        return minCSBLength;
    }

    public int getMaxCSBLength() {
        return maxCSBLength;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getMinInstanceCount() {
        return minInstanceCount;
    }

    public void setMinInstanceCount(int minInstanceCount) {
        this.minInstanceCount = minInstanceCount;
    }

    public int getMaxInstanceCount() {
        return maxInstanceCount;
    }

    public void setMaxInstanceCount(int maxInstanceCount) {
        this.maxInstanceCount = maxInstanceCount;
    }

    public PatternStrand getPatternStrand() {
        return patternStrand;
    }

    public void setPatternStrand(PatternStrand patternStrand) {
        this.patternStrand = patternStrand;
    }
}
