package MVC.View.Components.Dialogs;

import Model.AlgorithmType;
import Model.ClusterBy;
import Model.ClusterDenominator;
import MVC.Common.CSBFinderRequest;
import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.util.Hashtable;
import java.util.*;
import java.util.List;

import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.LINE_START;

public class InputParametersDialog extends JDialog {

    private JLabel clusterTypeLabel;
    private JLabel clusterDenominatorLabel;
    private JLabel quorumLabel;
    private JLabel numOfInsertionsLabel;
    //private JLabel quorumWithoutInsertionsLabel;
    private JLabel minCSBLengthLabel;
    private JLabel maxCSBLengthLabel;
    private JLabel patternFilePathLabel;
    //private JLabel bcountLabel;
    private JLabel familyClusterThresholdLabel;
    private JLabel segmentToDirectonsLabel;
    private JLabel algorithmLabel;
    private JLabel thresholdLabel;

    private JList<ClusterBy> clusterTypeField;
    private JList<ClusterDenominator> clusterDenominatorField;
    private JList<AlgorithmType> algorithmField;
    private JSpinner quorum;
    private JSlider quorumSlider;
    private JSpinner numOfInsertions;
    //private JSpinner quorumWithoutInsertions;
    //private JSlider quorumWithoutInsertionsSlider;
    private JSpinner minCSBLength;
    private JSpinner maxCSBLength;
    private JTextField patternFilePath;
    //private JCheckBox bcount;
    private JSlider familyClusterThreshold;
    private JCheckBox segmentToDirectons;
    private JButton loadPatternBtn;

    private JButton run;
    private RunListener<CSBFinderRequest> runListener;

    private GridBagConstraints gc;

    private JFileChooser fc;

    private ImageIcon questionMark;

    private JPanel fields;

    public InputParametersDialog(JFileChooser fc) {

        this.fc = fc;
        this.questionMark = MVC.View.Images.Icon.QUESTION_MARK.getIcon();
        setTitle("Parameters");

        initLabels();
        initInputComponents();

        run = new JButton("Run");
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CSBFinderRequest request = new CSBFinderRequest();
                initRequest(request);

                RunEvent<CSBFinderRequest> runEvent = new RunEvent<>(this, request);

                if (runListener != null) {
                    runListener.runEventOccurred(runEvent);
                }
            }
        });

        fields = new JPanel();
        fields.setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        initFields();
        addFieldsToGC();

        setLayout(new BorderLayout());
        add(fields, BorderLayout.PAGE_START);
        add(run, BorderLayout.LINE_END);

        pack();
    }

    private void initRequest(CSBFinderRequest request) {
        request.setNumberOfInsertions((int) numOfInsertions.getValue());
        request.setQuorum((int) quorum.getValue());
        //request.setQuorumWithoutInsertions((int) quorumWithoutInsertions.getValue());
        request.setMinimalCSBLength((int) minCSBLength.getValue());
        request.setMaximumCSBLength((int) maxCSBLength.getValue());
        String patternPath = patternFilePath.getText();
        request.setCsbPatternFilePath("optional".equals(patternPath) || "".equals(patternPath) ? null : patternFilePath.getText());
        //request.setMultCount(bcount.isSelected());
        request.setFamilyClusterThreshold(familyClusterThreshold.getValue() / 10.0f);
        request.setClusterType(clusterTypeField.getSelectedValue());
        request.setAlgorithm(algorithmField.getSelectedValue());
        request.setNonDirectons(!segmentToDirectons.isSelected());
        request.setClusterDenominator(clusterDenominatorField.getSelectedValue());

    }

    public void setRunListener(RunListener<CSBFinderRequest> runListener) {
        this.runListener = runListener;
    }

    private void initInputComponents() {

        clusterTypeField = new JList<>();
        clusterDenominatorField = new JList<>();
        algorithmField = new JList<>();

        quorum = new JSpinner();
        quorumSlider = new JSlider();

        numOfInsertions = new JSpinner();

        //quorumWithoutInsertions = new JSpinner();
        //quorumWithoutInsertionsSlider = new JSlider();

        minCSBLength = new JSpinner();

        maxCSBLength = new JSpinner();

        patternFilePath = new JTextField();

        //bcount = new JCheckBox();

        segmentToDirectons = new JCheckBox();

        familyClusterThreshold = new JSlider();

        loadPatternBtn = new JButton("Load File");
        loadPatternBtn.addActionListener(e -> {
            File f = chooseFile();
            if (f != null && f.exists() && !f.isDirectory()) {
                patternFilePath.setText(f.getPath());
            }
        });

    }

    private File chooseFile() {
        File f = null;
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f = fc.getSelectedFile();
        }

        return f;
    }

    private void initLabels() {

        ImageIcon icon = questionMark;

        String labelName = "Quorum";
        String desc = "Minimal number of input sequences that must contain a CSB instance.";
        quorumLabel = initLabel(icon, labelName, desc);

        labelName = "Insertions Allowed";
        desc = "Maximal number of insertions allowed in a CSB instance.";
        numOfInsertionsLabel = initLabel(icon, labelName, desc);

        labelName = "Quorum without insertions";
        desc = "Minimal number of input sequences that must contain a CSB instance with no insertions.";
        //quorumWithoutInsertionsLabel = initLabel(icon, labelName, desc);

        labelName = "CSB Min Length";
        desc = "Minimal length (number of genes) of a CSB.";
        minCSBLengthLabel = initLabel(icon, labelName, desc);

        labelName = "CSB Max Length";
        desc = "Maximal length (number of gene) of a CSB.";
        maxCSBLengthLabel = initLabel(icon, labelName, desc);

        labelName = "CSB Patterns File";
        desc = "If this option is used, CSBs are no longer extracted from the input sequences. " +
                "It specifies specific CSB patterns which the user is interested to find in the input sequences.";
        patternFilePathLabel = initLabel(icon, labelName, desc);

        labelName = "Count One Instance Per Sequence";
        desc = "If checked, CSB count indicates the number of input sequences with an instance, " +
                "rather than the total number of instances.";
        //bcountLabel = initLabel(icon, labelName, desc);

        labelName = "Family Clustering Threshold";
        desc = "Threshold used in the process of clustering CSBs to families.";
        familyClusterThresholdLabel = initLabel(icon, labelName, desc);

        labelName = "Cluster CSBs By";
        desc = "In the greedy CSB clustering to families, CSBs are sorted based on 'score' or 'length'.";
        clusterTypeLabel = initLabel(icon, labelName, desc);

        labelName = "Clustering denominator";
        desc = "In the greedy CSB clustering to families, a CSB is added to an existing cluster if the " +
                "(intersection between the CSB and the Cluster genes/X) is above a threshold. Choose X.";
        clusterDenominatorLabel = initLabel(icon, labelName, desc);

        labelName = "Algorithm";
        desc = "The algorithm used for finding CSBs.";
        algorithmLabel = initLabel(icon, labelName, desc);

        labelName = "Segment genomes to directons";
        desc = "If checked, genomes will be segmented to directons - consecutive genes on the same strand.";
        segmentToDirectonsLabel = initLabel(icon, labelName, desc);

    }

    private JLabel initLabel(ImageIcon icon, String label_name, String desc){
        JLabel label = new JLabel(label_name + ": ", icon, JLabel.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setToolTipText(desc);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        return label;
    }


    public void initFields() {

        // Quorum
        quorum.setModel(new SpinnerNumberModel(1, 1, 2000, 1));
        ((JSpinner.DefaultEditor) quorum.getEditor()).getTextField().setColumns(3);
        quorumSlider.setModel(new DefaultBoundedRangeModel(1, 0, 1, 2000));
        quorum.addChangeListener(e -> quorumSlider.setValue((Integer) quorum.getValue()));
        quorumSlider.addChangeListener(e -> quorum.setValue(quorumSlider.getValue()));

        // Number Of Insertions
        numOfInsertions.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        ((JSpinner.DefaultEditor)numOfInsertions.getEditor()).getTextField().setColumns(3);

        // Quorum without insertions
        /*
        quorumWithoutInsertions.setModel(new SpinnerNumberModel(1, 1, 2000, 1));
        ((JSpinner.DefaultEditor)quorumWithoutInsertions.getEditor()).getTextField().setColumns(3);
        quorumWithoutInsertionsSlider.setModel(new DefaultBoundedRangeModel(1, 0, 1, 2000));
        quorumWithoutInsertions.addChangeListener(e -> quorumWithoutInsertionsSlider.setValue((Integer) quorumWithoutInsertions.getValue()));
        quorumWithoutInsertionsSlider.addChangeListener(e -> quorumWithoutInsertions.setValue(quorumWithoutInsertionsSlider.getValue()));
        */
        // CSB min length
        minCSBLength.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));

        // CSB max length
        maxCSBLength.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 2, Integer.MAX_VALUE, 1));

        // csb pattern file path
        patternFilePath.setText("optional");
        patternFilePath.setColumns(14);

        // bcount
        //bcount.setSelected(true);

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

        initEnumList(algorithmField, Arrays.asList(AlgorithmType.values()));

        //directon segmantation
        segmentToDirectons.setSelected(true);
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

        JLabel title = new JLabel("Basic Parameters:");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 20));

        addComponentToGC(0, y++, 1, 0.6, insetLabel, title, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, quorumLabel, LINE_START);
        addComponentToGC(2, y, 1, 0.2, insetField, quorum, LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, quorumSlider, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, numOfInsertionsLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, numOfInsertions, LINE_START);

        title = new JLabel("Advanced parameters:");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 20));
        addComponentToGC(0, y++, 1, 0.6, insetLabel, title, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, segmentToDirectonsLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, segmentToDirectons, LINE_START);

        /*
        addComponentToGC(0, y, 1, 0.2, insetLabel, quorumWithoutInsertionsLabel, LINE_START);
        addComponentToGC(2, y, 1, 0.2, insetField, quorumWithoutInsertions, LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, quorumWithoutInsertionsSlider, LINE_START);
        */
        addComponentToGC(0, y, 1, 0.1, insetLabel, minCSBLengthLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, minCSBLength, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, maxCSBLengthLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, maxCSBLength, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, patternFilePathLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, patternFilePath, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, loadPatternBtn, LINE_START);

        /*
        addComponentToGC(0, y, 1, 0.1, insetLabel, bcountLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, bcount, LINE_START);
        */
        addComponentToGC(0, y, 1, 0.2, insetLabel, algorithmLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, algorithmField, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 1, insetField, new JLabel(""), FIRST_LINE_START);

        title = new JLabel("Clustering to families parameters:");
        title.setFont(new Font(title.getFont().getName(), Font.BOLD, 16));
        addComponentToGC(0, y++, 1, 0.6, insetLabel, title, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, familyClusterThresholdLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, familyClusterThreshold, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, thresholdLabel, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, clusterTypeLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, clusterTypeField, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 1, insetField, new JLabel(""), FIRST_LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, clusterDenominatorLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, clusterDenominatorField, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 1, insetField, new JLabel(""), FIRST_LINE_START);

        addComponentToGC(1, y, 1, 2, insetField, run, LINE_START);
    }

    private void addComponentToGC(int x, int y, double weightx, double weighty, Insets insets, Component c, int anchor) {
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.anchor = anchor;
        gc.insets = insets;
        gc.gridwidth = 1;
        fields.add(c , gc);
    }

    public void setGenomeData(int numberOfGenomes, int maxGenomeSize) {
        if (numberOfGenomes > 0) {
            ((SpinnerNumberModel)quorum.getModel()).setMaximum(numberOfGenomes);
            quorumSlider.getModel().setMaximum(numberOfGenomes);
            //quorumWithoutInsertionsSlider.getModel().setMaximum(numberOfGenomes);

            ((SpinnerNumberModel)maxCSBLength.getModel()).setMaximum(maxGenomeSize);
            maxCSBLength.setValue(maxGenomeSize);
        }
    }

}
