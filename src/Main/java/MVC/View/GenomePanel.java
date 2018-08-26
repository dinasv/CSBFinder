package MVC.View;

import MVC.View.Shapes.*;
import Utils.Gene;

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
    Random rnd = new Random();

    private static final String CSB_PREFIX = "Instances for CSB: ";

    public GenomePanel() {
        setGCLayout();
        setLayout(new BorderLayout());
        container = new JPanel(new GridBagLayout());
//
        scroll = new JScrollPane(container);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


        title = new JLabel("");
        title.setBorder(new EmptyBorder(5, 2, 5, 2));
        title.setFont(new Font(Font.SERIF, Font.BOLD, 18));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.WEST);
    }

    public void displayInstances(String pattenId, Map<String,List<List<Gene>>> instances) {
        container.removeAll();
        setTitle(pattenId);
        setData(instances);
        container.revalidate();
        container.repaint();

    }

    private void setTitle(String patternId) {
        title.setText(CSB_PREFIX + patternId);
    }

    private void setData(Map<String,List<List<Gene>>> instances) {

        JLabel geneName;

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);
        Insets insetList = new Insets(0, 0, 0, 15);

        Map<String, ShapeParams> genesLabelsUsed = new HashMap();

        for (Map.Entry<String, List<List<Gene>>> entry: instances.entrySet()) {
            geneName = getGeneNameComponent(entry.getKey());
            geneName.setPreferredSize(new Dimension(100, 25));
            geneName.setToolTipText(entry.getKey());

            JScrollPane instancesRow = getInstancesRow(entry.getValue(), genesLabelsUsed);
            instancesRow.setPreferredSize(new Dimension(1050, 35));

            gc.gridx = 0; gc.gridy = colIndex; gc.weightx = 0; gc.anchor = GridBagConstraints.FIRST_LINE_START; gc.insets = insetName;
            container.add(geneName, gc);
            gc.gridx = 1; gc.gridy = colIndex; gc.weightx = 2; gc.anchor = GridBagConstraints.FIRST_LINE_START; gc.insets = insetList;
            JPanel p = new JPanel(new BorderLayout());
            container.add(instancesRow, gc);
            colIndex += 1;
        }
    }

    private JScrollPane getInstancesRow(List<List<Gene>> instancesList, Map<String, ShapeParams> genesLabelsUsed) {

        int x = 0;
        int y = 0;
        List<ShapesContainer> shapesContainerList = new ArrayList<>();
        for (List<Gene> instance : instancesList) {
            ShapesContainer shapesContainer = getShapesContainer(instance, genesLabelsUsed, x, y);
            shapesContainerList.add(shapesContainer);
            x += shapesContainer.getContainerDimensions().getWidth() + CONTAINERS_DIST;
        }

        ShapesPanel shapesPanel = new ShapesPanel(shapesContainerList, CONTAINERS_DIST);
        JScrollPane scrollPane = new JScrollPane(shapesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        MouseAdapter ma = new MouseAdapterScroller(shapesPanel);

        shapesPanel.addMouseListener(ma);
        shapesPanel.addMouseMotionListener(ma);
        return scrollPane;

    }

    private ShapesContainer getShapesContainer(List<Gene> genes, Map<String, ShapeParams> genesLabelsUsed, int x, int y){
        List<ShapeParams> shapeParamsList = new ArrayList<>();
        for (Gene gene : genes) {

            Color color;
            if (genesLabelsUsed.containsKey(gene.getCog_id())){
                color = genesLabelsUsed.get(gene.getCog_id()).getColor();
            }else {
                color = getRandomColor();
            }
            ShapeParams shapeParams = new ShapeParams(x, y, color, new ShapeDimensions(
                    60, 15, 30), gene);

            shapeParamsList.add(shapeParams);
            genesLabelsUsed.put(gene.getCog_id(), shapeParams);
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

    private JLabel getGeneNameComponent(String geneName) {
        JLabel geneNameComponent = new JLabel(geneName);
        geneNameComponent.setToolTipText(geneName);
        return geneNameComponent;
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

}
