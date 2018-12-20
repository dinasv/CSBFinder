package MVC.View.Components.Panels;

import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import Core.OrthologyGroups.COG;
import MVC.View.Listeners.RowClickedListener;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SummaryPanel extends JSplitPane {

    private ResultsPanel resultsPanel;
    private CSBSummaryPanel csbSummaryPanel;

    public SummaryPanel() {
        csbSummaryPanel = new CSBSummaryPanel();

        resultsPanel = new ResultsPanel();

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(resultsPanel);
        setRightComponent(csbSummaryPanel);
        setResizeWeight(0.7);
    }

    public void setFamilyData(List<Family> familyList) {
        resultsPanel.setFamilyData(familyList);
        resultsPanel.selectFamiliesFirstRow();
    }

    public void setFamilyPatternsData(Family family){
        resultsPanel.setFamilyPatternsData(family);
    }

    public void setPatternRowClickedListener(RowClickedListener<Pattern> listener) {
        resultsPanel.setPatternRowClickedListener(listener);
    }

    public void setFamilyRowClickedListener(RowClickedListener<Family> listener) {
        resultsPanel.setFamilyRowClickedListener(listener);
    }

    public void setCogInfo(List<COG> patternGenes, Collection<COG> insertedGenes, Map<String, Color> colorsUsed) {
        csbSummaryPanel.displaySummary(patternGenes, insertedGenes, colorsUsed);
    }

    public void clearPanel(){
        resultsPanel.clearPanel();
        csbSummaryPanel.clearPanel();
    }

    public void setMissingInfoText(String text){
        csbSummaryPanel.setMissingInfoText(text);
    }

    public void fireTableDataChanged(){
        resultsPanel.fireTableDataChanged();
    }
}
