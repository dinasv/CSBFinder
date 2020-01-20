import model.genomes.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestReplicon {

    @Test
    public void testSplitToDirectons() {
        Replicon replicon = new Replicon("", 0, 0, Strand.FORWARD, true);
        replicon.addGene(new Gene("A", Strand.FORWARD));
        replicon.addGene(new Gene("B", Strand.FORWARD));
        replicon.addGene(new Gene("C", Strand.FORWARD));
        replicon.addGene(new Gene("D", Strand.FORWARD));

        List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

        Assert.assertEquals(1, directons.size());

        Directon directon = directons.get(0);
        Assert.assertEquals(0, directon.getStartIndex());
    }

    @Test
    public void testSplitToDirectonsCircular() {
        Replicon replicon = new Replicon("", 0, 0, Strand.FORWARD, true);
        replicon.addGene(new Gene("A", Strand.FORWARD));
        replicon.addGene(new Gene("B", Strand.REVERSE));
        replicon.addGene(new Gene("C", Strand.REVERSE));
        replicon.addGene(new Gene("D", Strand.FORWARD));

        List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

        Assert.assertEquals(2, directons.size());

        Directon firstDirecton = directons.get(0);
        Assert.assertEquals(2, firstDirecton.getGenes().size());
        Assert.assertEquals(2, firstDirecton.size());

        Directon secondDirecton = directons.get(1);
        Assert.assertEquals(2, secondDirecton.getGenes().size());
        Assert.assertEquals(1, secondDirecton.size());
    }
}
