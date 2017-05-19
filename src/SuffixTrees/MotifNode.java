package SuffixTrees;

import Main.Occurrence;

import java.util.*;

/**
 * Created by Dina on 7/21/2016.
 */
public class MotifNode implements Comparable<MotifNode> {
    private HashMap<Integer, MotifNode> target_nodes;
    /**
     * Contains the key of the string concatenation from the root
     */
    private int motif_key;

    private int copy_count;

    private String type;

    private MotifNode suffix;

    private int substring_length;

    private HashSet<String> occs_strings;

    /**
     * The concat of labels from root to this node
     * i.e. 1209|1088|1091|4723|
     */
    private String substring;

    /**
     * Saves occurrences of the motif that starts in the root and ends in this node
     */
    private ArrayList<Occurrence> Occs;
    /**
     * Saves all different keys of strings that the motif occurs in.
     * (union of getResult from all occurrences nodes)
     */
    private HashSet occsKeys;
    /**
     * Saves number of occurrences of the motif (including multiple occurrences in the same word)
     */
    private int occsIndexCount;

    private double std;
    private double expected;
    private int infix_count;
    private int suffix_count;
    private int prefix_count;

    private double p_val;

    private int exact_occs_conut;

    public MotifNode(String type){
        target_nodes = new HashMap<Integer, MotifNode>();
        suffix = null;

        motif_key = -1;
        copy_count = 0;
        this.type = type;

        Occs = new ArrayList<Occurrence>();
        occsKeys = new HashSet();
        std = 0;
        infix_count = 0;
        suffix_count = 0;
        prefix_count = 0;
        occsIndexCount = 0;
        expected = 0;

        substring = "";
        substring_length = 0;

        occs_strings = new HashSet<String>();
        p_val = 0;
        exact_occs_conut = 0;
    }

    public MotifNode(MotifNode other){
        substring = other.getSubstring();
        substring_length = other.getSubstring_length();
        motif_key = other.getMotifKey();
        copy_count = other.getCopy_count()+1;
        type = other.type;
        suffix = other.getSuffix();

        Occs = new ArrayList<Occurrence>();
        occs_strings = new HashSet<String>();
        occsKeys = new HashSet();

        std = 0;
        infix_count = 0;
        suffix_count = 0;
        prefix_count = 0;
        expected = 0;
        p_val = 0;
        exact_occs_conut = 0;

        HashMap<Integer, MotifNode> other_target_nodes = other.getTarget_nodes();
        target_nodes = new HashMap<Integer, MotifNode>(other_target_nodes.size());
        target_nodes.putAll(other_target_nodes);
    }

    public HashMap<Integer, MotifNode> getTarget_nodes(){
        return target_nodes;
    }
    public int getSubstring_length() {
        return substring_length;
    }

    public void setSubstring_length(int substring_length) {
        this.substring_length = substring_length;
    }

    public void addTargetNode(int ch, MotifNode node){
        target_nodes.put(ch, node);
    }

    public MotifNode getTargetNode(int ch){
        return target_nodes.get(ch);
    }
    public MotifNode getSuffix() {
        return suffix;
    }

    public void setSuffix(MotifNode suffix) {
        this.suffix = suffix;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public int getMotifKey(){
        return motif_key;
    }

    public void setKey(int key){
        motif_key = key;
    }

    public int getCopy_count(){
        return copy_count;
    }

    public Boolean addOcc(Occurrence occ, int max_insertion){

        //need to check if this string exist already to handle insertions, because for example if ABBC is an occurrence
        //the motif ABC with one insertion of B, B could be inserted in position 1 or 2, but it is the same occurrence
        Boolean is_new_occ = true;
        if (max_insertion > 0){
            is_new_occ = occs_strings.add(occ.getSubstring());
        }
        if (is_new_occ) {
            Occs.add(occ);
            OccurrenceNode occ_node;
            if (occ.getEdge() == null) {
                occ_node = occ.getNodeOcc();
                addOccKeys(occ_node.getResults().keySet());
            } else {//we are in the middle of the edge, the occ is a suffix of edge.getDest()
                occ_node = (OccurrenceNode)occ.getEdge().getDest();
                addOccKeys(occ_node.getResults().keySet());
            }
            incrementOccsCount(occ_node.getCount_by_indexes());
        }
        return is_new_occ;
    }

    public ArrayList<Occurrence> getOccs(){
        return Occs;
    }

    public void addOccKeys(Set<Integer> keys){
        occsKeys.addAll(keys);
    }

    public int getOccKeysSize(){
        return occsKeys.size();
    }

    public HashSet<Integer> getOccKeys(){
        return occsKeys;
    }

    public double getStd(){
        return std;
    }
    public void setStd(double std){
        this.std = std;
    }

    public int getInfix_count(){
        return infix_count;
    }

    public int getSuffix_count(){
        return suffix_count;
    }

    public int getPrefix_count(){
        return prefix_count;
    }

    public void setPrefix_count(int val){
        prefix_count = val;
    }

    public int getOccsIndexCount(){
        return occsIndexCount;
    }

    public void incrementOccsCount(int val){
        occsIndexCount += val;
    }

    @Override
    public int compareTo(MotifNode o) {

        double comparedSize = o.getStd();
        if (this.getStd() < comparedSize) {
            return 1;
        } else if (this.getStd() == comparedSize) {
            return 0;
        } else {
            return -1;
        }
    }

    public double getExpected() {
        return expected;
    }

    public void setExpected(double expected) {
        this.expected = expected;
    }

    public void setSubstring(String substr){
        substring = substr;
    }
    public String getSubstring(){
        return substring;
    }

    public static Comparator<MotifNode> OccCountComperator = new Comparator<MotifNode>() {

        public int compare(MotifNode node1, MotifNode node2) {

            double comparedSize = node2.getOccKeysSize();
            if (node1.getOccKeysSize() < comparedSize) {
                return 1;
            } else if (node1.getOccKeysSize() == comparedSize) {
                return 0;
            } else {
                return -1;
            }
        }

    };

    public static Comparator<MotifNode> motif_keyComperator = new Comparator<MotifNode>() {

        public int compare(MotifNode node1, MotifNode node2) {

            double comparedSize = node2.getMotifKey();
            if (node1.getMotifKey() < comparedSize) {
                return 1;
            } else if (node1.getMotifKey() == comparedSize) {
                return 0;
            } else {
                return -1;
            }
        }

    };

    public double getP_val() {
        return p_val;
    }

    public void setP_val(double p_val) {
        this.p_val = p_val;
    }

    public int getExact_occs_conut() {
        return exact_occs_conut;
    }

    public void setExact_occs_count(int exact_occs_conut) {
        this.exact_occs_conut = exact_occs_conut;
    }
}
