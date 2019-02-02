package MVC.View.Components.Panels;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;
import Core.Patterns.PatternLocationsInGenome;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenomePanelContainer extends JPanel {

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    GridBagConstraints gc;


    Map<String, Color> colorsUsed;

    public GenomePanelContainer(Map<String, Color> colorsUsed){

        this.colorsUsed = colorsUsed;

        setLayout(new GridBagLayout());
        setGCLayout();

        instancesPanel = new InstancesPanel(colorsUsed);

        labelsPanel = new InstancesLabelsPanel();

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.2; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(labelsPanel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.8; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(instancesPanel, gc);

    }

    public void displayPatterns(List<Pattern> patterns, int scrollWidth){

        List<String> patternNames = patterns.stream()
                                            .map(Pattern::getPatternId)
                                            .map(String::valueOf)
                                            .map(id -> "CSB " + id)
                                            .collect(Collectors.toList());

        instancesPanel.setData(patterns);
        labelsPanel.displayInstancesLabels(patternNames, instancesPanel.getFirstRowHeight(), instancesPanel.getFirstRowHeight());
        instancesPanel.showData(scrollWidth - labelsPanel.getPanelWidth());

        revalidate();
        repaint();
    }

    public void displayInstances(Pattern pattern, int scrollWidth, GenomesInfo genomesInfo, int numOfNeighbors) {

        List<String> genomeNames = new ArrayList<>();
        genomeNames.add("CSB");//first label

        genomeNames.addAll(pattern.getPatternLocations()
                .keySet()
                .stream()
                .map(key -> genomesInfo.getGenomeName(key))
                .collect(Collectors.toList()));

        instancesPanel.setData(pattern, genomesInfo, numOfNeighbors);
        labelsPanel.displayInstancesLabels(genomeNames, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        scrollWidth = scrollWidth-labelsPanel.getPanelWidth();
        instancesPanel.displayInstances(scrollWidth);

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
