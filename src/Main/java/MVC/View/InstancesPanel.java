package MVC.View;

import MVC.View.Shapes.*;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class InstancesPanel extends JPanel {


    private GridBagConstraints gc;

    private static final int CONTAINERS_DIST = 50;
    Random rnd = new Random();

    private  Map<String, Color> colorsUsed;

    private int rowHeight;

    private ShapeDimensions geneShapeDim;

    public InstancesPanel(int rowHeight, ShapeDimensions geneShapeDim) {

        setGCLayout();
        setLayout(new GridBagLayout());

        this.rowHeight = rowHeight;
        this.geneShapeDim = geneShapeDim;

        //setPreferredSize(new Dimension(1000, 500));

        colorsUsed = new HashMap<>();
        colorsUsed.put(Utils.UNK_CHAR, Color.lightGray);

    }

    public void displayInstances(String[] pattenCOGs, Map<String,List<List<Gene>>> instances, int scrollWidth) {
        removeAll();
        setData(instances, pattenCOGs, scrollWidth);
        revalidate();
        repaint();
    }


    private void setData(Map<String,List<List<Gene>>> instances, String[] pattenCOGs, int scrollWidth) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetList = new Insets(0, 0, 0, 15);

        List<List<Gene>> patternGenes = new ArrayList<>();
        List<Gene> patternGenesInner = new ArrayList<>();
        patternGenes.add(patternGenesInner);
        for (String cog: pattenCOGs) {
            patternGenesInner.add(new Gene(cog, "+"));
        }

        colIndex = setGenomePanelRow(patternGenes, colIndex, insetList, Color.WHITE,
                scrollWidth);

        Color light_gray = new Color(238,238,238);

        for (Map.Entry<String, List<List<Gene>>> entry: instances.entrySet()) {
            List<List<Gene>> instancesLists = entry.getValue();

            colIndex = setGenomePanelRow(instancesLists, colIndex, insetList, light_gray,
                    scrollWidth);

        }
    }

    private int setGenomePanelRow(List<List<Gene>> instances, int colIndex,
                                  Insets insetList, Color backgroundColor, int scrollWidth){

        JScrollPane instancesRow = getInstancesRow(instances, backgroundColor);

        scrollWidth -= 10;
        instancesRow.setPreferredSize(new Dimension(scrollWidth, rowHeight));

        gc.gridx = 0; gc.gridy = colIndex; gc.anchor = GridBagConstraints.FIRST_LINE_START; //gc.insets = insetList;
        add(instancesRow, gc);
        colIndex += 1;

        return colIndex;
    }



    private JScrollPane getInstancesRow(List<List<Gene>> instancesList, Color backgroundColor) {

        int x = 0;
        int y = 0;
        List<ShapesContainer> shapesContainerList = new ArrayList<>();
        for (List<Gene> instance : instancesList) {
            ShapesContainer shapesContainer = getShapesContainer(instance, x, y);
            shapesContainerList.add(shapesContainer);
            x += shapesContainer.getContainerDimensions().getWidth() + CONTAINERS_DIST;
        }

        ShapesPanel shapesPanel = new ShapesPanel(shapesContainerList, CONTAINERS_DIST, backgroundColor);
        JScrollPane scrollPane = new JScrollPane(shapesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));

        MouseAdapter ma = new MouseAdapterScroller(shapesPanel);

        shapesPanel.addMouseListener(ma);
        shapesPanel.addMouseMotionListener(ma);
        return scrollPane;

    }

    private ShapesContainer getShapesContainer(List<Gene> genes, int x, int y){
        List<ShapeParams> shapeParamsList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;

            if (colorsUsed.containsKey(gene.getCog_id())){
                color = colorsUsed.get(gene.getCog_id());
            }else {
                color = getRandomColor();
            }
            ShapeParams shapeParams = new ShapeParams(x, y, color, geneShapeDim, gene);

            shapeParamsList.add(shapeParams);
            colorsUsed.put(gene.getCog_id(), color);
        }
        return new ShapesContainer(shapeParamsList, x, y);
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
}
