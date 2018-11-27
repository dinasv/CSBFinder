package MVC.View.Components.Shapes;

import Core.Genomes.Gene;
import Core.Genomes.Strand;

import java.awt.*;

public class GeneShape implements Shape{

    private int x, y;

    private ShapeDimensions dim;

    private Color color;
    private Label label;
    private Strand strand;

    private int width;
    private int height;

    public GeneShape(int x, int y, Color color, ShapeDimensions dim, Gene gene) {
            this.x = x;
            this.y = y;

            this.dim = dim;
            this.color = color;

            this.label = new Label(gene.getCogId());
            this.strand = gene.getStrand();

            width = dim.getArrowWidth() + dim.getRectWidth();
            height = dim.getRectHeight();
    }

    public void draw(Graphics g) {

        int rectStartX = x;
        int arrowStartX = x + dim.getRectWidth();
        int arrowEndX = x + dim.getRectWidth() + dim.getArrowWidth();

        if (strand == Strand.REVERSE){
            rectStartX = x + dim.getArrowWidth();
            arrowStartX =  x + dim.getArrowWidth();
            arrowEndX = x;
        }

        // Draw gene
        int[] xPoints = {arrowStartX, arrowEndX, arrowStartX};
        int[] yPoints = {y, y + dim.getRectHeight() /2, y + dim.getRectHeight()};
        g.setColor(color);
        g.fillRect(rectStartX, y, dim.getRectWidth(), dim.getRectHeight());
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw label

        g.setColor(label.getColor());
        g.setFont(label.getFont());

        int labelWidth = g.getFontMetrics().stringWidth(label.getText());
        int labelPositionX = x + (dim.getRectWidth() - labelWidth)/2;
        if (strand == Strand.REVERSE){
            labelPositionX += dim.getArrowWidth();
        }

        g.drawString(label.getText(), labelPositionX, y + dim.getArrowHeight()+5);
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }



    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ShapeDimensions getShapeDimensions() {
        return dim;
    }

    public void setShapeDimensions(ShapeDimensions shapeDimensions) {
        this.dim = shapeDimensions;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Strand getStrand() {
        return strand;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }

}
