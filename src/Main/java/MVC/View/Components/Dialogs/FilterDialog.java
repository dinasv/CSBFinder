package MVC.View.Components.Dialogs;

import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;
import MVC.View.Models.Filters.PatternStrand;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.GridBagConstraints.*;

/**
 */
public class FilterDialog extends JDialog{

    private final static int TEXT_FIELD_SIZE = 14;
    private final static int MIN_PATTERN_LENGTH_DEF = 2;
    private final static int MAX_PATTERN_LENGTH_DEF = Integer.MAX_VALUE;
    private final static int MIN_SCORE_DEF = 0;
    private final static int MAX_SCORE_DEF = Integer.MAX_VALUE;
    private final static int MIN_COUNT_DEF = 1;
    private final static int MAX_COUNT_DEF = Integer.MAX_VALUE;
    private final static PatternStrand PATTERN_STRAND_DEF = PatternStrand.ALL;
    private final static String PATTERN_IDS_DEF = "";
    private final static String FAMILY_IDS_DEF = "";
    private final static String PATTERN_GENES_DEF = "";

    private final JLabel PATTERN_LENGTH_LABEL = new JLabel("CSB length between:");
    private JSpinner minPatternLength;
    private JSpinner maxPatternLength;

    private final JLabel SCORE_LABEL = new JLabel("Score between:");
    private JSpinner minScore;
    private JSpinner maxScore;

    private final JLabel COUNT_LABEL = new JLabel("Instance count between:");
    private JSpinner minCount;
    private JSpinner maxCount;

    private final JLabel STRAND_LABEL = new JLabel("Patterns strand:");
    private ButtonGroup strandBtns;
    private JRadioButton allStrandTypesBtn;
    private JRadioButton multiStrandTypesBtn;
    private JRadioButton oneStrandTypesBtn;
    private JPanel patternStrandPanel;

    private final JLabel PATTERN_ID_LABEL = new JLabel("Pattern IDs:");
    private JTextField patternIds;

    private final JLabel FAMILY_ID_LABEL = new JLabel("Family IDs:");
    private JTextField familyIds;

    private final JLabel PATTERN_GENES_LABEL = new JLabel("Pattern with genes:");
    private JTextField patternGenes;

    private GridBagConstraints gc;

    private JButton applyFilter;
    private JButton clearAll;

    private RunListener<FilterRequest> applyFilterListener;

    private JPanel fields;

    private FilterRequest request;

    public FilterDialog(){

        setTitle("Filter Table");

        request = new FilterRequest();

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
        addChangeListeners();

        JPanel buttons = new JPanel();
        buttons.add(applyFilter);
        buttons.add(clearAll);

        setLayout(new BorderLayout());
        add(fields, BorderLayout.PAGE_START);
        add(buttons, BorderLayout.LINE_END);
        pack();
    }

    private void initStrandBtns(){

        strandBtns = new ButtonGroup();
        allStrandTypesBtn = new JRadioButton(PatternStrand.ALL.description);
        allStrandTypesBtn.setActionCommand(PatternStrand.ALL.toString());

        multiStrandTypesBtn = new JRadioButton(PatternStrand.MULTI_STRAND.description);
        multiStrandTypesBtn.setActionCommand(PatternStrand.MULTI_STRAND.toString());

        oneStrandTypesBtn = new JRadioButton(PatternStrand.SINGLE_STRAND.description);
        oneStrandTypesBtn.setActionCommand(PatternStrand.SINGLE_STRAND.toString());

        strandBtns.add(allStrandTypesBtn);
        strandBtns.add(multiStrandTypesBtn);
        strandBtns.add(oneStrandTypesBtn);

        //Put the radio buttons in a column in a panel.
        patternStrandPanel = new JPanel(new FlowLayout());
        patternStrandPanel.add(allStrandTypesBtn);
        patternStrandPanel.add(multiStrandTypesBtn);
        patternStrandPanel.add(oneStrandTypesBtn);

    }

    private void addChangeListeners(){

        addBtnListener(allStrandTypesBtn);
        addBtnListener(multiStrandTypesBtn);
        addBtnListener(oneStrandTypesBtn);

        minPatternLength.addChangeListener(e -> request.setMinCSBLength((int)minPatternLength.getValue()));
        maxPatternLength.addChangeListener(e -> request.setMaxCSBLength((int)maxPatternLength.getValue()));

        minScore.addChangeListener(e -> request.setMinScore((int)minScore.getValue()));
        maxScore.addChangeListener(e -> request.setMaxScore((int)maxScore.getValue()));

        minCount.addChangeListener(e -> request.setMinInstanceCount((int)minCount.getValue()));
        maxCount.addChangeListener(e -> request.setMinInstanceCount((int)maxCount.getValue()));

        patternIds.getDocument().addDocumentListener((TextChangeListener) e ->
                request.setPatternIds(patternIds.getText()));

        familyIds.getDocument().addDocumentListener((TextChangeListener) e -> {
            request.setFamilyIds(familyIds.getText());
        });

        patternGenes.getDocument().addDocumentListener((TextChangeListener) e ->
                request.setPatternGenes(patternGenes.getText()));
    }

    private void addBtnListener(JRadioButton btn){
        btn.addChangeListener(e ->
                request.setPatternStrand(PatternStrand.valueOf(btn.getActionCommand())));

    }

    private void setClearAllActionListner(){
        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initFields();
                request.initAllFields();
            }
        });
    }

    private void setApplyFilterActionListener(){
        applyFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                RunEvent<FilterRequest> runEvent = new RunEvent<>(this, request);

                if (applyFilterListener != null) {
                    applyFilterListener.runEventOccurred(runEvent);
                }
            }
        });
    }

    private void initComponents(){

        initStrandBtns();

        minPatternLength = new JSpinner();
        minPatternLength.addChangeListener(e -> request.setMinCSBLength((int)minPatternLength.getValue()));
        maxPatternLength = new JSpinner();
        maxPatternLength.addChangeListener(e -> request.setMaxCSBLength((int)maxPatternLength.getValue()));

        minScore = new JSpinner();
        minScore.addChangeListener(e -> request.setMinScore((int)minScore.getValue()));
        maxScore = new JSpinner();
        maxScore.addChangeListener(e -> request.setMaxScore((int)maxScore.getValue()));

        minCount = new JSpinner();
        minCount.addChangeListener(e -> request.setMinInstanceCount((int)minCount.getValue()));
        maxCount = new JSpinner();
        maxCount.addChangeListener(e -> request.setMinInstanceCount((int)maxCount.getValue()));

        patternIds = new JTextField();
        patternIds.getDocument().addDocumentListener((TextChangeListener) e ->
                request.setPatternIds(patternIds.getText()));

        familyIds = new JTextField();
        familyIds.getDocument().addDocumentListener((TextChangeListener) e -> {
                request.setFamilyIds(familyIds.getText());
        });

        patternGenes = new JTextField();
        patternGenes.getDocument().addDocumentListener((TextChangeListener) e ->
                request.setPatternGenes(patternGenes.getText()));

    }

    private void initFields(){

        allStrandTypesBtn.setSelected(true);

        minPatternLength.setModel(new SpinnerNumberModel(MIN_PATTERN_LENGTH_DEF, MIN_PATTERN_LENGTH_DEF,
                MAX_PATTERN_LENGTH_DEF, 1));
        maxPatternLength.setModel(new SpinnerNumberModel(MAX_PATTERN_LENGTH_DEF, MIN_PATTERN_LENGTH_DEF,
                MAX_PATTERN_LENGTH_DEF, 1));

        minScore.setModel(new SpinnerNumberModel(MIN_SCORE_DEF, MIN_SCORE_DEF, MAX_SCORE_DEF, 1));
        maxScore.setModel(new SpinnerNumberModel(MAX_SCORE_DEF, MIN_SCORE_DEF, MAX_SCORE_DEF, 1));

        minCount.setModel(new SpinnerNumberModel(MIN_COUNT_DEF, MIN_COUNT_DEF, MAX_COUNT_DEF, 1));
        maxCount.setModel(new SpinnerNumberModel(MAX_COUNT_DEF, MIN_COUNT_DEF, MAX_COUNT_DEF, 1));

        patternIds.setText(PATTERN_IDS_DEF);
        familyIds.setText(FAMILY_IDS_DEF);
        patternGenes.setText(PATTERN_GENES_DEF);
    }

    private void addComponentsToGC(){
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);
        int y = 0;

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,  STRAND_LABEL, LINE_START);
        addComponentToGC(1, y++, 1, 0.6, insetField,3, patternStrandPanel, LINE_START);

        addIntervalComponentToGC(y++, PATTERN_LENGTH_LABEL, minPatternLength, maxPatternLength, insetLabel, insetField);
        addIntervalComponentToGC(y++, SCORE_LABEL, minScore, maxScore, insetLabel, insetField);
        addIntervalComponentToGC(y++, COUNT_LABEL, minCount, maxCount, insetLabel, insetField);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1, PATTERN_ID_LABEL, LINE_START);
        patternIds.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, patternIds, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1, PATTERN_GENES_LABEL, LINE_START);
        patternGenes.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, patternGenes, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1, FAMILY_ID_LABEL, LINE_START);
        familyIds.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, familyIds, LINE_START);
    }

    private void addIntervalComponentToGC(int y, JLabel label, JSpinner minSpinner, JSpinner maxSpinner,
                                          Insets insetLabel, Insets insetField){

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1, label, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, 1, minSpinner, LINE_START);
        addComponentToGC(2, y, 1, 0.2, insetField, 1, new JLabel("and"), CENTER);
        addComponentToGC(3, y, 1, 0.2, insetField, 1, maxSpinner, LINE_START);
    }

    private void addComponentToGC(int x, int y, double weightx, double weighty, Insets insets, int gridWidth, Component c, int anchor) {
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = weightx;
        gc.weighty = weighty;
        gc.anchor = anchor;
        gc.insets = insets;
        gc.gridwidth = gridWidth;
        fields.add(c , gc);
    }

    public void setApplyFilterListener(RunListener applyFilterListener){
        this.applyFilterListener = applyFilterListener;
    }

    private interface TextChangeListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }

}
