package MVC.View.Components.Shapes;

import Core.Genomes.Gene;
import Core.Genomes.Strand;

import java.awt.*;

public class GeneShape implements Shape{

    final static int PADDING_SMALL = 5;
    final static int PADDING_MEDIUM = 20;
    final static int PADDING_LARGE = 50;
    final static int ARROW_WIDTH = 30;

    private int x, y;

    private Color color;
    private Label label;
    private Strand strand;

    private int width;
    private int height;

    int labelWidth;
    int labelHeight;

    int rectWidth;

    public GeneShape(int x, int y, Color color, Gene gene, Graphics graphics) {
        this.x = x;
        this.y = y;

        this.color = color;

        this.label = new Label(gene.getCogId());
        this.strand = gene.getStrand();

        graphics.setFont(label.getFont());
        labelWidth = graphics.getFontMetrics().stringWidth(label.getText());
        labelHeight = graphics.getFontMetrics().getAscent();

        rectWidth = labelWidth + PADDING_MEDIUM;

        height = labelHeight + PADDING_MEDIUM;
        width = ARROW_WIDTH + rectWidth;

    }

    public void draw(Graphics g) {

       drawGene(g);
       drawLabel(g);

    }

    private void drawGene(Graphics g){
        int rectStartX = x;
        int arrowStartX = x + rectWidth;
        int arrowEndX = x + width;

        if (strand == Strand.REVERSE){
            rectStartX = x + ARROW_WIDTH;
            arrowStartX =  x + ARROW_WIDTH;
            arrowEndX = x;
        }

        int[] xPoints = {arrowStartX, arrowEndX, arrowStartX};
        int[] yPoints = {y, y + height /2, y + height};
        g.setColor(color);
        g.fillRect(rectStartX, y, rectWidth, height);
        g.fillPolygon(xPoints, yPoints, 3);

    }

    private void drawLabel(Graphics g){
        g.setColor(label.getColor());
        g.setFont(label.getFont());

        int labelPositionX = x + (rectWidth-labelWidth)/2;
        if (strand == Strand.REVERSE){
            labelPositionX += ARROW_WIDTH;
        }

        g.drawString(label.getText(), labelPositionX, y + height/2 + PADDING_SMALL);
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
