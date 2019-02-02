package MVC.View.Components.Panels;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GenomePanel extends JPanel {

    private JScrollPane scroll;
    private GenomePanelContainer container;

    public GenomePanel(Map<String, Color> colorsUsed ) {
        setLayout(new BorderLayout());

        container = new GenomePanelContainer(colorsUsed);
        scroll = new JScrollPane(container);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

    }

    public void displayInstances(Pattern pattern, GenomesInfo genomesInfo, int numOfNeighbors) {
        int scrollWidth = scroll.getViewport().getSize().width;

        container.displayInstances(pattern, scrollWidth, genomesInfo, numOfNeighbors);
        container.revalidate();
        container.repaint();
    }

    public void displayPatterns(List<Pattern> patterns) {
        int scrollWidth = scroll.getViewport().getSize().width;
        container.displayPatterns(patterns, scrollWidth);
        container.revalidate();
        container.repaint();
    }

    public void clearPanel(){
        container.clearPanel();
        container.revalidate();
        container.repaint();
    }

    public Map<String,Color> getColorsUsed(){
        return container.getColorsUsed();
    }
}
