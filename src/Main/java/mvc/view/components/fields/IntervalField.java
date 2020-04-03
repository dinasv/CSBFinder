package mvc.view.components.fields;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.LINE_START;

public class IntervalField {

    private int minDefault;
    private int maxDefault;

    private final JLabel label;

    private JSpinner fromSpinner;
    private JSpinner toSpinner;

    private SpinnerNumberModel fromModel;
    private SpinnerNumberModel toModel;

    public IntervalField(int textFieldCols, String labelTxt, int minDefault, int maxDefault){

        label = new JLabel(labelTxt);

        fromSpinner = new JSpinner();
        toSpinner = new JSpinner();

        this.minDefault = minDefault; this.maxDefault = maxDefault;

        fromModel = new SpinnerNumberModel(minDefault, minDefault, maxDefault, 1);
        toModel = new SpinnerNumberModel(maxDefault, minDefault, maxDefault, 1);

        setSpinnerWidth(fromSpinner, textFieldCols);
        setSpinnerWidth(toSpinner, textFieldCols);

        setModels();
    }

    public void setToMaxDefault(int max){
        maxDefault = max;
        toModel.setMaximum(max);
        toModel.setValue(Math.min(max, getToValue()));
    }

    public void setFromMinDefault(int min){
        minDefault = min;
        fromModel.setMinimum(min);
        fromModel.setValue(Math.max(min, getFromValue()));
    }

    public void initFields(){
        fromModel.setValue(minDefault);
        toModel.setValue(maxDefault);
    }

    public void addMinChangeListener(ChangeListener listener){
        fromSpinner.addChangeListener(listener);
    }

    public void addMaxChangeListener(ChangeListener listener){
        toSpinner.addChangeListener(listener);
    }

    public int getFromValue(){
        return (int) fromSpinner.getValue();
    }

    public int getToValue(){
        return (int) toSpinner.getValue();
    }

    private void setModels(){
        fromSpinner.setModel(fromModel);
        toSpinner.setModel(toModel);
    }

    private void setSpinnerWidth(JSpinner spinner, int width){
        JFormattedTextField textField = getTextField(spinner);
        if (textField != null ) {
            textField.setColumns(width);
        }
    }

    private JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "
                    + spinner.getEditor().getClass()
                    + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    public void addFieldToGC(JPanel panel, GridBagConstraints gc, int y){

        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);
        addIntervalComponentToGC(gc, panel, y, insetLabel, insetField);

    }

    private void addIntervalComponentToGC(GridBagConstraints gc, JPanel panel, int y,
                                          Insets insetLabel, Insets insetField){
        addComponentToGC(gc, panel,0, y, 1, 0.2, insetLabel, 1, label, LINE_START);
        addComponentToGC(gc, panel, 1, y, 1, 0.2, insetField, 1, fromSpinner, LINE_START);
        addComponentToGC(gc, panel, 2, y, 1, 0.2, insetField, 1, new JLabel("and"), CENTER);
        addComponentToGC(gc, panel,3, y, 1, 0.2, insetField, 1, toSpinner, LINE_START);
    }

    private void addComponentToGC(GridBagConstraints gc, JPanel panel, int x, int y, double weightx, double weighty,
                                  Insets insets, int gridWidth, Component c, int anchor) {
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.anchor = anchor;
        gc.insets = insets;
        gc.gridwidth = gridWidth;

        panel.add(c , gc);
    }
}
