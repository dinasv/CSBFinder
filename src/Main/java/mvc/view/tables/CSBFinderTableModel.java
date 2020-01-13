package mvc.view.tables;

import model.postprocess.Family;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CSBFinderTableModel<K, V> extends AbstractTableModel {

    private final ColumnProperty[] columns;

    private List<V> data;
    private Map<K, V> idToObject;

    public CSBFinderTableModel(ColumnProperty[] columns){
        super();

        this.columns = columns;
        data = new ArrayList<>();
        idToObject = new HashMap<>();
    }
    @Override
    public String getColumnName(int column) {
        return columns[column].toString();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex < columns.length){
            return columns[columnIndex].getFunctionReturnClass();
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
        V dataAtRow = data.get(rowIndex);

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


    public void setData(List<V> data) {
        this.data.clear();

        this.data.addAll(data);
    }

    public void setIdToObject(Map<K, V> idToObject){
        this.idToObject.clear();

        this.idToObject.putAll(idToObject);
    }

    public V getDataObject(K id) {
        return idToObject.get(id);
    }

    public void clearData(){
        data.clear();
        idToObject.clear();
    }

    public int getRowOfObject(K objKey){
        V obj = idToObject.get(objKey);
        if (obj != null) {
            return data.indexOf(obj);
        }
        return -1;
    }

    public abstract void setFamilies(List<Family> families);
}
