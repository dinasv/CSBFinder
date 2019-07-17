package MVC.View.Components.Panels;

import MVC.View.Tables.FamilyProperty;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Events.OpenDialogEvent;
import MVC.View.Listeners.Listener;
import MVC.View.Listeners.RowClickedListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel extends JPanel {

    private TablePanel<Integer, Family> familiesPanel;
    private TablePanel<String, Pattern> familyPatternsPanel;
    private TableButtonsPanel tableButtonsPanel;

    private static final double FAMILIES_PANEL_WEIGHT = 0.3;

    private static MVC.View.Tables.PatternProperty[] patternsColumns = {
            MVC.View.Tables.PatternProperty.ID,
            MVC.View.Tables.PatternProperty.LENGTH,
            MVC.View.Tables.PatternProperty.SCORE,
            MVC.View.Tables.PatternProperty.INSTANCE_COUNT,
            MVC.View.Tables.PatternProperty.CSB,
            MVC.View.Tables.PatternProperty.MAIN_CATEGORY,
    };

    private static MVC.View.Tables.FamilyProperty[] familyColumns = {
            MVC.View.Tables.FamilyProperty.FAMILY_ID,
            MVC.View.Tables.FamilyProperty.SCORE,
            MVC.View.Tables.FamilyProperty.LONGEST_MEMBER,
            MVC.View.Tables.FamilyProperty.MEMBERS,
    };

    public ResultsPanel(ImageIcon filterIcon) {

        MVC.View.Tables.PatternsTableModel patternsTableModel = new MVC.View.Tables.PatternsTableModel(patternsColumns);
        MVC.View.Tables.FamilyTableModel familyTableModel = new MVC.View.Tables.FamilyTableModel(familyColumns);

        familiesPanel = new TablePanel<>(FamilyProperty.FAMILY_ID, familyTableModel);
        familyPatternsPanel = new TablePanel<>(MVC.View.Tables.PatternProperty.ID, patternsTableModel);

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
