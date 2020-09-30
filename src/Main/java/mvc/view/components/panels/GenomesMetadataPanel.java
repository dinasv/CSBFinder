package mvc.view.components.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenomesMetadataPanel extends JPanel{

    private Map<String, Object[]> genomeToMetadata;

    private final DefaultTableModel model;

    public GenomesMetadataPanel() {

        super(new BorderLayout());

        genomeToMetadata = new HashMap<>();

        JTable table = new JTable();

        table.setAutoCreateRowSorter(true);
        model = (DefaultTableModel) table.getModel();

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setTableRows(List<String> genomeNames){

        model.setRowCount(0);

        genomeNames.stream()
                .map(genome -> genomeToMetadata.getOrDefault(genome, new String[]{genome}))
                .forEach(model::addRow);

    }

    public void setGenomeToMetadata(Map<String, Object[]> genomeToMetadata) {
        this.genomeToMetadata = genomeToMetadata;
    }

    public void setColumnNames(String[] columnNames){

        model.setColumnIdentifiers(columnNames);
    }

    public void clear(){
        model.setRowCount(0);
    }
}
