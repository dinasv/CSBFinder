import io.Parsers;
import model.AlgorithmType;
import model.CSBFinderWorkflow;
import model.genomes.Gene;
import model.genomes.GenomesInfo;
import model.genomes.Strand;
import model.Parameters;
import model.patterns.Pattern;
import model.postprocess.Family;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class PatternsOptionTest {
    private final String GENOMES_FILE_PATH1 = this.getClass().getResource("/genomes.fasta").getPath();
    private final String GENOMES_FILE_PATH2 = this.getClass().getResource("/genomes2.fasta").getPath();
    private final String GENOMES_FILE_PATH3 = this.getClass().getResource("/genomes13.fasta").getPath();
    private final String PATTERNS_FILE_PATH = this.getClass().getResource("/patterns.fasta").getPath();
    private final String PATTERNS_FILE_PATH2 = this.getClass().getResource("/patterns2.fasta").getPath();
    private final String PATTERNS_FILE_PATH3 = this.getClass().getResource("/patterns5.fasta").getPath();


    private List<Pattern> runWorkflow(String genomesFile, List<Pattern> patternsFromFile, Parameters params) throws Exception{

        GenomesInfo gi = Parsers.parseGenomesFile(genomesFile, params.circular);

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

    @Test
    public void testMP2() throws Exception {

        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH);
        Parameters parameters = new Parameters();
        parameters.algorithmType = AlgorithmType.MATCH_POINTS;

        List<Pattern> patterns = runWorkflow(GENOMES_FILE_PATH3, patternsFromFile, parameters);

        List<Pattern> expectedPatterns = new ArrayList<>();

        Gene[] expectedGenes = {new Gene("COG0001", Strand.INVALID),
                new Gene("COG0002", Strand.INVALID)};

        expectedPatterns.add(new Pattern(expectedGenes));

        Assert.assertEquals(expectedPatterns.size(), patterns.size());
        Assert.assertEquals(expectedPatterns, patterns);
        Assert.assertEquals(2, patterns.get(0).getInstancesPerGenomeCount());

    }

    @Test
    public void testCircularMP() throws Exception {
        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH3);
        Parameters parameters = new Parameters();
        parameters.algorithmType = AlgorithmType.MATCH_POINTS;
        parameters.circular = true;
        parameters.maxInsertion = 1;

        List<Pattern> patterns = runWorkflow(GENOMES_FILE_PATH1, patternsFromFile, parameters);

        Gene[] expectedGenes = {new Gene("COG0003", Strand.INVALID),
                new Gene("COG0004", Strand.INVALID),
                new Gene("COG0001", Strand.INVALID)};

        Pattern expectedPattern = new Pattern(expectedGenes);
        expectedPattern.setPatternId(patternsFromFile.get(0).getPatternId());

        Assert.assertEquals(1, patterns.size());

        Pattern pattern = patterns.get(0);

        Assert.assertEquals(expectedPattern, pattern);
        Assert.assertEquals(expectedPattern.getPatternId(), pattern.getPatternId());
        Assert.assertEquals(2, pattern.getInstancesPerGenomeCount());

    }

    @Test
    public void testCircularST() throws Exception {
        List<Pattern> patternsFromFile = Parsers.parsePatternsFile(PATTERNS_FILE_PATH3);
        Parameters parameters = new Parameters();
        parameters.algorithmType = AlgorithmType.SUFFIX_TREE;
        parameters.circular = true;
        parameters.maxInsertion = 1;

        List<Pattern> patterns = runWorkflow(GENOMES_FILE_PATH1, patternsFromFile, parameters);

        Gene[] expectedGenes = {new Gene("COG0003", Strand.INVALID),
                new Gene("COG0004", Strand.INVALID),
                new Gene("COG0001", Strand.INVALID)};

        Pattern expectedPattern = new Pattern(expectedGenes);
        expectedPattern.setPatternId(patternsFromFile.get(0).getPatternId());

        Assert.assertEquals(1, patterns.size());

        Pattern pattern = patterns.get(0);

        Assert.assertEquals(expectedPattern, pattern);
        Assert.assertEquals(expectedPattern.getPatternId(), pattern.getPatternId());
        Assert.assertEquals(2, pattern.getInstancesPerGenomeCount());

    }
}
