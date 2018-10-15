package MVC.View.Panels;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GenomePanelContainer  extends JPanel {

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    GridBagConstraints gc;

    private final ShapeDimensions geneShapeDim = new ShapeDimensions(60, 15, 30);

    Map<String, Color> colorsUsed;

    public GenomePanelContainer(Map<String, Color> colorsUsed){

        this.colorsUsed = colorsUsed;

        setLayout(new GridBagLayout());
        setGCLayout();

        instancesPanel = new InstancesPanel(geneShapeDim, colorsUsed);

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

    public void displayInstances(String[] pattenCOGs, Map<String, Map<String,List<InstanceInfo>>> instances, int scrollWidth) {

        List<String> genomeNames = new ArrayList<>();
        List<Map<String,List<InstanceInfo>>> instancesList = new ArrayList<>();
        for(Map.Entry<String, Map<String, List<InstanceInfo>>> entry: instances.entrySet()){
            genomeNames.add(entry.getKey());
            instancesList.add(entry.getValue());
        }

        instancesPanel.displayInstances(pattenCOGs, instancesList, scrollWidth-InstancesLabelsPanel.GENOME_NAME_WIDTH);
        labelsPanel.displayInstancesLabels(genomeNames, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        revalidate();
        repaint();
    }

    public void clearPanel(){
        instancesPanel.clearPanel();
        labelsPanel.clearPanel();
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public Map<String,Color> getColorsUsed(){
        return instancesPanel.getColorsUsed();
    }
}
