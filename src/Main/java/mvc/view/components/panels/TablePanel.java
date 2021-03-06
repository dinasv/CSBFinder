package mvc.view.components.panels;

import model.postprocess.Family;
import mvc.view.events.RowClickedEvent;
import mvc.view.listeners.RowClickedListener;
import mvc.view.tables.ColumnProperty;
import mvc.view.tables.CSBFinderTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        addListeners();

        add(scrollPane, BorderLayout.CENTER);
    }

    private void addListeners(){
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                rowClick(getSelectedRowObject());
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if (table.getSelectedRow() != -1){
                    rowClick(getSelectedRowObject());
                }
            }
        });
    }

    public void rowClick(V rowObject){
        rowClickedListener.rowClickedOccurred(new RowClickedEvent<>(rowObject));
    }

    public void select(int row){
        if (row >= 0 && row < table.getRowCount()) {
            table.setRowSelectionInterval(row, row);
        }
    }

    public V getSelectedRowObject(){
        K key = getSelectedObjectKey();
        if (key != null) {
            return model.getDataObject(getSelectedObjectKey());
        }
        return null;

    }
    public K getSelectedObjectKey(){
        int selectedRowIndex = table.getSelectedRow();
        if (selectedRowIndex != -1) {
            return (K) table.getValueAt(selectedRowIndex, model.getIndexOfColumn(selectionBy));
        }
        return null;
    }

    public int getObjectRow(K objKey){
        return model.getRowOfObject(objKey);
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

    public void setRowClickedListener(RowClickedListener<V> rowClickedListener) {
        this.rowClickedListener = rowClickedListener;
    }

    public void clearPanel(){
        model.clearData();
        model.fireTableDataChanged();
    }

    public void fireTableDataChanged(){
        model.fireTableDataChanged();
    }
}
