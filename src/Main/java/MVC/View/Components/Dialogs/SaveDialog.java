package MVC.View.Components.Dialogs;

import MVC.View.Events.DontShowSaveMsgEvent;
import MVC.View.Events.Event;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SaveDialog {

    private static final String DIALOG_TEXT = "Saving will overwrite the current session file, " +
            "only the displayed CSBs will be kept.";

    private static final Object[] OPTIONS = {"Save Anyway",
            "Save As...",
            "Cancel"};

    private Component parentComponent;

    private JPanel panel;
    private JCheckBox checkbox;

    private Listener<DontShowSaveMsgEvent> showSaveMsgListener;

    public SaveDialog(Component parentComponent){

        this.parentComponent = parentComponent;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel text = new JLabel(DIALOG_TEXT);
        checkbox  = new JCheckBox("Do not show this message again");

        panel.add(text);
        panel.add(checkbox);

        checkbox.addActionListener(e ->
                showSaveMsgListener.eventOccurred(new DontShowSaveMsgEvent(!checkbox.isSelected())));
    }

    public int showDialog(){

        int value = JOptionPane.showOptionDialog(parentComponent,
                panel,
                "Save",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                OPTIONS,
                OPTIONS[2]);

        return value;

    }

    public void setDontShowMsgListener(Listener<DontShowSaveMsgEvent> listener){
        showSaveMsgListener = listener;
    }



}
