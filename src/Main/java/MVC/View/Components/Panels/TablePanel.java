package MVC.View.Components.Panels;

import Core.PostProcess.Family;
import MVC.View.Events.RowClickedEvent;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Models.ColumnProperty;
import MVC.View.Models.CSBFinderTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;


public class TablePanel<K, V> extends JPanel {

    private JTable table;
    private CSBFinderTableModel<K, V> model;
    private JScrollPane scrollPane;
    private RowClickedListener<V> rowClickedListener;
    private ColumnProperty selectionBy;

    public TablePanel(ColumnProperty selectionBy, CSBFinderTableModel<K, V> tableModel) {
        setLayout(new BorderLayout());

        model = tableModel;
        this.selectionBy = selectionBy;
        table = new JTable(model);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                rowClickedListener.rowClickedOccurred(new RowClickedEvent<>(getSelectedRowObject()));
            }
        });
        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void select(){
        K selectedKey = (K)table.getValueAt(table.getSelectedRow(), model.getIndexOfColumn(selectionBy));

        table.setRowSelectionInterval(0, 0);
    }
    private V getSelectedRowObject(){

        return model.getDataObject((K)table.getValueAt(table.getSelectedRow(),
                model.getIndexOfColumn(selectionBy)));

    }

    public void setData(List<Family> families) {
        model.setFamilies(families);
        model.fireTableDataChanged();
    }

    public void selectFirstRow(){
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }



    public void setRowClickedListener(RowClickedListener rowClickedListener) {
        this.rowClickedListener = rowClickedListener;
    }

    public void clearPanel(){
        model.clearData();
        model.fireTableDataChanged();
    }

    public void fireTableDataChanged(){
        int row = table.getSelectedRow();
        model.fireTableDataChanged();

        if (row != -1) {
            table.setRowSelectionInterval(row, row);
        }
    }
}
