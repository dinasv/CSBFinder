package MVC.View.Components.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProgressBar extends JDialog {

    private JProgressBar bar;
    private JButton close;
    private JLabel messageLabel;
    private JTextPane messagePane;

    public ProgressBar(Window parent) {
        super(parent, ModalityType.APPLICATION_MODAL);
        close = new JButton("Close");
        close.addActionListener(this::actionPerformed);
        close.setVisible(false);

        messageLabel = new JLabel();

        bar = new JProgressBar();
        bar.setIndeterminate(true);

        setSize(400, 200);

        initLayout();
        setLocationRelativeTo(parent);
//        pack();
    }

    private void initLayout() {
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.CENTER;

        add(bar, gc);
        gc.gridy = 1;
        add(close, gc);
    }


    public void start(String title) {
        setTitle(title);
        bar.setVisible(true);
        close.setVisible(false);
        setVisible(true);
    }

    public void done(String message) {
        bar.setVisible(false);
        close.setVisible(true);
        setVisible(false);
    }

    private void actionPerformed(ActionEvent e) {
        if (((JButton) e.getSource()).isEnabled()) {setVisible(false);}
    }
}
