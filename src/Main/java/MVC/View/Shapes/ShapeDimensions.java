package MVC.View.Shapes;

/**
 * Dimensions of the square with an arrow
 */
public class ShapeDimensions {

    private int squareWidth;
    private int arrowWidth;
    private int squareHeight;

    public ShapeDimensions(int squareWidth, int arrowWidth, int squareHeight){

        this.squareWidth = squareWidth;
        this.arrowWidth = arrowWidth;
        this.squareHeight = squareHeight;
    }

    public ShapeDimensions(){
        this(100, 30, 60);
    }

    public int getSquareWidth() {
        return squareWidth;
    }

    public void setSquareWidth(int squareWidth) {
        this.squareWidth = squareWidth;
    }

    public int getArrowWidth() {
        return arrowWidth;
    }

    public void setArrowWidth(int arrowWidth) {
        this.arrowWidth = arrowWidth;
    }

    public int getSquareHeight() {
        return squareHeight;
    }

    public void setSquareHeight(int squareHeight) {
        this.squareHeight = squareHeight;
    }
}
