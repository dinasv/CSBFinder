package mvc.view.components.panels;

import mvc.view.components.shapes.GeneShape;
import mvc.view.events.DoubleClickGeneEvent;
import mvc.view.graphics.GeneColors;
import model.genomes.GenomesInfo;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;
import mvc.view.events.TooltipGeneEvent;
import mvc.view.listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenesViewPanel extends JPanel {

    private static final int MAX_PATTERNS_DISPLAY = 2000;

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

    private Stream<Map.Entry<String, List<InstanceLocation>>> getPatternGenomeToInstances(Pattern pattern){
        return pattern.getPatternLocations().getSortedLocations().stream()
                .collect(Collectors.groupingBy(location -> genomesInfo.getGenomeName(location.getGenomeId())))
                .entrySet().stream();
    }

    private List<String> getGenomeNames(List<Map.Entry<String, List<InstanceLocation>>> genomeToInstances){
        return genomeToInstances
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void displayPatterns(List<Pattern> patterns){

        int notDisplayedPatterns = 0;
        if (patterns.size() > MAX_PATTERNS_DISPLAY){
            notDisplayedPatterns = patterns.size() - MAX_PATTERNS_DISPLAY;
            patterns = patterns.subList(0, MAX_PATTERNS_DISPLAY);
        }

        viewMode = ViewMode.PATTERNS;
        patternsInView = patterns;

        List<String> patternNames = patterns.stream()
                                            .map(Pattern::getPatternId)
                                            .map(String::valueOf)
                                            .map(id -> "CSB " + id)
                                            .collect(Collectors.toList());

        genomeToInstances = patterns.stream()
                .flatMap(this::getPatternGenomeToInstances)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        genomeNames = getGenomeNames(genomeToInstances);

        instancesPanel.setData(patterns);
        labelsPanel.displayInstancesLabels(patternNames, instancesPanel.getFirstRowHeight(),
                instancesPanel.getFirstRowHeight());

        instancesPanel.displayGenes(calcInstancesScrollWidth());

        if (notDisplayedPatterns == 1) {
            instancesPanel.addLabelRow("One more CSB is not displayed");
        } else if (notDisplayedPatterns > 1){
            instancesPanel.addLabelRow(String.format("%d more CSBs are not displayed", notDisplayedPatterns));
        }

        content.revalidate();
        content.repaint();
    }

    public void displayInstances(Pattern pattern) {

        viewMode = ViewMode.INSTANCES;
        patternInView = pattern;

        genomeToInstances = getPatternGenomeToInstances(pattern)
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        genomeNames = getGenomeNames(genomeToInstances);

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

    public void alignGenes(GeneShape anchorGene, JPanel clickedPanel, int viewX){
        instancesPanel.alignPanels(anchorGene, clickedPanel, viewX);
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
