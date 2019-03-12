import Model.Patterns.PatternScore;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 */
public class ScoreTest {
    /**
     * Test if the score for large values is not infinity
     * @throws Exception
     */
    @Test
    public void testScoreLargePattern() throws Exception {
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
}
