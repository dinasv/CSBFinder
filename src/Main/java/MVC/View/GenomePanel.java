package MVC.View;

import MVC.Common.InstanceInfo;
import MVC.View.Shapes.*;
import Utils.Gene;
import Utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.*;
import java.util.List;

public class GenomePanel extends JPanel {

    private JScrollPane scroll;
    private GenomePanelContainer container;

    public GenomePanel() {
        setLayout(new BorderLayout());

        container = new GenomePanelContainer();
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

    public Map<String,Color> getColorsUsed(){
        return container.getColorsUsed();
    }
}
