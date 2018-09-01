package MVC.View;

import MVC.View.Shapes.*;
import Utils.Gene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class InstancesLabelsPanel extends JPanel {

    private GridBagConstraints gc;

    private static final int GENOME_NAME_WIDTH = 100;

    private int rowHeight;

    public InstancesLabelsPanel(int rowHeight) {
        setLayout(new GridBagLayout());
        setGCLayout();

        this.rowHeight = rowHeight;
        //add(new JLabel("BlaBla"), BorderLayout.CENTER);
    }

    public void displayInstancesLabels(Map<String,List<List<Gene>>> instances) {
        removeAll();

        setData(instances);

        revalidate();
        repaint();
    }


    private void setData(Map<String,List<List<Gene>>> instances) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);

        JLabel genomeRowLabel = getGenomeRowLabelComponent("CSB");
        colIndex = setLabelsRow(colIndex, insetName, genomeRowLabel);

        for (Map.Entry<String, List<List<Gene>>> entry: instances.entrySet()) {
            String genomeName = entry.getKey();

            genomeRowLabel = getGenomeRowLabelComponent(genomeName);
            colIndex = setLabelsRow(colIndex, insetName, genomeRowLabel);

        }
    }

    private int setLabelsRow(int colIndex, Insets insetName, JLabel genomeRowLabel){

        gc.gridx = 0; gc.gridy = colIndex; gc.anchor = GridBagConstraints.FIRST_LINE_START; //gc.insets = insetName;
        add(genomeRowLabel, gc);

        colIndex += 1;

        return colIndex;
    }


    private JLabel getGenomeRowLabelComponent(String label) {
        JLabel genomeRowLabelComponent = new JLabel(label);
        genomeRowLabelComponent.setToolTipText(label);
        genomeRowLabelComponent.setPreferredSize(new Dimension(GENOME_NAME_WIDTH, rowHeight));
        return genomeRowLabelComponent;
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

}
