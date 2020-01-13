package model.matchpointsbased;

import model.Algorithm;
import model.genomes.*;
import model.Parameters;
import model.patterns.Pattern;
import model.patterns.PatternsUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 */
public class MatchPointAlgorithm implements Algorithm {

    /**
     * Alphabet letter to matches in each genomic segment
     */
    private Map<Integer, Map<Integer, List<MatchPoint>>> matchLists;
    private List<GenomicSegment> genomicSegments;
    private GenomesInfo genomesInfo;
    private Parameters parameters;

    private ConcurrentMap<String, Pattern> patterns;

    private List<Pattern> patternsFromFile;
    private List<Pattern> refGenomesAsPatterns;

    private ExecutorService executor;

    private SegmentationType segmentationType;
    private ExtractPatternsFrom extractPatternsFrom;

    public MatchPointAlgorithm() {
        matchLists = new HashMap<>();
        genomesInfo = null;
        genomicSegments = new ArrayList<>();
        patterns = new ConcurrentHashMap<>();
        patternsFromFile = new ArrayList<>();
        refGenomesAsPatterns = new ArrayList<>();

        executor = Executors.newFixedThreadPool(1);

        extractPatternsFrom = ExtractPatternsFrom.ALL_GENOMES;
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
                Map<Integer, List<MatchPoint>> genomicSegmentToCogPositions = matchLists
                        .computeIfAbsent(currLetter, k -> new HashMap<>());

                List<MatchPoint> genePositions = genomicSegmentToCogPositions
                        .computeIfAbsent(Objects.hash(genomicSegment.getGenomeId(),
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
        segmentationType = params.nonDirectons ? SegmentationType.NON_DIRECTONS : SegmentationType.DIRECTONS;
    }

    @Override
    public void setGenomesInfo(GenomesInfo gi) {
        genomesInfo = gi;
        matchLists = new HashMap<>();
    }

    @Override
    public void setPatternsFromFile(List<Pattern> patternsFromFile) {
        this.patternsFromFile = patternsFromFile;

        if (patternsFromFile.size() > 0) {
            extractPatternsFrom = ExtractPatternsFrom.FILE;
        }
    }

    @Override
    public void setRefGenomesAsPatterns(List<Pattern> refGenomesPatterns){

        refGenomesAsPatterns = refGenomesPatterns;

        if (refGenomesPatterns.size() >0 && extractPatternsFrom != ExtractPatternsFrom.FILE) {
            extractPatternsFrom = ExtractPatternsFrom.REF_GENOMES;
        }
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

        if (extractPatternsFrom == ExtractPatternsFrom.FILE){
            extractPatternsFromFile(tasks);
        } else if (extractPatternsFrom == ExtractPatternsFrom.REF_GENOMES) {
            extractPatternsFromRefGenomes(tasks);
        }else{
            extractPatternsFromAllGenomes(tasks);
        }

        try {
            List<Future<Object>> answers = executor.invokeAll(tasks);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (extractPatternsFrom != ExtractPatternsFrom.FILE) {
            setPatternIds();
            removeRedundantPatterns();
        }
    }

    private void extractPatternsFromAllGenomes(List<Callable<Object>> tasks) {
        for (GenomicSegment genomicSegment : genomicSegments) {

            List<Gene> genes = genomicSegment.getGenes();
            tasks.add(new FindPatternsFromGenesThread(genes, genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                    parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));
        }
    }

    private void extractPatternsFromRefGenomes(List<Callable<Object>> tasks) {
        for (Pattern pattern : refGenomesAsPatterns){

            List<Gene> genes = Arrays.asList(pattern.getPatternGenes());

            Replicon replicon = new Replicon();
            replicon.addAllGenes(genes);

            if(segmentationType == SegmentationType.NON_DIRECTONS) {

                tasks.add(new FindPatternsFromGenesThread(genes, genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                        parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));

                replicon.reverseCompliment();

                tasks.add(new FindPatternsFromGenesThread(replicon.getGenes(), genomesInfo, parameters.quorum2, parameters.maxPatternLength,
                        parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));

            }else{

                List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

                for (Directon directon : directons) {
                    tasks.add(new FindPatternsFromGenesThread(directon.getGenes(), genomesInfo, parameters.quorum2,
                            parameters.maxPatternLength,
                            parameters.minPatternLength, parameters.maxInsertion, patterns, matchLists));
                }
            }

        }
    }

    private void extractPatternsFromFile(List<Callable<Object>> tasks){
        List<Pattern> legalPatterns = PatternsUtils.getLegalPatterns(patternsFromFile, genomesInfo);
        for (Pattern pattern : legalPatterns){

            tasks.add(new FindPatternFromFileThread(pattern, genomesInfo, parameters.quorum2, parameters.maxInsertion, patterns, matchLists));

        }
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
                PatternsUtils.addSubPatternToRemoveList(patterns, suffixStr, pattern, patternsToRemove);

                String prefixStr = getPrefix(pattern);
                PatternsUtils.addSubPatternToRemoveList(patterns, prefixStr, pattern, patternsToRemove);

            }

            if (parameters.nonDirectons) {
                PatternsUtils.removeReverseCompliments(patterns, pattern, patternsToRemove);
            }
        }


        patterns.keySet().removeAll(patternsToRemove);
    }

    private String getSuffix(Pattern pattern){

        Gene[] suffix = Arrays.copyOfRange( pattern.getPatternGenes(), 1, pattern.getLength());

        return Pattern.toString(suffix);
    }

    private String getPrefix(Pattern pattern){

        Gene[] prefix = Arrays.copyOfRange( pattern.getPatternGenes(), 0, pattern.getLength()-1);

        return Pattern.toString(prefix);
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

    private enum SegmentationType{
        DIRECTONS,
        NON_DIRECTONS
    }

    private enum ExtractPatternsFrom{
        FILE,
        REF_GENOMES,
        ALL_GENOMES
    }

}
