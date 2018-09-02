package MVC.View;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;
import MVC.View.Shapes.Label;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;
import Utils.InstanceLocation;

public class InstancesPanel extends JPanel {


    private GridBagConstraints gc;

    private static final int CONTAINERS_DIST = 50;
    Random rnd = new Random();

    private  Map<String, Color> colorsUsed;

    private int rowHeight;
    private int firstRowHeight;

    private ShapeDimensions geneShapeDim;

    public InstancesPanel(ShapeDimensions geneShapeDim) {

        setGCLayout();
        setLayout(new GridBagLayout());

        this.rowHeight = 0;
        this.firstRowHeight = 0;
        this.geneShapeDim = geneShapeDim;

        //setPreferredSize(new Dimension(1000, 500));

        colorsUsed = new HashMap<>();
        colorsUsed.put(Utils.UNK_CHAR, Color.lightGray);

    }

    public void displayInstances(String[] pattenCOGs, Map<String,List<InstanceInfo>> instances, int scrollWidth) {
        removeAll();
        setData(instances, pattenCOGs, scrollWidth);
        revalidate();
        repaint();
    }


    private void setData(Map<String,List<InstanceInfo>> instances, String[] pattenCOGs, int scrollWidth) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetList = new Insets(0, 0, 0, 15);

        List<InstanceInfo> patternGenes = new ArrayList<>();
        List<Gene> patternGenesInner = new ArrayList<>();
        for (String cog: pattenCOGs) {
            patternGenesInner.add(new Gene(cog, "+"));
        }

        List<ShapesInstance> shapesInstanceList = new ArrayList<>();
        shapesInstanceList.add(getShapesCSB(patternGenesInner, 0, 0));
        ShapesPanel instancesRowPanel = getInstancesRowPanel(shapesInstanceList,  Color.WHITE);

        if (instancesRowPanel != null) {
            firstRowHeight = instancesRowPanel.getPanelHeight() + 4;
        }
        colIndex = setGenomePanelRow(instancesRowPanel, colIndex, scrollWidth, firstRowHeight);


        Color light_gray = new Color(238,238,238);

        for (Map.Entry<String, List<InstanceInfo>> entry: instances.entrySet()) {
            List<InstanceInfo> instancesList = entry.getValue();

            shapesInstanceList = getShapeInstanceList(instancesList);
            instancesRowPanel = getInstancesRowPanel(shapesInstanceList, light_gray);
            if (instancesRowPanel != null) {
                rowHeight = instancesRowPanel.getPanelHeight() + 4;
            }
            colIndex = setGenomePanelRow(instancesRowPanel, colIndex, scrollWidth, rowHeight);
        }


    }



    private int setGenomePanelRow(ShapesPanel instancesRowPanel, int colIndex, int scrollWidth, int height){

        JScrollPane scrollPane = new JScrollPane(instancesRowPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));

        scrollWidth -= 10;
        scrollPane.setPreferredSize(new Dimension(scrollWidth, height));

        gc.gridx = 0; gc.gridy = colIndex; gc.anchor = GridBagConstraints.FIRST_LINE_START; //gc.insets = insetList;
        add(scrollPane, gc);
        colIndex += 1;

        return colIndex;
    }


    private List<ShapesInstance> getShapeInstanceList(List<InstanceInfo> instancesList){
        int x = 0;
        int y = 0;
        List<ShapesInstance> shapesInstanceList = new ArrayList<>();
        for (InstanceInfo instance : instancesList) {
            ShapesInstance shapesInstance = getShapesInstance(instance, x, y);
            shapesInstanceList.add(shapesInstance);
            x += shapesInstance.getWidth() + CONTAINERS_DIST;
        }
        return shapesInstanceList;
    }

    private ShapesPanel getInstancesRowPanel(List<ShapesInstance> shapesInstanceList, Color backgroundColor) {

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

    private ShapesInstance getShapesInstance(InstanceInfo instance, int x, int y){
        List<GeneShape> geneShapesList = getGeneShapesList(instance.getGenes(), x, y);

        Label instanceNameLabel = new Label(instance.getInstanceLocation().getRepliconName());
        Label instanceStartIndexLabel = new Label(Integer.toString(instance.getInstanceLocation().getActualStartIndex()));
        Label instanceEndIndexLabel = new Label(Integer.toString(instance.getInstanceLocation().getActualEndIndex()));
        return new ShapesInstance(geneShapesList, x, y, instanceNameLabel, instanceStartIndexLabel, instanceEndIndexLabel);
    }

    private List<GeneShape> getGeneShapesList(List<Gene> genes, int x, int y){
        List<GeneShape> geneShapesList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;

            if (colorsUsed.containsKey(gene.getCog_id())){
                color = colorsUsed.get(gene.getCog_id());
            }else {
                color = getRandomColor();
            }
            GeneShape geneShape = new GeneShape(x, y, color, geneShapeDim, gene);

            geneShapesList.add(geneShape);
            colorsUsed.put(gene.getCog_id(), color);
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
