package MVC.View.Components.Panels;

import MVC.View.Events.FilterTableEvent;
import MVC.View.Listeners.FilterTableListener;

import javax.swing.*;

/**
 */
public class TableButtonsPanel extends JPanel{

    JButton filterBtn;

    private FilterTableListener filterTableListener;

    public TableButtonsPanel(ImageIcon filterIcon) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        filterBtn = new JButton(filterIcon);
        filterBtn.setBorder(BorderFactory.createEmptyBorder());
        filterBtn.setToolTipText("Filter Table");
        add(filterBtn);

        filterBtn.addActionListener(e -> filterTableListener.filterTableOccurred(new FilterTableEvent()));
    }

    public void setFilterTableListener(FilterTableListener filterTableListener) {
        this.filterTableListener = filterTableListener;
    }

    public void disableFilterBtn(){
        filterBtn.setEnabled(false);
    }

    public void enableFilterBtn(){
        filterBtn.setEnabled(true);
    }

}
