package MVC.View;

import MVC.View.Events.FamilyRowClickedEvent;
import MVC.View.Listeners.FamilyRowClickedListener;
import MVC.View.Models.FamilyTableModel;
import Utils.Pattern;
import PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyPanel extends JPanel {

    private JTable table;
    private FamilyTableModel model;
    private JScrollPane scrollPane;
    private FamilyRowClickedListener rowClickedListener;

    public FamilyPanel() {
        setLayout(new BorderLayout());
        model = new FamilyTableModel();
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                FamilyTableModel model = ((FamilyTableModel) table.getModel());

                rowClickedListener.rowClickedOccurred(new FamilyRowClickedEvent(
                        //((FamilyTableModel) table.getModel()).getRowAt(table.getSelectedRow())
                        model.getPattern((String) table.getValueAt(table.getSelectedRow(), model.getIndexOfColumn(model.CSB)))
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
        table.setRowSelectionInterval(0,0);
    }

    public void setTopScoreData(List<Family> families) {
        List<Family> topScoreCSBs = getTopScoreCSBs(families);
        setData(topScoreCSBs);
    }

    private List<Family> getTopScoreCSBs(List<Family> families) {
        List<Family> topScoreCSBs = families.stream().map(Family::new).collect(Collectors.toList());
        topScoreCSBs.forEach(family -> family.getPatterns().sort(Comparator.comparing(Pattern::getScore).reversed()));
        topScoreCSBs.forEach(family -> {
            ArrayList<Pattern> plist = new ArrayList<>();
            plist.add(family.getPatterns().get(0));
            family.setPatterns(plist);
        });
        return topScoreCSBs;
    }

    public void setRowClickedListener(FamilyRowClickedListener rowClickedListener) {
        this.rowClickedListener = rowClickedListener;
    }

    public void clearPanel(){
        model.clearData();
        model.fireTableDataChanged();
    }
}
