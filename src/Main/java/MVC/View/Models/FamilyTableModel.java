package MVC.View.Models;

import Core.Genomes.Pattern;
import Core.PostProcess.Family;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FamilyTableModel extends AbstractTableModel {

    public final static String ID = "ID";
    public final static String LENGTH = "Length";
    public final static String SCORE = "Score";
    public final static String INSTANCE_COUNT = "Instance_Count";
    public final static String CSB = "CSB";
    public final static String MAIN_CATEGORY = "Main_Category";
    public final static String FAMILY_ID = "Family_ID";

    public final static  String[] columns = new String[] {
            ID,
            LENGTH ,
            SCORE,
            INSTANCE_COUNT ,
            CSB,
            MAIN_CATEGORY ,
            FAMILY_ID
    };


    private List<Pattern> data;
    private Map<String, Pattern> strToPatternMap;

    public FamilyTableModel(){
        super();
        data = new ArrayList<>();
        strToPatternMap = new HashMap<>();
    }
    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columns[columnIndex]) {
            case LENGTH:
                return Integer.class;
            case SCORE:
                return Double.class;
            case INSTANCE_COUNT:
                return Integer.class;
            case ID:
                return Integer.class;
            case FAMILY_ID:
                return Integer.class;
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
        switch (columns[columnIndex]) {
            case ID:
                return p.getPatternId();
            case LENGTH:
                return p.getLength();
            case SCORE:
                return p.getScore();
            case INSTANCE_COUNT:
                return p.getInstancesPerGenome();
            case CSB:
                return p.toString();
            case MAIN_CATEGORY:
                return p.getMainFunctionalCategory();
            case FAMILY_ID:
                return p.getFamilyId();
        }
        return null;
    }

    public int getIndexOfColumn(String column) {
        int index = -1;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equalsIgnoreCase(column)) {
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
