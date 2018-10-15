package MVC.View.Panels;

import MVC.Common.InstanceInfo;

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

    public void displayInstances(String[] pattenCOGs, Map<String,Map<String,List<InstanceInfo>>> instances) {
        int scroll_width = scroll.getViewport().getSize().width;
        container.displayInstances(pattenCOGs, instances, scroll_width);
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
