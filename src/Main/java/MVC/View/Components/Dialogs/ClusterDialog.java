package MVC.View.Components.Dialogs;

import Model.ClusterBy;
import Model.ClusterDenominator;
import MVC.Common.CSBFinderRequest;
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

    private JLabel clusterTypeLabel;
    private JLabel clusterDenominatorLabel;
    private JLabel familyClusterThresholdLabel;
    private JLabel thresholdLabel;

    private JList<ClusterBy> clusterTypeField;
    private JList<ClusterDenominator> clusterDenominatorField;
    private JSlider familyClusterThreshold;

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

        request.setFamilyClusterThreshold(familyClusterThreshold.getValue() / 10.0f);
        request.setClusterType(clusterTypeField.getSelectedValue());
        request.setClusterDenominator(clusterDenominatorField.getSelectedValue());

    }

    public void setRunListener(RunListener<CSBFinderRequest> runListener) {
        this.runListener = runListener;
    }

    private void initInputComponents() {

        clusterTypeField = new JList<>();

        clusterDenominatorField = new JList<>();

        familyClusterThreshold = new JSlider();

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

        // family cluster threshold
        thresholdLabel = new JLabel("0.8");
        thresholdLabel.setBorder(BorderFactory.createEtchedBorder());
        familyClusterThreshold.setModel(new DefaultBoundedRangeModel(8, 0, 0, 10));
        Hashtable<Integer, JLabel> table = new Hashtable<>();
        table.put(0, new JLabel("0"));
        table.put(10, new JLabel("1"));
        familyClusterThreshold.setLabelTable(table);
        familyClusterThreshold.setPaintLabels(true);
        familyClusterThreshold.addChangeListener(e -> {
            thresholdLabel.setText(String.valueOf(familyClusterThreshold.getValue() / 10.0));
        });

        // Cluster Type
        initEnumList(clusterTypeField, Arrays.asList(ClusterBy.values()));

        // Cluster Denominator
        initEnumList(clusterDenominatorField, Arrays.asList(ClusterDenominator.values()));


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
        addComponentToGC(1, y, 1, 0.2, insetField, familyClusterThreshold, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, thresholdLabel, LINE_START);

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
