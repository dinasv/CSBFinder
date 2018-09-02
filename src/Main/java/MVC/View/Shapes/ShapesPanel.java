package MVC.View.Shapes;

import java.awt.*;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes = new ArrayList<>();
    private int containers_dist;
    private int panelHeight;

    public ShapesPanel(List<List<ShapesInstance>> shapesInstanceList, int containers_dist, Color backgroundColor) {
        this.containers_dist = containers_dist;
        shapes = new ArrayList<>();

        setBackground(backgroundColor);

        //set preferred container size
        panelHeight = 0;
        if (shapesInstanceList.size() > 0) {
            List<ShapesInstance> repliconInstances = shapesInstanceList.get(0);
            if (repliconInstances.size() > 0) {
                panelHeight = repliconInstances.get(0).getHeight();
            }
        }

        this.setPreferredPanelSize(shapesInstanceList);

        //add shapes
        int i = 0;
        int x = 0;
        int x1, x2, y1, y2;

        for (List<ShapesInstance> repliconInstances : shapesInstanceList) {
            i++;
            int j = 0;
            for (ShapesInstance shapesInstance : repliconInstances) {
                j++;

                addShape(shapesInstance);

                x = shapesInstance.getX() + shapesInstance.getWidth();

                x1 = x + (int) (containers_dist * 0.2);
                y1 = shapesInstance.getGeneEndY();
                x2 = x + containers_dist - (int) (containers_dist * 0.2);
                y2 = shapesInstance.getGeneY();

                if (j < repliconInstances.size()) {
                    addShape(new DiagLinesShape(x1, y1, x2, y2));
                }
            }
            if (i < shapesInstanceList.size()){
                x1 = x + (int) (containers_dist * 0.4);

                addShape(new RectShape(x1, 0, 5, panelHeight, Color.black));
            }
        }

    }

    private void setPreferredPanelSize(List<List<ShapesInstance>> shapesInstanceList){
        int width = 0;

        for (List<ShapesInstance> repliconInstances : shapesInstanceList) {

            for (ShapesInstance shapesInstance: repliconInstances) {
                width += shapesInstance.getWidth();
            }
            width += containers_dist * (repliconInstances.size());

        }

        width -= containers_dist/2;

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


    public int getPanelHeight(){
        return panelHeight;
    }

}
