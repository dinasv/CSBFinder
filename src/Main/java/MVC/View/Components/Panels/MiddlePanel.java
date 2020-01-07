package MVC.View.Components.Panels;

import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Events.DoubleClickGeneEvent;
import MVC.View.Graphics.GeneColors;
import Model.Genomes.GenomesInfo;
import Model.Genomes.Taxon;
import Model.Patterns.Pattern;
import MVC.View.Events.TooltipGeneEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MiddlePanel extends JPanel {

    private GenesViewPanel viewInstancesPanel;
    private TaxaPanel taxaPanel;


    public MiddlePanel(GeneColors colorsUsed ) {

        viewInstancesPanel = new GenesViewPanel(colorsUsed);
        taxaPanel = new TaxaPanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Gene View", viewInstancesPanel);
        tabbedPane.addTab("Taxa View",  taxaPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

    }

    public void setGenomesInfo(GenomesInfo genomesInfo){
        viewInstancesPanel.setGenomesInfo(genomesInfo);
    }

    public void setGenomeToTaxa(Map<String, Taxon> genomeToTaxa){
        taxaPanel.setGenomeToTaxa(genomeToTaxa);
    }

    private void displayTaxa(){
        List<String> genomeNames = viewInstancesPanel.getGenomeNames();
        taxaPanel.displayTaxa(genomeNames);
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

        taxaPanel.clearText();
        displayTaxa();
    }

    public void displayPatterns(List<Pattern> patterns) {

        viewInstancesPanel.displayPatterns(patterns);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
        taxaPanel.clear();
        taxaPanel.setText("Click on a CSB in order to view its taxonomic distribution.");
    }

    public void clearPanel(){
        taxaPanel.clear();
        viewInstancesPanel.clearPanel();
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }


    public void setGeneTooltipListener(Listener<TooltipGeneEvent> geneTooltipListener) {
        viewInstancesPanel.setGeneTooltipListener(geneTooltipListener);
    }

    public void setGeneDoubleClickListener(Listener<DoubleClickGeneEvent> geneDoubleClickListener) {
        viewInstancesPanel.setGeneDoubleClickListener(geneDoubleClickListener);
    }

    public void alignGenes(GeneShape anchorGene, JPanel clickedPanel, int viewX){
        viewInstancesPanel.alignGenes(anchorGene, clickedPanel, viewX);
    }

    public void zoomOut(int zoomUnit){
        viewInstancesPanel.zoomOut(zoomUnit);
    }

    public void zoomIn(int zoomUnit){
        viewInstancesPanel.zoomIn(zoomUnit);
    }
}
