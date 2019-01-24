package MVC.View.Models;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class PatternsTableSorter {

    List<RowFilter<TableModel,Object>> filters;
    PatternsTableModel model;

    public PatternsTableSorter(PatternsTableModel model){
        filters = new ArrayList<>();
        this.model = model;
    }

    public void setCSBLength(int from, int to){

        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.AFTER, from-1,
                model.getIndexOfColumn(PatternProperty.LENGTH)));
        filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.BEFORE, to+1,
                model.getIndexOfColumn(PatternProperty.LENGTH)));

    }

    public TableRowSorter<TableModel> getSorter(){
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        RowFilter<TableModel, Object> rf = RowFilter.andFilter(filters);
        sorter.setRowFilter(rf);
        return sorter;
    }

    public void clear(){
        filters.clear();
    }
}
