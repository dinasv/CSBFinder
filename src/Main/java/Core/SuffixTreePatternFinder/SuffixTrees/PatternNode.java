package Core.SuffixTreePatternFinder.SuffixTrees;

import Core.Patterns.Instance;

import java.util.*;

/**
 * Represents a node in the pattern tree
 */
public class PatternNode {
    private Map<Integer, PatternNode> targetNodes;
    /**
     * Contains the key of the string concatenation from the root
     */
    private String patternKey;

    private int copyCount;

    private TreeType type;

    private PatternNode suffix;

    private int substringLength;

    /**
     * The concat of labels from root to this node
     * e.g. COG1209 COG1088 COG1091 COG 4723
     */
    private String substring;

    /**
     * Saves instances of the pattern that starts in the root and ends in this node
     */
    private List<Instance> instances;
    /**
     * Saves all different keys of strings that the pattern has an instance in.
     * (union of getResult from all instance nodes)
     */
    private Set instanceKeys;
    /**
     * Saves number of instances of the pattern (including multiple instances in the same input string)
     */
    private int instanceIndexCount;

    private double pVal;

    private int exactInstanceCount;

    public PatternNode(TreeType type){
        targetNodes = new HashMap<Integer, PatternNode>();
        suffix = null;

        patternKey = null;
        copyCount = 0;
        this.type = type;

        instances = new ArrayList<Instance>();
        instanceKeys = new HashSet();
        instanceIndexCount = 0;

        substring = "";
        substringLength = 0;

        pVal = 0;
        exactInstanceCount = 0;
    }

    public PatternNode(PatternNode other){
        substring = other.getSubstring();
        substringLength = other.getSubstringLength();
        patternKey = other.getPatternKey();
        copyCount = other.getCopyCount()+1;
        type = other.type;
        suffix = other.getSuffix();

        instances = new ArrayList<Instance>();
        instanceKeys = new HashSet();

        pVal = 0;
        exactInstanceCount = 0;

        Map<Integer, PatternNode> other_target_nodes = other.getTargetNodes();
        targetNodes = new HashMap<Integer, PatternNode>(other_target_nodes.size());
        targetNodes.putAll(other_target_nodes);
    }

    public Map<Integer, PatternNode> getTargetNodes(){
        return targetNodes;
    }
    public int getSubstringLength() {
        return substringLength;
    }

    public void setSubstringLength(int substringLength) {
        this.substringLength = substringLength;
    }

    public void addTargetNode(int ch, PatternNode node){
        targetNodes.put(ch, node);
    }

    public PatternNode getTargetNode(int ch){
        return targetNodes.get(ch);
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

    public String getPatternKey(){
        return patternKey;
    }

    public void setKey(String key){
        patternKey = key;
    }

    public int getCopyCount(){
        return copyCount;
    }

    public void addInstance(Instance instance){

        instances.add(instance);
        InstanceNode instance_node;
        if (instance.getEdge() == null) {
            instance_node = instance.getNodeInstance();
            addOccKeys(instance_node.getGenomeToLocationsInSubtree().keySet());
        } else {//we are in the middle of the edge, the instance is a suffix of edge.getDest()
            instance_node = (InstanceNode)instance.getEdge().getDest();
            addOccKeys(instance_node.getGenomeToLocationsInSubtree().keySet());
        }
        incrementInstanceCount(instance_node.getCountMultipleInstancesPerGenome());
    }

    public List<Instance> getInstances(){
        return instances;
    }

    public void addOccKeys(Set<Integer> keys){
        instanceKeys.addAll(keys);
    }

    public int getInstanceKeysSize(){
        return instanceKeys.size();
    }

    public Set<Integer> getInstanceKeys(){
        return instanceKeys;
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

    public double getpVal() {
        return pVal;
    }

    public void setpVal(double pVal) {
        this.pVal = pVal;
    }

    public int getExactInstanceCount() {
        return exactInstanceCount;
    }

    public void setExactInstanceCount(int exactInstanceCount) {
        this.exactInstanceCount = exactInstanceCount;
    }
}
