import IO.Parsers;
import Model.Genomes.GenomesInfo;
import org.junit.Assert;
import org.junit.Test;

public class GenomeParsingTest {
    private final String GENOMES_FILE_PATH = this.getClass().getResource("/genomes.fasta").getPath();

    @Test
    public void testRefGenomesEqualOutput() throws Exception {

        GenomesInfo gi = Parsers.parseGenomesFile(GENOMES_FILE_PATH);
        gi.computeDistancesBetweenGenomesAllVsAll();

        double DELTA = 0.001;
        Assert.assertEquals(0.8, gi.getGenomesDistance(0, 1), DELTA);
    }
}
