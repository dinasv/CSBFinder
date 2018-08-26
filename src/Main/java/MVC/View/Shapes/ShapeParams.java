package MVC.View.Shapes;

import Utils.Gene;

import java.awt.*;


/**
 */
public class ShapeParams {

    private int x;
    private int y;

    private ShapeDimensions shapeDimensions;

    private Color color;

    private Label label;

    private String strand;

    public ShapeParams(int x, int y, Color color, ShapeDimensions shapeDimensions, Gene gene){
        this.x = x;
        this.y = y;
        this.color = color;
        this.label = new Label(gene.getCog_id());

        this.shapeDimensions = shapeDimensions;

        this.strand = gene.getStrand();

    }

    public ShapeParams(int x, int y, Color color, Gene gene){
        this(x, y, color, new ShapeDimensions(), gene);
    }

    public ShapeParams(Color color, Gene gene){
        this(0, 0, color, gene);

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
        return shapeDimensions;
    }

    public void setShapeDimensions(ShapeDimensions shapeDimensions) {
        this.shapeDimensions = shapeDimensions;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }
}
