package MVC.View.Components.Panels;

import MVC.View.Listeners.PatternRowClickedListener;
import Core.PostProcess.Family;
import Core.Genomes.COG;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SummaryPanel extends JSplitPane {

    private SummaryTabs tabs;
    private CSBSummaryPanel csbSummaryPanel;

    public SummaryPanel() {
        csbSummaryPanel = new CSBSummaryPanel();

        tabs = new SummaryTabs();

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(tabs);
        setRightComponent(csbSummaryPanel);
        setResizeWeight(0.7);
    }


    public void setFamilyData(List<Family> familyList) {
        tabs.setFamilyData(familyList);
    }

    public void setFamilyRowClickedListener(PatternRowClickedListener listener) {
        tabs.setFamilyRowClickedListener(listener);
    }

    public void setCogInfo(List<COG> patternGenes, Collection<COG> insertedGenes, Map<String, Color> colorsUsed) {
        csbSummaryPanel.displaySummary(patternGenes, insertedGenes, colorsUsed);
    }

    public void clearPanel(){
        tabs.clearPanel();
        csbSummaryPanel.clearPanel();
    }

    public void setMissingInfoText(String text){
        csbSummaryPanel.setMissingInfoText(text);
    }

    public void fireTableDataChanged(){
        tabs.fireTableDataChanged();
    }
}
