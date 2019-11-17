package Model.MatchPointPatternFinder;

import Model.Genomes.*;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 */
public class FindPatternFromFileThread extends FindPatternsThread {

    private Pattern pattern;

    public FindPatternFromFileThread(Pattern pattern, GenomesInfo genomesInfo, int quorum, int maxInsertion,
                                     ConcurrentMap<String, Pattern> patterns,
                                     Map<Integer, Map<Integer, List<MatchPoint>>> matchLists) {

        super(genomesInfo, quorum, pattern.getLength(), pattern.getLength(), maxInsertion, patterns, matchLists);
        this.pattern = pattern;

    }

    protected void extractPatterns() {

        List<Gene> genes = Arrays.asList(pattern.getPatternGenes());
        WordArray wordArray = genomesInfo.createWordArray(genes);

        int patternStart = 0;
        extractPattern(genes, patternStart, wordArray);

    }

    protected void addPattern(Pattern pattern){
        if (pattern.getInstancesPerGenomeCount() >= quorum
                && pattern.getLength() >= minPatternLength && pattern.toString().equals(this.pattern.toString())) {

            pattern.setPatternId(this.pattern.getPatternId());
            patterns.put(pattern.toString(), pattern);

        }
    }
}
