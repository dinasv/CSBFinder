package MVC.View.Components.Panels;

import Core.Genomes.InstanceLocation;
import Core.Genomes.Pattern;
import Core.Genomes.PatternLocationsInGenome;
import MVC.Common.InstanceInfo;
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

    public void setData(Pattern pattern) {

        rows.clear();

        ShapesPanel instancesRowPanel = createPatternRow(pattern.getPatternGenes());

        if (instancesRowPanel != null) {
            firstRowHeight = instancesRowPanel.getPanelHeight() + PADDING;
        }
        rows.add(instancesRowPanel);

        List<ShapesInstance> shapesInstanceInnerList;
        int x;
        int y = 0;
        for (Map.Entry<Integer, PatternLocationsInGenome> genomeInstances: pattern.getPatternLocations().entrySet()) {
            x = 0;

            PatternLocationsInGenome locationsInGenome = genomeInstances.getValue();
            List<List<ShapesInstance>> genomeShapesInstances = new ArrayList<>();
            for (List<InstanceLocation> repliconInstances : locationsInGenome.getSortedLocations().values()) {

                shapesInstanceInnerList = new ArrayList<>();
                x = getShapeInstanceList(repliconInstances, shapesInstanceInnerList, x, y);

                genomeShapesInstances.add(shapesInstanceInnerList);

            }

            instancesRowPanel = getInstancesRowPanel(genomeShapesInstances, LIGHT_GRAY);
            if (instancesRowPanel != null) {
                rowHeight = instancesRowPanel.getPanelHeight() + PADDING;
            }
            rows.add(instancesRowPanel);
        }
    }

    public void showData(int scrollWidth){
        int rowIndex = 0;

        for (ShapesPanel row: rows){
            rowIndex = addInstancePanelRow(row, rowIndex, scrollWidth, row.getPanelHeight()+PADDING);
        }
    }

    private ShapesPanel createPatternRow(List<Gene> pattenGenes){

        List<ShapesInstance> shapesInstanceInnerList = new ArrayList<>();
        shapesInstanceInnerList.add(getShapesCSB(pattenGenes, 0, 0));
        List<List<ShapesInstance>> shapesInstanceOuterList = new ArrayList<>();
        shapesInstanceOuterList.add(shapesInstanceInnerList);
        ShapesPanel patternRow = getInstancesRowPanel(shapesInstanceOuterList,  Color.WHITE);

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


    private int getShapeInstanceList(List<InstanceLocation> instancesList,
                                                      List<ShapesInstance> shapesInstanceList, int x, int y){

        for (InstanceLocation instance : instancesList) {
            ShapesInstance shapesInstance = getShapesInstance(instance, x, y);
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
        List<GeneShape> geneShapesList = getGeneShapesList(genes, x, y);

        return new ShapesInstance(geneShapesList, x, y);
    }

    private ShapesInstance getShapesInstance(InstanceLocation instance, int x, int y){
        List<GeneShape> geneShapesList = getGeneShapesList(instance.getGenes(), x, y);

        Label instanceNameLabel = new Label(instance.getRepliconName());
        Label instanceStartIndexLabel = new Label(Integer.toString(instance.getActualStartIndex()));
        Label instanceEndIndexLabel = new Label(Integer.toString(instance.getActualEndIndex()-1));
        return new ShapesInstance(geneShapesList, x, y, instanceNameLabel, instanceStartIndexLabel, instanceEndIndexLabel);
    }

    private List<GeneShape> getGeneShapesList(List<Gene> genes, int x, int y){
        List<GeneShape> geneShapesList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;

            if (colorsUsed.containsKey(gene.getCogId())){
                color = colorsUsed.get(gene.getCogId());
            }else {
                color = getRandomColor();
            }
            GeneShape geneShape = new GeneShape(x, y, color, gene, getGraphics());

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
