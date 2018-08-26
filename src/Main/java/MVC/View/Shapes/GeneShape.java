package MVC.View.Shapes;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class GeneShape implements Shape{

    private int x, y;

    private int squareWidth;
    private int arrowWidth;
    private int squareHeight;
    private int arrowHeight;
    private Color color;
    private Label label;
    private String strand;

    public GeneShape(ShapeParams shapeParams) {
            this.x = shapeParams.getX();
            this.y = shapeParams.getY();

            this.squareWidth = shapeParams.getShapeDimensions().getSquareWidth();
            this.arrowWidth = shapeParams.getShapeDimensions().getArrowWidth();
            this.squareHeight = shapeParams.getShapeDimensions().getSquareHeight();
            this.color = shapeParams.getColor();

            arrowHeight = squareHeight / 2;

            this.label = shapeParams.getLabel();
            this.strand = shapeParams.getStrand();

    }

    public void draw(Graphics g) {

        int[] xPoints = {squareWidth, squareWidth+arrowWidth, squareWidth, 0};
        int[] yPoints = {0, arrowHeight, squareHeight, squareHeight};

        if (strand.equals("-")){
            int[] xPointsNew = {0, arrowWidth, squareWidth+arrowWidth, squareWidth+arrowWidth, arrowWidth, 0};
            int[] yPointsNew = {arrowHeight, 0, 0, squareHeight, squareHeight, arrowHeight};

            xPoints = xPointsNew;
            yPoints = yPointsNew;
        }

        GeneralPath geneShapePath = new GeneralPath();

        geneShapePath.moveTo(x , y);
        for (int i = 0; i < xPoints.length; i++) {
            geneShapePath.lineTo(xPoints[i] + x, yPoints[i] + y);
        }
        geneShapePath.closePath();

        Graphics2D g2d = (Graphics2D) g;
        g.setColor(color);
        g2d.fill(geneShapePath);

        g2d.setColor(label.getColor());
        g2d.setFont(label.getFont());

        int labelWidth = g2d.getFontMetrics().stringWidth(label.getText());
        int labelPositionX = x + (squareWidth - labelWidth)/2;
        if (strand.equals("-")){
            labelPositionX += arrowWidth;
        }
        g2d.drawString(label.getText(), labelPositionX, y+arrowHeight+5);
    }

}
