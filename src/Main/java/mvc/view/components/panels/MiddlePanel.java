package mvc.view.components.panels;

import mvc.view.components.shapes.GeneShape;
import mvc.view.events.DoubleClickGeneEvent;
import mvc.view.graphics.GeneColors;
import model.genomes.GenomesInfo;
import model.genomes.Taxon;
import model.patterns.Pattern;
import mvc.view.events.TooltipGeneEvent;
import mvc.view.listeners.Listener;

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

        tabbedPane.addTab("Gene view", viewInstancesPanel);
        tabbedPane.addTab("Taxa view",  taxaPanel);

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
        displayTaxa();
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();

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
