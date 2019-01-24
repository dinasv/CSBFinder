package MVC.View.Components;

import MVC.View.Components.Dialogs.InputFileChooser;
import MVC.View.Components.Dialogs.OutputTypeChooser;
import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Events.SelectParamsEvent;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.SaveOutputListener;
import MVC.View.Listeners.SelectParamsListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JPanel{

    private static final String SELECT_PARAMS_BTN_NAME = "Run";

    private JButton selectParams;
    private SelectParamsListener selectParamsListener;

    public Toolbar() {

        setBorder(BorderFactory.createEtchedBorder());
        selectParams =  new JButton(SELECT_PARAMS_BTN_NAME);
        selectParams.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(selectParams);

        selectParams.addActionListener(e -> {
            selectParamsListener.selectParamsOccurred(new SelectParamsEvent());
        });
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




