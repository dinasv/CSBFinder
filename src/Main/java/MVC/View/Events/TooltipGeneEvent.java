package MVC.View.Events;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Components.Shapes.Shape;

import javax.swing.*;

/**
 */
public class TooltipGeneEvent implements Event{

    private String cogId;
    private JPanel src;

    public TooltipGeneEvent(JPanel src, Shape shape) {

        this.cogId = "";
        if (shape instanceof GeneShape){
            GeneShape gene = (GeneShape) shape;
            this.cogId = gene.getLabel().getText();
        }

        this.src = src;
    }

    public String getCogId() {
        return cogId;
    }

    public JPanel getSrc() {
        return src;
    }

}
