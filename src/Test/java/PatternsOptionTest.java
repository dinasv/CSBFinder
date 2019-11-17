import IO.Parsers;
import Model.Algorithm;
import Model.AlgorithmType;
import Model.CSBFinderWorkflow;
import Model.Genomes.Gene;
import Model.Genomes.GenomesInfo;
import Model.Genomes.Strand;
import Model.Parameters;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class PatternsOptionTest {
    private final String GENOMES_FILE_PATH1 = this.getClass().getResource("/genomes2.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes3.fasta").getPath();
    private final String PATTERNS_FILE_PATH = this.getClass().getResource("/patterns.fasta").getPath();

    private void initAlgorithm(Algorithm algorithm, Parameters params, List<Pattern> patternsFromFile, GenomesInfo gi){
        algorithm.setParameters(params);
        algorithm.setPatternsFromFile(patternsFromFile);
        algorithm.setGenomesInfo(gi);
        algorithm.setNumOfThreads(1);
    }

    private List<Pattern> runAlgorithm(String genomesFile, Parameters params) throws Exception{

        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile);
        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH);

        CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

        workflow.setPatternsFromFile(patternsFromFile);

        workflow.setAlgorithm(AlgorithmType.SUFFIX_TREE.algorithm);
        workflow.run(params);

        return workflow.getFamilies().stream().map(Family::getPatterns)
                .flatMap(List::stream).collect(Collectors.toList());
    }

    @Test
    public void testLetterNotInAlphabetST() throws Exception {

        List<Pattern> patterns = runAlgorithm(GENOMES_FILE_PATH1, new Parameters());

        List<Pattern> expectedPatterns = new ArrayList<>();

        Gene[] expectedGenes = {new Gene("COG0001", Strand.INVALID),
                new Gene("COG0002", Strand.INVALID)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }
}
