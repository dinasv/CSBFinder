package model.suffixtreebased.suffixtrees;

import model.patterns.InstanceLocation;

import java.util.*;

/**
 */
public class InstanceNode extends SuffixNode {

    /**
     * Save for every genome in genomeToLocations, the position of the suffix of that string that ends at this node
     *
     * key = genome ID
     * value = a list of locations of this substring in this genome
     */
    private Map<Integer, List<InstanceLocation>> genomeToLocations;

    /**
     * The total number of <em>different</em> genomeToLocationsInSubtree that are stored in this
     * node and in underlying ones (i.e. nodes that can be reached through paths
     * starting from <tt>this</tt>.
     *
     * used only in suffix tree
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private int countInstancePerGenome = -1;
    private int countMultipleInstancesPerGenome = -1;
    /**
     * All the <em>different</em> locations that are stored in the subtree rooted in this node
     * (in all nodes that can be reached through paths starting from <tt>this</tt>.
     * i.e., locations of all substrings, that have a prefix identical to the current prefix
     *
     * key = genome id
     * value = a list of instance locations
     *
     * This must be calculated explicitly using computeAndCacheCount
     */
    private Map<Integer, List<InstanceLocation>> genomeToLocationsInSubtree;


    public InstanceNode(){
        genomeToLocationsInSubtree = new HashMap<>();
        genomeToLocations = new HashMap<>();
    }

    public InstanceNode(InstanceNode other){
        super(other);

        genomeToLocationsInSubtree = new HashMap<>();
        genomeToLocations = new HashMap<>();
    }

    /**
     * Add the location of the suffix (the starting index) in genome with the id {@code genomeId}
     */
    void addLocationToGenome(InstanceLocation instanceLocation) {

        List<InstanceLocation> locations = genomeToLocations.get(instanceLocation.getGenomeId());
        if (locations == null){
            locations = new ArrayList<>();
        }
        locations.add(instanceLocation);
        genomeToLocations.put(instanceLocation.getGenomeId(), locations);

        // addGene this reference to all the suffixes as well
        addIndexToSuffix(this, instanceLocation.getGenomeId(), instanceLocation);

    }

    private void addIndexToSuffix(InstanceNode node, int genomeId, InstanceLocation instanceLocation){
        InstanceNode iter = node.getSuffix();
        if (iter != null) {
            while (iter.getSuffix() != null &&
                    instanceLocation.getRelativeStartIndex() < instanceLocation.getGenomicLength()-1) {

                instanceLocation = new InstanceLocation(instanceLocation);
                instanceLocation.incrementRelativeStartIndex();

                List<InstanceLocation> instanceLocations = iter.genomeToLocations
                                                                .computeIfAbsent(genomeId, k -> new ArrayList<>());
                instanceLocations.add(instanceLocation);

                iter = iter.getSuffix();
            }
        }
    }

    /**
     * Computes the genomeToLocationsInSubtree and the number of genomeToLocationsInSubtree
     * that are stored on this node and on its children, and caches the result. Equal to getData, only with caching
     *
     * Performs the same operation on subnodes as well
     * @return the number of genomeToLocationsInSubtree
     */
    protected int computeAndCacheCount() {
        if (countInstancePerGenome == -1) {
            computeAndCacheCountRecursive();
        }
        return countInstancePerGenome;
    }

    private Map<Integer, List<InstanceLocation>> computeAndCacheCountRecursive() {
        countInstancePerGenome = 0;
        countMultipleInstancesPerGenome = 0;
        //add all data_indexes to genomeToLocationsInSubtree
        countMultipleInstancesPerGenome += multimapAddAll(genomeToLocationsInSubtree, genomeToLocations);

        Map<Integer, Edge> edges = getEdges();
        for (Map.Entry<Integer, Edge> entry : edges.entrySet()) {
            Edge e = entry.getValue();
            InstanceNode destNode = e.getDest();
            Map<Integer, List<InstanceLocation>> dest_data_indexes = destNode.computeAndCacheCountRecursive();

            countMultipleInstancesPerGenome += multimapAddAll(genomeToLocationsInSubtree, dest_data_indexes);

        }
        countInstancePerGenome = genomeToLocationsInSubtree.size();
        return genomeToLocationsInSubtree;
    }

    private static int multimapAddAll(Map<Integer, List<InstanceLocation>>  multimapTo, Map<Integer, List<InstanceLocation>>  multimap_from ){
        int indexes_counter = 0;
        for (Map.Entry<Integer, List<InstanceLocation>> entry : multimap_from.entrySet()) {
            int key = entry.getKey();

            List<InstanceLocation> indexSet = multimapTo.computeIfAbsent(key, k -> new ArrayList<>());

            List<InstanceLocation> indexSetToAdd = entry.getValue();

            indexSet.addAll(indexSetToAdd);
            indexes_counter += indexSetToAdd.size();
        }
        return indexes_counter;
    }

    /**
     * Returns the number of genomes that are stored in this subtree
     * Should be called after having called computeAndCacheCount.
     *
     * @throws IllegalStateException when this method is called without having called
     * computeAndCacheCount first
     * wasn't updated
     */
    public int getCountInstancePerGenome() throws IllegalStateException {
        if (-1 == countInstancePerGenome) {
            throw new IllegalStateException("getCountInstancePerGenome() shouldn't be called without calling " +
                    "computeAndCacheCount() first");
        }

        return countInstancePerGenome;
    }

    public int getCountMultipleInstancesPerGenome() throws IllegalStateException {
        if (-1 == countMultipleInstancesPerGenome) {
            throw new IllegalStateException("getCountInstancePerGenome() shouldn't be called without calling " +
                    "computeAndCacheCount() first");
        }

        return countMultipleInstancesPerGenome;
    }
    public Map<Integer, List<InstanceLocation>> getGenomeToLocationsInSubtree(){
        if (-1 == countInstancePerGenome) {
            throw new IllegalStateException("getGenomeToLocationsInSubtree() shouldn't be called without calling " +
                    "computeAndCacheCount() first");
        }

        return genomeToLocationsInSubtree;
    }

    public InstanceNode getSuffix() {
        return (InstanceNode)super.getSuffix();
    }

}

