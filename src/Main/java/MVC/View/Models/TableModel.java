package MVC.View.Models;

import Core.PostProcess.Family;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TableModel<T> extends AbstractTableModel {

    private final ColumnProperty[] columns;

    private List<T> data;
    private Map<Integer, T> idToRow;

    public TableModel(ColumnProperty[] columns){
        super();

        this.columns = columns;
        data = new ArrayList<>();
        idToRow = new HashMap<>();
    }
    @Override
    public String getColumnName(int column) {
        return columns[column].toString();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex < columns.length){
            return columns[columnIndex].getFunctionReturnType();
        }

        return String.class;
    }

    @Override
    public int getRowCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T dataAtRow = data.get(rowIndex);

        if(columnIndex < columns.length){
            return columns[columnIndex].getFunction().apply(dataAtRow);
        }
        return null;
    }

    public int getIndexOfColumn(ColumnProperty column) {
        int index = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == column) {
                index = i;
                break;
            }
        }

        return index;
    }

    public T getRowAt(int index) {
        if (this.getRowCount() <= index) {
            return null;
        }

        return this.data.get(index);
    }

    public void setData(List<T> data) {
        this.data.clear();

        this.data.addAll(data);
    }

    public void setIdToRow(Map<Integer, T> idToRow){
        this.idToRow.clear();

        this.idToRow.putAll(idToRow);
    }

    public T getDataObject(int id) {
        return idToRow.get(id);
    }

    public void clearData(){
        data.clear();
        idToRow.clear();
    }

    public abstract void setFamilies(List<Family> families);
}
