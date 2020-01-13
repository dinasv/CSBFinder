package mvc.view.components.shapes;

import java.awt.*;

/**
 */
public interface Shape {

    void draw(Graphics g);

    boolean containsPoint(Point point);

    Shape getShapeWithPoint(Point point);

    int getX();
}
