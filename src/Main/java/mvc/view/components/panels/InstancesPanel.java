package mvc.view.components.panels;

import mvc.view.events.DoubleClickGeneEvent;
import mvc.view.graphics.GeneColors;
import model.genomes.*;
import model.patterns.InstanceLocation;
import model.patterns.Pattern;
import mvc.view.components.shapes.*;
import mvc.view.components.shapes.Label;
import mvc.view.events.TooltipGeneEvent;
import mvc.view.listeners.Listener;

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
    private static final int ALIGNMENT_PADDING = 10000;

    private Font geneLabelFont;

    private GeneColors colorsUsed;

    private int rowHeight;
    private int firstRowHeight;

    private GenomesInfo genomesInfo;

    public static final Color LIGHT_GRAY = new Color(238,238,238);

    private List<ShapesPanel> rows;

    private int numOfNeighbors;

    private Listener<TooltipGeneEvent> geneTooltipListener;

    private Listener<DoubleClickGeneEvent> doubleClickListener;

    private int scrollWidth = 0;

    public InstancesPanel(GeneColors colorsUsed) {

        setGCLayout();
        setLayout(new GridBagLayout());

        geneLabelFont = Label.DEFAULT_FONT;

        this.rowHeight = 0;
        this.firstRowHeight = 0;

        rows = new ArrayList<>();

        this.colorsUsed = colorsUsed;

        this.numOfNeighbors = 0;

    }

    public void displayGenes() {
        displayGenes(scrollWidth);
    }

    public void displayGenes(int scrollWidth) {
        this.scrollWidth = scrollWidth;

        clearPanel();
        showData(scrollWidth);
        revalidate();
        alignRowsToDefault();
        repaint();

    }

    public void addLabelRow(String text){
        JPanel panel = new JPanel();
        JLabel label = new JLabel(text);
        panel.add(label);

        gc.gridx = 0; gc.gridy += 1; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        addInstancePanelRow(panel, scrollWidth, 25);
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
            x = ALIGNMENT_PADDING;

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

        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        for (ShapesPanel row: rows){
            addInstancePanelRow(row, scrollWidth, row.getPanelHeight()+PADDING);
            gc.gridy += 1;
        }

    }



    public void alignRowsToDefault(){
        for (ShapesPanel row: rows){
            alignRow(row);
        }
    }

    private void alignRow(ShapesPanel row){
        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, row);
        if (viewPort != null) {

            viewPort.setViewPosition( new Point(ALIGNMENT_PADDING, 0) );

            //revalidate and repaint scroll
            row.getParent().revalidate();
            row.getParent().repaint();
        }
    }

    private ShapesPanel createPatternRow(Gene[] patternGenes){

        List<GenesInstance> genesInstanceInnerList = new ArrayList<>();
        genesInstanceInnerList.add(getShapesCSB(patternGenes, ALIGNMENT_PADDING, 0));

        List<List<GenesInstance>> shapesInstanceOuterList = new ArrayList<>();
        shapesInstanceOuterList.add(genesInstanceInnerList);

        return getInstancesRowPanel(shapesInstanceOuterList,  LIGHT_GRAY);

    }

    private void addInstancePanelRow(JPanel instancesRowPanel, int scrollWidth, int height){

        JScrollPane scrollPane = new JScrollPane(instancesRowPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));

        scrollWidth -= 10;
        scrollPane.setPreferredSize(new Dimension(scrollWidth, height));

        add(scrollPane, gc);

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

        ShapesPanel shapesPanel = new ShapesPanel(shapesInstanceList, CONTAINERS_DIST, backgroundColor, ALIGNMENT_PADDING);
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
        int rightEndIndex = Math.min(instanceEndIndex + numOfNeighbors, replicon.getGenes().size());

        //circular instance - don't repeat genes, show instance genes from the beginning
        if (rightEndIndex - leftStartIndex > replicon.size()){
            int instanceLength = instanceEndIndex - instanceStartIndex;
            instanceStartIndex %= replicon.size();
            instanceEndIndex = instanceStartIndex + Math.min(instanceLength, replicon.size());
            leftStartIndex = instanceStartIndex;
            rightEndIndex = leftStartIndex + replicon.size();
        }

        List<GeneShape> leftNeighbors = getGeneShapesList(getGenes(replicon, leftStartIndex, instanceStartIndex));
        List<GeneShape> instanceShapesList = getGeneShapesList(getGenes(replicon, instanceStartIndex, instanceEndIndex));
        List<GeneShape> rightNeighbors = getGeneShapesList(getGenes(replicon, instanceEndIndex, rightEndIndex));

        Label instanceNameLabel = new Label(repliconName, geneLabelFont, getGraphics());

        int leftStart = leftStartIndex%replicon.size();
        int rightEnd = (rightEndIndex-1)%replicon.size();
        Label instanceStartIndexLabel = new Label(Integer.toString(leftStart), geneLabelFont, getGraphics());
        Label instanceEndIndexLabel = new Label(Integer.toString(rightEnd), geneLabelFont, getGraphics());

        return new GenesInstance(instanceShapesList, leftNeighbors, rightNeighbors, x, y, instanceNameLabel,
                instanceStartIndexLabel, instanceEndIndexLabel);
    }

    private Gene[] getGenes(Replicon replicon, int startIndex, int endIndex){

        List<Gene> repliconGenes = replicon.getGenes();

        if (repliconGenes == null || startIndex >= endIndex || startIndex < 0 || endIndex > repliconGenes.size()){
            return new Gene[0];
        }

        List<Gene> instanceList = repliconGenes.subList(startIndex, endIndex);

        return instanceList.toArray(new Gene[0]);
    }

    private List<GeneShape> getGeneShapesList(Gene[] genes){
        List<GeneShape> geneShapesList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color = colorsUsed.getColor(gene);

            GeneShape geneShape = new GeneShape(color, gene, getGraphics(), geneLabelFont);

            geneShapesList.add(geneShape);
        }
        return geneShapesList;
    }


    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public int getRowHeight(){
        return rowHeight;
    }

    public int getFirstRowHeight(){
        return firstRowHeight;
    }

    public void alignPanels(GeneShape anchorGene, JPanel clickedPanel, int viewX){


        for (ShapesPanel row : rows){

            if (row == clickedPanel){
                continue;
            }

            String cogId = anchorGene.getLabel().getText();
            GeneShape geneShape = row.getGeneShapeWithLabel(cogId);

            if (geneShape == null){
                continue;
            }

            int deltaX = anchorGene.getX() - viewX;

            Strand anchorStrand = anchorGene.getStrand() == Strand.INVALID ? Strand.FORWARD : anchorGene.getStrand();
            if (geneShape.getStrand() != anchorStrand && geneShape.getStrand() != Strand.INVALID){
                row.reverse();
            }

            int x = geneShape.getX();

            JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, row);
            if (viewPort != null) {

                Point p = viewPort.getViewPosition();
                p.x = x - deltaX;

                viewPort.setViewPosition(p);

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
