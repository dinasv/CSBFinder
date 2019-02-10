package MVC.View.Components.Panels;

import MVC.View.Events.OpenDialogEvent;
import MVC.View.Listeners.OpenDialogListener;

import javax.swing.*;

/**
 */
public class TableButtonsPanel extends JPanel{

    JButton filterBtn;

    private OpenDialogListener filterTableListener;

    public TableButtonsPanel(ImageIcon filterIcon) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        filterBtn = new JButton(filterIcon);
        filterBtn.setBorder(BorderFactory.createEmptyBorder());
        filterBtn.setToolTipText("Filter Table");
        add(filterBtn);

        filterBtn.addActionListener(e -> filterTableListener.openDialogOccurred(new OpenDialogEvent()));
    }

    public void setFilterTableListener(OpenDialogListener filterTableListener) {
        this.filterTableListener = filterTableListener;
    }

    public void disableFilterBtn(){
        filterBtn.setEnabled(false);
    }

    public void enableFilterBtn(){
        filterBtn.setEnabled(true);
    }

}
