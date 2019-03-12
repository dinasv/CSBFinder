package MVC.View.Components.Panels;

import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Events.OpenDialogEvent;
import MVC.View.Listeners.Listener;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Models.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel extends JPanel {

    private TablePanel<Integer, Family> familiesPanel;
    private TablePanel<String, Pattern> familyPatternsPanel;
    private TableButtonsPanel tableButtonsPanel;

    private static PatternProperty[] patternsColumns = {
            PatternProperty.ID,
            PatternProperty.LENGTH,
            PatternProperty.SCORE,
            PatternProperty.INSTANCE_COUNT,
            PatternProperty.CSB,
            PatternProperty.MAIN_CATEGORY,
    };

    private static FamilyProperty[] familyColumns = {
            FamilyProperty.FAMILY_ID,
            FamilyProperty.SCORE,
            FamilyProperty.MEMBERS,
    };

    public ResultsPanel(ImageIcon filterIcon) {

        PatternsTableModel patternsTableModel = new PatternsTableModel(patternsColumns);
        FamilyTableModel familyTableModel = new FamilyTableModel(familyColumns);

        familiesPanel = new TablePanel<>(FamilyProperty.FAMILY_ID, familyTableModel);
        familyPatternsPanel = new TablePanel<>(PatternProperty.ID, patternsTableModel);

        setLayout(new BorderLayout(2,2));
        setSplitPane();

        tableButtonsPanel = new TableButtonsPanel(filterIcon);
        add(tableButtonsPanel, BorderLayout.LINE_END);

    }


    public void setFilteredFamilies(List<Family> filteredFamilies){

        clearPanel();

        Family selectedFamily = familiesPanel.getSelectedRowObject();

        familiesPanel.setData(filteredFamilies);

        int row = 0;
        if (selectedFamily != null) {
            row = familiesPanel.getObjectRow(selectedFamily.getFamilyId());
            row = row == -1 ? 0 : row;
        }
        familiesPanel.select(row);
    }


    private void setSplitPane(){
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(2);
        splitPane.setLeftComponent(familiesPanel);
        splitPane.setRightComponent(familyPatternsPanel);
        splitPane.setResizeWeight(0.2);

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

}
