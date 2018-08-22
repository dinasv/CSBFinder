package SuffixTrees;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS on 7/20/2016.
 */
public class SuffixNode{
    /**
     * The suffix link as described in Ukkonen's paper.
     * if str is the string denoted by the path from the root to this, this.suffix
     * is the node denoted by the path that corresponds to str without the first char.
     */
    private SuffixNode suffix;

    /**
     * The set of edges starting from this node
     * used in suffix and enumeration tree
     */
    private final Map<Integer, Edge> edges;

    /**
     * The concat of labels from root to this node
     * i.e. 1209|X|1088|1091|4723|
     */
    private String substring;

    private int substring_length;

    public void addEdge(int ch, Edge e) {
        edges.put(ch, e);
    }

    //returns null if there is no such edge
    public Edge getEdge(int ch) {
        return edges.get(ch);
    }

    public Map<Integer, Edge> getEdges() {
        return edges;
    }

    public String getSubstring(){
        return substring;
    }

    public int getSubstring_length() {
        return substring_length;
    }

    public SuffixNode(){

        suffix = null;

        edges = new HashMap<Integer, Edge>();
        substring = "";
        substring_length = 0;
    }
    /**
     * Creates a copy of thie node, deep copies edges
     * @param other
     */
    public SuffixNode(SuffixNode other) {
        suffix = other.getSuffix();
        substring = other.getSubstring();
        substring_length = other.getSubstring_length();

        Map<Integer, Edge> other_edges = other.getEdges();
        edges = new HashMap<Integer, Edge>(other_edges.size());
        for (Map.Entry<Integer, Edge> entry : other_edges.entrySet()) {
            int ch = entry.getKey();
            Edge edge = entry.getValue();
            addEdge(ch, new Edge(edge));
        }
    }

    public SuffixNode getSuffix() {
        return suffix;
    }

    public void setSuffix(SuffixNode suffix) {
        this.suffix = suffix;
    }
}
