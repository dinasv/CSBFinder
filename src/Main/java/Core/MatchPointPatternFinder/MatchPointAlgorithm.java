package Core.MatchPointPatternFinder;

import Core.Algorithm;
import Core.Genomes.*;
import Core.Parameters;
import Core.Patterns.Pattern;

import java.util.*;
import java.util.concurrent.*;

/**
 */
public class MatchPointAlgorithm implements Algorithm {

    /**
     * Alphabet letter to matches in each genomic segment
     */
    private Map<Integer, Map<String, List<MatchPoint>>> matchLists;
    private List<GenomicSegment> genomicSegments;
    private GenomesInfo genomesInfo;
    private Parameters parameters;

    private ConcurrentMap<String, Pattern> patterns;

    private List<Pattern> patternsFromFile;

    private ExecutorService executor;

    public MatchPointAlgorithm() {
        matchLists = new HashMap<>();
        genomesInfo = null;
        genomicSegments = new ArrayList<>();
        patterns = new ConcurrentHashMap<>();
        patternsFromFile = new ArrayList<>();

        executor = Executors.newFixedThreadPool(1);
    }

    public void setNumOfThreads(int numOfThreads){
        executor = Executors.newFixedThreadPool(numOfThreads);
    }

    private void createMatchLists(boolean nonDirectons) {

        if (genomesInfo == null) {
            return;
        }

        for (Genome genome : genomesInfo.getGenomes()) {

            for (Replicon replicon : genome.getReplicons()) {
                if (nonDirectons) {//putWithSuffix replicon and its reverseCompliment

                    Replicon reversedReplicon = new Replicon(replicon);
                    reversedReplicon.reverseCompliment();

                    createMatchLists(reversedReplicon, genome.getId());
                    createMatchLists(replicon, genome.getId());

                } else {//split replicon to directons

                    List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

                    for (Directon directon : directons) {
                        createMatchLists(directon, genome.getId());
                    }

                }
            }
        }
    }

    private void createMatchLists(GenomicSegment genomicSegment, int currGenomeId) {

        genomicSegments.add(genomicSegment);

        List<Gene> genes = genomicSegment.getGenes();
        WordArray cogWord = genomesInfo.createWordArray(genes);
        genomesInfo.countParalogsInSeqs(cogWord, currGenomeId);

        for (int i = 0; i < cogWord.getLength(); i++) {
            int currLetter = cogWord.getLetter(i);
            if (currLetter != Alphabet.UNK_CHAR_INDEX) {
                Map<String, List<MatchPoint>> genomicSegmentToCogPositions = matchLists
                        .computeIfAbsent(currLetter, k -> new HashMap<>());

                List<MatchPoint> genePositions = genomicSegmentToCogPositions
                        .computeIfAbsent(genomeRepliconToHashString(genomicSegment.getGenomeId(),
                                genomicSegment.getRepliconId(), genomicSegment.getId()), k -> new ArrayList<>());

                MatchPoint matchPoint = new MatchPoint(genomicSegment, i);
                genePositions.add(matchPoint);
            }
        }
    }

    public static String genomeRepliconToHashString(int genomeId, int repliconId, int genomicSegmentId){
        return String.format("%d_%d_%d", genomeId, repliconId, genomicSegmentId);
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
            genomicSegments = new ArrayList<>();
            createMatchLists(parameters.nonDirectons);
        }

        patterns = new ConcurrentHashMap<>();
    }

    @Override
    public void findPatterns() {
        if (genomesInfo == null || parameters == null) {
            return;
        }

        initialize();

        List<Callable<Object>> tasks = new ArrayList<>();

        if (patternsFromFile.size() > 0){
            for (Pattern pattern : patternsFromFile){

                List<Gene> genes = pattern.getPatternGenes();

                Replicon replicon = new Replicon();
                replicon.addAllGenes(genes);

                if(parameters.nonDirectons) {

                    tasks.add(new FindPatternsThread(genes, genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                            parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));

                    replicon.reverseCompliment();

                    tasks.add(new FindPatternsThread(replicon.getGenes(), genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                            parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));

                }else{

                    List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

                    for (Directon directon : directons) {
                        tasks.add(new FindPatternsThread(directon.getGenes(), genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                                parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));
                    }
                }

            }
        } else {
            for (GenomicSegment genomicSegment : genomicSegments) {

                List<Gene> genes = genomicSegment.getGenes();
                //extractPatterns(genes);
                tasks.add(new FindPatternsThread(genes, genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                        parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));
            }
        }
        try {
            List<Future<Object>> answers = executor.invokeAll(tasks);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setPatternIds();
        removeRedundantPatterns();
    }

    private void setPatternIds(){
        int i = 1;
        for (Pattern pattern : patterns.values()){
            pattern.setPatternId(String.valueOf(i++));
        }
    }
    private void removeRedundantPatterns() {

        HashSet<String> patternsToRemove = new HashSet<>();
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {

            Pattern pattern = entry.getValue();

            if (!parameters.keepAllPatterns) {
                String suffixStr = getSuffix(pattern);
                addSubPatternToRemoveList(suffixStr, pattern, patternsToRemove);

                String prefixStr = getPrefix(pattern);
                addSubPatternToRemoveList(prefixStr, pattern, patternsToRemove);
            }

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

}
