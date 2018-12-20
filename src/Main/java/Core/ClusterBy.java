package Core;

import Core.Patterns.Pattern;

import java.util.Comparator;

/**
 * Defines a clustering method for the patterns
 */
public enum ClusterBy {
    LENGTH(new Pattern.LengthComparator()),
    SCORE(new Pattern.ScoreComparator());

    public final Comparator<Pattern> patternComparator;

    ClusterBy(Comparator<Pattern> comparator){
        patternComparator = comparator;
    }
}
