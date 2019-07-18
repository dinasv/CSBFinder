package MVC.View.Components.Panels;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Events.DoubleClickGeneEvent;
import Model.Genomes.GenomesInfo;
import Model.Genomes.Strand;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import MVC.View.Events.TooltipGeneEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GenomePanelContainer extends JPanel {

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    private GridBagConstraints gc;

    private Map<String, Color> colorsUsed;
    private GenomesInfo genomesInfo;

    private ViewMode viewMode;
    private int scrollWidth;
    private Pattern patternInView;
    private List<Map.Entry<String, List<InstanceLocation>>> genomeToInstances;

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

        viewMode = ViewMode.NONE;

        genomesInfo = null;
        patternInView = null;
        genomeToInstances = new ArrayList<>();

        scrollWidth = 0;
    }

    public void setGenomesInfo(GenomesInfo genomesInfo){
        this.genomesInfo = genomesInfo;
        instancesPanel.setGenomesInfo(genomesInfo);
    }

    public void setNumOfNeighbors(int numOfNeighbors){
        instancesPanel.setNumOfNeighbors(numOfNeighbors);

        if (viewMode == ViewMode.INSTANCES){
            instancesPanel.setData(patternInView, genomeToInstances);
            instancesPanel.displayInstances(scrollWidth);
        }

    }

    public void displayPatterns(List<Pattern> patterns, int scrollWidth){
        viewMode = ViewMode.PATTERNS;
        List<String> patternNames = patterns.stream()
                                            .map(Pattern::getPatternId)
                                            .map(String::valueOf)
                                            .map(id -> "CSB " + id)
                                            .collect(Collectors.toList());

        instancesPanel.setData(patterns);
        labelsPanel.displayInstancesLabels(patternNames, instancesPanel.getFirstRowHeight(),
                instancesPanel.getFirstRowHeight());

        this.scrollWidth = scrollWidth - labelsPanel.getPanelWidth();
        instancesPanel.showData(this.scrollWidth);

        revalidate();
        repaint();
    }

    public void displayInstances(Pattern pattern, int scrollWidth) {

        viewMode = ViewMode.INSTANCES;
        patternInView = pattern;

        List<String> genomeNames = new ArrayList<>();
        genomeNames.add("CSB");//first label

        genomeToInstances = pattern.getPatternLocations().getSortedLocations().stream()
                .collect(Collectors.groupingBy(location -> genomesInfo.getGenomeName(location.getGenomeId())))
                .entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toList());

        genomeNames.addAll(genomeToInstances
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        instancesPanel.setData(pattern, genomeToInstances);
        labelsPanel.displayInstancesLabels(genomeNames, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        this.scrollWidth = scrollWidth-labelsPanel.getPanelWidth();
        instancesPanel.displayInstances(this.scrollWidth);

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

    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        this.instancesPanel.setGeneTooltipListener(geneTooltipListener);
    }

    public void setGeneDoubleClickListener(Listener<DoubleClickGeneEvent> geneTooltipListener) {
        this.instancesPanel.setDoubleClickListener(geneTooltipListener);
    }

    public void alignGenes(GeneShape anchorGene, int viewX){
        instancesPanel.alignPanels(anchorGene, viewX);
    }



    private enum ViewMode {
        PATTERNS,
        INSTANCES,
        NONE
    }
}
