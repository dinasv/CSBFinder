package Core;

import Core.Patterns.Pattern;

import java.util.Comparator;

/**
 * Defines a sorting comparator in the clustering of patterns
 *
 * In the first step of the clustering, the patterns are sorted using a comparator.
 * The first cluster will consist from the first pattern
 */
public enum ClusterBy {
    SCORE(new Pattern.ScoreComparator()),
    LENGTH(new Pattern.LengthComparator());

    public final Comparator<Pattern> patternComparator;

    ClusterBy(Comparator<Pattern> comparator){
        patternComparator = comparator;
    }
}
