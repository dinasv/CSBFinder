package MVC.View.Components.Panels;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Events.DoubleClickGeneEvent;
import MVC.View.Graphics.GeneColors;
import Model.Genomes.GenomesInfo;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import MVC.View.Events.TooltipGeneEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenesViewPanel extends JPanel {

    private JPanel content;
    private JScrollPane scroll;

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    private GridBagConstraints gc;

    private GenomesInfo genomesInfo;

    private ViewMode viewMode;

    private Pattern patternInView;
    private List<Pattern> patternsInView;

    private List<Map.Entry<String, List<InstanceLocation>>> genomeToInstances;
    private List<String> genomeNames;

    public GenesViewPanel(GeneColors colorsUsed){

        viewMode = ViewMode.NONE;

        genomesInfo = null;
        patternInView = null;
        patternsInView = null;
        genomeToInstances = new ArrayList<>();
        genomeNames = new ArrayList<>();

        content = new JPanel(new GridBagLayout());

        setLayout(new BorderLayout());

        instancesPanel = new InstancesPanel(colorsUsed);
        labelsPanel = new InstancesLabelsPanel();

        gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.2; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        content.add(labelsPanel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.8; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        content.add(instancesPanel, gc);

        scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

    }

    public void setGenomesInfo(GenomesInfo genomesInfo){
        this.genomesInfo = genomesInfo;
        instancesPanel.setGenomesInfo(genomesInfo);
    }

    public void setNumOfNeighbors(int numOfNeighbors){
        instancesPanel.setNumOfNeighbors(numOfNeighbors);

        if (viewMode == ViewMode.INSTANCES){
            instancesPanel.setData(patternInView, genomeToInstances);
            instancesPanel.displayGenes();
        }

    }

    public void displayPatterns(List<Pattern> patterns){

        viewMode = ViewMode.PATTERNS;
        patternsInView = patterns;

        List<String> patternNames = patterns.stream()
                                            .map(Pattern::getPatternId)
                                            .map(String::valueOf)
                                            .map(id -> "CSB " + id)
                                            .collect(Collectors.toList());

        instancesPanel.setData(patterns);
        labelsPanel.displayInstancesLabels(patternNames, instancesPanel.getFirstRowHeight(),
                instancesPanel.getFirstRowHeight());

        instancesPanel.displayGenes(calcInstancesScrollWidth());

        content.revalidate();
        content.repaint();
    }

    public void displayInstances(Pattern pattern) {

        viewMode = ViewMode.INSTANCES;
        patternInView = pattern;

        genomeToInstances = pattern.getPatternLocations().getSortedLocations().stream()
                .collect(Collectors.groupingBy(location -> genomesInfo.getGenomeName(location.getGenomeId())))
                .entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        genomeNames = new ArrayList<>();
        genomeNames.addAll(genomeToInstances
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        instancesPanel.setData(pattern, genomeToInstances);

        List<String> labels = new ArrayList<>();
        labels.add("CSB");//first label
        labels.addAll(genomeNames);
        labelsPanel.displayInstancesLabels(labels, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        instancesPanel.displayGenes(calcInstancesScrollWidth());

        content.revalidate();
        content.repaint();
    }


    public List<String> getGenomeNames(){
        return genomeNames;
    }

    private int calcInstancesScrollWidth(){
        int scrollWidth = scroll.getViewport().getSize().width;
        return scrollWidth - labelsPanel.getPanelWidth();
    }

    public void clearPanel(){
        genomeToInstances.clear();
        genomeNames.clear();
        instancesPanel.clearPanel();
        labelsPanel.clearPanel();
    }


    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        this.instancesPanel.setGeneTooltipListener(geneTooltipListener);
    }

    public void setGeneDoubleClickListener(Listener<DoubleClickGeneEvent> geneTooltipListener) {
        this.instancesPanel.setDoubleClickListener(geneTooltipListener);
    }

    public void alignGenes(GeneShape anchorGene, int viewX){
        instancesPanel.alignPanels(anchorGene, viewX);
    }

    public void zoomOut(int zoomUnit){
        clearPanel();
        instancesPanel.zoomOut(zoomUnit);

        diplayView();
    }


    public void zoomIn(int zoomUnit){
        clearPanel();
        instancesPanel.zoomIn(zoomUnit);
        diplayView();
    }

    private void diplayView(){
        if (viewMode == ViewMode.INSTANCES){
            displayInstances(patternInView);
        }else if (viewMode == ViewMode.PATTERNS){
            displayPatterns(patternsInView);
        }
    }


    private enum ViewMode {
        PATTERNS,
        INSTANCES,
        NONE
    }
}
