package MVC.View.Components.Shapes;

import java.awt.*;

public class RectShape implements Shape{

    private int x, y;
    private int width, height;
    private Color color;

    public RectShape(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public void draw(Graphics g) {

        g.setColor(color);
        g.fillRect(x, y, width, height);

    }

}
