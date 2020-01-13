package mvc.view.components.panels;

import mvc.view.tables.FamilyProperty;
import model.patterns.Pattern;
import model.postprocess.Family;
import mvc.view.events.OpenDialogEvent;
import mvc.view.listeners.Listener;
import mvc.view.listeners.RowClickedListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel extends JPanel {

    private TablePanel<Integer, Family> familiesPanel;
    private TablePanel<String, Pattern> familyPatternsPanel;
    private TableButtonsPanel tableButtonsPanel;

    private static final double FAMILIES_PANEL_WEIGHT = 0.3;

    private static mvc.view.tables.PatternProperty[] patternsColumns = {
            mvc.view.tables.PatternProperty.ID,
            mvc.view.tables.PatternProperty.LENGTH,
            mvc.view.tables.PatternProperty.SCORE,
            mvc.view.tables.PatternProperty.INSTANCE_COUNT,
            mvc.view.tables.PatternProperty.CSB,
            mvc.view.tables.PatternProperty.MAIN_CATEGORY,
    };

    private static mvc.view.tables.FamilyProperty[] familyColumns = {
            mvc.view.tables.FamilyProperty.FAMILY_ID,
            mvc.view.tables.FamilyProperty.SCORE,
            mvc.view.tables.FamilyProperty.LONGEST_MEMBER,
            mvc.view.tables.FamilyProperty.MEMBERS,
    };

    public ResultsPanel(ImageIcon filterIcon) {

        mvc.view.tables.PatternsTableModel patternsTableModel = new mvc.view.tables.PatternsTableModel(patternsColumns);
        mvc.view.tables.FamilyTableModel familyTableModel = new mvc.view.tables.FamilyTableModel(familyColumns);

        familiesPanel = new TablePanel<>(FamilyProperty.FAMILY_ID, familyTableModel);
        familyPatternsPanel = new TablePanel<>(mvc.view.tables.PatternProperty.ID, patternsTableModel);

        setLayout(new BorderLayout(2,2));
        setSplitPane();

        tableButtonsPanel = new TableButtonsPanel(filterIcon);
        add(tableButtonsPanel, BorderLayout.LINE_END);

    }


    public void setFilteredFamilies(List<Family> filteredFamilies){

        clearPanel();

        familiesPanel.setData(filteredFamilies);

    }


    private void setSplitPane(){
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(2);
        splitPane.setLeftComponent(familiesPanel);
        splitPane.setRightComponent(familyPatternsPanel);
        splitPane.setResizeWeight(FAMILIES_PANEL_WEIGHT);

        add(splitPane, BorderLayout.CENTER);
    }


    public void setFilterTableListener(Listener<OpenDialogEvent> filterTableListener) {
        tableButtonsPanel.setFilterTableListener(filterTableListener);
    }

    public void setFamilyData(List<Family> familyList) {
        clearPanel();
        familiesPanel.setData(familyList);
    }

    public void selectFamiliesFirstRow(){
        familiesPanel.selectFirstRow();
    }

    public void setFamilyPatternsData(Family family){
        List<Family> families = new ArrayList<Family>();
        families.add(family);
        familyPatternsPanel.setData(families);
    }

    public void setFamilyRowClickedListener(RowClickedListener<Family> listener) {
        familiesPanel.setRowClickedListener(listener);
    }

    public void setPatternRowClickedListener(RowClickedListener<Pattern> listener) {
        familyPatternsPanel.setRowClickedListener(listener);
    }

    public void clearPanel(){
        familiesPanel.clearPanel();
        familyPatternsPanel.clearPanel();
    }

    public void fireTableDataChanged(){
        familiesPanel.fireTableDataChanged();
        familyPatternsPanel.fireTableDataChanged();
    }

    public void disableFilterBtn(){
        tableButtonsPanel.disableFilterBtn();
    }

    public void enableFilterBtn(){
        tableButtonsPanel.enableFilterBtn();
    }

    public void selectFamily(int familyId){

        int row = familiesPanel.getObjectRow(familyId);
        selectRow(familiesPanel, row);
    }

    public void selectPattern(String patternId){

        int row = familyPatternsPanel.getObjectRow(patternId);
        selectRow(familyPatternsPanel, row);
    }

    private void selectRow(TablePanel panel, int row){
        panel.fireTableDataChanged();

        row = row == -1 ? 0 : row;

        panel.select(row);

    }

}
