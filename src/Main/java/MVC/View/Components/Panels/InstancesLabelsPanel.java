package MVC.View.Components.Panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstancesLabelsPanel extends JPanel {

    private GridBagConstraints gc;

    public static final int GENOME_NAME_WIDTH = 300;


    public InstancesLabelsPanel() {
        setLayout(new GridBagLayout());
        setGCLayout();
    }

    public void displayInstancesLabels(List<String> instances, int firstRowHeight, int rowHeight) {

        setData(instances, firstRowHeight, rowHeight);

        revalidate();
        repaint();
    }

    public void clearPanel(){
        removeAll();
    }

    private void setData(List<String> instances, int firstRowHeight, int rowHeight) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);

        JLabel genomeRowLabel = getGenomeRowLabelComponent("CSB", firstRowHeight);
        colIndex = setLabelsRow(colIndex, insetName, genomeRowLabel);

        for (String genomeName: instances) {

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
