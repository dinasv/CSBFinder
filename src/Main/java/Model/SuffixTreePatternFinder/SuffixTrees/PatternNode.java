package Model.SuffixTreePatternFinder.SuffixTrees;

import Model.Patterns.Instance;

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

    private TreeType type;

    private PatternNode suffix;

    /**
     * Saves instances of the pattern that starts in the root and ends in this node
     */
    private List<Instance> instances;
    /**
     * Saves all different keys of strings that the pattern has an instance in.
     * (union of getResult from all instance nodes)
     */
    private Set<Integer> instanceKeys;
    /**
     * Saves number of instances of the pattern (including multiple instances in the same input string)
     */
    private int instanceIndexCount;

    private int exactInstanceCount;

    public PatternNode(TreeType type){

        this.type = type;

        targetNodes = new HashMap<>();
        suffix = null;
        patternKey = null;
        instances = new ArrayList<>();
        instanceKeys = new HashSet<>();
        instanceIndexCount = 0;

        exactInstanceCount = 0;
    }

    public Map<Integer, PatternNode> getTargetNodes(){
        return targetNodes;
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

    public void addInstance(Instance instance){

        instances.add(instance);
        InstanceNode instanceNode;
        if (instance.getEdge() == null) {
            instanceNode = instance.getNodeInstance();
            addOccKeys(instanceNode.getGenomeToLocationsInSubtree().keySet());
        } else {//we are in the middle of the edge, the instance is a suffix of edge.getDest()
            instanceNode = instance.getEdge().getDest();
            addOccKeys(instanceNode.getGenomeToLocationsInSubtree().keySet());
        }
        incrementInstanceCount(instanceNode.getCountMultipleInstancesPerGenome());
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

    public int getExactInstanceCount() {
        return exactInstanceCount;
    }

    public void setExactInstanceCount(int exactInstanceCount) {
        this.exactInstanceCount = exactInstanceCount;
    }
}
