package mvc.view.components.panels;

import model.genomes.Taxon;

import javax.swing.*;

import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TaxaPanel extends JPanel{

    private static final int NUM_OF_LEVELS = 4;
    private static final int NUM_OF_LEVELS_TO_DISPLAY = 3;
    private JTree tree;
    private DefaultTreeModel model;

    private JLabel text;

    private DefaultMutableTreeNode topNode;
    private Map<String, Taxon> genomeToTaxa;

    public TaxaPanel(){

        super(new BorderLayout());

        genomeToTaxa = new HashMap<>();

        topNode = new DefaultMutableTreeNode("");
        model = new DefaultTreeModel(topNode);

        //Create a tree that allows one selection at a time.
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //remove all icons
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        tree.setCellRenderer(renderer);

        JScrollPane treeView = new JScrollPane(tree);

        text = new JLabel();
        text.setBorder(new EmptyBorder(0,10,0,0));//top,left,bottom,right

        add(treeView, BorderLayout.CENTER);
        add(text, BorderLayout.PAGE_START);
    }


    public void setGenomeToTaxa(Map<String, Taxon> genomeToTaxa) {
        this.genomeToTaxa = genomeToTaxa;
    }


    public void displayTaxa(List<String> genomeNames) {
        if (genomeToTaxa.size() == 0){
            return;
        }

        topNode.removeAllChildren();

        for (String genomeName : genomeNames){
            Taxon taxon = genomeToTaxa.getOrDefault(genomeName, new Taxon());

            insertInto(genomeName, taxon, 0, topNode);

        }

        model.reload();

        expandAll(0, topNode);

    }


    private void expandAll(int taxaLevel, DefaultMutableTreeNode node){

        if (taxaLevel == NUM_OF_LEVELS_TO_DISPLAY) {
            return;
        }

        tree.expandPath(new TreePath(node.getPath()));

        DefaultMutableTreeNode child;
        for (int i = 0; i < node.getChildCount() ; i++) {
            child = (DefaultMutableTreeNode) node.getChildAt(i);

            expandAll(taxaLevel+1, child);
        }

    }

    private void insertInto(String genomeName, Taxon taxon, int taxaLevel, DefaultMutableTreeNode node){

        if (taxaLevel > NUM_OF_LEVELS) {
            DefaultMutableTreeNode genome = new DefaultMutableTreeNode(genomeName);
            model.insertNodeInto(genome, node, node.getChildCount());

            return;
        }

        tree.expandPath(new TreePath(node.getPath()));

        DefaultMutableTreeNode nextNode = null;
        DefaultMutableTreeNode child;

        for (int i = 0; i < node.getChildCount() & nextNode == null; i++) {
            child = (DefaultMutableTreeNode) node.getChildAt(i);
            TaxaInfo taxaInfo = (TaxaInfo) child.getUserObject();

            if (taxaInfo.taxaName.equals(taxon.getTaxaAtLevel(taxaLevel))) {
                taxaInfo.incrementGenomesCount();
                nextNode = child;
                //insert ordered alphabetically
            }else if (taxaInfo.taxaName.compareTo(taxon.getTaxaAtLevel(taxaLevel)) > 0){
                nextNode = new DefaultMutableTreeNode(new TaxaInfo(taxon.getTaxaAtLevel(taxaLevel), 1));
                model.insertNodeInto(nextNode, node, i);
            }
        }

        if (nextNode == null){
            nextNode = new DefaultMutableTreeNode(new TaxaInfo(taxon.getTaxaAtLevel(taxaLevel), 1));
            model.insertNodeInto(nextNode, node, node.getChildCount());
        }

        insertInto(genomeName, taxon, taxaLevel+1, nextNode);

    }

    public void setText(String text){
        this.text.setText(text);
    }

    public void clearText(){
        this.text.setText("");
    }

    public void clear(){
        topNode.removeAllChildren();
        model.reload();
    }

    private class TaxaInfo {
        public String taxaName;
        public int genomesCount;

        public TaxaInfo(String taxaName, int genomesCount) {
            this.taxaName = taxaName;
            this.genomesCount = genomesCount;
        }

        public String toString() {
            int totalTaxa = Taxon.getTaxaCount(taxaName);

            String text = String.format("%s [%s]", taxaName, genomesCount);
            if (totalTaxa != -1){
                text = String.format("%s [%s/%s]", taxaName, genomesCount, totalTaxa);
            }

            return text;
        }

        public void setGenomesCount(int genomesCount){
            this.genomesCount = genomesCount;
        }

        public void incrementGenomesCount(){
            this.genomesCount += 1;
        }
    }

}

