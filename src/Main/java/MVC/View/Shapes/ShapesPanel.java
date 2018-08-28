package MVC.View.Shapes;

import java.awt.*;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes = new ArrayList<>();

    public ShapesPanel(List<ShapesContainer> shapesContainers, int CONTAINERS_DIST, Color backgroundColor) {
        shapes = new ArrayList<>();

        setBackground(backgroundColor);

        int width = 0;
        int height = 0;
        if (shapesContainers.size() > 0){
            height = (int)shapesContainers.get(0).getContainerDimensions().getHeight();
            for (ShapesContainer shapesContainer: shapesContainers) {
                width += shapesContainer.getContainerDimensions().getWidth();
            }
        }
        width += CONTAINERS_DIST*shapesContainers.size()-CONTAINERS_DIST;
        setPreferredSize(new Dimension(width, height));

        int counter = 0;
        for (ShapesContainer shapesContainer: shapesContainers) {
            counter ++;

            for (ShapeParams shapeParams : shapesContainer.getShapeParamsList()) {
                addGeneShape(shapeParams);
            }

            if (counter < shapesContainers.size()) {
                int x = shapesContainer.getX() + (int) shapesContainer.getContainerDimensions().getWidth();
                int y = shapesContainer.getY();

                int x1 = x + (int) (CONTAINERS_DIST * 0.2);
                int y1 = y + (int) (shapesContainer.getContainerDimensions().getHeight() * 0.7);
                int x2 = x + CONTAINERS_DIST - (int) (CONTAINERS_DIST * 0.2);
                int y2 = y + (int) (shapesContainer.getContainerDimensions().getHeight() * 0.2);
                addDiagShape(x1, y1, x2, y2);
            }
        }

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Shape s : shapes) {
            s.draw(g);
        }
    }

    public void addGeneShape(ShapeParams shapeParams) {
        shapes.add(new GeneShape(shapeParams));
        repaint();
    }

    public void addDiagShape(int x1, int y1, int x2, int y2){
        shapes.add(new DiagLinesShape(x1, y1, x2, y2));
        repaint();
    }

}
