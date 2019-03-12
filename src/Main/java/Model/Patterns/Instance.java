package Model.Patterns;

import Model.SuffixTreePatternFinder.SuffixTrees.InstanceNode;
import Model.SuffixTreePatternFinder.SuffixTrees.Edge;

import java.util.List;
import java.util.Map;

/**
 * The instance starts from the root and ends in tha concat of lables to node instanceNode + the label on the edge
 * that starts with ch and ends in edgeIndex
 */
public class Instance implements Comparable<Instance>{
    /**
     * The pattern instance starts from the root and ends in node instanceNode + edge label until edgeIndex
     */
    private InstanceNode instanceNode;
    /**
     * outgoing edge from instanceNode
     */
    private Edge edge;
    /**
     * The instance ends at edgeIndex (concat of strings from root to node instanceNode + label[0:edgeIndex], edgeIndex
     * included) if edgeIndex = -1 than it ends in the node instanceNode
     */
    private int edgeIndex;
    //The error between Pattern and the Instance
    private int error;
    //number of deletions in the Instance (gap chars)
    private int deletions;

    private String substring;
    private int length;

    private int insertions;

    /**
     * An index to make sure that the current instance is minimal.
     * i.e., there is no other instance, which is a substring of the current instance.
     * e.g. Pattern P=B, Instance BB. BB is not a minimal instance, because B is a substring of BB,
     * and it is also an instance of P. When encountering the second letter of BB, (@code minimalInstanceIndex)
     * should be 1. Hence, |P|==(@code minimalInstanceIndex), and the coressponding suffix node should not be traversed.
     */
    private int minimalInstanceIndex;

    public Instance(InstanceNode instanceNode, Edge e, int edgeIndex, int error, int deletions, int insertions,
                    String substring, int length, int minimalInstanceIndex){
        this.instanceNode = instanceNode;
        this.edge = e;
        this.edgeIndex = edgeIndex;
        this.error = error;
        this.deletions = deletions;
        this.substring = substring;
        this.length = length;
        this.insertions = insertions;
        this.minimalInstanceIndex = minimalInstanceIndex;
    }

    public Instance(InstanceNode instanceNode){
        this(instanceNode, null, -1, 0, 0, 0, "", 0, 0);
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
        return edgeIndex;
    }

    public void setSubstring(String substring){
        this.substring = substring;
    }

    public String getSubstring(){
        return substring;
    }

    @Override
    public int compareTo(Instance o) {
        InstanceNode instanceNodeOther = o.getNodeInstance();
        if (o.getEdge() != null) {
            instanceNodeOther = (InstanceNode) o.getEdge().getDest();
        }

        InstanceNode instanceNodeThis = instanceNode;
        if (edge != null) {
            instanceNodeThis = (InstanceNode) edge.getDest();
        }

        return instanceNodeOther.getCountInstancePerGenome() - instanceNodeThis.getCountInstancePerGenome();
    }

    public int getInsertions() {
        return insertions;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Map<Integer, List<InstanceLocation>> getInstanceLocations() {
        InstanceNode instanceNode = this.instanceNode;
        if (edge != null) {
            Edge edge = this.edge;
            instanceNode = (InstanceNode) edge.getDest();
        }
        return instanceNode.getGenomeToLocationsInSubtree();
    }

    public int getMinimalInstanceIndex() {
        return minimalInstanceIndex;
    }
}
