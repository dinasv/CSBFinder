import io.Parsers;
import model.Algorithm;
import model.AlgorithmType;
import model.Parameters;
import model.genomes.Gene;
import model.genomes.GenomesInfo;
import model.genomes.Strand;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuffixTreeAlgorithmTest {

    private final String GENOMES_FILE_PATH1 = this.getClass().getResource("/genomes10.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes11.fasta").getPath();
    private final String GENOMES_FILE_PATH3 = this.getClass().getResource("/genomes12.fasta").getPath();

    private Parameters initParamsCrossStrand(){
        Parameters params = new Parameters();
        params.crossStrand = true;
        params.quorum2 = 2;
        return params;
    }

    private Parameters initParamsDirectons(){
        Parameters params = new Parameters();
        params.quorum2 = 2;
        return params;
    }

    private void initAlgorithm(Algorithm algorithm, Parameters params, GenomesInfo gi){
        algorithm.setParameters(params);
        algorithm.setGenomesInfo(gi);
        algorithm.setNumOfThreads(1);
    }

    private List<Pattern> runAlgorithm(String genomesFile, Parameters params) throws Exception{

        Algorithm algorithm = AlgorithmType.SUFFIX_TREE.getAlgorithm();
        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile, params.circular);

        initAlgorithm(algorithm, params, gi);

        algorithm.findPatterns();

        return algorithm.getPatterns();
    }

    @Test
    public void testCircularGenome() throws Exception {
        Parameters params = initParamsCrossStrand();
        params.circular = true;
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH1, params);

        List<Pattern> expectedPatterns = new ArrayList<>();
        Gene[] expectedGenes = {
                new Gene("COG0001", Strand.FORWARD),
                new Gene("COG0002", Strand.FORWARD),
                new Gene("COG0003", Strand.FORWARD)};
        expectedPatterns.add(new Pattern(expectedGenes));

        Map<Integer, Integer> expectedGenomeToStartIndex = new HashMap<>();
        expectedGenomeToStartIndex.put(0, 3);
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
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH2, params);

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

    @Test
    public void testCircularGenomeInsertions() throws Exception {
        Parameters params = initParamsCrossStrand();
        params.circular = true;
        params.maxInsertion = 1;
        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH3, params);

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
}
