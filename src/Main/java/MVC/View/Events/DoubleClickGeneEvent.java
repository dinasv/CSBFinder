package MVC.View.Events;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Components.Shapes.Shape;
import Model.Genomes.Strand;

import javax.swing.*;
import java.awt.*;

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
