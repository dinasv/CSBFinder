package MVC.View.Components.Dialogs;

import Model.ClusterBy;
import Model.ClusterDenominator;
import MVC.View.Requests.CSBFinderRequest;
import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.LINE_START;

public class ClusterDialog extends JDialog {

    private final static double CLUSTER_THRESHOLD = 0.8;

    private JLabel clusterTypeLabel;
    private JLabel clusterDenominatorLabel;
    private JLabel familyClusterThresholdLabel;
    private JLabel thresholdLabel;

    private JList<ClusterBy> clusterTypeField;
    private JList<ClusterDenominator> clusterDenominatorField;
    private JSpinner familyClusterThresholdSpinner;
    private JSlider familyClusterThresholdSlider;

    private JButton apply;
    private RunListener<CSBFinderRequest> runListener;

    private GridBagConstraints gc;

    private ImageIcon questionMark;
    private JPanel fields;

    public ClusterDialog() {

        this.questionMark = MVC.View.Images.Icon.QUESTION_MARK.getIcon();
        setTitle("Cluster to families");

        initLabels();
        initInputComponents();

        fields = new JPanel();
        fields.setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        apply = new JButton("Apply");
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CSBFinderRequest request = new CSBFinderRequest();
                initRequest(request);

                RunEvent<CSBFinderRequest> runEvent = new RunEvent<>(this, request);

                if (runListener != null) {
                    runListener.runEventOccurred(runEvent);
                }
            }
        });

        initFields();
        addFieldsToGC();

        setLayout(new BorderLayout());
        add(fields, BorderLayout.PAGE_START);
        add(apply, BorderLayout.LINE_END);

        pack();
    }

    private void initRequest(CSBFinderRequest request) {

        request.setFamilyClusterThreshold((double)familyClusterThresholdSpinner.getValue());
        request.setClusterType(clusterTypeField.getSelectedValue());
        request.setClusterDenominator(clusterDenominatorField.getSelectedValue());

    }

    public void setRunListener(RunListener<CSBFinderRequest> runListener) {
        this.runListener = runListener;
    }

    private void initInputComponents() {

        clusterTypeField = new JList<>();

        clusterDenominatorField = new JList<>();

        familyClusterThresholdSlider = new JSlider();

        familyClusterThresholdSpinner = new JSpinner();

    }


    private void initLabels() {

        ImageIcon icon = questionMark;

        String labelName;
        String desc;

        labelName = "Family Clustering Threshold";
        desc = "Threshold used in the process of clustering CSBs to families.";
        familyClusterThresholdLabel = initLabel(icon, labelName, desc);

        labelName = "Cluster CSBs By";
        desc = "In the greedy CSB clustering to families, CSBs are sorted based on 'score' or 'length'.";
        clusterTypeLabel = initLabel(icon, labelName, desc);

        labelName = "Clustering denominator";
        desc = "In the greedy CSB clustering to families, CSBs is added to an existing cluster if their intersection/X " +
                "is above a threshold.";
        clusterDenominatorLabel = initLabel(icon, labelName, desc);

    }

    private JLabel initLabel(ImageIcon icon, String label_name, String desc){
        JLabel label = new JLabel(label_name + ": ", icon, JLabel.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setToolTipText(desc);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        return label;
    }


    public void initFields() {

        setSpinnerSliderDoubleModel(familyClusterThresholdSlider, familyClusterThresholdSpinner, CLUSTER_THRESHOLD);

        // Cluster Type
        initEnumList(clusterTypeField, Arrays.asList(ClusterBy.values()));

        // Cluster Denominator
        initEnumList(clusterDenominatorField, Arrays.asList(ClusterDenominator.values()));

    }

    private void setSpinnerSliderDoubleModel(JSlider slider, JSpinner spinner, double defaultVal){
        int min = 0;
        int max = 100;

        slider.setModel(new DefaultBoundedRangeModel((int)(defaultVal*max), 0, min, max));

        Hashtable<Integer, JLabel> table = new Hashtable<>();
        table.put(min, new JLabel(String.valueOf(0)));
        table.put(max, new JLabel(String.valueOf(1)));
        slider.setLabelTable(table);

        slider.setPaintLabels(true);

        spinner.setModel(new SpinnerNumberModel(defaultVal, 0, 1, 0.05));
        setSpinnerWidth(spinner, 3);

        spinner.addChangeListener(e -> slider.setValue((int)((double)spinner.getValue()*max)));

        slider.addChangeListener(e -> spinner.setValue((double) slider.getValue()/max));

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


    private void setSpinnerWidth(JSpinner spinner, int width){
        JFormattedTextField textField = getTextField(spinner);
        if (textField != null ) {
            textField.setColumns(width);
        }
    }

    private <T extends Enum> void initEnumList(JList<T> jList, List<T> enumValues){
        DefaultListModel<T> model = new DefaultListModel<>();
        for (T e : enumValues){
            model.addElement(e);
        }
        jList.setModel(model);
        jList.setSelectedIndex(0);
    }

    private void addFieldsToGC() {
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);

        int y = 0;

        addComponentToGC(0, y, 1, 0.2, insetLabel, familyClusterThresholdLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, familyClusterThresholdSlider, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, familyClusterThresholdSpinner, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, clusterTypeLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, clusterTypeField, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 1, insetField, new JLabel(""), FIRST_LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, clusterDenominatorLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, clusterDenominatorField, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 1, insetField, new JLabel(""), FIRST_LINE_START);
        //addComponentToGC(1, y, 1, 2, insetField, apply, LINE_START);
    }

    private void addComponentToGC(int x, int y, double weightx, double weighty, Insets insets, Component c, int anchor) {
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.anchor = anchor;
        gc.insets = insets;
        fields.add(c , gc);
    }


}
