package MVC.View.Components.Dialogs;

import Core.OutputType;

import javax.swing.*;
import java.awt.*;

/**
 */
public class OutputTypeChooser extends JPanel {

    private ButtonGroup radioBtns;
    private JTextField datasetName;

    public OutputTypeChooser(){
        super(new GridBagLayout());

        datasetName = new JTextField();
        datasetName.setText("dataset");
        datasetName.setColumns(10);

        JLabel datasetNameLabel = new JLabel("Dataset Name: ", JLabel.LEFT);

        radioBtns = new ButtonGroup();
        JRadioButton btn1 = new JRadioButton(OutputType.TXT.toString());
        btn1.setActionCommand(OutputType.TXT.toString());
        btn1.setSelected(true);

        JRadioButton btn2 = new JRadioButton(OutputType.XLSX.toString());
        btn2.setActionCommand(OutputType.XLSX.toString());

        JRadioButton btn3 = new JRadioButton(OutputType.EXPORT.toString());
        btn3.setActionCommand(OutputType.EXPORT.toString());

        radioBtns.add(btn1);
        radioBtns.add(btn2);
        radioBtns.add(btn3);

        JLabel outputTypeLabel = new JLabel("Output type:", JLabel.LEFT);
        //Put the radio buttons in a column in a panel.
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(outputTypeLabel);
        radioPanel.add(btn1);
        radioPanel.add(btn2);
        radioPanel.add(btn3);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        add(datasetNameLabel, c);
        c.gridx = 1;
        add(datasetName, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 2;
        add(radioPanel, c);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
    }

    public OutputType getChosenOutput(){
        return OutputType.valueOf(radioBtns.getSelection().getActionCommand());
    }

    public String getDatasetName(){
        return datasetName.getText();
    }
}
