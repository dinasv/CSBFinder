package MVC.View.Components.Shapes;

/**
 * Dimensions of the square with an arrow
 */
public class ShapeDimensions {

    private int rectWidth;
    private int arrowWidth;
    private int rectHeight;
    private int arrowHeight;

    public ShapeDimensions(int rectWidth, int arrowWidth, int rectHeight){

        this.rectWidth = rectWidth;
        this.arrowWidth = arrowWidth;
        this.rectHeight = rectHeight;
        arrowHeight = rectHeight / 2;
    }

    public ShapeDimensions(){
        this(100, 30, 60);
    }

    public int getRectWidth() {
        return rectWidth;
    }

    public void setRectWidth(int rectWidth) {
        this.rectWidth = rectWidth;
    }

    public int getArrowWidth() {
        return arrowWidth;
    }

    public void setArrowWidth(int arrowWidth) {
        this.arrowWidth = arrowWidth;
    }

    public int getRectHeight() {
        return rectHeight;
    }

    public void setRectHeight(int rectHeight) {
        this.rectHeight = rectHeight;
    }

    public int getArrowHeight() {
        return arrowHeight;
    }

    public void setArrowHeight(int arrowHeight) {
        this.arrowHeight = arrowHeight;
    }
}
