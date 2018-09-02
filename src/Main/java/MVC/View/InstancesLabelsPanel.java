package MVC.View;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;
import Utils.Gene;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class InstancesLabelsPanel extends JPanel {

    private GridBagConstraints gc;

    public static final int GENOME_NAME_WIDTH = 100;


    public InstancesLabelsPanel() {
        setLayout(new GridBagLayout());
        setGCLayout();

        //add(new JLabel("BlaBla"), BorderLayout.CENTER);
    }

    public void displayInstancesLabels(Map<String,List<InstanceInfo>> instances, int firstRowHeight, int rowHeight) {
        removeAll();

        setData(instances, firstRowHeight, rowHeight);

        revalidate();
        repaint();
    }


    private void setData(Map<String,List<InstanceInfo>> instances, int firstRowHeight, int rowHeight) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);

        JLabel genomeRowLabel = getGenomeRowLabelComponent("CSB", firstRowHeight);
        colIndex = setLabelsRow(colIndex, insetName, genomeRowLabel);

        for (Map.Entry<String, List<InstanceInfo>> entry: instances.entrySet()) {
            String genomeName = entry.getKey();

            genomeRowLabel = getGenomeRowLabelComponent(genomeName, rowHeight);
            colIndex = setLabelsRow(colIndex, insetName, genomeRowLabel);

        }
    }

    private int setLabelsRow(int colIndex, Insets insetName, JLabel genomeRowLabel){

        gc.gridx = 0; gc.gridy = colIndex; gc.anchor = GridBagConstraints.FIRST_LINE_START; //gc.insets = insetName;
        add(genomeRowLabel, gc);

        colIndex += 1;

        return colIndex;
    }


    private JLabel getGenomeRowLabelComponent(String label, int rowHeight) {
        JLabel genomeRowLabelComponent = new JLabel(label);
        genomeRowLabelComponent.setToolTipText(label);
        genomeRowLabelComponent.setPreferredSize(new Dimension(GENOME_NAME_WIDTH, rowHeight));
        return genomeRowLabelComponent;
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

}
