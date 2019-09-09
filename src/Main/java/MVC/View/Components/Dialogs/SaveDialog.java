package MVC.View.Components.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SaveDialog extends JFrame{

    private static final String DIALOG_TEXT = "Saving will overwrite the current session file, " +
            "only filtered CSBs will be kept. Would you like to continue?";

    private JLabel text;

    private JButton okBtn;
    private JButton saveAsBtn;
    private JButton cancelBtn;

    public SaveDialog(){

        /*
        Object[] options = {"Save Anyway",
                "Save As...",
                "Cancel"};
        int n = JOptionPane.showOptionDialog(this,
                DIALOG_TEXT,
                "Save",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        setTitle("Save");

        text = new JLabel(DIALOG_TEXT);

        okBtn = new JButton("Save Anyway");
        saveAsBtn = new JButton("Save As...");
        cancelBtn = new JButton("Cancel");

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        setLayout(new BorderLayout());

        JPanel btns = new JPanel(new FlowLayout());
        btns.add(okBtn);
        btns.add(saveAsBtn);
        btns.add(cancelBtn);

        add(text, BorderLayout.PAGE_START);
        add(btns, BorderLayout.LINE_END);

        pack();*/
    }
}
