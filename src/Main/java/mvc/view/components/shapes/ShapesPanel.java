package mvc.view.components.shapes;

import mvc.view.events.DoubleClickGeneEvent;
import mvc.view.events.TooltipGeneEvent;
import mvc.view.listeners.Listener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import java.util.ArrayList;

import javax.swing.*;

public class ShapesPanel extends JPanel{

    private List<Shape> shapes;
    private List<List<GenesInstance>> shapesInstanceList;
    private int containersDist;
    private int panelHeight;
    private int alignmentPadding;

    private Listener<TooltipGeneEvent> geneTooltipListener;
    private Listener<DoubleClickGeneEvent> doubleClickListener;

    public ShapesPanel(List<List<GenesInstance>> shapesInstanceList, int containersDist, Color backgroundColor,
                       int alignmentPadding) {

        this.containersDist = containersDist;
        shapes = new ArrayList<>();
        this.shapesInstanceList = shapesInstanceList;
        this.alignmentPadding = alignmentPadding;

        setBackground(backgroundColor);

        setPanelSize();

        addShapes();

        addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2){
                    doubleClickEventOccured(e);
                }
            }
        });
    }

    private void doubleClickEventOccured(MouseEvent e){
        for (Shape shape : shapes) {
            if (shape.containsPoint(e.getPoint())) {
                doubleClickListener.eventOccurred(
                        new DoubleClickGeneEvent(this, shape.getShapeWithPoint(e.getPoint())));
                System.out.println(e.getPoint().y);
                break;
            }
        }
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
        revalidate();
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
        width += 2*alignmentPadding;

        setPreferredSize(new Dimension(width, panelHeight));
    }



    @Override
    public String getToolTipText(MouseEvent event) {
        tooltipGeneEventOccured(event, geneTooltipListener);
        return super.getToolTipText(event);
        //return null;
    }

    private void tooltipGeneEventOccured(MouseEvent event, Listener<TooltipGeneEvent> listener){
        for (Shape shape : shapes) {
            if (shape.containsPoint(event.getPoint())) {
                listener.eventOccurred(new TooltipGeneEvent(this, shape.getShapeWithPoint(event.getPoint())));
                break;
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

    public int getPanelHeight(){
        return panelHeight;
    }

    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        this.geneTooltipListener = geneTooltipListener;
    }

    public void setDoubleClickListener(Listener<DoubleClickGeneEvent> doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

    public GeneShape getGeneShapeWithLabel(String anchorGeneLabel){
        if (shapesInstanceList.size() == 0 || shapesInstanceList.get(0).size() == 0) {
            return null;
        }

        GenesInstance instance = shapesInstanceList.get(0).get(0);
        return instance.getGeneShapeWithLabel(anchorGeneLabel);

    }

    public void reverse(){
        for (List<GenesInstance> instances : shapesInstanceList){
            for (GenesInstance genesInstance : instances){
                genesInstance.reverse();
            }
        }

        shapes.clear();

        addShapes();

    }


}
