package mvc.view.components.panels;

import mvc.view.events.OpenDialogEvent;
import mvc.view.listeners.Listener;

import javax.swing.*;

/**
 */
public class TableButtonsPanel extends JPanel{

    JButton filterBtn;

    private Listener<OpenDialogEvent> filterTableListener;

    public TableButtonsPanel(ImageIcon filterIcon) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        filterBtn = new JButton(filterIcon);
        filterBtn.setBorder(BorderFactory.createEmptyBorder());
        filterBtn.setToolTipText("Filter");
        add(filterBtn);

        filterBtn.addActionListener(e -> filterTableListener.eventOccurred(new OpenDialogEvent()));
    }

    public void setFilterTableListener(Listener<OpenDialogEvent> filterTableListener) {
        this.filterTableListener = filterTableListener;
    }

    public void disableFilterBtn(){
        filterBtn.setEnabled(false);
    }

    public void enableFilterBtn(){
        filterBtn.setEnabled(true);
    }

}
