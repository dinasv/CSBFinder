package MVC.View.Components;

import MVC.View.Events.OpenDialogEvent;
import MVC.View.Events.SetNumOfNeighborsEvent;
import MVC.View.Images.Icon;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel{

    private static final String SELECT_PARAMS_BTN_NAME = "Find CSBs";
    private static final String CLUSTER_BTN_NAME = "Cluster to families";

    private JButton selectParamsBtn;
    private JButton clusterBtn;

    private final Integer[] NEIGHBORS_VALUES = {0, 1, 2, 3, 4, 5, 6,  7, 8, 9, 10};

    private JPanel selectNeighborsPanel;
    private JComboBox selectNumOfNeighbors;
    private JLabel selectNumOfNeighborsLabel = new JLabel("Neighbors:");

    private Listener<OpenDialogEvent> selectParamsListener;
    private Listener<OpenDialogEvent> clusterListener;
    private Listener<SetNumOfNeighborsEvent> setNumOfNeighborsListener;

    public Toolbar() {

        setBorder(BorderFactory.createEtchedBorder());

        JPanel buttons = new JPanel(new FlowLayout());

        selectParamsBtn = new JButton(Icon.RUN.getIcon());
        selectParamsBtn.setBorder(BorderFactory.createEmptyBorder());
        selectParamsBtn.setEnabled(false);
        selectParamsBtn.setToolTipText(SELECT_PARAMS_BTN_NAME);

        clusterBtn = new JButton(Icon.CLUSTER.getIcon());
        clusterBtn.setBorder(BorderFactory.createEmptyBorder());
        clusterBtn.setEnabled(false);
        clusterBtn.setToolTipText(CLUSTER_BTN_NAME);

        buttons.add(selectParamsBtn);
        buttons.add(clusterBtn);

        selectNumOfNeighbors = new JComboBox<>(NEIGHBORS_VALUES);
        selectNumOfNeighbors.setSelectedIndex(3);

        selectNeighborsPanel = new JPanel();
        selectNeighborsPanel.setLayout(new FlowLayout());
        selectNeighborsPanel.add(selectNumOfNeighborsLabel);
        selectNeighborsPanel.add(selectNumOfNeighbors);

        setLayout(new BorderLayout());

        add(buttons, BorderLayout.LINE_START);
        add(selectNeighborsPanel, BorderLayout.LINE_END);

        selectParamsBtn.addActionListener(e -> selectParamsListener.eventOccurred(new OpenDialogEvent()));
        clusterBtn.addActionListener(e -> clusterListener.eventOccurred(new OpenDialogEvent()));
        selectNumOfNeighbors.addActionListener(e -> setNumOfNeighborsListener.eventOccurred(
                                                new SetNumOfNeighborsEvent(getNumOfNeighbors())));
    }


    public void setSelectParamsListener(Listener<OpenDialogEvent> selectParamsListener) {
        this.selectParamsListener = selectParamsListener;
    }

    public void setClusterListener(Listener<OpenDialogEvent> clusterListener) {
        this.clusterListener = clusterListener;
    }

    public void setSetNumOfNeighborsListener(Listener<SetNumOfNeighborsEvent> setNumOfNeighborsListener) {
        this.setNumOfNeighborsListener = setNumOfNeighborsListener;
    }

    public void enableSelectParamsBtn() {
        selectParamsBtn.setEnabled(true);
    }

    public void disableSelectParamsBtn() {
        selectParamsBtn.setEnabled(false);
    }

    public void enableClusterBtn() {
        clusterBtn.setEnabled(true);
    }

    public void disableClusterBtn() {
        clusterBtn.setEnabled(false);
    }

    public int getNumOfNeighbors(){
       return selectNumOfNeighbors.getSelectedIndex();
    }

}




