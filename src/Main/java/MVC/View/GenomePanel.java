package MVC.View;

import MVC.View.Shapes.*;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class GenomePanel extends JPanel {
    private JLabel title;
    private JScrollPane scroll;
    private JPanel container;
    private GridBagConstraints gc;

    private static final int CONTAINERS_DIST = 50;
    private static final int GENOME_NAME_WIDTH = 100;
    Random rnd = new Random();

    private static final String CSB_PREFIX = "Instances for CSB: ";

    private  Map<String, Color> colorsUsed;

    public GenomePanel() {
        setGCLayout();
        setLayout(new BorderLayout());
        container = new JPanel(new GridBagLayout());

        scroll = new JScrollPane(container);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));

        add(scroll);

        colorsUsed = new HashMap<>();
        colorsUsed.put(Utils.UNK_CHAR, Color.lightGray);
    }

    public void displayInstances(String[] pattenCOGs, Map<String,List<List<Gene>>> instances) {
        container.removeAll();
        setData(instances, pattenCOGs);
        container.revalidate();
        container.repaint();

    }


    private void setData(Map<String,List<List<Gene>>> instances, String[] pattenCOGs) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);
        Insets insetList = new Insets(0, 0, 0, 15);

        List<List<Gene>> patternGenes = new ArrayList<>();
        List<Gene> patternGenesInner = new ArrayList<>();
        patternGenes.add(patternGenesInner);
        for (String cog: pattenCOGs) {
            patternGenesInner.add(new Gene(cog, "+"));
        }

        JLabel genomeRowLabel = getGenomeRowLabelComponent("CSB");
        colIndex = setGenomePanelRow(patternGenes, colIndex, insetName, insetList, genomeRowLabel, Color.LIGHT_GRAY);

        for (Map.Entry<String, List<List<Gene>>> entry: instances.entrySet()) {
            String genomeName = entry.getKey();
            List<List<Gene>> instancesLists = entry.getValue();

            genomeRowLabel = getGenomeRowLabelComponent(genomeName);
            colIndex = setGenomePanelRow(instancesLists, colIndex, insetName, insetList, genomeRowLabel, Color.white);

        }
    }

    private int setGenomePanelRow(List<List<Gene>> instances, int colIndex, Insets insetName,
                                  Insets insetList, JLabel genomeRowLabel, Color backgroundColor){

        JScrollPane instancesRow = getInstancesRow(instances, backgroundColor);
        int instanceRowWidth = scroll.getViewport().getSize().width - GENOME_NAME_WIDTH - 10;
        instancesRow.setPreferredSize(new Dimension(instanceRowWidth, 35));

        gc.gridx = 0; gc.gridy = colIndex; gc.weightx = 0; gc.anchor = GridBagConstraints.FIRST_LINE_START; gc.insets = insetName;
        container.add(genomeRowLabel, gc);
        gc.gridx = 1; gc.gridy = colIndex; gc.weightx = 2; gc.anchor = GridBagConstraints.FIRST_LINE_START; gc.insets = insetList;
        JPanel p = new JPanel(new BorderLayout());
        container.add(instancesRow, gc);
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
            ShapeParams shapeParams = new ShapeParams(x, y, color, new ShapeDimensions(
                    60, 15, 30), gene);

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

    private JLabel getGenomeRowLabelComponent(String label) {
        JLabel genomeRowLabelComponent = new JLabel(label);
        genomeRowLabelComponent.setToolTipText(label);
        genomeRowLabelComponent.setPreferredSize(new Dimension(GENOME_NAME_WIDTH, 25));
        return genomeRowLabelComponent;
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public Map<String,Color> getColorsUsed(){
        return colorsUsed;
    }
}
