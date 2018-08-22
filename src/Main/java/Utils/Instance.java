package Utils;

import SuffixTrees.InstanceNode;
import SuffixTrees.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The motif instance starts from the root and ends in tha concat of lables to node instanceNode + the label on the edge
 * that starts with ch and ends in edge_index
 */
public class Instance implements Comparable<Instance>{
    /**
     * The motif instance starts from the root and ends in node instanceNode + edge label until edge_index
     */
    private InstanceNode instanceNode;
    /**
     * outgoing edge from instanceNode
     */
    private Edge edge;
    /**
     * The instance ends at edge_index (concat of strings from root to node instanceNode + label[0:edge_index], edge_index
     * included) if edge_index = -1 than it ends in the node instanceNode
     */
    private int edge_index;
    //The error between motif and the Instance
    private int error;
    //number of deletions in the Instance (gap chars)
    private int deletions;

    private List<Integer> insertion_indexes;

    private String substring;
    private int length;

    /**
     * All the <em>different</em> instance_info that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     * key = genome id
     * value = a list of instance locations
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private Map<Integer, List<InstanceLocation>> instance_locations;


    public Instance(InstanceNode instanceNode, Edge e, int edge_index, int error, int deletions, List<Integer> insertion_indexes, String substring, int length){
        this.instanceNode = instanceNode;
        this.edge = e;
        this.edge_index = edge_index;
        this.error = error;
        this.deletions = deletions;
        this.substring = substring;
        this.length = length;
        this.insertion_indexes = new ArrayList<>();
        this.insertion_indexes.addAll(insertion_indexes);
    }

    public Instance(InstanceNode instanceNode, Edge e, int edge_index, int error, int deletions){
        this(instanceNode, e, edge_index, error, deletions, new ArrayList<>(), "", 0);
    }

    public Instance(int deletions, String substring){
        this(null, null, -2, -1, deletions);

        this.substring = substring;
    }

    public InstanceNode getNodeInstance(){
        return instanceNode;
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

    public void addInsertionIndex(int index){
        insertion_indexes.add(index);
    }

    public void addAllInsertionIndexes(List<Integer> indexes){
        insertion_indexes.addAll(indexes);
    }

    @Override
    public int compareTo(Instance o) {
        InstanceNode instance_node_other = o.getNodeInstance();
        if (o.getEdge() != null) {
            instance_node_other = (InstanceNode) o.getEdge().getDest();
        }

        InstanceNode instance_node_this = instanceNode;
        if (edge != null) {
            instance_node_this = (InstanceNode) edge.getDest();
        }

        return instance_node_other.getCount_by_keys() - instance_node_this.getCount_by_keys();
    }
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Instance)) return false;
        return ((Instance) other).getNodeInstance().getCount_by_keys() == instanceNode.getCount_by_keys();
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

    public List<Integer> getInsertionIndexes(){
        return insertion_indexes;
    }

    public Map<Integer, List<InstanceLocation>> getInstanceLocations() {
        InstanceNode instance_node = instanceNode;
        if (edge != null) {
            Edge temp_edge = edge;
            instance_node = (InstanceNode) temp_edge.getDest();
        }
        return instance_node.getResults();
    }
}
