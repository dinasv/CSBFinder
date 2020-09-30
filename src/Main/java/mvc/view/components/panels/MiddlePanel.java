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
    private GenomesMetadataPanel genomesMetadataPanel;


    public MiddlePanel(GeneColors colorsUsed ) {

        viewInstancesPanel = new GenesViewPanel(colorsUsed);
        taxaPanel = new TaxaPanel();
        genomesMetadataPanel = new GenomesMetadataPanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Gene view", viewInstancesPanel);
        tabbedPane.addTab("Taxa view",  taxaPanel);
        tabbedPane.addTab("Genome Metadata",  genomesMetadataPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

    }

    public void setGenomesInfo(GenomesInfo genomesInfo){
        viewInstancesPanel.setGenomesInfo(genomesInfo);
    }

    public void setGenomeToTaxa(Map<String, Taxon> genomeToTaxa){
        taxaPanel.setGenomeToTaxa(genomeToTaxa);
    }

    public void setGenomeMetadata(String[] columnNames, Map<String, Object[]> genomeToMetadata){
        genomesMetadataPanel.setGenomeToMetadata(genomeToMetadata);
        genomesMetadataPanel.setColumnNames(columnNames);
    }

    private void displayTaxa(){
        List<String> genomeNames = viewInstancesPanel.getGenomeNames();
        taxaPanel.displayTaxa(genomeNames);
    }

    private void displayMetadata(){
        List<String> genomeNames = viewInstancesPanel.getGenomeNames();
        genomesMetadataPanel.setTableRows(genomeNames);
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
        displayMetadata();
    }

    public void displayPatterns(List<Pattern> patterns) {

        viewInstancesPanel.displayPatterns(patterns);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();

        taxaPanel.clearText();
        displayTaxa();
        displayMetadata();
    }

    public void clearPanel(){
        taxaPanel.clear();
        genomesMetadataPanel.clear();
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
