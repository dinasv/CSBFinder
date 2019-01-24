package MVC.View.Components.Dialogs;

import MVC.View.Events.RunEvent;
import MVC.View.Listeners.RunListener;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.LINE_START;

/**
 */
public class FilterDialog extends JDialog{

    private JLabel CSBLengthLabel;
    private JSpinner minCSBLength;
    private JSpinner maxCSBLength;

    private JLabel scoreLabel;
    private JSpinner minScore;
    private JSpinner maxScore;

    private GridBagConstraints gc;

    private JButton applyFilter;
    private JButton clearAll;

    private RunListener applyFilterListener;

    JPanel fields;

    public FilterDialog(){

        setTitle("Filter Table");

        applyFilter = new JButton("Apply");
        setApplyFilterActionListener();

        clearAll = new JButton("Clear All");
        setClearAllActionListner();

        fields = new JPanel();
        fields.setLayout(new GridBagLayout());
        gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.NONE;

        initComponents();
        initLabels();
        addComponentsToGC();
        initFields();

        JPanel buttons = new JPanel();
        buttons.add(applyFilter);
        buttons.add(clearAll);

        setLayout(new BorderLayout());
        add(fields, BorderLayout.PAGE_START);
        add(buttons, BorderLayout.LINE_END);
        pack();
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
    }

    private void initComponents(){
        minCSBLength = new JSpinner();
        maxCSBLength = new JSpinner();
        minScore = new JSpinner();
        maxScore = new JSpinner();
    }

    private void initLabels(){
        CSBLengthLabel = new JLabel("CSB length between:");
        scoreLabel = new JLabel("Score between:");
    }

    private void initFields(){
        // CSB min length
        minCSBLength.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        // CSB max length
        maxCSBLength.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 2, Integer.MAX_VALUE, 1));

        // CSB min length
        minScore.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        // CSB max length
        maxScore.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 1));

    }

    private void addComponentsToGC(){
        int y = 0;

        addIntervalComponentToGC(y++, CSBLengthLabel, minCSBLength, maxCSBLength);
        addIntervalComponentToGC(y++, scoreLabel, minScore, maxScore);
    }

    private void addIntervalComponentToGC(int y, JLabel label, JSpinner minSpinner, JSpinner maxSpinner){
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);

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
