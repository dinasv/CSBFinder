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
    public void testSplitToDirectonsX() {

        Replicon replicon1 = new Replicon("", 0, 0, Strand.REVERSE, false);
        replicon1.addGene(new Gene("X", Strand.REVERSE));
        replicon1.addGene(new Gene("B", Strand.REVERSE));
        replicon1.addGene(new Gene("C", Strand.REVERSE));
        replicon1.addGene(new Gene("X", Strand.REVERSE));

        Replicon replicon2 = new Replicon("", 0, 0, Strand.REVERSE, false);
        replicon2.addGene(new Gene("X", Strand.FORWARD));
        replicon2.addGene(new Gene("B", Strand.REVERSE));
        replicon2.addGene(new Gene("C", Strand.REVERSE));
        replicon2.addGene(new Gene("X", Strand.REVERSE));

        Replicon replicon3 = new Replicon("", 0, 0, Strand.REVERSE, false);
        replicon3.addGene(new Gene("X", Strand.REVERSE));
        replicon3.addGene(new Gene("B", Strand.REVERSE));
        replicon3.addGene(new Gene("C", Strand.REVERSE));
        replicon3.addGene(new Gene("X", Strand.FORWARD));

        Replicon[] replicons = {replicon1, replicon2, replicon3};

        for (Replicon replicon :
                replicons) {

            List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

            Assert.assertEquals(1, directons.size());

            Directon directon = directons.get(0);
            Assert.assertEquals(1, directon.getStartIndex());
            Assert.assertEquals(2, directon.size());
        }

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
