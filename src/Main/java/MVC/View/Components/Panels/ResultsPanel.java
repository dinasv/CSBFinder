package MVC.View.Components.Panels;

import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import MVC.View.Listeners.FilterTableListener;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Listeners.RunListener;
import MVC.View.Models.*;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResultsPanel extends JPanel {

    private TablePanel familiesPanel;
    private TablePanel familyPatternsPanel;
    private TableButtonsPanel tableButtonsPanel;

    private PatternsTableSorter patternsSorter;

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


        familiesPanel = new TablePanel(FamilyProperty.FAMILY_ID, familyTableModel);
        familyPatternsPanel = new TablePanel(PatternProperty.ID, patternsTableModel);

        patternsSorter = new PatternsTableSorter(patternsTableModel);

        setLayout(new BorderLayout(2,2));
        setSplitPane();

        tableButtonsPanel = new TableButtonsPanel(filterIcon);
        add(tableButtonsPanel, BorderLayout.LINE_END);

    }

    public void setFilterRequest(FilterRequest filterRequest){
        patternsSorter.clear();
        patternsSorter.setCSBLength(filterRequest.getMinCSBLength(), filterRequest.getMaxCSBLength());
        familyPatternsPanel.setSorter(patternsSorter.getSorter());
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

    public void setPatternsTableSorter(TableRowSorter<TableModel> sorter){
        familyPatternsPanel.setSorter(sorter);
    }

    public void setFilterTableListener(FilterTableListener filterTableListener) {
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
}
