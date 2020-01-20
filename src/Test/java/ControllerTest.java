import model.Controller;
import org.junit.Assert;
import org.junit.Test;

public class ControllerTest {

    private final String PLASMID_GENOMES_FILE_PATH = this.getClass().getResource("/plasmid_genomes.fasta").getPath();

    @Test
    public void testControllerPrintedCSBs() {
        String[] args = {"-in", PLASMID_GENOMES_FILE_PATH,
                "-q", "30",
                "-alg", "match_points",
                "--cross-strand",
                "-debug"};
        int numOfCsbs = 56;

        Controller controller = new Controller(args);

        Assert.assertEquals(numOfCsbs, controller.getPrintedCSBs());
    }
}
