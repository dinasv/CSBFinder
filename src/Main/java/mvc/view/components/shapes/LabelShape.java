package mvc.view.components.shapes;

import java.awt.*;

public class LabelShape implements Shape{

    private int x, y;
    private Label label;

    public LabelShape(int x, int y, Label label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public void draw(Graphics g) {
        // Draw label
        g.setColor(label.getColor());
        g.setFont(label.getFont());

        g.drawString(label.getText(), x, y);

    }

    @Override
    public boolean containsPoint(Point point) {
        return false;
    }

    @Override
    public Shape getShapeWithPoint(Point point) {
        return this;
    }

    public int getLabelHeight(){
        return label.getHeight();
    }

    public int getLabelWidth(){
        return label.getWidth();
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public Label getLabel(){
        return label;
    }

    public int getX(){
        return x;
    }

}
