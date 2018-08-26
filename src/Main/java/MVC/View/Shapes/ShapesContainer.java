package MVC.View.Shapes;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 */
public class ShapesContainer extends JPanel{

    private List<ShapeParams> shapeParamsList;
    private int x;
    private int y;
    //the distance between shapes
    private int DIST_SHAPES = 10;
    private Dimension containerDimensions;

    public ShapesContainer(List<ShapeParams> shapeParamsList, int x, int y){

        this.shapeParamsList = shapeParamsList;
        this.x = x;
        this.y = y;

        init();

    }

    private void init(){

        int curr_x = x;

        ShapeDimensions sd = null;
        for (ShapeParams shapeParams : shapeParamsList) {
            shapeParams.setX(curr_x);
            shapeParams.setY(y);

            sd = shapeParams.getShapeDimensions();
            curr_x += sd.getSquareWidth() + sd.getArrowWidth() + DIST_SHAPES;
        }

        int height = 0;
        if (sd != null) {
            height = sd.getSquareHeight();
        }
        containerDimensions = new Dimension(curr_x-x-DIST_SHAPES, height);
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public List<ShapeParams> getShapeParamsList(){
        return shapeParamsList;
    }

    public Dimension getContainerDimensions(){
        return containerDimensions;
    }

}
