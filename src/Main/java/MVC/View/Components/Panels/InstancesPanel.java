package MVC.View.Components.Panels;

import Core.Genomes.Genome;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Replicon;
import Core.Patterns.InstanceLocation;
import Core.Patterns.Pattern;
import Core.Patterns.PatternLocationsInGenome;
import MVC.View.Components.Shapes.*;
import MVC.View.Components.Shapes.Label;
import Core.Genomes.Gene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class InstancesPanel extends JPanel {

    private GridBagConstraints gc;

    private static final int CONTAINERS_DIST = 50;
    private static final int PADDING = 4;

    private Random rnd = new Random();

    private  Map<String, Color> colorsUsed;

    private int rowHeight;
    private int firstRowHeight;

    public static final Color LIGHT_GRAY = new Color(238,238,238);

    private List<ShapesPanel> rows;

    public InstancesPanel(Map<String, Color> colorsUsed) {

        setGCLayout();
        setLayout(new GridBagLayout());

        this.rowHeight = 0;
        this.firstRowHeight = 0;

        rows = new ArrayList<>();

        this.colorsUsed = colorsUsed;

    }

    public void displayInstances(int scrollWidth) {
        showData(scrollWidth);
        revalidate();
        repaint();
    }

    public void clearPanel(){
        removeAll();
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

    public void setData(Pattern pattern, GenomesInfo genomesInfo, int numOfNeighbors) {

        rows.clear();

        addPatternRow(pattern);

        ShapesPanel instancesRowPanel;
        List<ShapesInstance> shapesInstanceInnerList;
        int x;
        int y = 0;
        for (Map.Entry<Integer, PatternLocationsInGenome> genomeInstances: pattern.getPatternLocations().entrySet()) {
            x = 0;

            PatternLocationsInGenome locationsInGenome = genomeInstances.getValue();
            List<List<ShapesInstance>> genomeShapesInstances = new ArrayList<>();
            for (List<InstanceLocation> repliconInstances : locationsInGenome.getSortedLocations().values()) {

                shapesInstanceInnerList = new ArrayList<>();
                x = addShapeInstanceList(repliconInstances, shapesInstanceInnerList, x, y, genomesInfo, numOfNeighbors);

                genomeShapesInstances.add(shapesInstanceInnerList);

            }

            instancesRowPanel = getInstancesRowPanel(genomeShapesInstances, LIGHT_GRAY);
            if (instancesRowPanel != null) {
                rowHeight = instancesRowPanel.getPanelHeight() + PADDING;
            }
            rows.add(instancesRowPanel);
        }
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

    private ShapesPanel createPatternRow(List<Gene> patternGenes){

        List<ShapesInstance> shapesInstanceInnerList = new ArrayList<>();
        shapesInstanceInnerList.add(getShapesCSB(patternGenes, 0, 0));
        List<List<ShapesInstance>> shapesInstanceOuterList = new ArrayList<>();
        shapesInstanceOuterList.add(shapesInstanceInnerList);
        ShapesPanel patternRow = getInstancesRowPanel(shapesInstanceOuterList,  LIGHT_GRAY);

        return patternRow;
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


    private int addShapeInstanceList(List<InstanceLocation> instancesList, List<ShapesInstance> shapesInstanceList,
                                     int x, int y, GenomesInfo genomesInfo, int numOfNeighbors){

        for (InstanceLocation instance : instancesList) {
            ShapesInstance shapesInstance = getShapesInstance(instance, x, y, genomesInfo, numOfNeighbors);
            shapesInstanceList.add(shapesInstance);
            x += shapesInstance.getWidth() + CONTAINERS_DIST;
        }
        return x;
    }

    private ShapesPanel getInstancesRowPanel(List<List<ShapesInstance>> shapesInstanceList, Color backgroundColor) {

        ShapesPanel shapesPanel = new ShapesPanel(shapesInstanceList, CONTAINERS_DIST, backgroundColor);
        MouseAdapter ma = new MouseAdapterScroller(shapesPanel);
        shapesPanel.addMouseListener(ma);
        shapesPanel.addMouseMotionListener(ma);

        return shapesPanel;

    }

    private ShapesInstance getShapesCSB(List<Gene> genes, int x, int y){

        List<GeneShape> geneShapesList = getGeneShapesList(genes);

        return new ShapesInstance(geneShapesList, x, y);
    }

    private ShapesInstance getShapesInstance(InstanceLocation instance, int x, int y, GenomesInfo genomesInfo,
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

        return new ShapesInstance(instanceShapesList, leftNeighbors, rightNeighbors, x, y, instanceNameLabel,
                instanceStartIndexLabel, instanceEndIndexLabel);
    }

    private List<Gene> getGenes(Replicon replicon, int startIndex, int endIndex){
        List<Gene> instanceList = new ArrayList<>();

        List<Gene> repliconGenes = replicon.getGenes();

        if (repliconGenes != null) {
            instanceList = repliconGenes.subList(startIndex, endIndex);
        }

        return instanceList;
    }

    private List<GeneShape> getGeneShapesList(List<Gene> genes){
        List<GeneShape> geneShapesList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;

            if (colorsUsed.containsKey(gene.getCogId())){
                color = colorsUsed.get(gene.getCogId());
            }else {
                color = getRandomColor();
            }
            GeneShape geneShape = new GeneShape(color, gene, getGraphics());

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
}
