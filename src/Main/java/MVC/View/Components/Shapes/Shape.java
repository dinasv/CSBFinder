package MVC.View.Components.Shapes;

import java.awt.*;

/**
 */
public interface Shape {

    void draw(Graphics g);

    boolean containsPoint(Point point);

    Shape getShapeWithPoint(Point point);

    int getX();
}
