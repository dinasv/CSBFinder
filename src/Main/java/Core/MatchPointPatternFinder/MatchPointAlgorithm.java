package Core.MatchPointPatternFinder;

import Core.Algorithm;
import Core.Genomes.*;
import Core.Parameters;
import Core.Patterns.InstanceLocation;
import Core.Patterns.Pattern;
import Core.Patterns.PatternLocationsInGenome;

import java.util.*;

/**
 */
public class MatchPointAlgorithm implements Algorithm {

    private Map<Integer, Map<Integer, List<MatchPoint>>> matchLists;
    private List<GenomicSegment> genomicSegments;
    private GenomesInfo genomesInfo;
    private Parameters parameters;
    private Map<String, Pattern> patterns;

    private int patternId;

    private List<Pattern> patternsFromFile;

    public MatchPointAlgorithm() {
        matchLists = new HashMap<>();
        genomesInfo = null;
        genomicSegments = new ArrayList<>();
        patterns = new HashMap<>();
        patternsFromFile = new ArrayList<>();

        patternId = 1;
    }

    private void createMatchLists(boolean nonDirectons) {
        if (genomesInfo == null) {
            return;
        }

        for (Genome genome : genomesInfo.getGenomes()) {

            int genomeId = genome.getId();
            for (Replicon replicon : genome.getReplicons()) {
                if (nonDirectons) {//put replicon and its reverseCompliment

                    createMatchLists(replicon, genome.getId(), nonDirectons);

                    //reverseCompliment replicon
                    replicon = new Replicon(replicon);
                    replicon.reverseCompliment();
                    createMatchLists(replicon, genome.getId(), nonDirectons);

                } else {//split replicon to directons

                    List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

                    for (Directon directon : directons) {
                        createMatchLists(directon, genome.getId(), nonDirectons);
                    }

                }
            }
        }
    }

    private void createMatchLists(GenomicSegment genomicSegment, int currGenomeId, boolean nonDirectons) {

        genomicSegments.add(genomicSegment);

        List<Gene> genes = genomicSegment.getGenes();
        WordArray cogWord = genomesInfo.createWordArray(genes);
        genomesInfo.countParalogsInSeqs(cogWord, currGenomeId);

        for (int i = 0; i < cogWord.getLength(); i++) {
            int currLetter = cogWord.getLetter(i);
            if (currLetter != Alphabet.UNK_CHAR_INDEX) {
                Map<Integer, List<MatchPoint>> genomeIdToCogPositions = matchLists.get(currLetter);
                if (genomeIdToCogPositions == null) {
                    genomeIdToCogPositions = new HashMap<>();
                    matchLists.put(currLetter, genomeIdToCogPositions);
                }

                List<MatchPoint> genePositions = genomeIdToCogPositions.get(currGenomeId);

                if (genePositions == null) {
                    genePositions = new ArrayList<>();
                    genomeIdToCogPositions.put(currGenomeId, genePositions);
                }

                MatchPoint matchPoint = new MatchPoint(genomicSegment, i);
                genePositions.add(matchPoint);
            }
        }
    }

    @Override
    public void setParameters(Parameters params) {
        parameters = params;
    }

    @Override
    public void setGenomesInfo(GenomesInfo gi) {
        genomesInfo = gi;
        matchLists = new HashMap<>();
    }

    @Override
    public void setPatternsFromFile(List<Pattern> patternsFromFile) {
        this.patternsFromFile = patternsFromFile;
    }

    private void initialize() {

        if (matchLists.size() == 0) {
            createMatchLists(parameters.nonDirectons);
        }

        patterns = new HashMap<>();
        patternId = 1;
    }

    @Override
    public void findPatterns() {
        if (genomesInfo == null || parameters == null) {
            return;
        }

        initialize();

        for (GenomicSegment genomicSegment : genomicSegments) {

            List<Gene> genes = genomicSegment.getGenes();
            WordArray wordArray = genomesInfo.createWordArray(genes);

            //go over all possible start indices of a pattern
            for (int patternStart = 0; patternStart < wordArray.getLength(); patternStart++) {

                List<Gene> patternGenes = genomicSegment.getGenes().subList(patternStart, patternStart + 1);
                Pattern pattern = new Pattern(patternId++, patternGenes);

                int letter = wordArray.getLetter(patternStart);
                if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unkonwn char in a pattern
                    continue;
                }

                initializePattern(letter, pattern);

                //extend pattern to length > 1, one character at a time
                extendPattern(pattern, patternStart, wordArray, genomicSegment);
            }
        }

        removeRedundantPatterns();
    }

    private void extendPattern(Pattern pattern, int patternStart, WordArray wordArray, GenomicSegment genomicSegment) {

        for (int patternEnd = patternStart + 1; patternEnd < wordArray.getLength(); patternEnd++) {

            int letter = wordArray.getLetter(patternEnd);

            if (letter == Alphabet.UNK_CHAR_INDEX) {//There can't be an unkonwn char in a pattern
                break;
            }

            List<Gene> extendedPatternGenes = genomicSegment.getGenes().subList(patternStart, patternEnd + 1);
            Pattern extendedPattern = new Pattern(patternId++, extendedPatternGenes);

            if (patterns.containsKey(extendedPattern.toString())) {
                pattern = patterns.get(extendedPattern.toString());
                continue;
            }

            extendPattern(letter, pattern, extendedPattern);

            pattern = extendedPattern;
            //pruning
            if (pattern.getInstancesPerGenome() < parameters.quorum2) {
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
                int genomeId = entry.getKey();

                currGenomeMatchList = matchList.get(genomeId);

                if (currGenomeMatchList != null) {

                    for (MatchPoint matchPoint : currGenomeMatchList) {
                        GenomicSegment currGenomicSegment = matchPoint.getGenomicSegment();
                        int pos = matchPoint.getPosition();

                        InstanceLocation instanceLocation = new InstanceLocation(currGenomicSegment.getId(),
                                currGenomicSegment.getGenomeId(), pos, 1,
                                currGenomicSegment.getStrand(), currGenomicSegment.getStartIndex(),
                                currGenomicSegment.size());

                        pattern.addInstanceLocation(instanceLocation);
                    }
                }
            }
        }
    }

    private void extendPattern(int letter, Pattern pattern, Pattern extendedPattern) {

        String extendedPatternStr = extendedPattern.toString();

        Map<Integer, List<MatchPoint>> matchList = matchLists.get(letter);

        if (matchList != null) {
            for (Map.Entry<Integer, PatternLocationsInGenome> genomeToInstanceLocations : pattern.getPatternLocations().entrySet()) {

                int genomeId = genomeToInstanceLocations.getKey();
                List<MatchPoint> matchList_y = matchList.get(genomeId);

                PatternLocationsInGenome instanceLocationsInGenome = genomeToInstanceLocations.getValue();
                if (instanceLocationsInGenome != null) {
                    for (Map.Entry<Integer, List<InstanceLocation>> entry : instanceLocationsInGenome.getSortedLocations().entrySet()) {
                        extendInstances(entry.getValue(), matchList_y, extendedPattern);
                    }
                }
            }
        }
        if (extendedPattern.getInstancesPerGenome() >= parameters.quorum2 && extendedPattern.getLength() >= parameters.minPatternLength) {
            patterns.put(extendedPatternStr, extendedPattern);
        }
    }

    private void extendInstances(List<InstanceLocation> instanceList, List<MatchPoint> matchList_y,
                                 Pattern extendedPattern) {

        if (matchList_y != null) {
            InstanceLocation currInstance;
            InstanceLocation nextInstance;
            MatchPoint matchPoint;

            int instancePtr = 0;
            int matchPointPtr = 0;
            int relativeMatchPointIndex;

            while (instancePtr < instanceList.size() && matchPointPtr < matchList_y.size()) {
                currInstance = instanceList.get(instancePtr);
                if (instancePtr < instanceList.size() - 1) {
                    nextInstance = instanceList.get(instancePtr + 1);
                } else {
                    nextInstance = null;
                }
                matchPoint = matchList_y.get(matchPointPtr);
                relativeMatchPointIndex = matchPoint.getPosition();

                //match point is in an earlier word in this sequence
                if (matchPoint.getGenomicSegment().getId() < currInstance.getRepliconId()) {
                    matchPointPtr++;
                    //The match point is in a later word in this sequence
                } else if (matchPoint.getGenomicSegment().getId() > currInstance.getRepliconId()) {
                    instancePtr++;
                } else {//matchPoint.getWordId() == currInstance.getWordId()
                    //the match point index is too small to extend curr instance
                    if (relativeMatchPointIndex < currInstance.getRelativeStartIndex()) {
                        matchPointPtr++;
                        //The match point is closer to next instance
                    } else if (nextInstance != null &&
                            matchPoint.getGenomicSegment().getId() == nextInstance.getRepliconId() &&
                            relativeMatchPointIndex >= nextInstance.getRelativeEndIndex()) {
                        instancePtr++;
                    } else {//The match point is > currInstance.getEnd()
                        int instanceLength = relativeMatchPointIndex - currInstance.getRelativeStartIndex() + 1;
                        int numOfInsertions = instanceLength - extendedPattern.getLength();
                        if (numOfInsertions <= parameters.maxInsertion) {
                            InstanceLocation extendedPatternInstance = new InstanceLocation(currInstance);
                            extendedPatternInstance.setInstanceLength(instanceLength);
                            extendedPattern.addInstanceLocation(extendedPatternInstance);

                            matchPointPtr++;//each match point used only once
                        }
                        instancePtr++;
                    }
                }
            }
        }
    }

    private void removeRedundantPatterns() {

        HashSet<String> patternsToRemove = new HashSet<>();
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {

            Pattern pattern = entry.getValue();

            String suffixStr = getSuffix(pattern);
            addSubPatternToRemoveList(suffixStr, pattern, patternsToRemove);

            String prefixStr = getPrefix(pattern);
            addSubPatternToRemoveList(prefixStr, pattern, patternsToRemove);

            if (parameters.nonDirectons) {
                removeReverseCompliments(pattern, patternsToRemove);
            }
        }
        patterns.keySet().removeAll(patternsToRemove);
    }

    private String getSuffix(Pattern pattern){

        List<Gene> suffix = new ArrayList<>(pattern.getPatternGenes());
        suffix.remove(0);

        return Pattern.toString(suffix);
    }

    private String getPrefix(Pattern pattern){

        List<Gene> prefix = new ArrayList<>(pattern.getPatternGenes());
        prefix.remove(prefix.size()-1);

        return Pattern.toString(prefix);
    }

    private void addSubPatternToRemoveList(String subPatternStr, Pattern pattern, HashSet<String> patternsToRemove){
        Pattern subPattern = patterns.get(subPatternStr);

        if (subPattern != null) {
            int patternCount = pattern.getInstancesPerGenome();
            int suffixCount = subPattern.getInstancesPerGenome();
            if (suffixCount == patternCount) {
                patternsToRemove.add(subPatternStr);
            }
        }
    }

    private void removeReverseCompliments(Pattern pattern, HashSet<String> patternsToRemove) {

        List<Gene> reversePatternGenes = pattern.getReverseComplimentPattern();
        String reversedPatternStr = Pattern.toString(reversePatternGenes);
        Pattern reversedPattern = patterns.get(reversedPatternStr);

        String patternStr = pattern.toString();
        if (reversedPattern != null && !patternsToRemove.contains(patternStr)) {
            patternsToRemove.add(reversedPatternStr);
        }
    }


    @Override
    public List<Pattern> getPatterns() {
        return new ArrayList<>(patterns.values());
    }

    @Override
    public int getPatternsCount() {
        return patterns.size();
    }

    @Override
    public Parameters getParameters() {
        return parameters;
    }

    private class MatchPoint {

        private final GenomicSegment genomicSegment;
        private final int position;

        public MatchPoint(GenomicSegment genomicSegment, int position) {
            this.genomicSegment = genomicSegment;
            this.position = position;
        }

        public GenomicSegment getGenomicSegment() {
            return genomicSegment;
        }

        public int getPosition() {
            return position;
        }
    }
}
