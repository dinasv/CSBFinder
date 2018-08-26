package MVC.View;

import Utils.Gene;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenomeTableModel extends AbstractTableModel {

    List<String> rows;
    Map<String,List<Gene>>  data;
    int columnSize = 0;

    public void setData(Map<String, List<Gene>> data) {
        this.data = data;
        this.rows = new ArrayList<>(data.keySet());
        columnSize = Collections.max(data.keySet().stream()
                    .map(key -> data.get(key).size())
                    .collect(Collectors.toList())) + 1;


    }

    @Override
    public int getRowCount() {
        if (rows != null) {
            return rows.size();
        }

        return 0;
    }

    @Override
    public int getColumnCount() {
        if (rows != null){
          return columnSize;
        }

        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String genome = rows.get(rowIndex);
        if (data.containsKey(genome)) {
            if (columnIndex == 0) {
                return genome;
            }

            if (data.get(genome).size() <= columnIndex) {
                return "lkmewlk";
            } else {
                return data.get(genome).get(columnIndex).getCog_id();
            }
        }

        return "saddsa";
    }
}
