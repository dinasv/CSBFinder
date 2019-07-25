package MVC.View.Components.Shapes;

import Model.Genomes.Gene;
import Model.Genomes.Strand;

import java.awt.*;

public class GeneShape implements Shape{

    private final static int PADDING_SMALL = 5;
    private final static int PADDING_MEDIUM = 20;
    private final static int ARROW_WIDTH = 30;

    private int x, y;

    private Color color;
    private Label label;
    private Strand strand;

    private int width;
    private int height;

    private int labelWidth;
    private int labelHeight;

    private int rectWidth;

    private Graphics graphics;

    public GeneShape(Color color, Gene gene, Graphics graphics, Font font) {
        this(0, 0, color, gene, graphics, font);
    }

    public GeneShape(int x, int y, Color color, Gene gene, Graphics graphics, Font font) {
        this.x = x;
        this.y = y;

        this.color = color;
        this.graphics = graphics;

        this.label = new Label(gene.getCogId(), font, graphics);
        this.strand = gene.getStrand();

        calculateDimensions();

    }

    private void calculateDimensions(){

        labelWidth = label.getWidth();
        labelHeight = label.getHeight();

        rectWidth = labelWidth + PADDING_MEDIUM;

        height = labelHeight + PADDING_MEDIUM;
        width = ARROW_WIDTH + rectWidth;
    }

    public void draw(Graphics g) {

       drawGene(g);
       drawLabel(g);

    }

    @Override
    public boolean containsPoint(Point point) {
        return point.x >= x && point.x <= x+width && point.y >= y && point.y <= y + height;
    }

    @Override
    public Shape getShapeWithPoint(Point point) {
        return this;
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

    public String toString(){
        return label.getText() + strand.toString();
    }
}
