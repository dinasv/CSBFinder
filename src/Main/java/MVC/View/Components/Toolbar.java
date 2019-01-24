package MVC.View.Components;

import MVC.View.Events.SelectParamsEvent;
import MVC.View.Listeners.SelectParamsListener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel{

    private static final String SELECT_PARAMS_BTN_NAME = "Find CSBs";

    private JButton selectParams;
    private SelectParamsListener selectParamsListener;

    public Toolbar(ImageIcon runIcon) {

        setBorder(BorderFactory.createEtchedBorder());
        selectParams =  new JButton(runIcon);
        selectParams.setBorder(BorderFactory.createEmptyBorder());
        selectParams.setEnabled(false);
        selectParams.setToolTipText(SELECT_PARAMS_BTN_NAME);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(selectParams);

        selectParams.addActionListener(e -> selectParamsListener.selectParamsOccurred(new SelectParamsEvent()));
    }


    public void setSelectParamsListener(SelectParamsListener selectParamsListener) {
        this.selectParamsListener = selectParamsListener;
    }

    public void enableSelectParamsBtn() {
        selectParams.setEnabled(true);
    }
    public void disableSelectParamsBtn() {
        selectParams.setEnabled(false);
    }

}




