import Model.Algorithm;
import Model.AlgorithmType;
import Model.CSBFinderWorkflow;
import Model.Genomes.GenomesInfo;
import Model.Parameters;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import IO.Parsers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 */
public class CompareAlgorithmsTest {

    private final String GENOMES_FILE_PATH = this.getClass().getResource("/genomes.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes4.fasta").getPath();
    private final String GENOMES_FILE_PATH3 = this.getClass().getResource("/genomes5.fasta").getPath();
    private final String GENOMES_FILE_PATH4 = this.getClass().getResource("/genomes6.fasta").getPath();
    private final String GENOMES_FILE_PATH5 = this.getClass().getResource("/genomes7.fasta").getPath();
    private final String GENOMES_FILE_PATH6 = this.getClass().getResource("/genomes8.fasta").getPath();
    private final String REF_GENOMES_FILE_PATH = this.getClass().getResource("/ref_genomes.txt").getPath();
    private final String PLASMID_GENOMES_FILE_PATH = this.getClass().getResource("/plasmid_genomes.fasta").getPath();
    private final String PLASMID_GENOMES_SMALL_FILE_PATH = this.getClass().getResource("/plasmid_genomes_small.txt").getPath();
    private final String PLASMID_GENOMES_SMALL_FILE_PATH2 = this.getClass().getResource("/plasmid_genomes_small2.txt").getPath();

    @Test
    public void testRefGenomesEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.maxInsertion = 1;

        GenomesInfo gi = Parsers.parseGenomesFile(GENOMES_FILE_PATH);
        CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

        List<Pattern> patternsFromFile = Parsers.parseReferenceGenomesFile(gi, REF_GENOMES_FILE_PATH);
        workflow.setPatternsFromFile(patternsFromFile);

        workflow.setAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm);
        workflow.run(params);
        List<Family> familiesAlg1 = workflow.getFamilies();

        //gi.initAlphabet();
        workflow.setAlgorithm(AlgorithmType.MATCH_POINTS.algorithm);
        workflow.run(params);
        List<Family> familiesAlg2 = workflow.getFamilies();

        Assert.assertEquals(familiesAlg1, familiesAlg2);

    }

    @Test
    public void testDirectonsEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.quorum2 = 2;
        params.maxInsertion = 1;
        params.maxPatternLength = 2;
        //params.keepAllPatterns = true;

        String[] files = {GENOMES_FILE_PATH2, GENOMES_FILE_PATH4, GENOMES_FILE_PATH5, GENOMES_FILE_PATH6,
                PLASMID_GENOMES_SMALL_FILE_PATH, PLASMID_GENOMES_SMALL_FILE_PATH2};

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
        params.maxInsertion = 1;

        List<Pattern> patternsAlg1 = runAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm, GENOMES_FILE_PATH3, params);

        List<Pattern> patternsAlg2 = runAlgorithm(AlgorithmType.MATCH_POINTS.algorithm, GENOMES_FILE_PATH3, params);

        comparePatterns(patternsAlg1, patternsAlg2);

    }

    @Test
    public void testRealDatasetEqualOutput() throws Exception {
        Parameters params = new Parameters();
        params.quorum2 = 10;
        params.keepAllPatterns = true;
        params.maxInsertion = 2;

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
            Assert.assertEquals(pattern.getInstancesPerGenomeCount(), alg2Pattern.getInstancesPerGenomeCount());
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
