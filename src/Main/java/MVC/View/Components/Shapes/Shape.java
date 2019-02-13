package MVC.View.Components.Shapes;

import java.awt.*;

/**
 */
public interface Shape {

    void draw(Graphics g);

    boolean containsPoint(Point point);

    String getTooltip(Point point);
}
