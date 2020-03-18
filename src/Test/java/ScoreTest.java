import io.Parsers;
import model.genomes.GenomesInfo;
import model.patterns.PatternScore;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 */
public class ScoreTest {

    private final String GENOMES_FILE_PATH = this.getClass().getResource("/genomes8.fasta").getPath();

    @Test
    public void testScore() {
        int MAX_GENOME_SIZE = 2000;
        int NUMBER_OF_GENOMES = 1500;
        int DATASET_LENGTH_SUM = MAX_GENOME_SIZE * NUMBER_OF_GENOMES;
        int PATTERN_LENGTH = 9;
        int GENOMES_WITH_INSTANCE = 30;
        int MAX_INSERTIONS = 0;
        double PARALOG_FREQUENCY = Math.log(10)*50;
        double epsilon = 0.01;

        PatternScore patternScore = new PatternScore(MAX_GENOME_SIZE, NUMBER_OF_GENOMES, DATASET_LENGTH_SUM,
                new HashMap<>(), new HashMap<>());

        double score = patternScore.pvalCrossGenome(PATTERN_LENGTH, MAX_INSERTIONS, PARALOG_FREQUENCY, GENOMES_WITH_INSTANCE);

        Assert.assertEquals(0, score, epsilon);
    }

    /**
     * Test if the score for large values is not infinity
     * @throws Exception
     */
    @Test
    public void testScoreLargePattern() {
        int MAX_GENOME_SIZE = 20000;
        int NUMBER_OF_GENOMES = 30000;
        int DATASET_LENGTH_SUM = MAX_GENOME_SIZE * NUMBER_OF_GENOMES;
        int PATTERN_LENGTH = 10000;
        int GENOMES_WITH_INSTANCE = 10000;
        int MAX_INSERTIONS = 0;
        int PARALOG_FREQUENCY = 1;

        PatternScore patternScore = new PatternScore(MAX_GENOME_SIZE, NUMBER_OF_GENOMES, DATASET_LENGTH_SUM, new HashMap<>(),
                new HashMap<>());

        double score = patternScore.pvalCrossGenome(PATTERN_LENGTH, MAX_INSERTIONS, PARALOG_FREQUENCY, GENOMES_WITH_INSTANCE);

        Assert.assertFalse(Double.isInfinite(score));
    }

    @Test
    public void testScoreSmallPattern() {
        int MAX_GENOME_SIZE = 4000;
        int NUMBER_OF_GENOMES = 1500;
        int DATASET_LENGTH_SUM = MAX_GENOME_SIZE * NUMBER_OF_GENOMES;
        int PATTERN_LENGTH = 2;
        int GENOMES_WITH_INSTANCE = 1000;
        int MAX_INSERTIONS = 10;
        int PARALOG_FREQUENCY = 1;

        PatternScore patternScore = new PatternScore(MAX_GENOME_SIZE, NUMBER_OF_GENOMES, DATASET_LENGTH_SUM, new HashMap<>(),
                new HashMap<>());

        double score = patternScore.pvalCrossGenome(PATTERN_LENGTH, MAX_INSERTIONS, PARALOG_FREQUENCY, GENOMES_WITH_INSTANCE);

        Assert.assertFalse(Double.isInfinite(score));
    }

    @Test
    public void testLargeNumOfInsertionsAndPattern() {
        int MAX_GENOME_SIZE = 50000;
        int NUMBER_OF_GENOMES = 1500;
        int DATASET_LENGTH_SUM = MAX_GENOME_SIZE * NUMBER_OF_GENOMES;
        int PATTERN_LENGTH = 10000;
        int GENOMES_WITH_INSTANCE = 1000;
        int MAX_INSERTIONS = 100;
        int PARALOG_FREQUENCY = 1;

        PatternScore patternScore = new PatternScore(MAX_GENOME_SIZE, NUMBER_OF_GENOMES, DATASET_LENGTH_SUM, new HashMap<>(),
                new HashMap<>());

        double score = patternScore.pvalCrossGenome(PATTERN_LENGTH, MAX_INSERTIONS, PARALOG_FREQUENCY, GENOMES_WITH_INSTANCE);

        Assert.assertFalse(Double.isInfinite(score));
        Assert.assertNotEquals(0, score);
    }

    /**
     *
     */
    @Test
    public void testAvgScore() {
        int MAX_GENOME_SIZE = 2000;
        int NUMBER_OF_GENOMES = 1000;
        int DATASET_LENGTH_SUM = MAX_GENOME_SIZE * NUMBER_OF_GENOMES;
        int PATTERN_LENGTH = 10;
        int GENOMES_WITH_INSTANCE = 10;
        int MAX_INSERTIONS = 0;
        int PARALOG_FREQUENCY = 1;
        double epsilon = 1;

        PatternScore patternScore = new PatternScore(MAX_GENOME_SIZE, NUMBER_OF_GENOMES, DATASET_LENGTH_SUM,
                new HashMap<>(),
                new HashMap<>());

        double score = patternScore.pvalCrossGenome(PATTERN_LENGTH, MAX_INSERTIONS, PARALOG_FREQUENCY, GENOMES_WITH_INSTANCE);
        Assert.assertEquals(607, score, epsilon);
    }

    @Test
    public void testNumOfGenomesCorrection() throws IOException {

        GenomesInfo gi = Parsers.parseGenomesFile(GENOMES_FILE_PATH, false);
        gi.computeDistancesBetweenGenomesAllVsAll();

        // all genomes are independent
        double delta = 1;
        PatternScore patternScore = new PatternScore(gi, delta);

        Assert.assertEquals(3, patternScore.getNumberOfGenomes());

        // any two genomes are considered similar
        delta = 0;
        patternScore = new PatternScore(gi, delta);

        Assert.assertEquals(1, patternScore.getNumberOfGenomes());

        delta = 0.6;
        patternScore = new PatternScore(gi, delta);

        Assert.assertEquals(2, patternScore.getNumberOfGenomes());

    }

    @Test
    public void testNumOfInstancesCorrection() throws IOException {

        GenomesInfo gi = Parsers.parseGenomesFile(GENOMES_FILE_PATH, false);
        gi.computeDistancesBetweenGenomesAllVsAll();

        double dist = gi.getGenomesDistance(0, 1);
        double epsilon = 0.001;
        Collection<Integer> genomeIds = Arrays.asList(0, 1);

        PatternScore patternScore = new PatternScore(gi, dist-epsilon);
        Assert.assertEquals(1, patternScore.calcCorrectedNumOfGenomes(genomeIds));

        patternScore = new PatternScore(gi, dist+epsilon);
        Assert.assertEquals(2, patternScore.calcCorrectedNumOfGenomes(genomeIds));

    }
}
