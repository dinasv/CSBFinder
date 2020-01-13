package mvc.view.events;

import mvc.view.components.shapes.GeneShape;
import mvc.view.components.shapes.Shape;
import model.genomes.Strand;

import javax.swing.*;

/**
 */
public class DoubleClickGeneEvent implements Event{

    private String cogId;
    private Strand strand;
    private JPanel src;


    private GeneShape anchorGene;

    public DoubleClickGeneEvent(JPanel src, Shape shape) {
        strand = Strand.INVALID;
        this.cogId = "";
        this.src = src;

        anchorGene = null;

        if (shape instanceof GeneShape){
            anchorGene = (GeneShape) shape;
        }
    }

    public String getCogId() {
        return cogId;
    }

    public JPanel getSrc() {
        return src;
    }

    public Strand getStrand() {
        return strand;
    }

    public GeneShape getAnchorGene() {
        return anchorGene;
    }

}
