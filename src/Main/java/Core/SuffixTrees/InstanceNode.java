package Core.SuffixTrees;

import Core.Genomes.InstanceLocation;
import Core.Genomes.Strand;

import java.util.*;

/**
 * Created by Dina on 7/20/2016.
 */
public class InstanceNode extends SuffixNode {

    /**
     * Save for every string key in data, the position of the suffix of that string that ends at this node
     * sequence_id : {(replicon_id|start_index|numericValue)}
     */
    private Map<Integer, List<InstanceLocation>> data;

    /**
     * The total number of <em>different</em> results that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     *
     * used only in suffix tree
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private int countByKeys = -1;
    private int countByIndexes = -1;
    /**
     * All the <em>different</em> results that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     * key = genome id
     * value = a list of instance locations
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private Map<Integer, List<InstanceLocation>> results;


    public InstanceNode(){
        results = new HashMap<Integer, List<InstanceLocation>>();
        data = new HashMap<Integer, List<InstanceLocation>>();
    }

    public InstanceNode(InstanceNode other){
        super(other);

        results = new HashMap<Integer, List<InstanceLocation>>();
        data = new HashMap<Integer, List<InstanceLocation>>();
    }

    /**
     * Add position of the suffix (the starting index) of the string "key"
     */
    void addDataIndex(int key, InstanceLocation instanceLocation) {

        List<InstanceLocation> key_indexes = data.get(key);
        if (key_indexes == null){
            key_indexes = new ArrayList<InstanceLocation>();
        }
        key_indexes.add(instanceLocation);
        data.put(key, key_indexes);

        // add this reference to all the suffixes as well
        addIndexToSuffix(this, key, instanceLocation);

    }

    private void addIndexToSuffix(InstanceNode node, int key, InstanceLocation instanceLocation){
        InstanceNode iter = node.getSuffix();
        if (iter != null) {
            while (iter.getSuffix() != null) {
                Strand strand = instanceLocation.getStrand();
                int startIndex = instanceLocation.getStartIndex();
                int endIndex = instanceLocation.getEndIndex();

                int repliconId = instanceLocation.getRepliconId();
                instanceLocation = new InstanceLocation(repliconId, startIndex + strand.numericValue, endIndex,
                                        strand);


                List<InstanceLocation> key_indexes = iter.data.get(key);
                if (key_indexes == null){
                    key_indexes = new ArrayList<InstanceLocation>();
                    iter.data.put(key, key_indexes);
                }
                key_indexes.add(instanceLocation);

                iter = iter.getSuffix();
            }
        }
    }

    /**
     * Computes the results and the number of results that are stored on this node and on its
     * children, and caches the result. Equal to getData, only with caching
     *
     * Performs the same operation on subnodes as well
     * @return the number of results
     */
    protected int computeAndCacheCount() {
        if (countByKeys == -1) {
            computeAndCacheCountRecursive();
        }
        return countByKeys;
    }

    private Map<Integer, List<InstanceLocation>> computeAndCacheCountRecursive() {
        countByKeys = 0;
        countByIndexes = 0;
        //add all data_indexes to results
        countByIndexes += multimapAddAll(results, data);

        Map<Integer, Edge> edges = getEdges();
        for (Map.Entry<Integer, Edge> entry : edges.entrySet()) {
            Edge e = entry.getValue();
            InstanceNode destNode = (InstanceNode)e.getDest();
            Map<Integer, List<InstanceLocation>> dest_data_indexes = destNode.computeAndCacheCountRecursive();

            countByIndexes += multimapAddAll(results, dest_data_indexes);

        }
        countByKeys = results.size();
        return results;
    }

    private static int multimapAddAll(Map<Integer, List<InstanceLocation>>  multimap_to, Map<Integer, List<InstanceLocation>>  multimap_from ){
        int indexes_counter = 0;
        for (Map.Entry<Integer, List<InstanceLocation>> entry : multimap_from.entrySet()) {
            int key = entry.getKey();

            List<InstanceLocation> indexSet =  multimap_to.get(key);
            if (indexSet == null) {
                indexSet = new ArrayList<InstanceLocation>();
                multimap_to.put(key, indexSet);
            }

            List indexSetToAdd = entry.getValue();

            indexSet.addAll(indexSetToAdd);
            indexes_counter += indexSetToAdd.size();
        }
        return indexes_counter;
    }

    /**
     * Returns the number of results that are stored on this node and on its
     * children.
     * Should be called after having called computeAndCacheCount.
     *
     * @throws IllegalStateException when this method is called without having called
     * computeAndCacheCount first
     * wasn't updated
     */
    public int getCountByKeys() throws IllegalStateException {
        if (-1 == countByKeys) {
            throw new IllegalStateException("getCountByKeys() shouldn't be called without calling computeAndCacheCount() first");
        }

        return countByKeys;
    }

    public int getCountByIndexes() throws IllegalStateException {
        if (-1 == countByKeys) {
            throw new IllegalStateException("getCountByKeys() shouldn't be called without calling computeAndCacheCount() first");
        }

        return countByIndexes;
    }
    public Map<Integer, List<InstanceLocation>> getResults(){
        if (-1 == countByKeys) {
            throw new IllegalStateException("getResults() shouldn't be called without calling computeCount() first");
        }

        return results;
    }

    public InstanceNode getSuffix() {
        return (InstanceNode)super.getSuffix();
    }

}

