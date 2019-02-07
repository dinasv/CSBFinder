import Core.Algorithm;
import Core.AlgorithmType;
import Core.Genomes.Gene;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Strand;
import Core.Parameters;
import Core.Patterns.Pattern;
import IO.Parsers;
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
        algorithm.setPatternsFromFile(patternsFromFile);
        algorithm.setGenomesInfo(gi);
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

    private Parameters initParamsSetLenggth(){
        Parameters params = initParamsNonDirectons();
        params.minPatternLength = 2;
        params.maxPatternLength = 2;

        return params;
    }

    private List<Pattern> runAlgorithm(String genomesFile, Parameters params) throws Exception{

        Algorithm algorithm = AlgorithmType.MATCH_POINTS.algorithm;
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
        List<Gene> expectedGenes = new ArrayList<>();
        expectedGenes.add(new Gene("COG0001", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0002", Strand.FORWARD));
        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }

    @Test
    public void testRefGenomesReverseComplimentPattern() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsNonDirectons());

        List<Pattern> expectedPatterns = new ArrayList<>();
        List<Gene> expectedGenes = new ArrayList<>();
        expectedGenes.add(new Gene("COG0001", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0002", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0003", Strand.REVERSE));
        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }

    @Test
    public void testKeepAllPatterns() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsKeepAll());

        List<Pattern> expectedPatterns = new ArrayList<>();
        List<Gene> expectedGenes = new ArrayList<>();
        expectedGenes.add(new Gene("COG0001", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0002", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0003", Strand.REVERSE));
        expectedPatterns.add(new Pattern(expectedGenes));
        expectedPatterns.add(new Pattern(expectedGenes.subList(0, 2)));
        expectedPatterns.add(new Pattern(expectedGenes.subList(1, 3)));

        Assert.assertTrue(patterns.size() == expectedPatterns.size());
        for (Pattern pattern : expectedPatterns) {
            Assert.assertTrue(patterns.contains(pattern));
        }
    }

    @Test
    public void testMinMaxLengthPatterns() throws Exception {
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, initParamsSetLenggth());

        List<Pattern> expectedPatterns = new ArrayList<>();
        List<Gene> expectedGenes = new ArrayList<>();
        expectedGenes.add(new Gene("COG0001", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0002", Strand.FORWARD));
        expectedGenes.add(new Gene("COG0003", Strand.REVERSE));

        expectedPatterns.add(new Pattern(expectedGenes.subList(0, 2)));
        expectedPatterns.add(new Pattern(expectedGenes.subList(1, 3)));

        Assert.assertTrue(patterns.size() == expectedPatterns.size());
        for (Pattern pattern : expectedPatterns) {
            Assert.assertTrue(patterns.contains(pattern));
        }
    }

}
