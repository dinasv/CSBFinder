package MVC.View.Components.Panels;

import Core.PostProcess.Family;
import MVC.View.Events.RowClickedEvent;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Models.ColumnProperty;
import MVC.View.Models.TableModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TablePanel extends JPanel {

    private JTable table;
    private TableModel model;
    private JScrollPane scrollPane;
    private RowClickedListener rowClickedListener;

    public TablePanel(ColumnProperty selectionBy, TableModel tableModel) {
        setLayout(new BorderLayout());

        model = tableModel;

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                TableModel model = ((TableModel) table.getModel());

                rowClickedListener.rowClickedOccurred(new RowClickedEvent(
                        model.getDataObject((Integer) table.getValueAt(table.getSelectedRow(),
                                model.getIndexOfColumn(selectionBy)))
                ));
            }
        });
        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
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
