package MVC.View.Components.Panels;

import MVC.View.Listeners.PatternRowClickedListener;
import Core.PostProcess.Family;
import MVC.View.Models.PatternProperties;
import MVC.View.Models.PatternsTableModel;

import javax.swing.*;
import java.util.List;

public class SummaryTabs extends JTabbedPane {

    private PatternsPanel patternsPanel;
    private PatternsPanel familyTopScorePanel;

    private static PatternProperties[] columns = {
            PatternProperties.ID,
            PatternProperties.LENGTH,
            PatternProperties.SCORE,
            PatternProperties.INSTANCE_COUNT,
            PatternProperties.CSB,
            PatternProperties.MAIN_CATEGORY,
            PatternProperties.FAMILY_ID,
    };

    public SummaryTabs() {

        patternsPanel = new PatternsPanel(columns);
        familyTopScorePanel = new PatternsPanel(columns);
        addTab("CSBs", patternsPanel);
        addTab("Top Scoring CSBs", familyTopScorePanel);
    }

    public void setFamilyData(List<Family> familyList) {
        patternsPanel.setData(familyList);
        familyTopScorePanel.setTopScoreData(familyList);
    }

    public void setFamilyRowClickedListener(PatternRowClickedListener listener) {
        patternsPanel.setRowClickedListener(listener);
        familyTopScorePanel.setRowClickedListener(listener);
    }

    public void clearPanel(){
        patternsPanel.clearPanel();
        familyTopScorePanel.clearPanel();
    }

    public void fireTableDataChanged(){
        patternsPanel.fireTableDataChanged();
        familyTopScorePanel.fireTableDataChanged();
    }
}
