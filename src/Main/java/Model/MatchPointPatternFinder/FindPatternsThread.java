package Model.MatchPointPatternFinder;

import Model.Genomes.*;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import Model.Patterns.PatternLocationsInGenome;
import Model.Patterns.PatternLocationsInReplicon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 */
public class FindPatternsThread implements Callable<Object> {

    private List<Gene> genes;
    private GenomesInfo genomesInfo;
    private int quorum;
    private int maxPatternLength;
    private int minPatternLength;
    private int maxInsertion;

    /**
     * Only get operations
     */
    private Map<Integer, Map<String, List<MatchPoint>>> matchLists;

    /**
     * Shared by all threads, the patterns are added dynamically
     */
    private ConcurrentMap<String, Pattern> patterns;

    public FindPatternsThread(List<Gene> genes, GenomesInfo genomesInfo, int quorum, int maxPatternLength,
                              int minPatternLength, int maxInsertion, ConcurrentMap<String, Pattern> patterns, Map<Integer,
            Map<String, List<MatchPoint>>> matchLists) {

        this.genes = genes;
        this.genomesInfo = genomesInfo;
        this.quorum = quorum;
        this.maxPatternLength = maxPatternLength;
        this.minPatternLength = minPatternLength;
        this.maxInsertion = maxInsertion;
        this.patterns = patterns;
        this.matchLists = matchLists;
    }

    private void extractPatterns() {
        WordArray wordArray = genomesInfo.createWordArray(genes);

        //go over all possible start indices of a pattern
        for (int patternStart = 0; patternStart < wordArray.getLength(); patternStart++) {

            List<Gene> patternGenes = genes.subList(patternStart, patternStart + 1);
            Pattern pattern = new Pattern("-1", patternGenes);

            int letter = wordArray.getLetter(patternStart);
            if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unkonwn char in a pattern
                continue;
            }

            initializePattern(letter, pattern);

            //extend pattern to length > 1, one character at a time
            extendPattern(pattern, patternStart, wordArray, genes);
        }
    }

    private void extendPattern(Pattern pattern, int patternStart, WordArray wordArray, List<Gene> genes) {

        int endIndex = Math.min(wordArray.getLength() - patternStart, maxPatternLength);
        for (int patternEnd = patternStart + 1; patternEnd < patternStart + endIndex; patternEnd++) {

            int letter = wordArray.getLetter(patternEnd);

            if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unkonwn char in a pattern
                break;
            }

            List<Gene> extendedPatternGenes = genes.subList(patternStart, patternEnd + 1);
            Pattern extendedPattern = new Pattern("-1", extendedPatternGenes);

            if (patterns.containsKey(extendedPattern.toString())) {
                pattern = patterns.get(extendedPattern.toString());
                continue;
            }

            extendPattern(letter, pattern, extendedPattern);

            pattern = extendedPattern;
            //pruning
            if (pattern.getInstancesPerGenome() < quorum) {
                return;
            }
        }

    }

    private void initializePattern(int letter, Pattern pattern) {

        Map<String, List<MatchPoint>> matchList = matchLists.get(letter);
        List<MatchPoint> currGenomeMatchList;

        //initialize instanceLists, using matchLists
        if (matchList != null) {
            for (Map.Entry<String, List<MatchPoint>> entry : matchList.entrySet()) {
                //int genomeId = entry.getKey();

                currGenomeMatchList = entry.getValue();

                if (currGenomeMatchList != null) {

                    for (MatchPoint matchPoint : currGenomeMatchList) {
                        GenomicSegment currGenomicSegment = matchPoint.getGenomicSegment();
                        int pos = matchPoint.getPosition();

                        InstanceLocation instanceLocation = new InstanceLocation(currGenomicSegment.getRepliconId(),
                                currGenomicSegment.getGenomeId(), pos, 1,
                                currGenomicSegment.getStrand(), currGenomicSegment.getStartIndex(),
                                currGenomicSegment.size(), currGenomicSegment.getId());

                        pattern.addInstanceLocation(instanceLocation);
                    }
                }
            }
        }
    }

    private void extendPattern(int letter, Pattern pattern, Pattern extendedPattern) {

        String extendedPatternStr = extendedPattern.toString();

        Map<String, List<MatchPoint>> genomeRepliconToMatchList = matchLists.get(letter);
        if (genomeRepliconToMatchList == null) {
            return;
        }

        for (Map.Entry<Integer, PatternLocationsInGenome> genomeToInstanceLocations :
                pattern.getPatternLocations().entrySet()) {


            PatternLocationsInGenome instanceLocationsInGenome = genomeToInstanceLocations.getValue();

            if (instanceLocationsInGenome != null) {
                for (Map.Entry<Integer, PatternLocationsInReplicon> repliconToLocations :
                        instanceLocationsInGenome.getRepliconToLocations().entrySet()) {

                    List<InstanceLocation> locations = repliconToLocations.getValue().getInstanceLocations();
                    Map<Integer, List<InstanceLocation>> genomicSegmentToLocations = locations.stream()
                            .collect(Collectors.groupingBy(InstanceLocation::getGenomicSegmentId));

                    for (Map.Entry<Integer, List<InstanceLocation>> entry : genomicSegmentToLocations.entrySet()) {

                        extendInstances(genomeRepliconToMatchList, entry.getValue(), extendedPattern);
                    }
                }
            }
        }

        if (extendedPattern.getInstancesPerGenome() >= quorum
                && extendedPattern.getLength() >= minPatternLength) {

            patterns.put(extendedPatternStr, extendedPattern);

        }
    }

    /**
     * Goes over a list of instances and a list of match points in the same genomic segment in parallel.
     * Each match point extends at most one instance - the closest one to the match point
     *
     * The list of match points and the list of instances must be ordered by their start index
     *
     * @param genomeRepliconToMatchList
     * @param instanceList
     * @param extendedPattern
     */
    private void extendInstances(Map<String, List<MatchPoint>> genomeRepliconToMatchList,
                                 List<InstanceLocation> instanceList, Pattern extendedPattern) {

        if (instanceList.size() == 0){
            return;
        }

        InstanceLocation firstInstance = instanceList.get(0);
        String hashString = MatchPointAlgorithm.genomeRepliconToHashString(firstInstance.getGenomeId(),
                        firstInstance.getRepliconId(),
                        firstInstance.getGenomicSegmentId());
        List<MatchPoint> genomicSegmentMatchList = genomeRepliconToMatchList.get(hashString);

        if (genomicSegmentMatchList == null || genomicSegmentMatchList.size() == 0) {
            return;
        }

        Iterator<InstanceLocation> instanceIterator = instanceList.iterator();
        Iterator<MatchPoint> matchPointIterator = genomicSegmentMatchList.iterator();

        InstanceLocation currInstance = instanceIterator.next();
        InstanceLocation nextInstance;
        MatchPoint matchPoint = matchPointIterator.next();
        int relativeMatchPointIndex;

        while (currInstance != null && matchPoint != null) {

            relativeMatchPointIndex = matchPoint.getPosition();

            assert matchPoint.getGenomicSegment().getId() == currInstance.getGenomicSegmentId();

            //the match point index is too small to extend curr instance
            if (relativeMatchPointIndex < currInstance.getRelativeEndIndex()) {
                matchPoint = matchPointIterator.hasNext() ? matchPointIterator.next() : null;
            }else{
                nextInstance = instanceIterator.hasNext() ? instanceIterator.next() : null;
                //The match point is closer to next instance
                if (nextInstance != null && relativeMatchPointIndex >= nextInstance.getRelativeEndIndex()) {
                    currInstance = nextInstance;
                } else {//The match point is >= currInstance.getRelativeEndIndex()
                    int instanceLength = relativeMatchPointIndex - currInstance.getRelativeStartIndex() + 1;
                    int numOfInsertions = instanceLength - extendedPattern.getLength();
                    if (numOfInsertions <= maxInsertion) {
                        InstanceLocation extendedPatternInstance = new InstanceLocation(currInstance);
                        extendedPatternInstance.setInstanceLength(instanceLength);
                        extendedPattern.addInstanceLocation(extendedPatternInstance);

                        matchPoint = matchPointIterator.hasNext() ? matchPointIterator.next() : null;//each match point used only once
                    }
                    currInstance = nextInstance;
                }
            }
        }
    }

    @Override
    public Object call() throws Exception {
        try {
            extractPatterns();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
