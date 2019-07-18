package MVC.View.Components.Shapes;

import java.awt.*;
import java.awt.geom.Line2D;

public class DiagLinesShape implements Shape{

    private int x1, y1, x2, y2;
    private int DIST = 5;

    public DiagLinesShape(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void draw(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        g2d.draw(new Line2D.Double(x1 + DIST, y1+ DIST, x2+ DIST, y2+ DIST));

    }

    @Override
    public boolean containsPoint(Point point) {
        return point.x >= x1 && point.x <= x2+ DIST && point.y >= y1 && point.y <= y2+ DIST;
    }

    @Override
    public Shape getShapeWithPoint(Point point) {
        return this;
    }

    public int getX(){
        return x1;
    }

}
