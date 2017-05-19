package Main;

import SuffixTrees.OccurrenceNode;
import SuffixTrees.Edge;

import java.util.ArrayList;

/**
 * The motif occurence starts from the root and ends in tha concat of lables to node occ + the label on the edge
 * that starts with ch and ends in edge_index
 */
public class Occurrence implements Comparable<Occurrence>{
    /**
     * The motif occurence starts from the root and ends in node occ + edge label until edge_index
     */
    private OccurrenceNode occ;
    /**
     * outgoing edge from occ
     */
    private Edge edge;
    /**
     * The occurrence ends at edge_index (concat of strings from root to node occ + label[0:edge_index], edge_index
     * included) if edge_index = -1 than it ends in the node occ
     */
    private int edge_index;
    //The error between motif and the Occurrence
    private int error;
    //number of deletions in the Occurrence (gap chars)
    private int deletions;

    private ArrayList<Integer> insertion_indexes;

    private String substring;
    private int length;

    public Occurrence(OccurrenceNode occ, Edge e, int edge_index, int error, int deletions, ArrayList<Integer> insertion_indexes, String substring, int length){
        this.occ = occ;
        this.edge = e;
        this.edge_index = edge_index;
        this.error = error;
        this.deletions = deletions;
        //this.insertions = insertions;
        this.substring = substring;
        this.length = length;
        this.insertion_indexes = new ArrayList<>();
        this.insertion_indexes.addAll(insertion_indexes);
    }

    public Occurrence(OccurrenceNode occ, Edge e, int edge_index, int error, int deletions){
        this.occ = occ;
        this.edge = e;
        this.edge_index = edge_index;
        this.error = error;
        this.deletions = deletions;
        this.substring = "";
        this.length = 0;
        this.insertion_indexes = new ArrayList<>();
    }

    public Occurrence(int deletions, String substring){
        occ = null;
        edge = null;
        edge_index = -2;
        error = -1;
        this.deletions = deletions;
        this.substring = substring;
    }

    public OccurrenceNode getNodeOcc(){
        return occ;
    }

    public int getError(){
        return error;
    }
    public int getDeletions(){
        return deletions;
    }

    public Edge getEdge(){
        return edge;
    }

    public int getEdgeIndex(){
        return edge_index;
    }

    public void setSubstring(String substring){
        this.substring = substring;
    }

    public String getSubstring(){
        return substring;
    }

    public void add_insertion_index(int index){
        insertion_indexes.add(index);
    }

    public void add_all_insertion_indexes(ArrayList<Integer> indexes){
        insertion_indexes.addAll(indexes);
    }

    @Override
    public int compareTo(Occurrence o) {
        OccurrenceNode occ_node_other = o.getNodeOcc();
        if (o.getEdge() != null) {
            occ_node_other = (OccurrenceNode) o.getEdge().getDest();
        }

        OccurrenceNode occ_node_this = occ;
        if (edge != null) {
            occ_node_this = (OccurrenceNode) edge.getDest();
        }

        return occ_node_other.getCount_by_keys() - occ_node_this.getCount_by_keys();
    }
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Occurrence)) return false;
        return ((Occurrence) other).getNodeOcc().getCount_by_keys() == occ.getCount_by_keys();
    }

    public int getInsertions() {
        return insertion_indexes.size();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public ArrayList<Integer> get_insertion_indexes(){
        return insertion_indexes;
    }
}
