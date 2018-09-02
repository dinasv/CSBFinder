package MVC.View;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class GenomePanelContainer  extends JPanel {

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    GridBagConstraints gc;

    private final ShapeDimensions geneShapeDim = new ShapeDimensions(60, 15, 30);

    public GenomePanelContainer(){

        setLayout(new GridBagLayout());
        setGCLayout();

        instancesPanel = new InstancesPanel(geneShapeDim);

        labelsPanel = new InstancesLabelsPanel();

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.2; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(labelsPanel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.8; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(instancesPanel, gc);

        /*
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(labelsPanel);
        setRightComponent(instancesPanel);
        setResizeWeight(0.2);*/
    }

    public void displayInstances(String[] pattenCOGs, Map<String,List<InstanceInfo>> instances, int scrollWidth) {

        instancesPanel.displayInstances(pattenCOGs,instances, scrollWidth-InstancesLabelsPanel.GENOME_NAME_WIDTH);
        labelsPanel.displayInstancesLabels(instances, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        revalidate();
        repaint();
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public Map<String,Color> getColorsUsed(){
        return instancesPanel.getColorsUsed();
    }
}
