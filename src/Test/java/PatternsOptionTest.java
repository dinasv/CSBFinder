import IO.Parsers;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class PatternsOptionTest {
    private final String GENOMES_FILE_PATH1 = this.getClass().getResource("/genomes.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes2.fasta").getPath();
    private final String PATTERNS_FILE_PATH = this.getClass().getResource("/patterns.fasta").getPath();
    private final String PATTERNS_FILE_PATH2 = this.getClass().getResource("/patterns2.fasta").getPath();


    private List<Pattern> runWorkflow(String genomesFile, List<Pattern> patternsFromFile, Parameters params) throws Exception{

        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile);

        CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

        workflow.setPatternsFromFile(patternsFromFile);

        workflow.setAlgorithm(params.algorithmType.getAlgorithm());
        workflow.run(params);

        return workflow.getFamilies().stream().map(Family::getPatterns)
                .flatMap(List::stream).collect(Collectors.toList());
    }

    @Test
    public void testLetterNotInAlphabetST() throws Exception {

        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH);
        Parameters parameters = new Parameters();
        parameters.algorithmType = AlgorithmType.SUFFIX_TREE;

        List<Pattern> patterns = runWorkflow(GENOMES_FILE_PATH2, patternsFromFile, parameters);

        List<Pattern> expectedPatterns = new ArrayList<>();

        Gene[] expectedGenes = {new Gene("COG0001", Strand.INVALID),
                new Gene("COG0002", Strand.INVALID)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns, patterns);

    }

    @Test
    public void testMP() throws Exception {

        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH2);
        Parameters parameters = new Parameters();
        parameters.algorithmType = AlgorithmType.MATCH_POINTS;

        List<Pattern> patterns = runWorkflow(GENOMES_FILE_PATH1, patternsFromFile, parameters);

        List<Pattern> expectedPatterns = new ArrayList<>();

        Gene[] expectedGenes = {new Gene("COG0001", Strand.INVALID),
                new Gene("COG0002", Strand.INVALID),
                new Gene("COG0003", Strand.INVALID)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns.size(), patterns.size());
        Assert.assertEquals(expectedPatterns, patterns);
        Assert.assertEquals(patternsFromFile.get(0).getPatternId(), patterns.get(0).getPatternId());

    }
}
