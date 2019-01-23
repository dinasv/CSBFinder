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
    private final String REF_GENOMES_FILE_PATH = this.getClass().getResource("/ref_genomes.txt").getPath();

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

        gi.initAlphabet();
        workflow.setAlgorithm(AlgorithmType.MATCH_POINTS.algorithm);
        List<Family> familiesAlg2 = workflow.run(params);

        Assert.assertEquals(familiesAlg1, familiesAlg2);

    }
}
