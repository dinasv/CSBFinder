package MVC.View.Components.Shapes;

import MVC.View.Events.GeneTooltipEvent;
import MVC.View.Listeners.Listener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes;
    private List<List<GenesInstance>> shapesInstanceList;
    private int containersDist;
    private int panelHeight;

    private Listener<GeneTooltipEvent> geneTooltipListener;

    public ShapesPanel(List<List<GenesInstance>> shapesInstanceList, int containersDist, Color backgroundColor) {

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
            List<GenesInstance> repliconInstances = shapesInstanceList.get(0);
            if (repliconInstances.size() > 0) {
                panelHeight = repliconInstances.get(0).getHeight();
            }
        }
        this.setPreferredPanelSize(shapesInstanceList);
    }

    private void addShapes(){
        int diffRepliconInstanceCount = 0;
        int x = 0;

        for (List<GenesInstance> repliconInstances : shapesInstanceList) {
            diffRepliconInstanceCount++;
            int sameRepliconInstanceCount = 0;
            for (GenesInstance genesInstance : repliconInstances) {
                sameRepliconInstanceCount++;

                shapes.add(genesInstance);

                x = genesInstance.getX() + genesInstance.getWidth();

                // diagonal lines between two instances on the same replicon
                addDiagonalLine(x, sameRepliconInstanceCount, genesInstance, repliconInstances);

            }
            // parallel line between two instances in different replicons
            if (diffRepliconInstanceCount < shapesInstanceList.size()){
                addParallelLine(x);
            }
        }
        repaint();
    }

    private void addDiagonalLine(int x, int sameRepliconInstanceCount, GenesInstance genesInstance,
                                 List<GenesInstance> repliconInstances){

        int x1 = x + (int) (containersDist * 0.2);
        int y1 = genesInstance.getGeneEndY();
        int x2 = x + containersDist - (int) (containersDist * 0.2);
        int y2 = genesInstance.getGeneY();

        // diagonal lines between two instances on the same replicon
        if (sameRepliconInstanceCount < repliconInstances.size()) {
            shapes.add(new DiagLinesShape(x1, y1, x2, y2));
        }
    }

    private void addParallelLine(int x){
        int newX = x + (int) (containersDist * 0.4);

        shapes.add(new RectShape(newX, 0, 5, panelHeight, Color.black));
    }

    private void setPreferredPanelSize(List<List<GenesInstance>> shapesInstanceList){
        int width = 0;

        for (List<GenesInstance> repliconInstances : shapesInstanceList) {

            for (GenesInstance genesInstance : repliconInstances) {
                width += genesInstance.getWidth();
            }
            width += containersDist * (repliconInstances.size());

        }

        width -= containersDist /2;

        setPreferredSize(new Dimension(width, panelHeight));
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        for (Shape shape : shapes) {
            if (shape.containsPoint(event.getPoint())) {
                geneTooltipListener.eventOccurred(new GeneTooltipEvent(this, shape.getTooltip(event.getPoint())));
                return super.getToolTipText(event);
            }
        }
        return null;
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

    public void setGeneTooltipListener(Listener<GeneTooltipEvent> geneTooltipListener) {
        this.geneTooltipListener = geneTooltipListener;
    }

}
