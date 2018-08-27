package MVC.View;

import MVC.Common.CSBFinderRequest;
import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

import static java.awt.GridBagConstraints.*;

public class InputPanel extends JPanel {

    private JLabel clusterTypeLabel;
    private JLabel quorumLabel;
    private JLabel numOfInsertionsLabel;
    private JLabel quorumWithoutInsertionsLabel;
    private JLabel minCSBLengthLabel;
    private JLabel maxCSBLengthLabel;
    private JLabel datasetNameLabel;
    private JLabel patternFilePathLabel;
    private JLabel geneInfoFilePathLabel;
    private JLabel bcountLabel;
    private JLabel familyClusterThresholdLabel;


    private JList clusterTypeField;
    private JSpinner quorum;
    private RunListener runListener;
    private JSpinner numOfInsertions;
    private JSpinner quorumWithoutInsertions;
    private JSpinner minCSBLength;
    private JSpinner maxCSBLength;
    private JTextField datasetName;
    private JTextField patternFilePath;
    private JTextField geneInfoFilePath;
    private JCheckBox bcount;
    private JSlider familyClusterThreshold;

    private JLabel thresholdLabel;

    private JButton loadPatternBtn;
    private JButton loadGeneInfoBtn;

    private JButton run;

    private GridBagConstraints gc;

    public InputPanel() {
        setMinimumSize(new Dimension(450, 800));

        initLabels();
        initInputComponents();

        run = new JButton("Run");
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CSBFinderRequest request = new CSBFinderRequest();
                initRequest(request);

                RunEvent runEvent = new RunEvent(this, request);

                if (runListener != null) {
                    runListener.runEventOccurred(runEvent);
                }
            }
        });

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Parameters"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        setLayout(new GridBagLayout());

        gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        initFields();
        addFieldsToGC();

    }

    private void initRequest(CSBFinderRequest request) {
        request.setNumberOfInsertions((int) numOfInsertions.getValue());
        request.setQuorum((int) quorum.getValue());
        request.setQuorumWithoutInsertions((int) quorumWithoutInsertions.getValue());
        request.setMinimalCSBLength((int) minCSBLength.getValue());
        request.setMaximumCSBLength((int) maxCSBLength.getValue());
        request.setDatasetName(datasetName.getText());
        String patternPath = patternFilePath.getText();
        request.setCsbPatternFilePath("optional".equals(patternPath) || "".equals(patternPath) ? null : patternFilePath.getText());
        String geneInfoPath = geneInfoFilePath.getText();
        request.setGeneInfoFilePath("optional".equals(geneInfoPath) || "".equals(geneInfoPath) ? null : geneInfoFilePath.getText());
        request.setMultCount(bcount.isSelected());
        request.setFamilyClusterThreshold(familyClusterThreshold.getValue() / 10.0f);
        request.setClusterType((String) clusterTypeField.getSelectedValue());
    }

    public void setRunListener(RunListener runListener) {
        this.runListener = runListener;
    }

    private void initInputComponents() {

        clusterTypeField = new JList();

        quorum = new JSpinner();

        numOfInsertions = new JSpinner();

        quorumWithoutInsertions = new JSpinner();

        minCSBLength = new JSpinner();

        maxCSBLength = new JSpinner();

        datasetName = new JTextField();

        patternFilePath = new JTextField();

        geneInfoFilePath = new JTextField();

        bcount = new JCheckBox();
        
        familyClusterThreshold = new JSlider();

        loadPatternBtn = new JButton("Load File");
        loadPatternBtn.addActionListener(e -> {
            File f = choseFile();
            if (f != null && f.exists() && !f.isDirectory()) {
                patternFilePath.setText(f.getPath());
            }
        });
        loadGeneInfoBtn = new JButton("Load File");
        loadGeneInfoBtn.addActionListener(e -> {
            File f = choseFile();
            if (f != null && f.exists() && !f.isDirectory()) {
                geneInfoFilePath.setText(f.getPath());
            }
        });

    }

    private File choseFile() {
        File f = null;
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            f = fc.getSelectedFile();
        }

        return f;
    }

    private void initLabels() {
        quorumLabel = new JLabel("Quorum: ");
        numOfInsertionsLabel = new JLabel("Insertions Allowed: ");
        quorumWithoutInsertionsLabel = new JLabel("Quorum without insertions: ");
        minCSBLengthLabel = new JLabel("CSB Min Length: ");
        maxCSBLengthLabel = new JLabel("CSB Max Length: ");
        datasetNameLabel = new JLabel("Dataset Name: ");
        patternFilePathLabel = new JLabel("CSB Patterns File: ");
        geneInfoFilePathLabel = new JLabel("Gene Orthology Info File: ");
        bcountLabel = new JLabel("Count One Instance Per Sequence: ");
        familyClusterThresholdLabel = new JLabel("Family Clustering Threshold: ");
        clusterTypeLabel = new JLabel("Cluster By: ");
    }

    private void initFields() {

        // Quorum
        quorum.setModel(new SpinnerNumberModel(1, 1, 2000, 1));
        ((JSpinner.DefaultEditor) quorum.getEditor()).getTextField().setColumns(3);

        // Number Of Insertions
        numOfInsertions.setModel(new SpinnerNumberModel(0, 0, 5, 1));
        ((JSpinner.DefaultEditor)numOfInsertions.getEditor()).getTextField().setColumns(3);

        // Quorum without insertions
        quorumWithoutInsertions.setModel(new SpinnerNumberModel(1, 1, 5, 1));
        ((JSpinner.DefaultEditor)quorumWithoutInsertions.getEditor()).getTextField().setColumns(3);

        // CSB min length
        minCSBLength.setModel(new SpinnerNumberModel(2, 2, 100, 1));

        // CSB max length
        maxCSBLength.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 2, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor)maxCSBLength.getEditor()).getTextField().setColumns(8);

        // dataset name
        datasetName.setText("dataset");
        datasetName.setColumns(10);

        // csb pattern file path
        patternFilePath.setText("optional");
        patternFilePath.setColumns(15);

        // gene family information file path
        geneInfoFilePath.setText("optional");
        geneInfoFilePath.setColumns(15);

        // bcount
        bcount.setSelected(true);

        // family cluster threshold
        thresholdLabel = new JLabel("0.8");
        thresholdLabel.setBorder(BorderFactory.createEtchedBorder());
        familyClusterThreshold.setModel(new DefaultBoundedRangeModel(8, 0, 0, 10));
        Hashtable table = new Hashtable();
        table.put(new Integer(0), new JLabel("0"));
        table.put(new Integer(10), new JLabel("1"));
        familyClusterThreshold.setLabelTable(table);
        familyClusterThreshold.setPaintLabels(true);
        familyClusterThreshold.addChangeListener(e -> {
            thresholdLabel.setText(String.valueOf(familyClusterThreshold.getValue() / 10.0));
        });

        // Cluster Type
        DefaultListModel clusterModel = new DefaultListModel();
        clusterModel.addElement("Score");
        clusterModel.addElement("Length");
        clusterTypeField.setModel(clusterModel);
        clusterTypeField.setSelectedIndex(0);
    }

    private void addFieldsToGC() {
        Insets insetLabel = new Insets(0, 0, 0, 5);
        Insets insetField = new Insets(0, 0, 0, 0);

        int y = 0;
        addComponentToGC(0, y, 1, 0.1, insetLabel, quorumLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, quorum, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, numOfInsertionsLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, numOfInsertions, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, quorumWithoutInsertionsLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, quorumWithoutInsertions, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, minCSBLengthLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, minCSBLength, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, maxCSBLengthLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, maxCSBLength, FIRST_LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, datasetNameLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, datasetName, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, patternFilePathLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, patternFilePath, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, loadPatternBtn, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, geneInfoFilePathLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, geneInfoFilePath, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, loadGeneInfoBtn, LINE_START);

        addComponentToGC(0, y, 1, 0.1, insetLabel, bcountLabel, LINE_START);
        addComponentToGC(1, y++, 1, 0.1, insetField, bcount, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, familyClusterThresholdLabel, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, familyClusterThreshold, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, thresholdLabel, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, clusterTypeLabel, FIRST_LINE_START);
        addComponentToGC(1, y++, 1, 0.2, insetField, clusterTypeField, FIRST_LINE_START);

        addComponentToGC(1, y, 1, 2, insetField, run, FIRST_LINE_START);
    }

    private void addComponentToGC(int x, int y, double weightx, double weighty, Insets insets, Component c, int anchor) {
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.anchor = anchor;
        gc.insets = insets;
        add(c , gc);
    }

}
