package MVC.View.Shapes;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class GeneShape implements Shape{

    private int x, y;

    private int rectWidth;
    private int arrowWidth;
    private int rectHeight;
    private int arrowHeight;
    private Color color;
    private Label label;
    private String strand;

    public GeneShape(ShapeParams shapeParams) {
            this.x = shapeParams.getX();
            this.y = shapeParams.getY();

            this.rectWidth = shapeParams.getShapeDimensions().getSquareWidth();
            this.arrowWidth = shapeParams.getShapeDimensions().getArrowWidth();
            this.rectHeight = shapeParams.getShapeDimensions().getSquareHeight();
            this.color = shapeParams.getColor();

            arrowHeight = rectHeight / 2;

            this.label = shapeParams.getLabel();
            this.strand = shapeParams.getStrand();

    }

    public void draw(Graphics g) {

        int rectStartX = x;
        int arrowStartX = x + rectWidth;
        int arrowEndX = x + rectWidth + arrowWidth;

        if (strand.equals("-")){
            rectStartX = x + arrowWidth;
            arrowStartX =  x + arrowWidth;
            arrowEndX = x;
        }

        // Draw gene
        int[] xPoints = {arrowStartX, arrowEndX, arrowStartX};
        int[] yPoints = {y, y+ rectHeight /2, y+ rectHeight};
        g.setColor(color);
        g.fillRect(rectStartX, y, rectWidth, rectHeight);
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw label
        g.setColor(label.getColor());
        g.setFont(label.getFont());

        int labelWidth = g.getFontMetrics().stringWidth(label.getText());
        int labelPositionX = x + (rectWidth - labelWidth)/2;
        if (strand.equals("-")){
            labelPositionX += arrowWidth;
        }
        g.drawString(label.getText(), labelPositionX, y+arrowHeight+5);
    }

}
