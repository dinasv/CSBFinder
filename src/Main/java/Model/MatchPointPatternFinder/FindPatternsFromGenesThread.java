package Model.MatchPointPatternFinder;

import Model.Genomes.*;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 */
public class FindPatternsFromGenesThread extends FindPatternsThread {

    private List<Gene> genes;

    public FindPatternsFromGenesThread(List<Gene> genes, GenomesInfo genomesInfo, int quorum, int maxPatternLength,
                                       int minPatternLength, int maxInsertion, ConcurrentMap<String, Pattern> patterns, Map<Integer,
            Map<Integer, List<MatchPoint>>> matchLists) {

        super(genomesInfo, quorum, maxPatternLength, minPatternLength, maxInsertion, patterns, matchLists);
        this.genes = genes;

    }

    protected void extractPatterns() {

        WordArray wordArray = genomesInfo.createWordArray(genes);

        //go over all possible start indices of a pattern
        for (int patternStart = 0; patternStart < wordArray.getLength(); patternStart++) {
            extractPattern(genes, patternStart, wordArray);

        }
    }

    protected void addPattern(Pattern pattern){
        if (pattern.getInstancesPerGenomeCount() >= quorum
                && pattern.getLength() >= minPatternLength) {

            patterns.put(pattern.toString(), pattern);

        }
    }
}
