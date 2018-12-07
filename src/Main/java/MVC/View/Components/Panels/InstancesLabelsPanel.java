package MVC.View.Components.Panels;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class InstancesLabelsPanel extends JPanel {

    private GridBagConstraints gc;

    public InstancesLabelsPanel() {
        setLayout(new GridBagLayout());
        setGCLayout();
    }

    public void displayInstancesLabels(List<String> instances, int firstRowHeight, int rowHeight) {

        setData(instances, firstRowHeight, rowHeight);

        revalidate();
        repaint();
    }

    public int getPanelWidth(){
        return (int)this.getPreferredSize().getWidth();
    }


    public void clearPanel(){
        removeAll();
    }

    private void setData(List<String> names, int firstRowHeight, int rowHeight) {

        int colIndex = 0;

        // For GridBagLayout
        Insets insetName = new Insets(0, 0, 0, 5);

        Iterator<String> namesIt = names.iterator();

        JLabel rowLabel;
        if (namesIt.hasNext()) {
            rowLabel = getGenomeRowLabelComponent(namesIt.next(), firstRowHeight);
            colIndex = setLabelsRow(colIndex, insetName, rowLabel);
        }

        while (namesIt.hasNext()) {
            rowLabel = getGenomeRowLabelComponent(namesIt.next(), rowHeight);
            colIndex = setLabelsRow(colIndex, insetName, rowLabel);
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
        int panelWidth = genomeRowLabelComponent.getPreferredSize().width;
        genomeRowLabelComponent.setPreferredSize(new Dimension(panelWidth, rowHeight));
        return genomeRowLabelComponent;
    }

    private void setGCLayout() {
        gc = new GridBagConstraints();
    }

}
