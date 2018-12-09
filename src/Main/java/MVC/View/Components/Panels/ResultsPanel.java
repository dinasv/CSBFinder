package MVC.View.Components.Panels;

import Core.Genomes.Pattern;
import Core.PostProcess.Family;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Models.FamilyProperty;
import MVC.View.Models.FamilyTableModel;
import MVC.View.Models.PatternProperty;
import MVC.View.Models.PatternsTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultsPanel extends JSplitPane {

    private TablePanel familiesPanel;
    private TablePanel familyPatternsPanel;

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

    public ResultsPanel() {

        familiesPanel = new TablePanel(FamilyProperty.FAMILY_ID, new FamilyTableModel(familyColumns));
        familyPatternsPanel = new TablePanel(PatternProperty.ID, new PatternsTableModel(patternsColumns));

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(familiesPanel);
        setRightComponent(familyPatternsPanel);
        setResizeWeight(0.2);
    }

    private void addPanels(){
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.05;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(familiesPanel, gridBagConstraints);

        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.95;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(familyPatternsPanel, gridBagConstraints);

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
