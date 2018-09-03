package MVC.View;

import MVC.View.Listeners.FamilyRowClickedListener;
import PostProcess.Family;

import javax.swing.*;
import java.util.List;

public class SummaryTabs extends JTabbedPane {

    private FamilyPanel familyPanel;
    private FamilyPanel familyTopScorePanel;

    public SummaryTabs() {
        familyPanel = new FamilyPanel();
        familyTopScorePanel = new FamilyPanel();
        addTab("CSBs", familyPanel);
        addTab("Top Scoring CSBs", familyTopScorePanel);
    }

    public void setFamilyData(List<Family> familyList) {
        familyPanel.setData(familyList);
        familyTopScorePanel.setTopScoreData(familyList);
    }

    public void setFamilyRowClickedListener(FamilyRowClickedListener listener) {
        familyPanel.setRowClickedListener(listener);
        familyTopScorePanel.setRowClickedListener(listener);
    }

    public void clearPanel(){
        familyPanel.clearPanel();
        familyTopScorePanel.clearPanel();
    }
}
