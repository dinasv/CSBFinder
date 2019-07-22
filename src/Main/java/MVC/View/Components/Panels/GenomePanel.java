package MVC.View.Components.Panels;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Events.DoubleClickGeneEvent;
import Model.Genomes.GenomesInfo;
import Model.Genomes.Strand;
import Model.Patterns.Pattern;
import MVC.View.Events.TooltipGeneEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GenomePanel extends JPanel {

    private GenomePanelContainer viewInstancesPanel;

    public GenomePanel(Map<String, Color> colorsUsed ) {
        setLayout(new BorderLayout());

        viewInstancesPanel = new GenomePanelContainer(colorsUsed);

        add(viewInstancesPanel, BorderLayout.CENTER);

    }

    public void setGenomesInfo(GenomesInfo genomesInfo){
        viewInstancesPanel.setGenomesInfo(genomesInfo);
    }

    public void setNumOfNeighbors(int numOfNeighbors){

        viewInstancesPanel.setNumOfNeighbors(numOfNeighbors);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }

    public void displayInstances(Pattern pattern) {

        viewInstancesPanel.displayInstances(pattern);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }

    public void displayPatterns(List<Pattern> patterns) {

        viewInstancesPanel.displayPatterns(patterns);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }

    public void clearPanel(){
        viewInstancesPanel.clearPanel();
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }

    public Map<String,Color> getColorsUsed(){
        return viewInstancesPanel.getColorsUsed();
    }

    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        viewInstancesPanel.setGeneTooltipListener(geneTooltipListener);
    }

    public void setGeneDoubleClickListener(Listener<DoubleClickGeneEvent> geneDoubleClickListener) {
        viewInstancesPanel.setGeneDoubleClickListener(geneDoubleClickListener);
    }

    public void alignGenes(GeneShape anchorGene, int viewX){
        viewInstancesPanel.alignGenes(anchorGene, viewX);
    }

    public void zoomOut(int zoomUnit){
        viewInstancesPanel.zoomOut(zoomUnit);
    }

    public void zoomIn(int zoomUnit){
        viewInstancesPanel.zoomIn(zoomUnit);
    }
}
