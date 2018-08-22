package SuffixTrees;

import Utils.InstanceLocation;

import java.util.*;

/**
 * Created by Dina on 7/20/2016.
 */
public class InstanceNode extends SuffixNode {

    /**
     * Save for every string key in data, the position of the suffix of that string that ends at this node
     * sequence_id : {(replicon_id|start_index|strand)}
     */
    private HashMap<Integer, List<InstanceLocation>> data;

    /**
     * The total number of <em>different</em> results that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     *
     * used only in suffix tree
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private int count_by_keys = -1;
    private int count_by_indexes = -1;
    /**
     * All the <em>different</em> results that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     * key = genome id
     * value = a list of instance locations
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private HashMap<Integer, List<InstanceLocation>> results;


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
    void addDataIndex(int key, InstanceLocation instance_info) {

        List<InstanceLocation> key_indexes = data.get(key);
        if (key_indexes == null){
            key_indexes = new ArrayList<InstanceLocation>();
        }
        key_indexes.add(instance_info);
        data.put(key, key_indexes);

        // add this reference to all the suffixes as well
        addIndexToSuffix(this, key, instance_info);

    }

    private void addIndexToSuffix(InstanceNode node, int key, InstanceLocation instance_info){
        InstanceNode iter = node.getSuffix();
        if (iter != null) {
            while (iter.getSuffix() != null) {
                int strand = instance_info.getStrand();
                int start_index = instance_info.getStartIndex();
                int replicon_id = instance_info.getRepliconId();
                instance_info = new InstanceLocation(replicon_id, start_index + strand,   strand);

                List<InstanceLocation> key_indexes = iter.data.get(key);
                if (key_indexes == null){
                    key_indexes = new ArrayList<InstanceLocation>();
                    iter.data.put(key, key_indexes);
                }
                key_indexes.add(instance_info);

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
        if (count_by_keys == -1) {
            computeAndCacheCountRecursive();
        }
        return count_by_keys;
    }

    private HashMap<Integer, List<InstanceLocation>> computeAndCacheCountRecursive() {
        count_by_keys = 0;
        count_by_indexes = 0;
        //add all data_indexes to results
        count_by_indexes += multimapAddAll(results, data);

        HashMap<Integer, Edge> edges = getEdges();
        for (Map.Entry<Integer, Edge> entry : edges.entrySet()) {
            Edge e = entry.getValue();
            InstanceNode destNode = (InstanceNode)e.getDest();
            HashMap<Integer, List<InstanceLocation>> dest_data_indexes = destNode.computeAndCacheCountRecursive();

            count_by_indexes += multimapAddAll(results, dest_data_indexes);

        }
        count_by_keys = results.size();
        return results;
    }

    private static int multimapAddAll(HashMap<Integer, List<InstanceLocation>>  multimap_to, HashMap<Integer, List<InstanceLocation>>  multimap_from ){
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
    public int getCount_by_keys() throws IllegalStateException {
        if (-1 == count_by_keys) {
            throw new IllegalStateException("getCount_by_keys() shouldn't be called without calling computeAndCacheCount() first");
        }

        return count_by_keys;
    }

    public int getCount_by_indexes() throws IllegalStateException {
        if (-1 == count_by_keys) {
            throw new IllegalStateException("getCount_by_keys() shouldn't be called without calling computeAndCacheCount() first");
        }

        return count_by_indexes;
    }
    public HashMap<Integer, List<InstanceLocation>> getResults(){
        if (-1 == count_by_keys) {
            throw new IllegalStateException("getResults() shouldn't be called without calling computeCount() first");
        }

        return results;
    }

    public InstanceNode getSuffix() {
        return (InstanceNode)super.getSuffix();
    }

}

