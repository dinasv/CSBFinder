package MVC.View.Components.Panels;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GenomePanel extends JPanel {

    private JScrollPane scroll;
    private GenomePanelContainer viewInstancesPanel;

    public GenomePanel(Map<String, Color> colorsUsed ) {
        setLayout(new BorderLayout());

        viewInstancesPanel = new GenomePanelContainer(colorsUsed);
        scroll = new JScrollPane(viewInstancesPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

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
        int scrollWidth = scroll.getViewport().getSize().width;

        viewInstancesPanel.displayInstances(pattern, scrollWidth);
        viewInstancesPanel.revalidate();
        viewInstancesPanel.repaint();
    }

    public void displayPatterns(List<Pattern> patterns) {
        int scrollWidth = scroll.getViewport().getSize().width;
        viewInstancesPanel.displayPatterns(patterns, scrollWidth);
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
}
