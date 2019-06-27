package MVC.View.Components.Panels;

import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import Model.OrthologyGroups.COG;
import MVC.View.Events.OpenDialogEvent;
import MVC.View.Listeners.Listener;
import MVC.View.Listeners.RowClickedListener;

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

        if (filteredFamilies.size() == 0){
            csbSummaryPanel.clearPanel();
        }
    }

    public void setFilterTableListener(Listener<OpenDialogEvent> filterTableListener) {
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

    public void familyTableRowClick(Family family){
        resultsPanel.familyTableRowClick(family);
    }

    public void patternsTableRowClick(Pattern pattern){
        resultsPanel.patternsTableRowClick(pattern);
    }

    public void selectFamily(int familyId){
        resultsPanel.selectFamily(familyId);
    }

    public void selectPattern(String patternId){
        resultsPanel.selectPattern(patternId);
    }
}
