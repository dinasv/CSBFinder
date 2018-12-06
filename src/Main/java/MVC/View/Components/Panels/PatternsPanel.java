package MVC.View.Components.Panels;

import MVC.View.Events.FamilyRowClickedEvent;
import MVC.View.Listeners.PatternRowClickedListener;
import MVC.View.Models.PatternProperties;
import MVC.View.Models.PatternsTableModel;
import Core.Genomes.Pattern;
import Core.PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PatternsPanel extends JPanel {

    private JTable table;
    private PatternsTableModel model;
    private JScrollPane scrollPane;
    private PatternRowClickedListener rowClickedListener;

    public PatternsPanel(PatternProperties[] columns) {
        setLayout(new BorderLayout());

        model = new PatternsTableModel(columns);

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                PatternsTableModel model = ((PatternsTableModel) table.getModel());

                rowClickedListener.rowClickedOccurred(new FamilyRowClickedEvent(
                        model.getPattern((String) table.getValueAt(table.getSelectedRow(), model.getIndexOfColumn(PatternProperties.CSB)))
                ));
            }
        });
        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setData(List<Family> families) {
        model.setData(families);
        model.fireTableDataChanged();
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    public void setTopScoreData(List<Family> families) {
        List<Family> topScoreCSBs = getTopScoreCSBs(families);
        setData(topScoreCSBs);
    }

    private List<Family> getTopScoreCSBs(List<Family> families) {
        List<Family> topScoreCSBs = families.stream().map(Family::new).collect(Collectors.toList());

        topScoreCSBs.forEach(family -> {
            ArrayList<Pattern> plist = new ArrayList<>();
            plist.add(family.getTopScoringPattern());
            family.setPatterns(plist);
        });

        return topScoreCSBs;
    }

    public void setRowClickedListener(PatternRowClickedListener rowClickedListener) {
        this.rowClickedListener = rowClickedListener;
    }

    public void clearPanel(){
        model.clearData();
        model.fireTableDataChanged();
    }

    public void fireTableDataChanged(){
        int row = table.getSelectedRow();
        model.fireTableDataChanged();

        if (row != -1) {
            table.setRowSelectionInterval(row, row);
        }
    }
}
