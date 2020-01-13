import model.Algorithm;
import model.AlgorithmType;
import model.genomes.Gene;
import model.genomes.GenomesInfo;
import model.genomes.Strand;
import model.Parameters;
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
    private final String REF_GENOMES_FILE_PATH = this.getClass().getResource("/ref_genomes.txt").getPath();

    private void initAlgorithm(Algorithm algorithm, Parameters params, List<Pattern> patternsFromFile, GenomesInfo gi){
        algorithm.setParameters(params);
        algorithm.setRefGenomesAsPatterns(patternsFromFile);
        algorithm.setGenomesInfo(gi);
        algorithm.setNumOfThreads(1);
    }

    private Parameters initParamsNonDirectons(){
        Parameters params = new Parameters();
        params.nonDirectons = true;
        params.quorum2 = 2;
        return params;
    }

    private Parameters initParamsKeepAll(){
        Parameters params = initParamsNonDirectons();
        params.keepAllPatterns = true;

        return params;
    }

    private Parameters initParamsSetLength(){
        Parameters params = initParamsNonDirectons();
        params.minPatternLength = 2;
        params.maxPatternLength = 2;

        return params;
    }

    private List<Pattern> runAlgorithm(String genomesFile, Parameters params) throws Exception{

        Algorithm algorithm = AlgorithmType.MATCH_POINTS.getAlgorithm();
        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile);
        List<Pattern> patternsFromFile = Parsers.parseReferenceGenomesFile(gi, REF_GENOMES_FILE_PATH);

        initAlgorithm(algorithm, params, patternsFromFile, gi);

        algorithm.findPatterns();

        return algorithm.getPatterns();
    }

    @Test
    public void testRefGenomesPatternsDuplicateGenes() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH1, initParamsNonDirectons());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                new Gene("COG0002", Strand.FORWARD)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }


    @Test
    public void testRefGenomesReverseComplimentPattern() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsNonDirectons());

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {new Gene("COG0001", Strand.FORWARD),
                                new Gene("COG0002", Strand.FORWARD),
                                new Gene("COG0003", Strand.REVERSE)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

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

}
