package MVC.View.Components.Dialogs;

import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;
import MVC.View.Models.Filters.PatternStrand;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.*;

/**
 */
public class FilterDialog extends JDialog{

    private final JLabel CSB_LENGTH_LABEL = new JLabel("CSB length between:");
    private JSpinner minCSBLength;
    private JSpinner maxCSBLength;

    private final JLabel SCORE_LABEL = new JLabel("Score between:");
    private JSpinner minScore;
    private JSpinner maxScore;

    private final JLabel COUNT_LABEL = new JLabel("Instance count between:");
    private JSpinner minCount;
    private JSpinner maxCount;

    private final JLabel STRAND_LABEL = new JLabel("Patterns strand:");
    private ButtonGroup strandBtns;
    private JRadioButton allStrandTypesBtn;
    private JPanel patternStrandPanel;

    private final JLabel PATTERN_ID_LABEL = new JLabel("Pattern ID:");
    private JTextField patternId;

    private final JLabel PATTERN_GENES_LABEL = new JLabel("Pattern with genes:");
    private JTextField patternGenes;

    private GridBagConstraints gc;

    private JButton applyFilter;
    private JButton clearAll;

    private RunListener<FilterRequest> applyFilterListener;

    private JPanel fields;


    public FilterDialog(){

        setTitle("Filter Table");

        //buttons
        applyFilter = new JButton("Apply");
        setApplyFilterActionListener();

        clearAll = new JButton("Clear All");
        setClearAllActionListner();

        //fields panel
        fields = new JPanel();
        fields.setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        initComponents();
        addComponentsToGC();
        initFields();

        JPanel buttons = new JPanel();
        buttons.add(applyFilter);
        buttons.add(clearAll);

        setLayout(new GridLayout(3,0));
        add(patternStrandPanel);
        add(fields);
        add(buttons);
        pack();
    }

    private void initStrandBtns(){

        strandBtns = new ButtonGroup();
        allStrandTypesBtn = new JRadioButton(PatternStrand.ALL.description);
        allStrandTypesBtn.setActionCommand(PatternStrand.ALL.toString());

        JRadioButton btn2 = new JRadioButton(PatternStrand.MULTI_STRAND.description);
        btn2.setActionCommand(PatternStrand.MULTI_STRAND.toString());

        JRadioButton btn3 = new JRadioButton(PatternStrand.SINGLE_STRAND.description);
        btn3.setActionCommand(PatternStrand.SINGLE_STRAND.toString());

        strandBtns.add(allStrandTypesBtn);
        strandBtns.add(btn2);
        strandBtns.add(btn3);

        //Put the radio buttons in a column in a panel.
        patternStrandPanel = new JPanel(new FlowLayout());
        patternStrandPanel.add(STRAND_LABEL);
        patternStrandPanel.add(allStrandTypesBtn);
        patternStrandPanel.add(btn2);
        patternStrandPanel.add(btn3);

    }

    private void setClearAllActionListner(){
        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initFields();
            }
        });
    }

    private void setApplyFilterActionListener(){
        applyFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FilterRequest request = new FilterRequest();
                initRequest(request);

                RunEvent<FilterRequest> runEvent = new RunEvent<>(this, request);

                if (applyFilterListener != null) {
                    applyFilterListener.runEventOccurred(runEvent);
                }
            }
        });
    }

    private void initRequest(FilterRequest filterRequest){

        filterRequest.setMinCSBLength((int)minCSBLength.getValue());
        filterRequest.setMaxCSBLength((int)maxCSBLength.getValue());
        filterRequest.setMinScore((int)minScore.getValue());
        filterRequest.setMaxScore((int)maxScore.getValue());
        filterRequest.setMaxInstanceCount((int)maxCount.getValue());
        filterRequest.setMinInstanceCount((int)minCount.getValue());
        filterRequest.setPatternId(patternId.getText());
        filterRequest.setPatternGenes(patternGenes.getText());

        filterRequest.setPatternStrand(PatternStrand.valueOf(strandBtns.getSelection().getActionCommand()));
    }

    private void initComponents(){

        initStrandBtns();

        minCSBLength = new JSpinner();
        maxCSBLength = new JSpinner();
        minScore = new JSpinner();
        maxScore = new JSpinner();
        minCount = new JSpinner();
        maxCount = new JSpinner();

        patternId = new JTextField();
        patternGenes = new JTextField();
    }

    private void initFields(){

        allStrandTypesBtn.setSelected(true);

        minCSBLength.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        maxCSBLength.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 2, Integer.MAX_VALUE, 1));

        minScore.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        maxScore.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 1));

        minCount.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        maxCount.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 1));

        patternId.setText("");
        patternGenes.setText("");
    }

    private void addComponentsToGC(){
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);
        int y = 0;

        addIntervalComponentToGC(y++, CSB_LENGTH_LABEL, minCSBLength, maxCSBLength, insetLabel, insetField);
        addIntervalComponentToGC(y++, SCORE_LABEL, minScore, maxScore, insetLabel, insetField);
        addIntervalComponentToGC(y++, COUNT_LABEL, minCount, maxCount, insetLabel, insetField);

        addComponentToGC(0, y, 1, 0.2, insetLabel, PATTERN_ID_LABEL, LINE_START);
        patternId.setColumns(12);
        addComponentToGC(1, y++, 1, 0.2, insetLabel, patternId, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, PATTERN_GENES_LABEL, LINE_START);
        patternGenes.setColumns(12);
        addComponentToGC(1, y++, 1, 0.2, insetLabel, patternGenes, LINE_START);
    }

    private void addIntervalComponentToGC(int y, JLabel label, JSpinner minSpinner, JSpinner maxSpinner,
                                          Insets insetLabel, Insets insetField){

        addComponentToGC(0, y, 1, 0.2, insetLabel, label, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, minSpinner, LINE_START);
        addComponentToGC(2, y, 1, 0.2, insetField, new JLabel("and"), CENTER);
        addComponentToGC(3, y, 1, 0.2, insetField, maxSpinner, LINE_START);
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

    public void setApplyFilterListener(RunListener applyFilterListener){
        this.applyFilterListener = applyFilterListener;
    }
}
