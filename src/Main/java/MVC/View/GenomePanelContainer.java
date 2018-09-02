package MVC.View;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class GenomePanelContainer  extends JPanel {

    private InstancesLabelsPanel labelsPanel;
    private InstancesPanel instancesPanel;

    GridBagConstraints gc;

    private final ShapeDimensions geneShapeDim = new ShapeDimensions(60, 15, 30);

    public GenomePanelContainer(){

        setLayout(new GridBagLayout());
        setGCLayout();

        instancesPanel = new InstancesPanel(geneShapeDim);

        labelsPanel = new InstancesLabelsPanel();

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.2; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(labelsPanel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0.8; gc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(instancesPanel, gc);

        /*
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(labelsPanel);
        setRightComponent(instancesPanel);
        setResizeWeight(0.2);*/
    }

    public void displayInstances(String[] pattenCOGs, Map<String,List<InstanceInfo>> instances, int scrollWidth) {
        //setData(instances, pattenCOGs, scrollWidth);

        instancesPanel.displayInstances(pattenCOGs,instances, scrollWidth-InstancesLabelsPanel.GENOME_NAME_WIDTH);
        labelsPanel.displayInstancesLabels(instances, instancesPanel.getFirstRowHeight(), instancesPanel.getRowHeight());

        revalidate();
        repaint();
    }

/*
    private void setData(Map<String,List<List<Gene>>> instances, String[] pattenCOGs, int scrollWidth) {

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
        colIndex = setGenomePanelRow(patternGenes, colIndex, insetName, insetList, genomeRowLabel, Color.WHITE,
                scrollWidth);

        Color light_gray = new Color(238,238,238);

        for (Map.Entry<String, List<List<Gene>>> entry: instances.entrySet()) {
            String genomeName = entry.getKey();
            List<List<Gene>> instancesLists = entry.getValue();

            genomeRowLabel = getGenomeRowLabelComponent(genomeName);
            colIndex = setGenomePanelRow(instancesLists, colIndex, insetName, insetList, genomeRowLabel, light_gray,
                    scrollWidth);

        }
    }

    private int setGenomePanelRow(List<List<Gene>> instances, int colIndex, Insets insetName,
                                  Insets insetList, JLabel genomeRowLabel, Color backgroundColor, int scrollWidth){

        JScrollPane instancesRow = getInstancesRow(instances, backgroundColor);

        //scrollWidth -= GENOME_NAME_WIDTH - 10;
        scrollWidth = 200;
        instancesRow.setPreferredSize(new Dimension(scrollWidth, INSTANCE_ROW_HEIGHT));

        gc.gridx = 0; gc.gridy = colIndex; gc.weightx = 0; gc.anchor = GridBagConstraints.FIRST_LINE_START; gc.insets = insetName;
        labelsPanel.add(genomeRowLabel, gc);
        gcRight.gridx = 0; gcRight.gridy = colIndex; gcRight.weightx = 2; gcRight.anchor = GridBagConstraints.FIRST_LINE_START; gcRight.insets = insetList;
        //JPanel p = new JPanel(new BorderLayout());
        instancesPanel.add(instancesRow, gcRight);
        colIndex += 1;

        return colIndex;
    }



    private JScrollPane getInstancesRow(List<List<Gene>> instancesList, Color backgroundColor) {

        int x = 0;
        int y = 0;
        List<ShapesInstance> shapesContainerList = new ArrayList<>();
        for (List<Gene> instance : instancesList) {
            ShapesInstance shapesContainer = getShapesContainer(instance, x, y);
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

    private ShapesInstance getShapesContainer(List<Gene> genes, int x, int y){
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
        return new ShapesInstance(shapeParamsList, x, y);
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
    }*/

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

    public Map<String,Color> getColorsUsed(){
        return instancesPanel.getColorsUsed();
    }
}
