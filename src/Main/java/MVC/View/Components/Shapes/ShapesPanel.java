package MVC.View.Components.Shapes;

import java.awt.*;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes;
    private List<List<ShapesInstance>> shapesInstanceList;
    private int containersDist;
    private int panelHeight;

    public ShapesPanel(List<List<ShapesInstance>> shapesInstanceList, int containersDist, Color backgroundColor) {

        this.containersDist = containersDist;
        shapes = new ArrayList<>();
        this.shapesInstanceList = shapesInstanceList;

        setBackground(backgroundColor);

        setPanelSize();

        addShapes();
    }

    private void setPanelSize(){
        panelHeight = 0;
        if (shapesInstanceList.size() > 0) {
            List<ShapesInstance> repliconInstances = shapesInstanceList.get(0);
            if (repliconInstances.size() > 0) {
                panelHeight = repliconInstances.get(0).getHeight();
            }
        }
        this.setPreferredPanelSize(shapesInstanceList);
    }

    private void addShapes(){
        int diffRepliconInstanceCount = 0;
        int x = 0;

        for (List<ShapesInstance> repliconInstances : shapesInstanceList) {
            diffRepliconInstanceCount++;
            int sameRepliconInstanceCount = 0;
            for (ShapesInstance shapesInstance : repliconInstances) {
                sameRepliconInstanceCount++;

                shapes.add(shapesInstance);

                x = shapesInstance.getX() + shapesInstance.getWidth();

                // diagonal lines between two instances on the same replicon
                addDiagonalLine(x, sameRepliconInstanceCount, shapesInstance, repliconInstances);

            }
            // parallel line between two instances in different replicons
            if (diffRepliconInstanceCount < shapesInstanceList.size()){
                int x1 = x + (int) (containersDist * 0.4);

                shapes.add(new RectShape(x1, 0, 5, panelHeight, Color.black));
            }
        }
        repaint();
    }

    private void addDiagonalLine(int x, int sameRepliconInstanceCount, ShapesInstance shapesInstance,
                                 List<ShapesInstance> repliconInstances){

        int x1 = x + (int) (containersDist * 0.2);
        int y1 = shapesInstance.getGeneEndY();
        int x2 = x + containersDist - (int) (containersDist * 0.2);
        int y2 = shapesInstance.getGeneY();

        // diagonal lines between two instances on the same replicon
        if (sameRepliconInstanceCount < repliconInstances.size()) {
            shapes.add(new DiagLinesShape(x1, y1, x2, y2));
        }
    }

    private void setPreferredPanelSize(List<List<ShapesInstance>> shapesInstanceList){
        int width = 0;

        for (List<ShapesInstance> repliconInstances : shapesInstanceList) {

            for (ShapesInstance shapesInstance: repliconInstances) {
                width += shapesInstance.getWidth();
            }
            width += containersDist * (repliconInstances.size());

        }

        width -= containersDist /2;

        setPreferredSize(new Dimension(width, panelHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Shape s : shapes) {
            s.draw(g);
        }

    }

    public int getPanelHeight(){
        return panelHeight;
    }

}
