package MVC.View;

import MVC.View.Listeners.FamilyRowClickedListener;
import PostProcess.Family;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class SummaryPanel extends JSplitPane {

    private SummaryTabs tabs;
    private CSBSummaryPanel csbSummaryPanel;

    public SummaryPanel() {
        tabs = new SummaryTabs();
        csbSummaryPanel = new CSBSummaryPanel();

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(tabs);
        setRightComponent(csbSummaryPanel);
        setResizeWeight(0.7);
    }


    public void setFamilyData(List<Family> familyList) {
        tabs.setFamilyData(familyList);
    }

    public void setFamilyRowClickedListener(FamilyRowClickedListener listener) {
        tabs.setFamilyRowClickedListener(listener);
    }

    public void setCogInfo(Map<String,String> cogInfo) {
        csbSummaryPanel.displaySummary(cogInfo);
    }
}
