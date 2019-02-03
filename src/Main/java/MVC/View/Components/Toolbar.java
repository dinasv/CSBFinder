package MVC.View.Components;

import MVC.View.Events.SelectParamsEvent;
import MVC.View.Events.SetNumOfNeighborsEvent;
import MVC.View.Listeners.SelectParamsListener;
import MVC.View.Listeners.SetNumOfNeighborsListener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel{

    private static final String SELECT_PARAMS_BTN_NAME = "Find CSBs";

    private JButton selectParams;

    private final Integer[] NEIGHBORS_VALUES = {0, 1, 2, 3, 4, 5, 6,  7, 8, 9, 10};

    private JPanel selectNeighborsPanel;
    private JComboBox selectNumOfNeighbors;
    private JLabel selectNumOfNeighborsLabel = new JLabel("Neighbors:");

    private SelectParamsListener selectParamsListener;
    private SetNumOfNeighborsListener setNumOfNeighborsListener;

    public Toolbar(ImageIcon runIcon) {

        setBorder(BorderFactory.createEtchedBorder());
        selectParams =  new JButton(runIcon);
        selectParams.setBorder(BorderFactory.createEmptyBorder());
        selectParams.setEnabled(false);
        selectParams.setToolTipText(SELECT_PARAMS_BTN_NAME);

        selectNumOfNeighbors = new JComboBox<>(NEIGHBORS_VALUES);
        selectNumOfNeighbors.setSelectedIndex(3);

        selectNeighborsPanel = new JPanel();
        selectNeighborsPanel.setLayout(new FlowLayout());
        selectNeighborsPanel.add(selectNumOfNeighborsLabel);
        selectNeighborsPanel.add(selectNumOfNeighbors);

        setLayout(new BorderLayout());

        add(selectParams, BorderLayout.LINE_START);
        add(selectNeighborsPanel, BorderLayout.LINE_END);

        selectParams.addActionListener(e -> selectParamsListener.selectParamsOccurred(new SelectParamsEvent()));
        selectNumOfNeighbors.addActionListener(e -> setNumOfNeighborsListener.setNumOfNeighborsOccurred(
                                                new SetNumOfNeighborsEvent(getNumOfNeighbors())));
    }


    public void setSelectParamsListener(SelectParamsListener selectParamsListener) {
        this.selectParamsListener = selectParamsListener;
    }

    public void setSetNumOfNeighborsListener(SetNumOfNeighborsListener setNumOfNeighborsListener) {
        this.setNumOfNeighborsListener = setNumOfNeighborsListener;
    }

    public void enableSelectParamsBtn() {
        selectParams.setEnabled(true);
    }

    public void disableSelectParamsBtn() {
        selectParams.setEnabled(false);
    }

    public int getNumOfNeighbors(){
       return selectNumOfNeighbors.getSelectedIndex();
    }

}




