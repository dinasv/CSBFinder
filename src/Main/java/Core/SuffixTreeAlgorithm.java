package Core;

import Core.Genomes.WordArray;
import Core.SuffixTrees.*;

import java.util.*;

import Core.Genomes.*;

/**
 * Suffix Tree based algorithmType for CSB pattern discovery
 *
 * A CSB is a substring of at least quorum1 input sequences and must have instance in at least quorum2 input sequences
 * An instance can differ from a CSB by at most k insertions
 */
public class SuffixTreeAlgorithm implements Algorithm{

    public static long countNodesInPatternTree;
    public static long countNodesInDataTree;

    private int maxError;
    private int maxWildcards;
    private int maxDeletion;
    private int maxInsertion;
    private int q1;
    private int q2;
    private int minPatternLength;
    private int maxPatternLength;

    private Parameters parameters;

    private DatasetTree datasetTree;

    //contains all extracted patterns
    private Map<String, Pattern> patterns;

    private boolean multCount;

    private int lastPatternKey;

    private boolean nonDirectons;

    private boolean debug;

    private int totalCharsInData;
    private GenomesInfo gi;

    private PatternNode patternTreeRoot;

    private List<Pattern> patternsFromFile;

    /**
     *
     * @param params args
     * @param datasetTree GST representing all input sequences
     * @param patternTrie
     * @param gi Information regarding the input genomes (sequences)
     */
    public SuffixTreeAlgorithm(){

        parameters = null;
        this.gi = null;

        patterns = new HashMap<>();
        patternsFromFile = new ArrayList<>();

    }

    public void setParameters(Parameters params){

        this.parameters = params;

        // args
        this.maxError = params.maxError;
        this.maxWildcards = params.maxWildcards;
        this.maxDeletion = params.maxDeletion;
        this.maxInsertion = params.maxInsertion;
        q1 = params.quorum1;
        q2 = params.quorum2;
        this.nonDirectons = params.nonDirectons;
        this.minPatternLength = params.minPatternLength;
        this.maxPatternLength = params.maxPatternLength;
        this.multCount = params.multCount;
        this.debug = params.debug;

    }

    public void setGenomesInfo(GenomesInfo gi){
        this.gi = gi;

        datasetTree = new DatasetTree(gi);
    }

    public void setPatternsFromFile(List<Pattern> patternsFromFile){
        this.patternsFromFile = patternsFromFile;
    }


    public Parameters getParameters(){
        return parameters;
    }

    public int getPatternsCount(){
        return patterns.size();
    }

    public void initialize(){
        totalCharsInData = -1;
        lastPatternKey = 0;
        countNodesInPatternTree = 0;
        countNodesInDataTree = 0;

        patterns = new HashMap<>();

        setPatternTreeRoot();
    }

    private void setPatternTreeRoot(){
        if (patternsFromFile.size() > 0) {
            PatternsTree patternsTree = new PatternsTree(patternsFromFile, gi);
            Trie patternTrie = patternsTree.getTrie();
            patternTreeRoot = patternTrie.getRoot();
        }else{//all patterns will be extracted from the data tree
            patternTreeRoot = new PatternNode(TreeType.VIRTUAL);
            patternTreeRoot.setKey(++lastPatternKey);
        }
    }


    /**
     * Calls the recursive function spellPatterns
     * @param patternNode a node in the pattern tree, the pattern tree traversal begins from this node
     */
    public void findPatterns() {
        if (parameters == null || gi == null || datasetTree == null){
            return;
        }

        initialize();

        datasetTree.buildTree(parameters.nonDirectons);
        GeneralizedSuffixTree datasetSuffixTree = datasetTree.getSuffixTree();
        datasetSuffixTree.computeCount();
        totalCharsInData = ((InstanceNode) datasetSuffixTree.getRoot()).getCountMultipleInstancesPerGenome();

        InstanceNode dataTreeRoot = (InstanceNode) datasetSuffixTree.getRoot();
        //the instance of an empty string is the root of the data tree
        Instance empty_instance = new Instance(dataTreeRoot, null, -1, 0, 0);
        countNodesInDataTree++;

        patternTreeRoot.addInstance(empty_instance, maxInsertion);
        if (patternTreeRoot.getType()== TreeType.VIRTUAL){
            spellPatternsVirtually(patternTreeRoot, dataTreeRoot, -1, null, new ArrayList<>(),
                    0, 0);
        }else{
            spellPatterns(patternTreeRoot, new ArrayList<>(), 0, 0);
        }

        removeRedundantPatterns();
    }


    /**
     * works as the regular substring function
     *
     * @param seq
     * @param start_index
     * @param end_index
     * @return
     */
    private WordArray getSubstring(WordArray seq, int start_index, int end_index) {
        return new WordArray(seq.getWordArray(), seq.getStartIndex() + start_index,
                seq.getStartIndex() + end_index);
    }


    /**
     * Remove patterns that are suffixes of existing patterns, and has the same number of instances
     * If a pattern passes the quorum1, all its sub-patterns also pass the quorum1
     * If a (sub-pattern instance count = pattern instance count) : the sub-pattern is always a part of the larger
     * pattern
     * Therefore it is sufficient to remove each pattern suffix if it has the same instance count
     */
    private void removeRedundantPatterns() {
        if(patternTreeRoot.getType() == TreeType.VIRTUAL) {
            HashSet<String> patternsToRemove = new HashSet<>();
            for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {

                Pattern pattern = entry.getValue();
                List<Gene> patternGenes = pattern.getPatternGenes();
                List<Gene> patternSuffix = new ArrayList<>(patternGenes);
                patternSuffix.remove(0);
                String suffixStr = patternSuffix.toString();

                Pattern suffix = patterns.get(suffixStr);

                if (suffix != null) {
                    int patternCount = pattern.getInstancesPerGenome();
                    int suffixCount = suffix.getInstancesPerGenome();
                    if (suffixCount == patternCount) {
                        patternsToRemove.add(suffixStr);
                    }
                }

                if (nonDirectons) {
                    removeReverseCompliments(pattern, patternsToRemove);
                }
            }
            patterns.keySet().removeAll(patternsToRemove);
        }
    }

    private void removeReverseCompliments(Pattern pattern, HashSet<String> patternsToRemove){

        List<Gene> reversePatternGenes = pattern.getReverseComplimentPattern();
        //String[] genes = genesToStrArr(reversePatternGenes);
        String reversedPatternStr = reversePatternGenes.toString();
        Pattern reversedPattern = patterns.get(reversedPatternStr);

        String patternStr = pattern.getPatternGenes().toString();
        if (reversedPattern != null && !patternsToRemove.contains(patternStr)){
            patternsToRemove.add(reversedPatternStr);
        }
    }


    /**
     * Add to node an edge with label = gap. edge.dest = copy of node, its edged are deep copied
     *
     * @param node
     */
    private void addWildcardEdge(PatternNode node, Boolean copy_node) {
        PatternNode targetNode = node.getTargetNode(Alphabet.WC_CHAR_INDEX);
        if (targetNode == null) {
            int[] wildcard = {Alphabet.WC_CHAR_INDEX};
            //create a copy of node
            PatternNode newnode = new PatternNode(node);
            node.addTargetNode(Alphabet.WC_CHAR_INDEX, newnode);
        } else {
            PatternNode newnode = node.getTargetNode(Alphabet.WC_CHAR_INDEX);
            newnode = new PatternNode(newnode);
            targetNode.addTargetNode(Alphabet.WC_CHAR_INDEX, newnode);
        }
    }

    private Boolean starts_with_wildcard(List<Gene> pattern) {
        if (pattern.size() > 0) {
            if (pattern.get(0).getCogId().equals('*')) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a character to str
     * @param str
     * @param ch the index of the character to add, converted to a letter
     * @return the extended str
     */
    private List<Gene> appendChar(List<Gene> str, int ch) {
        Gene cog = gi.getLetter(ch);
        List<Gene> extended_string = new ArrayList<>();
        extended_string.addAll(str);
        extended_string.add(cog);

        return extended_string;
    }


    /**
     * Recursive function that traverses over the subtree rooted at pattern_node, which is a node of a suffix tree.
     * This operation 'spells' all possible strings with infix 'pattern', that have enough instances (q1 exact instances
     * and q2 approximate instances)
     *
     * @param pattern_node node in the enumeration tree that represents the current pattern
     * @param pattern     represents a string concatenation of edge labels from root to pattern_node
     * @param pattern_length
     * @param pattern_wildcard_count  number of wildcards in the pattern
     * @return The maximal number of different string indexes that one of the extended patterns by a char appear in
     */
    private int spellPatterns(PatternNode pattern_node, List<Gene> pattern, int pattern_length, int pattern_wildcard_count) {
        if (pattern_wildcard_count < maxWildcards && pattern_node.getType().equals("enumeration")) {
            //add to pattern_node an edge with "_", pointing to a new node that will save the instances
            addWildcardEdge(pattern_node, true);
        }

        List<Instance> instances = pattern_node.getInstances();

        Map<Integer, PatternNode> target_nodes = pattern_node.getTargetNodes();

        //the maximal number of different instances, of one of the extended patterns
        int max_num_of_diff_instances = -1;
        int num_of_diff_instance = 0;

        PatternNode target_node;
        for (Map.Entry<Integer, PatternNode> entry : target_nodes.entrySet()) {
            int alpha = entry.getKey();
            Gene alpha_ch = gi.getLetter(alpha);
            target_node = entry.getValue();

            //go over edges that are not wild cards
            if (alpha != Alphabet.WC_CHAR_INDEX) {
                num_of_diff_instance = extendPattern(alpha, -1, null, null,
                                    pattern_wildcard_count, pattern, target_node, pattern_node, instances, pattern_length);

                if (num_of_diff_instance > max_num_of_diff_instances) {
                    max_num_of_diff_instances = num_of_diff_instance;
                }
                //For memory saving, remove pointer to target node
                pattern_node.addTargetNode(alpha, null);
            }
        }

        //handle wild card edge
        if (pattern_node.getType().equals("pattern") || pattern_wildcard_count < maxWildcards) {
            target_node = pattern_node.getTargetNode(Alphabet.WC_CHAR_INDEX);
            if (target_node != null) {
                num_of_diff_instance = extendPattern(Alphabet.WC_CHAR_INDEX, -1, null, null,
                            pattern_wildcard_count + 1, pattern, target_node, pattern_node, instances, pattern_length);
                if (num_of_diff_instance > max_num_of_diff_instances) {
                    max_num_of_diff_instances = num_of_diff_instance;
                }
            }
        }
        countNodesInPatternTree++;

        return max_num_of_diff_instances;
    }


    /**
     * * Recursive function that traverses over the subtree rooted at patternNode, which is a node of a suffix tree.
     * This operation 'spells' all possible strings with infix 'pattern', that have enough instances (q1 exact instances
     * and q2 approximate instances)
     * It is same as spellPatterns, only that the suffix tree of patterns is not saved in memory, it is created virtually
     * from data suffix tree.
     *
     * @param patternNode
     * @param dataNode
     * @param dataEdgeIndex
     * @param dataEdge
     * @param pattern
     * @param patternLength
     * @param wildcardCount
     * @return
     */
    private int spellPatternsVirtually(PatternNode patternNode, InstanceNode dataNode, int dataEdgeIndex,
                                       Edge dataEdge,
                                       List<Gene> pattern, int patternLength, int wildcardCount) {

        List<Instance> instances = patternNode.getInstances();
        //the maximal number of different instances, of one of the extended patterns
        int maxNumOfDiffInstances = -1;
        int numOfDiffInstances = 0;

        Map<Integer, Edge> dataNodeEdges = null;

        WordArray dataEdgeLabel;
        if (dataEdge != null) {
            dataEdgeLabel = dataEdge.getLabel();
            if (dataEdgeIndex >= dataEdgeLabel.getLength()) {//we reached to the end of the edge
                dataNode = (InstanceNode) dataEdge.getDest();
                dataEdgeIndex = -1;
                dataEdge = null;
            }
        }

        PatternNode targetNode;

        if (dataEdgeIndex == -1){
            dataEdgeIndex ++;
            dataNodeEdges = dataNode.getEdges();

            for (Map.Entry<Integer, Edge> entry : dataNodeEdges.entrySet()) {
                int alpha = entry.getKey();
                Gene alpha_ch = gi.getLetter(alpha);
                dataEdge = entry.getValue();
                InstanceNode data_tree_target_node = (InstanceNode) dataEdge.getDest();

                if (data_tree_target_node.getCountInstancePerGenome() >= q1) {

                    if (alpha == Alphabet.UNK_CHAR_INDEX) {
                        if (q1 == 0 && gi.getLetter(pattern.get(0)) != Alphabet.UNK_CHAR_INDEX) {
                            spellPatternsVirtually(patternNode, dataNode, dataEdgeIndex + 1, dataEdge,
                            pattern, patternLength, wildcardCount);
                        }
                    } else {

                        targetNode = new PatternNode(TreeType.VIRTUAL);
                        targetNode.setKey(++lastPatternKey);

                        numOfDiffInstances = extendPattern(alpha, dataEdgeIndex + 1, dataNode, dataEdge,
                                wildcardCount, pattern, targetNode, patternNode, instances, patternLength);

                        if (numOfDiffInstances > maxNumOfDiffInstances) {
                            maxNumOfDiffInstances = numOfDiffInstances;
                        }
                    }
                }
            }
        }else{//dataEdgeIndex>=1 && dataEdgeIndex < dataEdgeLabel.getLength()
            dataEdgeLabel = dataEdge.getLabel();
            int alpha = dataEdgeLabel.getLetter(dataEdgeIndex);

            InstanceNode data_tree_target_node = (InstanceNode) dataEdge.getDest();

            if (data_tree_target_node.getCountInstancePerGenome() >= q1) {
                if (alpha != Alphabet.UNK_CHAR_INDEX) {

                    targetNode = new PatternNode(TreeType.VIRTUAL);
                    targetNode.setKey(++lastPatternKey);

                    numOfDiffInstances = extendPattern(alpha, dataEdgeIndex + 1, dataNode, dataEdge,
                            wildcardCount, pattern, targetNode, patternNode, instances, patternLength);

                    if (numOfDiffInstances > maxNumOfDiffInstances) {
                        maxNumOfDiffInstances = numOfDiffInstances;
                    }
                }
            }
        }

        countNodesInPatternTree++;

        return maxNumOfDiffInstances;
    }

    private void handlePattern(Pattern new_pattern, List<Gene> extended_pattern){
        patterns.put(extended_pattern.toString(), new_pattern);
    }


    /**
     * Extend pattern recursively by one character, if it passes the q1 and q2 - add to pattern list
     *
     * @param alpha                the char to append
     * @param wildcard_count how many wildcard in the pattern
     * @param pattern                previous pattern string, before adding alpha. i.e. COG1234|COG2000|
     * @param targetNode          node the extended pattern
     * @param pattern_node           node of pattern
     * @param Instances            the instances of pattern
     * @param pattern_length
     * @return num of different instances of extended pattern
     */

    private int extendPattern(int alpha, int data_edge_index, InstanceNode data_node, Edge data_edge,
                              int wildcard_count, List<Gene> pattern, PatternNode targetNode,
                              PatternNode pattern_node, List<Instance> Instances, int pattern_length) {

        List<Gene> extendedPattern = appendChar(pattern, alpha);
        PatternNode extendedPatternNode = targetNode;
        int extended_pattern_length = pattern_length + 1;

        //if there is a wildcard in the current pattern, have to create a copy of the subtree
        if (wildcard_count > 0 && alpha != Alphabet.WC_CHAR_INDEX) {
            extendedPatternNode = new PatternNode(extendedPatternNode);
            pattern_node.addTargetNode(alpha, extendedPatternNode);
        }

        extendedPatternNode.setSubstring(extendedPattern.toString());
        extendedPatternNode.setSubstringLength(extended_pattern_length);

        int exactInstancesCount = 0;
        //go over all instances of the pattern
        for (Instance instance : Instances) {
            int currExactInstanceCount = extendInstance(extendedPatternNode, instance, alpha);
            if (currExactInstanceCount > 0){
                exactInstancesCount = currExactInstanceCount;
            }
        }
        extendedPatternNode.setExactInstanceCount(exactInstancesCount);

        int instancesCount;
        if (multCount){
            instancesCount = extendedPatternNode.getInstanceIndexCount();
        }else {
            instancesCount = extendedPatternNode.getInstanceKeysSize();
        }

        if (exactInstancesCount >= q1 && instancesCount >= q2 &&
                (extended_pattern_length - wildcard_count <= maxPatternLength)) {

            TreeType type = extendedPatternNode.getType();
            int ret;
            if (type == TreeType.VIRTUAL){
                ret = spellPatternsVirtually(extendedPatternNode, data_node, data_edge_index, data_edge,
                        extendedPattern, extended_pattern_length, wildcard_count);
            }else {
                ret = spellPatterns(extendedPatternNode, extendedPattern, extended_pattern_length, wildcard_count);
            }

            if (extended_pattern_length - wildcard_count >= minPatternLength) {
                if (type == TreeType.STATIC) {
                    if (extendedPatternNode.getPatternKey()>0) {
                        Pattern newPattern = new Pattern(extendedPatternNode.getPatternKey(),
                                extendedPattern
                                //instancesCount,
                                //extendedPatternNode.getExactInstanceCount()
                                 );
                        newPattern.addInstanceLocations(extendedPatternNode.getInstances());

                        handlePattern(newPattern, extendedPattern);

                    }
                } else if (type == TreeType.VIRTUAL) {
                    if (alpha != Alphabet.WC_CHAR_INDEX) {
                        if (!(starts_with_wildcard(extendedPattern))) {
                            //make sure that extendedPattern is right maximal, if extendedPattern has the same number of
                            // instances as the longer pattern, prefer the longer pattern
                            if (instancesCount > ret || debug) {// instancesCount >= ret always
                                Pattern newPattern = new Pattern(extendedPatternNode.getPatternKey(),
                                        extendedPattern);
                                        //extendedPatternNode.getInstanceKeys().size(),
                                        //extendedPatternNode.getExactInstanceCount());
                                newPattern.addInstanceLocations(extendedPatternNode.getInstances());

                                handlePattern(newPattern, extendedPattern);

                                if (debug && (getPatternsCount() % 5000 == 0) ){
                                    MemoryUtils.measure();
                                }
                            }
                        }
                    } else {
                        if (ret <= 0) {
                            instancesCount = -1;
                        } else {
                            instancesCount = ret;
                        }
                    }
                }
            }
        }
        return instancesCount;
    }

    /**
     * Extends instance, increments error depending on ch
     *
     * @param extended_pattern extended pattern node
     * @param instance the current instance
     * @param ch  the character of the pattern, need to check if the next char on the instance is equal
     * @return list of all possible extended instances
     */
    private int extendInstance(PatternNode extended_pattern, Instance instance, int ch) {
        //values of current instance
        InstanceNode nodeInstance = instance.getNodeInstance();
        Edge instanceEdge = instance.getEdge();
        int edgeIndex = instance.getEdgeIndex();
        int error = instance.getError();
        int deletions = instance.getDeletions();
        int insertions = instance.getInsertions();

        //values of the extended instance
        int nextEdgeIndex = edgeIndex;
        Edge next_edge_instance = instanceEdge;
        InstanceNode next_node_instance = nodeInstance;

        int exactInstanceCount = 0;

        //The substring ends at the current nodeInstance, edgeIndex = -1
        if (instanceEdge == null) {
            //Go over all the edges from nodeInstance, see if the instance can be extended
            Map<Integer, Edge> instance_edges = nodeInstance.getEdges();

            //we can extend the instance using all outgoing edges, increment error if needed
            if (ch == Alphabet.WC_CHAR_INDEX) {
                exactInstanceCount = addAllInstanceEdges(false, instance, instance_edges, deletions, error, nodeInstance,
                        edgeIndex, ch, extended_pattern);
                //extend instance by deletions char
                if (deletions < maxDeletion) {
                    addInstanceToPattern(extended_pattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge, edgeIndex,
                            error, deletions + 1);
                }
            } else {
                if (insertions < maxInsertion && instance.getLength() > 0){
                    addAllInstanceEdges(true, instance, instance_edges, deletions, error, nodeInstance,
                            edgeIndex, ch, extended_pattern);
                }
                if (error < maxError) {
                    //go over all outgoing edges
                    exactInstanceCount = addAllInstanceEdges(false, instance, instance_edges, deletions,
                            error, nodeInstance, edgeIndex, ch, extended_pattern);
                    //extend instance by deletions char
                    if (deletions < maxDeletion) {
                        addInstanceToPattern(extended_pattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge, edgeIndex,
                                error, deletions + 1);
                    }
                } else {//error = max error, only instanceEdge starting with ch can be added, or deletions
                    nextEdgeIndex++;
                    next_edge_instance = nodeInstance.getEdge(ch);
                    next_node_instance = nodeInstance;
                    //Exists an instanceEdge starting with ch, add it to instances
                    if (next_edge_instance != null) {
                        exactInstanceCount = ((InstanceNode)next_edge_instance.getDest()).getCountInstancePerGenome();
                        //The label contains only 1 char, go to next nodeInstance
                        if (next_edge_instance.getLabel().getLength() == 1) {
                            next_node_instance = (InstanceNode) next_edge_instance.getDest();
                            next_edge_instance = null;
                            nextEdgeIndex = -1;
                        }
                        addInstanceToPattern(extended_pattern, instance, ch, next_node_instance, next_edge_instance,
                                nextEdgeIndex, error, deletions);
                    } else {
                        //extend instance by deletions char
                        if (deletions < maxDeletion) {
                            addInstanceToPattern(extended_pattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge,
                                    edgeIndex, error, deletions + 1);
                        }
                    }
                }
            }
        } else {//Edge is not null, the substring ends at the middle of the instanceEdge, at index edgeIndex
            WordArray label = instanceEdge.getLabel();
            //check the next char on the label, at edgeIndex+1
            nextEdgeIndex++;
            int next_ch = label.getLetter(nextEdgeIndex);

            //If we reached the end of the label by incrementing edgeIndex, get next nodeInstance
            if (nextEdgeIndex == label.getLength() - 1) {
                next_node_instance = (InstanceNode) instanceEdge.getDest();
                next_edge_instance = null;
                nextEdgeIndex = -1;
            }

            if (insertions < maxInsertion && instance.getLength() > 0){
                if (next_ch != ch) {
                    //String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
                    String extended_instance_string = "";
                    Instance next_instance = new Instance(next_node_instance, next_edge_instance, nextEdgeIndex,
                            error, deletions, instance.getInsertionIndexes(), extended_instance_string, instance.getLength() + 1);
                    next_instance.addInsertionIndex(instance.getLength());
                    next_instance.addAllInsertionIndexes(instance.getInsertionIndexes());
                    extendInstance(extended_pattern, next_instance, ch);
                    countNodesInDataTree++;
                }
            }

            //if the char is equal add anyway
            if (next_ch == ch) {
                exactInstanceCount = ((InstanceNode)instanceEdge.getDest()).getCountInstancePerGenome();
                addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, nextEdgeIndex, error,
                        deletions);
            } else {
                if (ch == Alphabet.WC_CHAR_INDEX) {
                    addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, nextEdgeIndex, error,
                            deletions);
                } else {
                    if (error < maxError) {//check if the error is not maximal, to add not equal char
                        addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, nextEdgeIndex,
                                error + 1, deletions);
                    }
                    //extend instance by deletions char
                    if (deletions < maxDeletion) {
                        addInstanceToPattern(extended_pattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge, edgeIndex, error, deletions + 1);
                    }
                }
            }
        }
        if (error > 0 || deletions > 0 || insertions > 0){
            exactInstanceCount = 0;
        }
        return exactInstanceCount;
    }

    /**
     * Go over all outgoing edges of instance node
     *
     * @param instance
     * @param instance_edges  edge set of instance_node
     * @param deletions
     * @param error
     * @param instance_node
     * @param edge_index
     * @param ch
     * @param patternNode
     */
    private int addAllInstanceEdges(Boolean make_insertion, Instance instance, Map<Integer, Edge> instance_edges,
                                    int deletions, int error, InstanceNode instance_node, int edge_index, int ch,
                                    PatternNode patternNode) {
        int curr_error = error;
        int next_edge_index;
        int exact_instance_count = 0;

        //go over all outgoing edges
        for (Map.Entry<Integer, Edge> entry : instance_edges.entrySet()) {
            int next_ch = entry.getKey();
            Edge next_edge = entry.getValue();
            InstanceNode next_node = instance_node;

            if (ch == next_ch) {
                curr_error = error;
                exact_instance_count = ((InstanceNode)next_edge.getDest()).getCountInstancePerGenome();
            } else {
                if (ch != Alphabet.WC_CHAR_INDEX) {//Substitution - the chars are different, increment error
                    curr_error = error + 1;
                }
            }

            //The label contains only 1 char, go to next instance_node
            if (next_edge.getLabel().getLength() == 1) {
                next_node = (InstanceNode) next_edge.getDest();
                next_edge = null;
                next_edge_index = -1;
            } else {//label contains more the 1 char, increment edge_index
                next_edge_index = edge_index + 1;
            }

            if (make_insertion) {
                if (ch != next_ch) {
                    //String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
                    String extended_instance_string = "";
                    Instance next_instance = new Instance(next_node, next_edge, next_edge_index, error, deletions,
                            instance.getInsertionIndexes(), extended_instance_string, instance.getLength() + 1);
                    next_instance.addInsertionIndex(instance.getLength());
                    extendInstance(patternNode, next_instance, ch);
                    countNodesInDataTree++;
                }
            } else {
                addInstanceToPattern(patternNode, instance, next_ch, next_node, next_edge, next_edge_index, curr_error, deletions);
            }
        }
        return exact_instance_count;
    }

    /**
     * @param extended_pattern
     * @param instance
     * @param next_ch
     * @param next_node
     * @param next_edge
     * @param next_edge_index
     * @param next_error
     * @param next_deletions
     * @throws Exception
     */
    private void addInstanceToPattern(PatternNode extended_pattern, Instance instance, int next_ch, InstanceNode next_node,
                                      Edge next_edge, int next_edge_index, int next_error, int next_deletions) {

        //String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
        String extended_instance_string = "";
        Instance next_instance = new Instance(next_node, next_edge, next_edge_index, next_error, next_deletions,
                instance.getInsertionIndexes(), extended_instance_string, instance.getLength()+1);
        extended_pattern.addInstance(next_instance, maxInsertion);

        countNodesInDataTree++;
    }


    /**
     * @return
     */
    public List<Pattern> getPatterns() {
        return new ArrayList<Pattern>(patterns.values());
    }


}
