package MVC.View.Components.Panels;

import MVC.View.Events.DoubleClickGeneEvent;
import Model.Genomes.*;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import MVC.View.Components.Shapes.*;
import MVC.View.Components.Shapes.Label;
import MVC.View.Events.TooltipGeneEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class InstancesPanel extends JPanel {

    private GridBagConstraints gc;

    private static final int CONTAINERS_DIST = 50;
    private static final int PADDING = 4;

    private Font geneLabelFont;

    private Random rnd = new Random();

    private  Map<String, Color> colorsUsed;

    private int rowHeight;
    private int firstRowHeight;

    private GenomesInfo genomesInfo;

    public static final Color LIGHT_GRAY = new Color(238,238,238);

    private List<ShapesPanel> rows;

    private int numOfNeighbors;

    private Listener<TooltipGeneEvent> geneTooltipListener;

    private Listener<DoubleClickGeneEvent> doubleClickListener;

    private int scrollWidth = 0;

    public InstancesPanel(Map<String, Color> colorsUsed) {

        setGCLayout();
        setLayout(new GridBagLayout());

        geneLabelFont = Label.DEFAULT_FONT;

        this.rowHeight = 0;
        this.firstRowHeight = 0;

        rows = new ArrayList<>();

        this.colorsUsed = colorsUsed;

        this.numOfNeighbors = 0;

    }

    public void displayInstances() {
        displayInstances(scrollWidth);
    }

    public void displayInstances(int scrollWidth) {
        this.scrollWidth = scrollWidth;

        clearPanel();
        showData(scrollWidth);
        revalidate();
        repaint();
    }

    public void clearPanel(){
        removeAll();
    }

    public void setNumOfNeighbors(int numOfNeighbors){
        this.numOfNeighbors = numOfNeighbors;
    }

    public void setGenomesInfo(GenomesInfo genomesInfo) {
        this.genomesInfo = genomesInfo;
    }

    public void setData(List<Pattern> patterns){

        rows.clear();

        for (Pattern pattern: patterns) {
            ShapesPanel instancesRowPanel = createPatternRow(pattern.getPatternGenes());

            if (instancesRowPanel != null) {
                firstRowHeight = instancesRowPanel.getPanelHeight() + PADDING;
            }

            rows.add(instancesRowPanel);
        }
    }

    public void setData(Pattern pattern, List<Map.Entry<String, List<InstanceLocation>>> genomeToInstances) {

        rows.clear();

        addPatternRow(pattern);

        ShapesPanel instancesRowPanel;
        List<GenesInstance> genesInstanceInnerList;
        int x;
        int y = 0;
        for (Map.Entry<String, List<InstanceLocation>> genomeInstances: genomeToInstances) {
            x = 0;

            List<InstanceLocation> locationsInGenome = genomeInstances.getValue();
            List<List<GenesInstance>> genomeShapesInstances = new ArrayList<>();

            Map<Integer, List<InstanceLocation>> locationsInReplicon = locationsInGenome.stream()
                    .collect(Collectors.groupingBy(InstanceLocation::getRepliconId));

            for (List<InstanceLocation> repliconInstances : locationsInReplicon.values()) {

                genesInstanceInnerList = new ArrayList<>();
                x = addShapeInstanceList(repliconInstances, genesInstanceInnerList, x, y,
                        genomesInfo, numOfNeighbors);

                genomeShapesInstances.add(genesInstanceInnerList);

            }

            instancesRowPanel = getInstancesRowPanel(genomeShapesInstances, LIGHT_GRAY);
            if (instancesRowPanel != null) {
                rowHeight = instancesRowPanel.getPanelHeight() + PADDING;
            }
            rows.add(instancesRowPanel);
        }
    }

    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        this.geneTooltipListener = geneTooltipListener;
    }

    public void setDoubleClickListener(Listener<DoubleClickGeneEvent> doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

    private void addPatternRow(Pattern pattern){
        ShapesPanel instancesRowPanel = createPatternRow(pattern.getPatternGenes());

        if (instancesRowPanel != null) {
            firstRowHeight = instancesRowPanel.getPanelHeight() + PADDING;
        }
        rows.add(instancesRowPanel);
    }

    public void showData(int scrollWidth){
        int rowIndex = 0;

        for (ShapesPanel row: rows){
            rowIndex = addInstancePanelRow(row, rowIndex, scrollWidth, row.getPanelHeight()+PADDING);
        }
    }

    private ShapesPanel createPatternRow(Gene[] patternGenes){

        List<GenesInstance> genesInstanceInnerList = new ArrayList<>();
        genesInstanceInnerList.add(getShapesCSB(patternGenes, 0, 0));

        List<List<GenesInstance>> shapesInstanceOuterList = new ArrayList<>();
        shapesInstanceOuterList.add(genesInstanceInnerList);

        return getInstancesRowPanel(shapesInstanceOuterList,  LIGHT_GRAY);

    }

    private int addInstancePanelRow(ShapesPanel instancesRowPanel, int rowIndex, int scrollWidth, int height){

        JScrollPane scrollPane = new JScrollPane(instancesRowPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));

        scrollWidth -= 10;
        scrollPane.setPreferredSize(new Dimension(scrollWidth, height));

        gc.gridx = 0; gc.gridy = rowIndex; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(scrollPane, gc);

        return rowIndex+1;
    }


    private int addShapeInstanceList(List<InstanceLocation> instancesList, List<GenesInstance> genesInstanceList,
                                     int x, int y, GenomesInfo genomesInfo, int numOfNeighbors){

        for (InstanceLocation instance : instancesList) {
            GenesInstance genesInstance = getShapesInstance(instance, x, y, genomesInfo, numOfNeighbors);
            genesInstanceList.add(genesInstance);
            x += genesInstance.getWidth() + CONTAINERS_DIST;
        }
        return x;
    }

    private ShapesPanel getInstancesRowPanel(List<List<GenesInstance>> shapesInstanceList, Color backgroundColor) {

        ShapesPanel shapesPanel = new ShapesPanel(shapesInstanceList, CONTAINERS_DIST, backgroundColor);
        shapesPanel.setToolTipText("");
        shapesPanel.setGeneTooltipListener(geneTooltipListener);
        shapesPanel.setDoubleClickListener(doubleClickListener);

        MouseAdapter ma = new MouseAdapterScroller(shapesPanel);
        shapesPanel.addMouseListener(ma);
        shapesPanel.addMouseMotionListener(ma);

        return shapesPanel;

    }

    private GenesInstance getShapesCSB(Gene[] genes, int x, int y){

        List<GeneShape> geneShapesList = getGeneShapesList(genes);

        return new GenesInstance(geneShapesList, x, y);
    }

    private GenesInstance getShapesInstance(InstanceLocation instance, int x, int y, GenomesInfo genomesInfo,
                                            int numOfNeighbors){

        Genome genome = genomesInfo.getGenome(instance.getGenomeId());
        Replicon replicon = genome.getReplicon(instance.getRepliconId());
        String repliconName = replicon.getName();

        int instanceStartIndex = instance.getActualStartIndex();
        int instanceEndIndex = instance.getActualEndIndex();

        int leftStartIndex = Math.max(0, instanceStartIndex - numOfNeighbors);
        int rightEndIndex = Math.min(instanceEndIndex + numOfNeighbors, replicon.size());

        List<GeneShape> leftNeighbors = getGeneShapesList(getGenes(replicon, leftStartIndex, instanceStartIndex));
        List<GeneShape> instanceShapesList = getGeneShapesList(getGenes(replicon, instanceStartIndex, instanceEndIndex));
        List<GeneShape> rightNeighbors = getGeneShapesList(getGenes(replicon, instanceEndIndex, rightEndIndex));

        Label instanceNameLabel = new Label(repliconName);

        Label instanceStartIndexLabel = new Label(Integer.toString(leftStartIndex));
        Label instanceEndIndexLabel = new Label(Integer.toString(rightEndIndex-1));

        return new GenesInstance(instanceShapesList, leftNeighbors, rightNeighbors, x, y, instanceNameLabel,
                instanceStartIndexLabel, instanceEndIndexLabel);
    }

    private Gene[] getGenes(Replicon replicon, int startIndex, int endIndex){
        List<Gene> instanceList = new ArrayList<>();

        List<Gene> repliconGenes = replicon.getGenes();

        if (repliconGenes != null) {
            instanceList = repliconGenes.subList(startIndex, endIndex);
        }

        return instanceList.toArray(new Gene[instanceList.size()]);
    }

    private List<GeneShape> getGeneShapesList(Gene[] genes){
        List<GeneShape> geneShapesList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;

            if (colorsUsed.containsKey(gene.getCogId())){
                color = colorsUsed.get(gene.getCogId());
            }else {
                color = getRandomColor();
            }
            GeneShape geneShape = new GeneShape(color, gene, getGraphics(), geneLabelFont);

            geneShapesList.add(geneShape);
            colorsUsed.put(gene.getCogId(), color);
        }
        return geneShapesList;
    }

    private Color getRandomColor(){
        float hue = rnd.nextFloat();
        // Saturation between 0.1 and 0.3
        float saturation = (rnd.nextInt(2000) + 1000) / 10000f;
        float luminance = 0.9f;
        Color color = Color.getHSBColor(hue, saturation, luminance);

        return color;
    }


    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public Map<String,Color> getColorsUsed(){
        return colorsUsed;
    }

    public int getRowHeight(){
        return rowHeight;
    }

    public int getFirstRowHeight(){
        return firstRowHeight;
    }

    public void alignPanels(GeneShape anchorGene, int viewX){
        for (ShapesPanel row : rows){
            String cogId = anchorGene.getLabel().getText();
            GeneShape geneShape = row.getGeneShapeWithLabel(cogId);

            if (geneShape == null){
                break;
            }

            int deltaX = anchorGene.getX() - viewX;

            if (geneShape.getStrand() != anchorGene.getStrand()){
                row.reverse();
            }

            int x = geneShape.getX();

            JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, row);
            if (viewPort != null) {

                Rectangle view = viewPort.getViewRect();
                view.x = x - deltaX;

                row.scrollRectToVisible(view);
            }
        }
    }


    public void zoomOut(int zoomUnit){
        geneLabelFont = new Font(geneLabelFont.getName(), geneLabelFont.getStyle(), geneLabelFont.getSize() - zoomUnit);
    }

    public void zoomIn(int zoomUnit){
        geneLabelFont = new Font(geneLabelFont.getName(), geneLabelFont.getStyle(), geneLabelFont.getSize() + zoomUnit);
    }
}
