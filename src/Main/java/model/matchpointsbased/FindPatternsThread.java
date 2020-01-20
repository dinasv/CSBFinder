package model.matchpointsbased;

import model.genomes.*;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

public abstract class FindPatternsThread implements Callable<Object> {

    protected GenomesInfo genomesInfo;
    protected int quorum;
    protected int maxPatternLength;
    protected int minPatternLength;
    protected int maxInsertion;

    /**
     * Only get operations
     */
    protected Map<Integer, Map<Integer, List<MatchPoint>>> matchLists;

    /**
     * Shared by all threads, the patterns are added dynamically
     */
    protected ConcurrentMap<String, Pattern> patterns;

    public FindPatternsThread(GenomesInfo genomesInfo, int quorum, int maxPatternLength,
                              int minPatternLength, int maxInsertion, ConcurrentMap<String, Pattern> patterns, Map<Integer,
            Map<Integer, List<MatchPoint>>> matchLists) {

        this.genomesInfo = genomesInfo;
        this.quorum = quorum;
        this.maxPatternLength = maxPatternLength;
        this.minPatternLength = minPatternLength;
        this.maxInsertion = maxInsertion;
        this.patterns = patterns;
        this.matchLists = matchLists;

    }

    abstract void extractPatterns();

    abstract void addPattern(Pattern pattern);


    protected void extractPattern(List<Gene> genes, int patternStart, WordArray wordArray) {
        List<Gene> patternGenes = genes.subList(patternStart, patternStart + 1);
        Pattern pattern = new Pattern("-1", patternGenes);

        int letter = wordArray.getLetter(patternStart);
        if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unknown char in a pattern
            return;
        }

        initializePattern(letter, pattern);

        //extend pattern to length > 1, one character at a time
        extendPattern(pattern, patternStart, wordArray, genes);
    }

    private void extendPattern(Pattern pattern, int patternStart, WordArray wordArray, List<Gene> genes) {

        int endIndex = Math.min(wordArray.getLength() - patternStart, maxPatternLength);
        for (int patternEnd = patternStart + 1; patternEnd < patternStart + endIndex; patternEnd++) {

            int letter = wordArray.getLetter(patternEnd);

            if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unknown char in a pattern
                break;
            }

            List<Gene> extendedPatternGenes = genes.subList(patternStart, patternEnd + 1);
            Pattern extendedPattern = new Pattern("-1", extendedPatternGenes);

            if (patterns.containsKey(extendedPattern.toString())) {
                pattern = patterns.get(extendedPattern.toString());
                continue;
            }

            extendPattern(letter, pattern, extendedPattern);
            addPattern(extendedPattern);

            pattern = extendedPattern;
            //pruning
            if (pattern.getInstancesPerGenomeCount() < quorum) {
                return;
            }
        }

    }

    private void initializePattern(int letter, Pattern pattern) {

        Map<Integer, List<MatchPoint>> matchList = matchLists.get(letter);
        List<MatchPoint> currGenomeMatchList;

        //initialize instanceLists, using matchLists
        if (matchList == null) {
            return;
        }
        for (Map.Entry<Integer, List<MatchPoint>> entry : matchList.entrySet()) {

            currGenomeMatchList = entry.getValue();

            if (currGenomeMatchList == null) {
                continue;
            }

            for (MatchPoint matchPoint : currGenomeMatchList) {
                GenomicSegment currGenomicSegment = matchPoint.getGenomicSegment();
                int pos = matchPoint.getPosition();

//                Replicon replicon = genomesInfo.getGenome(currGenomicSegment.getGenomeId())
//                        .getReplicon(currGenomicSegment.getRepliconId());
                if (pos < currGenomicSegment.size()) {
                    InstanceLocation instanceLocation = new InstanceLocation(currGenomicSegment.getRepliconId(),
                            currGenomicSegment.getGenomeId(), pos, 1,
                            currGenomicSegment.getStrand(), currGenomicSegment.getStartIndex(),
                            currGenomicSegment.size(), currGenomicSegment.getId());

                    pattern.addInstanceLocation(instanceLocation);
                }
            }
        }
    }

    private void extendPattern(int letter, Pattern pattern, Pattern extendedPattern) {

        Map<Integer, List<MatchPoint>> genomeRepliconToMatchList = matchLists.get(letter);
        if (genomeRepliconToMatchList == null) {
            return;
        }

        List<InstanceLocation> instancesToExtend = pattern.getPatternLocations().getInstanceLocations();
        Iterator<InstanceLocation> instanceIterator = instancesToExtend.iterator();

        InstanceLocation currInstance = null;
        if (instanceIterator.hasNext()) {
            currInstance = instanceIterator.next();
        }

        int genomeId;
        int repliconId;
        int genomicSegmentId;

        while (currInstance != null) {

            genomeId = currInstance.getGenomeId();
            repliconId = currInstance.getRepliconId();
            genomicSegmentId = currInstance.getGenomicSegmentId();

            List<MatchPoint> genomicSegmentMatchList = getMatchList(genomeId, repliconId, genomicSegmentId,
                    genomeRepliconToMatchList);

            //no match points on the current genomic segment, get the first instance in the next genomic segment
            if (genomicSegmentMatchList == null) {
                currInstance = getNextInstanceWithDiffValues(instanceIterator, genomeId, repliconId, genomicSegmentId);
            } else {
                Iterator<MatchPoint> matchPointIterator = genomicSegmentMatchList.iterator();
                currInstance = extendInstances(matchPointIterator, instanceIterator, extendedPattern, currInstance,
                        genomeId, repliconId, genomicSegmentId);
            }
        }

    }


    private InstanceLocation getNextInstanceWithDiffValues(Iterator<InstanceLocation> instanceIterator,
                                                           int genomeId, int repliconId, int genomicSegmentId) {
        InstanceLocation currInstance = null;
        while (instanceIterator.hasNext()) {
            currInstance = instanceIterator.next();
            if (atLeastOneValueChanged(currInstance, genomeId, repliconId, genomicSegmentId)) {
                break;
            }
            currInstance = null;
        }
        return currInstance;
    }

    /**
     * Goes over a list of instances and a list of match points in the same genomic segment in parallel.
     * Each match point extends at most one instance - the closest one to the match point
     * <p>
     * The list of match points and the list of instances must be ordered by their start index
     * The way these lists were constructed should keep them ordered
     *
     * @param genomicSegmentMatchListIt ordered match points in the current genomic segment
     * @param instanceIt                ordered instances from all genomic segments
     * @param extendedPattern
     */
    private InstanceLocation extendInstances(Iterator<MatchPoint> genomicSegmentMatchListIt,
                                             Iterator<InstanceLocation> instanceIt, Pattern extendedPattern,
                                             InstanceLocation currInstance,
                                             int genomeId, int repliconId, int genomicSegmentId) {

        //there are no match points for currentInstance, get next instance
        if (!genomicSegmentMatchListIt.hasNext()) {
            return instanceIt.hasNext() ? instanceIt.next() : null;
        }

        MatchPoint matchPoint = genomicSegmentMatchListIt.next();
        InstanceLocation nextInstance = null;
        int relativeMatchPointIndex;

        while (currInstance != null && matchPoint != null &&
                !atLeastOneValueChanged(currInstance, genomeId, repliconId, genomicSegmentId)) {

            relativeMatchPointIndex = matchPoint.getPosition();

            boolean hashCollisionCondition = matchPoint.getGenomicSegmentId() != currInstance.getGenomicSegmentId();
            boolean earlyMatchPointCondition = relativeMatchPointIndex < currInstance.getRelativeEndIndex();

            //the match point index is too small to extend curr instance
            if (hashCollisionCondition || earlyMatchPointCondition) {
                matchPoint = genomicSegmentMatchListIt.hasNext() ? genomicSegmentMatchListIt.next() : null;
                if (matchPoint == null) {
                    nextInstance = instanceIt.hasNext() ? instanceIt.next() : null;
                }
            } else {
                nextInstance = instanceIt.hasNext() ? instanceIt.next() : null;
                //The match point is closer to next instance
                if (nextInstance != null && areOnSameGenomicSegment(currInstance, nextInstance)
                        && relativeMatchPointIndex >= nextInstance.getRelativeEndIndex()) {
                    currInstance = nextInstance;
                } else {//The match point is >= currInstance.getRelativeEndIndex()
                    int instanceLength = relativeMatchPointIndex - currInstance.getRelativeStartIndex() + 1;
                    int numOfInsertions = instanceLength - extendedPattern.getLength();
                    int repliconSize = genomesInfo.getGenome(genomeId).getReplicon(repliconId).size();
                    if (numOfInsertions <= maxInsertion && instanceLength <= repliconSize) {
                        InstanceLocation extendedPatternInstance = new InstanceLocation(currInstance);
                        extendedPatternInstance.setInstanceLength(instanceLength);
                        extendedPattern.addInstanceLocation(extendedPatternInstance);

                        //each match point used only once
                        matchPoint = genomicSegmentMatchListIt.hasNext() ? genomicSegmentMatchListIt.next() : null;
                    }
                    currInstance = nextInstance;
                }
            }
        }

        return nextInstance;
    }

    private List<MatchPoint> getMatchList(int genomeId, int repliconId, int genomicSegmentId,
                                          Map<Integer, List<MatchPoint>> genomeRepliconToMatchList) {

        int hash = Objects.hash(genomeId, repliconId, genomicSegmentId);
        List<MatchPoint> genomicSegmentMatchList = genomeRepliconToMatchList.get(hash);

        if (genomicSegmentMatchList == null || genomicSegmentMatchList.size() == 0) {
            return null;
        } else {
            return genomicSegmentMatchList;
        }
    }

    private boolean areOnSameGenomicSegment(InstanceLocation instance1, InstanceLocation instance2) {
        return !atLeastOneValueChanged(instance1, instance2.getGenomeId(), instance2.getRepliconId(),
                instance2.getGenomicSegmentId());
    }

    private boolean atLeastOneValueChanged(InstanceLocation instance, int genomeId, int repliconId,
                                           int genomicSegmentId) {

        return instance.getGenomeId() != genomeId || instance.getRepliconId() != repliconId
                || instance.getGenomicSegmentId() != genomicSegmentId;
    }

    @Override
    public Object call() {
        try {
            extractPatterns();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
