package mvc.view.components.dialogs;

import mvc.view.components.fields.IntervalField;
import mvc.view.events.RunEvent;
import mvc.view.listeners.RunListener;
import mvc.view.tables.filters.PatternStrand;
import mvc.view.requests.FilterRequest;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

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
    private final static String FUNCTIONAL_CATEGORY_DEF = "";
    private final static int TEXT_FIELD_COLS = 13;

    private IntervalField patternLengthField;
    private IntervalField scoreField;
    private IntervalField countField;
    private IntervalField membersField;

    private final JLabel STRAND_LABEL = new JLabel("CSB strand:");
    private ButtonGroup strandBtns;
    private JRadioButton allStrandTypesBtn;
    private JRadioButton crossStrandTypesBtn;
    private JRadioButton oneStrandTypesBtn;
    private JPanel patternStrandPanel;

    private final String PATTERN_ID_LABEL = "CSB IDs:";
    private JTextField patternIds;

    private final String FAMILY_ID_LABEL = "Family IDs:";
    private JTextField familyIds;

    private final String PATTERN_GENES_LABEL = "CSB with gene IDs:";
    private JTextField patternGenes;
    private JComboBox<BooleanOperator> genesComboBox;

    private final String MAIN_FUNCTIONAL_CATEGORY_LABEL = "CSB main functional category:";
    private JTextField mainFunctionalCategory;
    private JComboBox<FunctionalCategoryOption> functionalCategoryComboBox;

    private final String PATTERN_GENES_CATEGORY_LABEL = "Keep CSBs with genes described as:";
    private JTextField genesCategory;
    private JComboBox<BooleanOperator> genesCategoryComboBox;

    private final String PATTERN_GENES_CATEGORY_EXCLUDE_LABEL = "Exclude CSBs with genes described as:";
    private JTextField genesCategoryExclude;

    private GridBagConstraints gc;

    private JButton applyFilter;
    private JButton clearAll;

    private RunListener<FilterRequest> applyFilterListener;

    private JPanel fields;

    private FilterRequest request;

    public FilterDialog(){

        setTitle("Filter");

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

        crossStrandTypesBtn = new JRadioButton(PatternStrand.CROSS_STRAND.description);
        crossStrandTypesBtn.setActionCommand(PatternStrand.CROSS_STRAND.toString());

        oneStrandTypesBtn = new JRadioButton(PatternStrand.SINGLE_STRAND.description);
        oneStrandTypesBtn.setActionCommand(PatternStrand.SINGLE_STRAND.toString());

        strandBtns.add(allStrandTypesBtn);
        strandBtns.add(crossStrandTypesBtn);
        strandBtns.add(oneStrandTypesBtn);

        //Put the radio buttons in a column in a panel.
        patternStrandPanel = new JPanel(new FlowLayout());
        patternStrandPanel.add(allStrandTypesBtn);
        patternStrandPanel.add(crossStrandTypesBtn);
        patternStrandPanel.add(oneStrandTypesBtn);

    }

    private void addChangeListeners(){

        addBtnListener(allStrandTypesBtn);
        addBtnListener(crossStrandTypesBtn);
        addBtnListener(oneStrandTypesBtn);

        patternLengthField.addMinChangeListener(e -> request.setMinCSBLength(patternLengthField.getFromValue()));
        patternLengthField.addMaxChangeListener(e -> request.setMaxCSBLength(patternLengthField.getToValue()));

        scoreField.addMinChangeListener(e -> request.setMinScore(scoreField.getFromValue()));
        scoreField.addMaxChangeListener(e -> request.setMaxScore(scoreField.getToValue()));

        countField.addMinChangeListener(e -> request.setMinInstanceCount(countField.getFromValue()));
        countField.addMaxChangeListener(e -> request.setMaxInstanceCount(countField.getToValue()));

        membersField.addMinChangeListener(e -> request.setMinFamilyMembers(membersField.getFromValue()));
        membersField.addMaxChangeListener(e -> request.setMaxFamilyMembers(membersField.getToValue()));

        addTextFieldListener(patternIds, request::setPatternIds);

        addTextFieldListener(familyIds, request::setFamilyIds);

        addTextFieldListener(patternGenes, request::setPatternGenes);
        genesComboBox.addActionListener(e ->
                request.setGenesOperator(genesComboBox.getItemAt(genesComboBox.getSelectedIndex())));


        addTextFieldListener(genesCategory, request::setGenesCategory);
        genesCategoryComboBox.addActionListener(e ->
                request.setGenesCategoryOperator(
                        genesCategoryComboBox.getItemAt(genesCategoryComboBox.getSelectedIndex())));

        addTextFieldListener(genesCategoryExclude, request::setGenesCategoryExclude);

        addTextFieldListener(mainFunctionalCategory, request::setMainFunctionalCategory);
        functionalCategoryComboBox.addActionListener(e ->
                request.setFunctionalCategoryOption(functionalCategoryComboBox.getItemAt(
                        functionalCategoryComboBox.getSelectedIndex())));


    }

    private void addTextFieldListener(JTextField textField, Consumer<String> requestFunc){
        textField.getDocument().addDocumentListener((TextChangeListener) e -> {
            requestFunc.accept(textField.getText());
        });
    }

    private void addBtnListener(JRadioButton btn){
        btn.addChangeListener(e ->
                request.setPatternStrand(PatternStrand.valueOf(btn.getActionCommand())));

    }

    private void setClearAllActionListner(){
        clearAll.addActionListener(e -> {
            initFields();
            request.initAllFields();
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

        String label = "CSB length between:";
        patternLengthField = new IntervalField(TEXT_FIELD_COLS, label, MIN_PATTERN_LENGTH_DEF, MAX_PATTERN_LENGTH_DEF);

        label = "Score between:";
        scoreField = new IntervalField(TEXT_FIELD_COLS, label, MIN_SCORE_DEF, MAX_SCORE_DEF);

        label = "Instance count between:";
        countField = new IntervalField(TEXT_FIELD_COLS, label, MIN_COUNT_DEF, MAX_COUNT_DEF);

        label = "Family members between:";
        membersField = new IntervalField(TEXT_FIELD_COLS, label, MIN_COUNT_DEF, MAX_COUNT_DEF);

        patternIds = new JTextField();

        familyIds = new JTextField();

        patternGenes = new JTextField();

        genesComboBox = new JComboBox<>(BooleanOperator.values());

        mainFunctionalCategory = new JTextField();

        functionalCategoryComboBox = new JComboBox<>(FunctionalCategoryOption.values());

        genesCategory = new JTextField();

        genesCategoryComboBox = new JComboBox<>(BooleanOperator.values());

        genesCategoryExclude = new JTextField();

    }

    private void initFields(){

        patternLengthField.initFields();
        scoreField.initFields();
        countField.initFields();
        membersField.initFields();

        allStrandTypesBtn.setSelected(true);

        patternIds.setText(PATTERN_IDS_DEF);
        familyIds.setText(FAMILY_IDS_DEF);
        patternGenes.setText(PATTERN_GENES_DEF);
        mainFunctionalCategory.setText(FUNCTIONAL_CATEGORY_DEF);
        genesCategory.setText(FUNCTIONAL_CATEGORY_DEF);
        genesCategoryExclude.setText(FUNCTIONAL_CATEGORY_DEF);

        genesCategoryComboBox.setSelectedIndex(0);
        genesComboBox.setSelectedIndex(0);
        functionalCategoryComboBox.setSelectedIndex(0);
    }

    private void addComponentsToGC(){
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);
        int y = 0;

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,  STRAND_LABEL, LINE_START);
        addComponentToGC(1, y++, 1, 0.6, insetField,3, patternStrandPanel, LINE_START);

        patternLengthField.addFieldToGC(fields, gc, y++);
        scoreField.addFieldToGC(fields, gc, y++);
        countField.addFieldToGC(fields, gc, y++);

        ImageIcon icon = mvc.view.graphics.Icon.QUESTION_MARK.getIcon();
        String desc = "Enter one or more values, separated by a comma";

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, PATTERN_ID_LABEL, desc), LINE_START);
        patternIds.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, patternIds, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, PATTERN_GENES_LABEL, desc), LINE_START);
        patternGenes.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y, 1, 0.2, insetField, 1, patternGenes, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, 2, genesComboBox, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, MAIN_FUNCTIONAL_CATEGORY_LABEL, desc), LINE_START);
        mainFunctionalCategory.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y, 1, 0.2, insetField, 1, mainFunctionalCategory, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, 2, functionalCategoryComboBox, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, PATTERN_GENES_CATEGORY_LABEL, desc), LINE_START);
        genesCategory.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y, 1, 0.2, insetField, 1, genesCategory, LINE_START);
        addComponentToGC(2, y++, 1, 0.2, insetField, 2, genesCategoryComboBox, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, PATTERN_GENES_CATEGORY_EXCLUDE_LABEL, desc), LINE_START);
        genesCategoryExclude.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, genesCategoryExclude, LINE_START);
        //addComponentToGC(2, y++, 1, 0.2, insetField, 2, genesCategoryExcludeComboBox, LINE_START);

        addComponentToGC(0, y, 1, 0.2, insetLabel, 1,
                initLabel(icon, FAMILY_ID_LABEL, desc), LINE_START);
        familyIds.setColumns(TEXT_FIELD_SIZE);
        addComponentToGC(1, y++, 1, 0.2, insetField, 1, familyIds, LINE_START);

        membersField.addFieldToGC(fields, gc, y++);

    }

    private JLabel initLabel(ImageIcon icon, String labelName, String desc){
        JLabel label = new JLabel(labelName, icon, JLabel.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setToolTipText(desc);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        return label;
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

    public void setGenomeData(int numberOfGenomes, int maxGenomeSize) {
        if (numberOfGenomes > 0) {
            patternLengthField.setToMaxDefault(maxGenomeSize);
            countField.setToMaxDefault(numberOfGenomes);
        }
    }

    public void setApplyFilterListener(RunListener<FilterRequest> applyFilterListener){
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
