package SuffixTrees;

import Main.Instance;

import java.util.*;

/**
 * Represents a node in the pattern tree
 */
public class PatternNode {
    private HashMap<Integer, PatternNode> target_nodes;
    /**
     * Contains the key of the string concatenation from the root
     */
    private int pattern_key;

    private int copy_count;

    private TreeType type;

    private PatternNode suffix;

    private int substring_length;

    /**
     * The concat of labels from root to this node
     * i.e. 1209|1088|1091|4723|
     */
    private String substring;

    /**
     * Saves instances of the pattern that starts in the root and ends in this node
     */
    private ArrayList<Instance> Instances;
    /**
     * Saves all different keys of strings that the pattern has an instance in.
     * (union of getResult from all instance nodes)
     */
    private HashSet InstanceKeys;
    /**
     * Saves number of instances of the pattern (including multiple instances in the same input string)
     */
    private int instanceIndexCount;

    private double p_val;

    private int exact_instance_count;

    public PatternNode(TreeType type){
        target_nodes = new HashMap<Integer, PatternNode>();
        suffix = null;

        pattern_key = -1;
        copy_count = 0;
        this.type = type;

        Instances = new ArrayList<Instance>();
        InstanceKeys = new HashSet();
        instanceIndexCount = 0;

        substring = "";
        substring_length = 0;

        p_val = 0;
        exact_instance_count = 0;
    }

    public PatternNode(PatternNode other){
        substring = other.getSubstring();
        substring_length = other.getSubstring_length();
        pattern_key = other.getPatternKey();
        copy_count = other.getCopy_count()+1;
        type = other.type;
        suffix = other.getSuffix();

        Instances = new ArrayList<Instance>();
        InstanceKeys = new HashSet();

        p_val = 0;
        exact_instance_count = 0;

        HashMap<Integer, PatternNode> other_target_nodes = other.getTarget_nodes();
        target_nodes = new HashMap<Integer, PatternNode>(other_target_nodes.size());
        target_nodes.putAll(other_target_nodes);
    }

    public HashMap<Integer, PatternNode> getTarget_nodes(){
        return target_nodes;
    }
    public int getSubstring_length() {
        return substring_length;
    }

    public void setSubstring_length(int substring_length) {
        this.substring_length = substring_length;
    }

    public void addTargetNode(int ch, PatternNode node){
        target_nodes.put(ch, node);
    }

    public PatternNode getTargetNode(int ch){
        return target_nodes.get(ch);
    }
    public PatternNode getSuffix() {
        return suffix;
    }

    public void setSuffix(PatternNode suffix) {
        this.suffix = suffix;
    }

    public TreeType getType(){
        return type;
    }

    public void setType(TreeType type){
        this.type = type;
    }

    public int getPatternKey(){
        return pattern_key;
    }

    public void setKey(int key){
        pattern_key = key;
    }

    public int getCopy_count(){
        return copy_count;
    }

    public void addInstance(Instance instance, int max_insertion){

        Instances.add(instance);
        InstanceNode instance_node;
        if (instance.getEdge() == null) {
            instance_node = instance.getNodeInstance();
            addOccKeys(instance_node.getResults().keySet());
        } else {//we are in the middle of the edge, the instance is a suffix of edge.getDest()
            instance_node = (InstanceNode)instance.getEdge().getDest();
            addOccKeys(instance_node.getResults().keySet());
        }
        incrementInstanceCount(instance_node.getCount_by_indexes());
    }

    public ArrayList<Instance> getInstances(){
        return Instances;
    }

    public void addOccKeys(Set<Integer> keys){
        InstanceKeys.addAll(keys);
    }

    public int getInstanceKeysSize(){
        return InstanceKeys.size();
    }

    public HashSet<Integer> getInstanceKeys(){
        return InstanceKeys;
    }

    public int getInstanceIndexCount(){
        return instanceIndexCount;
    }

    public void incrementInstanceCount(int val){
        instanceIndexCount += val;
    }

    public void setSubstring(String substr){
        substring = substr;
    }
    public String getSubstring(){
        return substring;
    }

    public double getP_val() {
        return p_val;
    }

    public void setP_val(double p_val) {
        this.p_val = p_val;
    }

    public int getExact_instance_count() {
        return exact_instance_count;
    }

    public void setExact_instance_count(int exact_occs_conut) {
        this.exact_instance_count = exact_occs_conut;
    }
}
