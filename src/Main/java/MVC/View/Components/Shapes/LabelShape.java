package MVC.View.Components.Shapes;

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

        //int labelWidth = g.getFontMetrics().stringWidth(label.getText());

        g.drawString(label.getText(), x, y);

    }

    public int getLabelHeight(Graphics g){
        return g.getFontMetrics().getHeight();
    }

    public int getLabelWidth(Graphics g){
        return g.getFontMetrics().stringWidth(label.getText());
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

}
