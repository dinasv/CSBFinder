package MVC.View.Components.Dialogs;

import javax.swing.*;
import java.awt.*;

public class SaveDialog {

    private static final String DIALOG_TEXT = "Saving will overwrite the current session file, " +
            "only the displayed CSBs will be kept.";

    private static final Object[] OPTIONS = {"Save Anyway",
            "Save As...",
            "Cancel"};

    private Component parentComponent;

    public SaveDialog(Component parentComponent){

        this.parentComponent = parentComponent;
    }

    public int showDialog(){

        int value = JOptionPane.showOptionDialog(parentComponent,
                DIALOG_TEXT,
                "Save",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                OPTIONS,
                OPTIONS[2]);

        return value;

    }

}
