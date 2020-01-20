import model.Algorithm;
import model.AlgorithmType;
import model.genomes.Gene;
import model.genomes.GenomesInfo;
import model.genomes.Strand;
import model.Parameters;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;
import io.Parsers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 */
public class MatchPointAlgorithmTest {
    private final String GENOMES_FILE_PATH1 = this.getClass().getResource("/genomes2.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes3.fasta").getPath();
    private final String GENOMES_FILE_PATH3 = this.getClass().getResource("/genomes9.fasta").getPath();
    private final String GENOMES_FILE_PATH4 = this.getClass().getResource("/genomes11.fasta").getPath();
    private final String GENOMES_FILE_PATH5 = this.getClass().getResource("/genomes12.fasta").getPath();
    private final String REF_GENOMES_FILE_PATH = this.getClass().getResource("/ref_genomes.txt").getPath();

    private void initAlgorithm(Algorithm algorithm, Parameters params, GenomesInfo gi){
        algorithm.setParameters(params);
        //algorithm.setRefGenomesAsPatterns(patternsFromFile);
        algorithm.setGenomesInfo(gi);
        algorithm.setNumOfThreads(1);
    }

    private Parameters initParamsDirectons(){
        Parameters params = new Parameters();
        params.quorum2 = 2;
        return params;
    }

    private Parameters initParamsCrossStrand(){
        Parameters params = new Parameters();
        params.crossStrand = true;
        params.quorum2 = 2;
        return params;
    }

    private Parameters initParamsKeepAll(){
        Parameters params = initParamsCrossStrand();
        params.keepAllPatterns = true;

        return params;
    }

    private Parameters initParamsSetLength(){
        Parameters params = initParamsCrossStrand();
        params.minPatternLength = 2;
        params.maxPatternLength = 2;

        return params;
    }

    private List<Pattern> runAlgorithm(String genomesFile, Parameters params) throws Exception{

        Algorithm algorithm = AlgorithmType.MATCH_POINTS.getAlgorithm();
        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile, params.circular);
        //List<Pattern> patternsFromFile = Parsers.parseReferenceGenomesFile(gi, REF_GENOMES_FILE_PATH);

        initAlgorithm(algorithm, params, gi);

        algorithm.findPatterns();

        return algorithm.getPatterns();
    }

    @Test
    public void testRefGenomesPatternsDuplicateGenes() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH1, initParamsCrossStrand());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                new Gene("COG0002", Strand.FORWARD)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }


    @Test
    public void testRefGenomesReverseComplimentPattern() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsCrossStrand());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                new Gene("COG0002", Strand.FORWARD),
                                new Gene("COG0003", Strand.REVERSE)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }

    @Test
    public void testInstanceLocations() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsCrossStrand());

        for (Pattern pattern : patterns){
            for (InstanceLocation loc :pattern.getPatternLocations().getSortedLocations()){
                Assert.assertEquals(1, loc.getActualStartIndex());
                Assert.assertEquals(4, loc.getActualEndIndex());
            }
        }

    }

    @Test
    public void testKeepAllPatterns() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsKeepAll());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                    new Gene("COG0002", Strand.FORWARD),
                                    new Gene("COG0003", Strand.REVERSE)};
        expectedPatterns.add(new Pattern(expectedGenes));
        expectedPatterns.add(new Pattern(Arrays.copyOfRange(expectedGenes,0, 2)));
        expectedPatterns.add(new Pattern(Arrays.copyOfRange(expectedGenes,1, 3)));

        Assert.assertEquals(patterns.size(), expectedPatterns.size());
        for (Pattern pattern : expectedPatterns) {
            Assert.assertTrue(patterns.contains(pattern));
        }
    }

    @Test
    public void testMinMaxLengthPatterns() throws Exception {
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsSetLength());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                new Gene("COG0002", Strand.FORWARD),
                                new Gene("COG0003", Strand.REVERSE)};

        expectedPatterns.add(new Pattern(Arrays.copyOfRange(expectedGenes, 0, 2)));
        expectedPatterns.add(new Pattern(Arrays.copyOfRange(expectedGenes,1, 3)));

        Assert.assertEquals(patterns.size(), expectedPatterns.size());
        for (Pattern pattern : expectedPatterns) {
            Assert.assertTrue(patterns.contains(pattern));
        }
    }

    @Test
    public void testCircularGenome() throws Exception {
        Parameters params = initParamsCrossStrand();
        params.circular = true;
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH3, params);

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {
                new Gene("COG0001", Strand.FORWARD),
                new Gene("COG0002", Strand.FORWARD),
                new Gene("COG0003", Strand.FORWARD)};
        expectedPatterns.add(new Pattern(expectedGenes));

        Map<Integer, Integer> expectedGenomeToStartIndex = new HashMap<>();
        expectedGenomeToStartIndex.put(0, 2);
        expectedGenomeToStartIndex.put(1, 0);

        Assert.assertEquals(expectedPatterns, patterns);
        for (Pattern pattern : patterns){
            List<InstanceLocation> locations = pattern.getPatternLocations().getSortedLocations();
            for (InstanceLocation location : locations){
                int expectedStartIndex = expectedGenomeToStartIndex.get(location.getGenomeId());
                Assert.assertEquals(expectedStartIndex, location.getActualStartIndex());
            }
        }
    }

    @Test
    public void testCircularGenomeInsertions() throws Exception {
        Parameters params = initParamsCrossStrand();
        params.circular = true;
        params.maxInsertion = 1;
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH5, params);

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {
                new Gene("COG0001", Strand.FORWARD),
                new Gene("COG0002", Strand.FORWARD),
                new Gene("COG0004", Strand.FORWARD)};
        expectedPatterns.add(new Pattern(expectedGenes));

        Map<Integer, Integer> expectedGenomeToStartIndex = new HashMap<>();
        expectedGenomeToStartIndex.put(0, 2);
        expectedGenomeToStartIndex.put(1, 0);

        Assert.assertEquals(expectedPatterns, patterns);
        for (Pattern pattern : patterns){
            List<InstanceLocation> locations = pattern.getPatternLocations().getSortedLocations();
            for (InstanceLocation location : locations){
                int expectedStartIndex = expectedGenomeToStartIndex.get(location.getGenomeId());
                Assert.assertEquals(expectedStartIndex, location.getActualStartIndex());
            }
        }
    }

    @Test
    public void testCircularGenomeDirectons() throws Exception {
        Parameters params = initParamsDirectons();
        params.circular = true;
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH4, params);

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {
                new Gene("COG0001", Strand.INVALID),
                new Gene("COG0002", Strand.INVALID),
                new Gene("COG0003", Strand.INVALID)};
        expectedPatterns.add(new Pattern(expectedGenes));

        Map<Integer, Integer> expectedGenomeToStartIndex = new HashMap<>();
        expectedGenomeToStartIndex.put(0, 2);
        expectedGenomeToStartIndex.put(1, 0);

        Assert.assertEquals(expectedPatterns, patterns);
        for (Pattern pattern : patterns){
            List<InstanceLocation> locations = pattern.getPatternLocations().getSortedLocations();
            for (InstanceLocation location : locations){
                int expectedStartIndex = expectedGenomeToStartIndex.get(location.getGenomeId());
                Assert.assertEquals(expectedStartIndex, location.getActualStartIndex());
            }
        }
    }

}
