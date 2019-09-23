package MVC.View.Components.Dialogs;

import javax.swing.*;

/**
 */
public class SettingsDialog{

    private JFrame mainFrame;
    private JPanel panel;

    private JLabel showSaveMsgLabel;
    private JCheckBox showSaveMsgChckBox;

    private static final String SHOW_OVERWRITE_MSG_TXT = "Show a warning message when overwriting the current " +
            "session file";
    private static final String TITLE = "Settings";

    public SettingsDialog(JFrame mainFrame){
        this.mainFrame = mainFrame;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        showSaveMsgLabel = new JLabel(SHOW_OVERWRITE_MSG_TXT);
        showSaveMsgChckBox = new JCheckBox();

        panel.add(showSaveMsgChckBox);
        panel.add(showSaveMsgLabel);

    }

    public int showDialog(boolean saveMsgVal){

        showSaveMsgChckBox.setSelected(saveMsgVal);

        int value = JOptionPane.showConfirmDialog(mainFrame,
            panel,
            TITLE,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null);

        return value;

    }

    public boolean showSaveMsg(){
        return showSaveMsgChckBox.isSelected();
    }
}
