package MVC.View.Shapes;

import java.awt.*;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes = new ArrayList<>();
    private int containers_dist;
    private int panelHeight;

    public ShapesPanel(List<ShapesInstance> shapesInstanceList, int containers_dist, Color backgroundColor) {
        this.containers_dist = containers_dist;
        shapes = new ArrayList<>();

        setBackground(backgroundColor);

        //set preferred container size
        panelHeight = 0;
        if (shapesInstanceList.size() > 0) {
            panelHeight = shapesInstanceList.get(0).getHeight();
        }

        this.setPreferredPanelSize(shapesInstanceList);

        //add shapes
        int counter = 0;
        for (ShapesInstance shapesInstance : shapesInstanceList) {
            counter ++;

            addShape(shapesInstance);

            if (counter < shapesInstanceList.size()) {
                int x = shapesInstance.getX() +  shapesInstance.getWidth();

                int x1 = x + (int) (containers_dist * 0.2);
                int y1 = shapesInstance.getGeneEndY();
                int x2 = x + containers_dist - (int) (containers_dist * 0.2);
                int y2 = shapesInstance.getGeneY();
                addDiagShape(x1, y1, x2, y2);
            }
        }

    }

    private void setPreferredPanelSize(List<ShapesInstance> shapesInstanceList){
        int width = 0;

        for (ShapesInstance shapesInstance : shapesInstanceList) {
            width += shapesInstance.getWidth();
        }

        width += containers_dist * (shapesInstanceList.size() - 1);
        setPreferredSize(new Dimension(width, panelHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Shape s : shapes) {
            s.draw(g);
        }

    }

    private void addShape(Shape shape){
        shapes.add(shape);
        repaint();
    }

    /*
    private void addGeneShape(ShapeParams shapeParams) {
        shapes.add(new GeneShape(shapeParams));
        repaint();
    }*/

    private void addDiagShape(int x1, int y1, int x2, int y2){
        shapes.add(new DiagLinesShape(x1, y1, x2, y2));
        repaint();
    }

    private void addLabel(int x, int y, Label label){
        shapes.add(new LabelShape(x, y, label));
        repaint();
    }

    public int getPanelHeight(){
        return panelHeight;
    }

}
