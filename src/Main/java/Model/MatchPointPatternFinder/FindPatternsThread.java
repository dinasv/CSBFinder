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
    private Map<Integer, Map<Integer, List<MatchPoint>>> matchLists;

    /**
     * Shared by all threads, the patterns are added dynamically
     */
    private ConcurrentMap<String, Pattern> patterns;

    public FindPatternsThread(List<Gene> genes, GenomesInfo genomesInfo, int quorum, int maxPatternLength,
                              int minPatternLength, int maxInsertion, ConcurrentMap<String, Pattern> patterns, Map<Integer,
            Map<Integer, List<MatchPoint>>> matchLists) {

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

        Map<Integer, List<MatchPoint>> matchList = matchLists.get(letter);
        List<MatchPoint> currGenomeMatchList;

        //initialize instanceLists, using matchLists
        if (matchList != null) {
            for (Map.Entry<Integer, List<MatchPoint>> entry : matchList.entrySet()) {

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

        Map<Integer, List<MatchPoint>> genomeRepliconToMatchList = matchLists.get(letter);
        if (genomeRepliconToMatchList == null) {
            return;
        }

        List<InstanceLocation> instancesToExtend = pattern.getPatternLocations().getInstanceLocations();
        Iterator<InstanceLocation> instanceIterator = instancesToExtend.iterator();

        InstanceLocation currInstance = null;
        if(instanceIterator.hasNext()){
            currInstance = instanceIterator.next();
        }

        while (currInstance != null) {

            int genomeId = currInstance.getGenomeId();
            int repliconId = currInstance.getRepliconId();
            int genomicSegmentId = currInstance.getGenomicSegmentId();

            List<MatchPoint> genomicSegmentMatchList = getMatchList(genomeId, repliconId, genomicSegmentId,
                    genomeRepliconToMatchList);

            /*
            while (instanceIterator.hasNext() && genomicSegmentMatchList == null) {
                while (instanceIterator.hasNext()) {
                    currInstance = instanceIterator.next();
                    if (atLeastOneValueChanged(currInstance, genomeId, repliconId, genomicSegmentId)) {
                        genomeId = currInstance.getGenomeId();
                        repliconId = currInstance.getRepliconId();
                        genomicSegmentId = currInstance.getGenomicSegmentId();
                        break;
                    }
                }

                genomicSegmentMatchList = getMatchList(genomeId, repliconId, genomicSegmentId,
                        genomeRepliconToMatchList);
            }*/

            if (genomicSegmentMatchList == null) {
                currInstance = null;
                if (instanceIterator.hasNext()){
                    instanceIterator.next();
                }
                continue;
            }

            currInstance = extendInstances(genomicSegmentMatchList, instanceIterator, extendedPattern, currInstance,
                    genomeId, repliconId, genomicSegmentId);
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
     * The way these lists were constructed should keep them ordered
     *
     * @param genomicSegmentMatchList ordered match points in the current genomic segment
     * @param instanceIt ordered instances from all genomic segments
     * @param extendedPattern
     */
    private InstanceLocation extendInstances(List<MatchPoint> genomicSegmentMatchList,
                                             Iterator<InstanceLocation> instanceIt,
                                 Pattern extendedPattern, InstanceLocation currInstance, int genomeId, int repliconId,
                                 int genomicSegmentId) {

        Iterator<MatchPoint> matchPointIterator = genomicSegmentMatchList.iterator();
        MatchPoint matchPoint = matchPointIterator.next();
        InstanceLocation nextInstance = null;
        int relativeMatchPointIndex;

        while (currInstance != null && matchPoint != null &&
                !atLeastOneValueChanged(currInstance, genomeId, repliconId, genomicSegmentId)) {

            relativeMatchPointIndex = matchPoint.getPosition();

            assert matchPoint.getGenomicSegment().getId() == currInstance.getGenomicSegmentId();

            //the match point index is too small to extend curr instance
            if (relativeMatchPointIndex < currInstance.getRelativeEndIndex()) {
                matchPoint = matchPointIterator.hasNext() ? matchPointIterator.next() : null;
            }else{
                nextInstance = instanceIt.hasNext() ? instanceIt.next() : null;
                //The match point is closer to next instance
                if (nextInstance != null && areOnSameGenomicSegment(currInstance, nextInstance)
                        && relativeMatchPointIndex >= nextInstance.getRelativeEndIndex()) {
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


        currInstance = nextInstance;

        if (matchPoint == null && currInstance != null &&
                !atLeastOneValueChanged(currInstance, genomeId, repliconId, genomicSegmentId)){
            currInstance = null;
        }

        return currInstance;
    }

    private List<MatchPoint> getMatchList(int genomeId, int repliconId, int genomicSegmentId,
                                          Map<Integer, List<MatchPoint>> genomeRepliconToMatchList){

        int hash = Objects.hash(genomeId, repliconId, genomicSegmentId);
        List<MatchPoint> genomicSegmentMatchList = genomeRepliconToMatchList.get(hash);

        if (genomicSegmentMatchList == null || genomicSegmentMatchList.size() == 0){
            return null;
        }else{
            return genomicSegmentMatchList;
        }
    }

    private boolean areOnSameGenomicSegment(InstanceLocation instance1, InstanceLocation instance2){
        return !atLeastOneValueChanged(instance1, instance2.getGenomeId(), instance2.getRepliconId(),
                instance2.getGenomicSegmentId());
    }

    private boolean atLeastOneValueChanged(InstanceLocation instance, int genomeId, int repliconId,
                                           int genomicSegmentId){

        return instance.getGenomeId() != genomeId || instance.getRepliconId() != repliconId
                || instance.getGenomicSegmentId() != genomicSegmentId ;
    }

    @Override
    public Object call() {
        try {
            extractPatterns();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
