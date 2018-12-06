package MVC.View.Models;

import Core.Genomes.Instance;
import Core.Genomes.Pattern;
import Core.PostProcess.Family;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatternsTableModel extends AbstractTableModel {

    private final PatternProperties[] columns;

    private List<Pattern> data;
    private Map<String, Pattern> strToPatternMap;

    public PatternsTableModel(PatternProperties[] columns){
        super();

        this.columns = columns;
        data = new ArrayList<>();
        strToPatternMap = new HashMap<>();
    }
    @Override
    public String getColumnName(int column) {
        return columns[column].toString();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (columnIndex < columns.length){
            return columns[columnIndex].returnType;
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
        Pattern p = data.get(rowIndex);

        if(columnIndex < columns.length){
            return columns[columnIndex].patternFunction.apply(p);
        }
        return null;
    }

    public int getIndexOfColumn(PatternProperties column) {
        int index = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == column) {
                index = i;
                break;
            }
        }

        return index;
    }

    public Pattern getRowAt(int index) {
        if (this.getRowCount() <= index) {
            return null;
        }

        return this.data.get(index);
    }

    public void setData(List<Family> families) {

        clearData();

        families.forEach(family -> family.getPatterns()
                .forEach(pattern -> pattern.setFamilyId(family.getFamilyId())));

        strToPatternMap.putAll(families.stream()
                .map(Family::getPatterns)
                .flatMap(List::stream)
                .collect(Collectors.toMap(Pattern::toString, Function.identity())));

        data.addAll(families.stream()
                .map(Family::getPatterns)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    public Pattern getPattern(String pattern) {
        return strToPatternMap.get(pattern);
    }

    public void clearData(){
        data.clear();
        strToPatternMap.clear();
    }
}
