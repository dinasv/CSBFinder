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

    private JLabel CSBLengthLabel1;
    private JLabel CSBLengthLabel2;
    private JSpinner minCSBLength;
    private JSpinner maxCSBLength;

    private GridBagConstraints gc;

    private JButton applyFilter;
    private JButton clearAll;

    private RunListener applyFilterListener;

    JPanel fields;

    public FilterDialog(){
        fields = new JPanel();
        fields.setLayout(new GridBagLayout());
        setTitle("Filter Table");

        applyFilter = new JButton("Apply");
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

        clearAll = new JButton("Clear All");
        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initFields();
            }
        });
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

    private void initRequest(FilterRequest filterRequest){
        filterRequest.setMinCSBLength((int)minCSBLength.getValue());
        filterRequest.setMaxCSBLength((int)maxCSBLength.getValue());
    }

    private void initComponents(){
        minCSBLength = new JSpinner();
        maxCSBLength = new JSpinner();
    }

    private void initLabels(){
        CSBLengthLabel1 = new JLabel("CSB length between:");
        CSBLengthLabel2 = new JLabel(" and ");

    }

    private void initFields(){
        // CSB min length
        minCSBLength.setModel(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
        // CSB max length
        maxCSBLength.setModel(new SpinnerNumberModel(Integer.MAX_VALUE, 2, Integer.MAX_VALUE, 1));

    }

    private void addComponentsToGC(){
        Insets insetLabel = new Insets(0, 10, 5, 5);
        Insets insetField = new Insets(0, 0, 5, 5);

        int y = 0;

        addComponentToGC(0, y, 1, 0.2, insetLabel, CSBLengthLabel1, LINE_START);
        addComponentToGC(1, y, 1, 0.2, insetField, minCSBLength, LINE_START);
        addComponentToGC(2, y, 1, 0.2, insetField, CSBLengthLabel2, CENTER);
        addComponentToGC(3, y++, 1, 0.2, insetField, maxCSBLength, LINE_START);

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
