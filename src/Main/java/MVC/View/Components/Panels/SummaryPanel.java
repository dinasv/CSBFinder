package MVC.View.Components.Panels;

import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import Core.OrthologyGroups.COG;
import MVC.View.Listeners.FilterTableListener;
import MVC.View.Listeners.RowClickedListener;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SummaryPanel extends JSplitPane {

    private ResultsPanel resultsPanel;
    private CSBSummaryPanel csbSummaryPanel;

    public SummaryPanel(ImageIcon filterIcon) {
        csbSummaryPanel = new CSBSummaryPanel();

        resultsPanel = new ResultsPanel(filterIcon);

        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setDividerSize(2);
        setLeftComponent(resultsPanel);
        setRightComponent(csbSummaryPanel);
        setResizeWeight(0.7);
    }

    public void setFilteredFamilies(List<Family> filteredFamilies){
        resultsPanel.setFilteredFamilies(filteredFamilies);
    }

    public void setFilterTableListener(FilterTableListener filterTableListener) {
        resultsPanel.setFilterTableListener(filterTableListener);
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

    public void disableFilterBtn(){
        resultsPanel.disableFilterBtn();
    }

    public void enableFilterBtn(){
        resultsPanel.enableFilterBtn();
    }
}
