package mvc.view.components.panels;

import mvc.view.graphics.GeneColors;
import model.patterns.Pattern;
import model.postprocess.Family;
import model.cogs.COG;
import mvc.view.events.OpenDialogEvent;
import mvc.view.listeners.Listener;
import mvc.view.listeners.RowClickedListener;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

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

    /*
    public void setFilteredFamilies(List<Family> filteredFamilies){

        resultsPanel.setFilteredFamilies(filteredFamilies);

        if (filteredFamilies.size() == 0){
            csbSummaryPanel.clearPanel();
        }
    }*/

    public void setFilterTableListener(Listener<OpenDialogEvent> filterTableListener) {
        resultsPanel.setFilterTableListener(filterTableListener);
    }

    public void setFamilyData(List<Family> familyList) {
        resultsPanel.setFamilyData(familyList);
        //resultsPanel.selectFamiliesFirstRow();

        if (familyList.size() == 0){
            csbSummaryPanel.clearPanel();
        }
    }

    public void selectFirstRow(){
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

    public void setCogInfo(List<COG> patternGenes, Collection<COG> insertedGenes, GeneColors colorsUsed) {
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

    public void selectFamily(int familyId){
        resultsPanel.selectFamily(familyId);
    }

    public void selectPattern(String patternId){
        resultsPanel.selectPattern(patternId);
    }
}
