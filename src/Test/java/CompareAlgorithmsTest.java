import Core.Algorithm;
import Core.AlgorithmType;
import Core.CSBFinderWorkflow;
import Core.Genomes.GenomesInfo;
import Core.Parameters;
import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import IO.Parsers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class CompareAlgorithmsTest {

    private final String GENOMES_FILE_PATH = this.getClass().getResource("/genomes.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes4.fasta").getPath();
    private final String GENOMES_FILE_PATH3 = this.getClass().getResource("/genomes5.fasta").getPath();
    private final String GENOMES_FILE_PATH4 = this.getClass().getResource("/genomes6.fasta").getPath();
    private final String GENOMES_FILE_PATH5 = this.getClass().getResource("/genomes7.fasta").getPath();
    private final String REF_GENOMES_FILE_PATH = this.getClass().getResource("/ref_genomes.txt").getPath();
    private final String PLASMID_GENOMES_FILE_PATH = this.getClass().getResource("/plasmid_genomes.fasta").getPath();

    @Test
    public void testRefGenomesEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.maxInsertion = 1;

        GenomesInfo gi = Parsers.parseGenomesFile(GENOMES_FILE_PATH);
        CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

        List<Pattern> patternsFromFile = Parsers.parseReferenceGenomesFile(gi, REF_GENOMES_FILE_PATH);
        workflow.setPatternsFromFile(patternsFromFile);

        workflow.setAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm);
        List<Family> familiesAlg1 = workflow.run(params);

        //gi.initAlphabet();
        workflow.setAlgorithm(AlgorithmType.MATCH_POINTS.algorithm);
        List<Family> familiesAlg2 = workflow.run(params);

        Assert.assertEquals(familiesAlg1, familiesAlg2);

    }

    @Test
    public void testDirectonsEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.quorum2 = 1;

        String[] files = {GENOMES_FILE_PATH2, GENOMES_FILE_PATH4, GENOMES_FILE_PATH5};

        for (String file : files) {
            List<Pattern> patternsAlg1 = runAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm, file, params);

            List<Pattern> patternsAlg2 = runAlgorithm(AlgorithmType.MATCH_POINTS.algorithm, file, params);

            comparePatterns(patternsAlg1, patternsAlg2);
        }

    }

    @Test
    public void testNonDirectonsEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.quorum2 = 2;
        params.nonDirectons = true;

        List<Pattern> patternsAlg1 = runAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm, GENOMES_FILE_PATH3, params);

        List<Pattern> patternsAlg2 = runAlgorithm(AlgorithmType.MATCH_POINTS.algorithm, GENOMES_FILE_PATH3, params);

        comparePatterns(patternsAlg1, patternsAlg2);

    }

    @Test
    public void testRealDatasetEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.quorum2 = 10;
        params.keepAllPatterns = true;
        //params.nonDirectons = true;

        List<Pattern> patternsAlg1 = runAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm, PLASMID_GENOMES_FILE_PATH, params);

        List<Pattern> patternsAlg2 = runAlgorithm(AlgorithmType.MATCH_POINTS.algorithm, PLASMID_GENOMES_FILE_PATH, params);

        comparePatterns(patternsAlg1, patternsAlg2);

    }

    private void comparePatterns(List<Pattern> patternsAlg1, List<Pattern> patternsAlg2){
        Assert.assertEquals(patternsAlg1.size(), patternsAlg2.size());
        for (Pattern pattern : patternsAlg1){
            Assert.assertTrue(patternsAlg2.contains(pattern));
            int index = patternsAlg2.indexOf(pattern);
            Pattern alg2Pattern = patternsAlg2.get(index);
            Assert.assertEquals(pattern.getInstancesPerGenome(), alg2Pattern.getInstancesPerGenome());
        }
    }

    private List<Pattern> runAlgorithm(Algorithm algorithm, String genomesFile, Parameters params) throws Exception{

        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile);

        algorithm.setParameters(params);
        algorithm.setGenomesInfo(gi);
        algorithm.setNumOfThreads(1);
        algorithm.findPatterns();

        return algorithm.getPatterns();
    }
}
