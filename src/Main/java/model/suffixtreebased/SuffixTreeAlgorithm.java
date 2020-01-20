package model.suffixtreebased;

import model.Algorithm;
import model.MemoryUtils;
import model.Parameters;
import model.patterns.Instance;
import model.patterns.Pattern;
import model.patterns.PatternsUtils;
import model.suffixtreebased.suffixtrees.*;

import java.util.*;

import model.genomes.*;

/**
 * Suffix Tree based algorithm for CSB pattern discovery
 * <p>
 * A CSB is a substring of at least (@code q1) input sequences and must have instance in at least (@code q2)
 * input sequences
 * An instance can differ from a CSB by at most k insertions
 */
public class SuffixTreeAlgorithm implements Algorithm {

    public static long countNodesInPatternTree;
    public static long countNodesInDataTree;

    private int maxError;
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

    private boolean crossStrand;

    private boolean debug;

    private int totalCharsInData;
    private GenomesInfo gi;

    private PatternNode patternTreeRoot;

    private List<Pattern> patternsFromFile;

    public SuffixTreeAlgorithm() {

        parameters = null;
        this.gi = null;

        patterns = new HashMap<>();
        patternsFromFile = new ArrayList<>();

    }

    public void setParameters(Parameters params) {

        this.parameters = params;

        // args
        this.maxError = params.maxError;
        this.maxDeletion = params.maxDeletion;
        this.maxInsertion = params.maxInsertion;
        q1 = params.quorum1;
        q2 = params.quorum2;
        this.crossStrand = params.crossStrand;
        this.minPatternLength = params.minPatternLength;
        this.maxPatternLength = params.maxPatternLength;
        this.multCount = params.multCount;
        this.debug = params.debug;

    }

    public void setGenomesInfo(GenomesInfo gi) {
        this.gi = gi;

        datasetTree = new DatasetTree(gi);
    }

    public void setPatternsFromFile(List<Pattern> patternsFromFile) {
        this.patternsFromFile = patternsFromFile;
    }

    /*
    @Override
    public void setRefGenomesAsPatterns(List<Pattern> refGenomesPatterns) {
        if (this.patternsFromFile.size() == 0) {
            this.patternsFromFile = refGenomesPatterns;
        }
    }*/

    public Parameters getParameters() {
        return parameters;
    }

    @Override
    public void setNumOfThreads(int numOfThreads) {
        //TODO: implement
    }

    public int getPatternsCount() {
        return patterns.size();
    }

    private void initialize() {
        totalCharsInData = -1;
        lastPatternKey = 0;
        countNodesInPatternTree = 0;
        countNodesInDataTree = 0;

        patterns = new HashMap<>();

        setPatternTreeRoot();
    }

    private void setPatternTreeRoot() {
        if (patternsFromFile.size() > 0) {
            List<Pattern> legalPatterns = PatternsUtils.getLegalPatterns(patternsFromFile, gi);
            PatternsTree patternsTree = new PatternsTree(legalPatterns, gi, crossStrand);
            Trie patternTrie = patternsTree.getTrie();
            patternTreeRoot = patternTrie.getRoot();
        } else {//all patterns will be extracted from the data tree
            patternTreeRoot = new PatternNode(TreeType.VIRTUAL);
            patternTreeRoot.setKey(Integer.toString(++lastPatternKey));
        }
    }


    public void findPatterns() {
        if (parameters == null || gi == null || datasetTree == null) {
            return;
        }

        initialize();

        datasetTree.buildTree(parameters.crossStrand);
        GeneralizedSuffixTree datasetSuffixTree = datasetTree.getSuffixTree();
        datasetSuffixTree.computeCount();
        totalCharsInData = datasetSuffixTree.getRoot().getCountMultipleInstancesPerGenome();

        InstanceNode dataTreeRoot = datasetSuffixTree.getRoot();
        //the instance of an empty string is the root of the data tree
        Instance emptyInstance = new Instance(dataTreeRoot);
        countNodesInDataTree++;

        patternTreeRoot.addInstance(emptyInstance);
        if (patternTreeRoot.getType() == TreeType.VIRTUAL) {
            spellPatternsVirtually(patternTreeRoot, dataTreeRoot, -1, null, new Gene[0],
                    0);
        } else {
            spellPatterns(patternTreeRoot, new Gene[0], 0);
        }

        removeRedundantPatterns();
    }

    /**
     * Remove patterns that are suffixes of existing patterns, and has the same number of instances
     * If a pattern passes the quorum, all its sub-patterns also pass the quorum
     * If a (sub-pattern instance count = pattern instance count) : the sub-pattern is always a part of the larger
     * pattern
     * Therefore it is sufficient to remove each pattern suffix if it has the same instance count
     */
    private void removeRedundantPatterns() {
        HashSet<String> patternsToRemove = new HashSet<>();
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {

            Pattern pattern = entry.getValue();

            if (!parameters.keepAllPatterns) {
                String suffixStr = getSuffix(pattern);
                PatternsUtils.addSubPatternToRemoveList(patterns, suffixStr, pattern, patternsToRemove);
            }

            if (crossStrand) {
                PatternsUtils.removeReverseCompliments(patterns, pattern, patternsToRemove);
            }
        }
        patterns.keySet().removeAll(patternsToRemove);
    }

    private String getSuffix(Pattern pattern) {

        Gene[] suffix = Arrays.copyOfRange(pattern.getPatternGenes(), 1, pattern.getLength());

        return Pattern.toString(suffix);
    }

    /**
     * Adds a character to str
     *
     * @param str
     * @param ch  the index of the character to addGene, converted to a letter
     * @return the extended str
     */
    private Gene[] appendChar(Gene[] str, int ch) {
        Gene cog = gi.getLetter(ch);
        Gene[] extendedString = Arrays.copyOf(str, str.length + 1);
        extendedString[extendedString.length - 1] = cog;

        return extendedString;
    }


    /**
     * Recursive function that traverses over the subtree rooted at patternNode, which is a node of a suffix tree.
     * This operation 'spells' all possible strings with infix 'pattern', that have enough instances (q1 exact instances
     * and q2 approximate instances)
     *
     * @param patternNode            node in the enumeration tree that represents the current pattern
     * @param pattern                represents a string concatenation of edge labels from root to patternNode
     * @param pattern_length
     * @return The maximal number of different string indexes that one of the extended patterns by a char appear in
     */
    private int spellPatterns(PatternNode patternNode, Gene[] pattern, int pattern_length) {

        List<Instance> instances = patternNode.getInstances();

        Map<Integer, PatternNode> targetNodes = patternNode.getTargetNodes();

        //the maximal number of different instances, of one of the extended patterns
        int maxNumOfDiffInstances = -1;
        int numOfDiffInstance = 0;

        PatternNode target_node;
        for (Map.Entry<Integer, PatternNode> entry : targetNodes.entrySet()) {
            int alpha = entry.getKey();
            //Gene alpha_ch = gi.getLetter(alpha);
            target_node = entry.getValue();

            numOfDiffInstance = extendPattern(alpha, -1, null, null,
                     pattern, target_node, patternNode, instances, pattern_length);

            if (numOfDiffInstance > maxNumOfDiffInstances) {
                maxNumOfDiffInstances = numOfDiffInstance;
            }
            //For memory saving, remove pointer to target node
            patternNode.addTargetNode(alpha, null);

        }

        countNodesInPatternTree++;

        return maxNumOfDiffInstances;
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
     * @return
     */
    private int spellPatternsVirtually(PatternNode patternNode, InstanceNode dataNode, int dataEdgeIndex,
                                       Edge dataEdge,
                                       Gene[] pattern, int patternLength) {

        List<Instance> instances = patternNode.getInstances();
        //the maximal number of different instances, of one of the extended patterns
        int maxNumOfDiffInstances = -1;
        int numOfDiffInstances = 0;

        Map<Integer, Edge> dataNodeEdges = null;

        WordArray dataEdgeLabel;
        if (dataEdge != null) {
            dataEdgeLabel = dataEdge.getLabel();
            if (dataEdgeIndex >= dataEdgeLabel.getLength()) {//we reached to the end of the edge
                dataNode = dataEdge.getDest();
                dataEdgeIndex = -1;
                dataEdge = null;
            }
        }

        if (dataEdgeIndex == -1) {
            dataEdgeIndex++;
            dataNodeEdges = dataNode.getEdges();

            for (Map.Entry<Integer, Edge> entry : dataNodeEdges.entrySet()) {
                int alpha = entry.getKey();
                //Gene alpha_ch = gi.getLetter(alpha);
                dataEdge = entry.getValue();
                InstanceNode dataTreeTargetNode = dataEdge.getDest();

                if (dataTreeTargetNode.getCountInstancePerGenome() >= q1) {

                    if (alpha == Alphabet.UNK_CHAR_INDEX) {
                        if (q1 == 0 && gi.getLetter(pattern[0]) != Alphabet.UNK_CHAR_INDEX) {
                            spellPatternsVirtually(patternNode, dataNode, dataEdgeIndex + 1, dataEdge,
                                    pattern, patternLength);
                        }
                    } else {

                        maxNumOfDiffInstances = extendPattern(patternNode, dataNode, dataEdgeIndex, dataEdge, pattern,
                                patternLength, alpha, instances, maxNumOfDiffInstances);
                    }
                }
            }
        } else {//dataEdgeIndex>=1 && dataEdgeIndex < dataEdgeLabel.getLength()
            dataEdgeLabel = dataEdge.getLabel();
            int alpha = dataEdgeLabel.getLetter(dataEdgeIndex);

            InstanceNode data_tree_target_node = dataEdge.getDest();

            if (data_tree_target_node.getCountInstancePerGenome() >= q1) {
                if (alpha != Alphabet.UNK_CHAR_INDEX) {

                    maxNumOfDiffInstances = extendPattern(patternNode, dataNode, dataEdgeIndex,
                    dataEdge, pattern, patternLength, alpha, instances, maxNumOfDiffInstances);
                }
            }
        }

        countNodesInPatternTree++;

        return maxNumOfDiffInstances;
    }

    private int extendPattern(PatternNode patternNode, InstanceNode dataNode, int dataEdgeIndex,
                               Edge dataEdge, Gene[] pattern, int patternLength,
                               int alpha, List<Instance> instances, int maxNumOfDiffInstances){

        PatternNode targetNode = new PatternNode(TreeType.VIRTUAL);
        targetNode.setKey(Integer.toString(++lastPatternKey));

        int numOfDiffInstances = extendPattern(alpha, dataEdgeIndex + 1, dataNode, dataEdge,
                 pattern, targetNode, patternNode, instances, patternLength);

        if (numOfDiffInstances > maxNumOfDiffInstances) {
            maxNumOfDiffInstances = numOfDiffInstances;
        }

        return maxNumOfDiffInstances;
    }


    /**
     * Extend pattern recursively by one character, if it passes the q1 and q2 - addGene to pattern list
     *
     * @param alpha          the char to append
     * @param pattern        previous pattern string, before adding alpha. i.e. COG1234|COG2000|
     * @param extendedPatternNode     node the extended pattern
     * @param pattern_node   node of pattern
     * @param Instances      the instances of pattern
     * @param pattern_length
     * @return num of different instances of extended pattern
     */

    private int extendPattern(int alpha, int data_edge_index, InstanceNode data_node, Edge data_edge,
                              Gene[] pattern, PatternNode extendedPatternNode,
                              PatternNode pattern_node, List<Instance> Instances, int pattern_length) {

        Gene[] extendedPattern = appendChar(pattern, alpha);

        int extendedPatternLength = pattern_length + 1;

        int exactInstancesCount = 0;
        //go over all instances of the pattern
        for (Instance instance : Instances) {
            int currExactInstanceCount = extendInstance(pattern, extendedPatternNode, instance, alpha);
            if (currExactInstanceCount > 0) {
                exactInstancesCount = currExactInstanceCount;
            }
        }
        extendedPatternNode.setExactInstanceCount(exactInstancesCount);

        int instancesCount;
        if (multCount) {
            instancesCount = extendedPatternNode.getInstanceIndexCount();
        } else {
            instancesCount = extendedPatternNode.getInstanceKeysSize();
        }

        if (exactInstancesCount >= q1 && instancesCount >= q2) {

            TreeType type = extendedPatternNode.getType();
            int ret = -1;
            if (extendedPatternLength < maxPatternLength) {
                if (type == TreeType.VIRTUAL) {
                    ret = spellPatternsVirtually(extendedPatternNode, data_node, data_edge_index, data_edge,
                            extendedPattern, extendedPatternLength);
                } else {
                    ret = spellPatterns(extendedPatternNode, extendedPattern, extendedPatternLength);
                }
            }

            if (extendedPatternLength >= minPatternLength) {

                //make sure that extendedPattern is right maximal, if extendedPattern has the same number of
                // instances as the longer pattern, prefer the longer pattern
                if (extendedPatternNode.getPatternKey() != null &&
                        (instancesCount > ret || parameters.keepAllPatterns)) // instancesCount >= ret always
                {

                    Pattern newPattern = new Pattern(extendedPatternNode.getPatternKey(),
                            extendedPattern);

                    newPattern.addInstanceLocations(extendedPatternNode.getInstances());

                    patterns.put(newPattern.toString(), newPattern);

                    if (debug && (getPatternsCount() % 5000 == 0)) {
                        MemoryUtils.measure();
                        System.out.println(getPatternsCount() + " patterns found");
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
        return instancesCount;

    }

    /**
     * Extends instance, increments error depending on ch
     *
     * @param extendedPattern extended pattern node
     * @param instance        the current instance
     * @param ch              the character of the pattern, need to check if the next char on the instance is equal
     * @return list of all possible extended instances
     */

    private int extendInstance(Gene[] patternGenes, PatternNode extendedPattern, Instance instance, int ch) {
        //values of current instance
        InstanceNode nodeInstance = instance.getNodeInstance();
        Edge instanceEdge = instance.getEdge();
        int edgeIndex = instance.getEdgeIndex();
        int error = instance.getError();
        int deletions = instance.getDeletions();
        int insertions = instance.getInsertions();

        //values of the extended instance
        int nextEdgeIndex = edgeIndex;
        Edge nextEdgeInstance = instanceEdge;
        InstanceNode nextNodeInstance = nodeInstance;

        int exactInstanceCount = 0;

        //The substring ends at the current nodeInstance, edgeIndex = -1
        if (instanceEdge == null) {
            //Go over all the edges from nodeInstance, see if the instance can be extended
            Map<Integer, Edge> instanceEdges = nodeInstance.getEdges();

            //we can extend the instance using all outgoing edges, increment error if needed

            if (insertions < maxInsertion && instance.getLength() > 0) {
                addAllInstanceEdges(true, instance, instanceEdges, deletions, error, nodeInstance,
                        edgeIndex, ch, extendedPattern, patternGenes);
            }
            if (error < maxError) {
                //go over all outgoing edges
                exactInstanceCount = addAllInstanceEdges(false, instance, instanceEdges, deletions,
                        error, nodeInstance, edgeIndex, ch, extendedPattern, patternGenes);
                //extend instance by deletions char
                if (deletions < maxDeletion) {
                    addInstanceToPattern(extendedPattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge, edgeIndex,
                            error, deletions + 1);
                }
            } else {//error = max error, only instanceEdge starting with ch can be added, or deletions
                nextEdgeIndex++;
                nextEdgeInstance = nodeInstance.getEdge(ch);
                nextNodeInstance = nodeInstance;
                //Exists an instanceEdge starting with ch, addGene it to instances
                if (nextEdgeInstance != null) {
                    exactInstanceCount = nextEdgeInstance.getDest().getCountInstancePerGenome();
                    //The label contains only 1 char, go to next nodeInstance
                    if (nextEdgeInstance.getLabel().getLength() == 1) {
                        nextNodeInstance = nextEdgeInstance.getDest();
                        nextEdgeInstance = null;
                        nextEdgeIndex = -1;
                    }
                    addInstanceToPattern(extendedPattern, instance, ch, nextNodeInstance, nextEdgeInstance,
                            nextEdgeIndex, error, deletions);
                } else {
                    //extend instance by deletions char
                    if (deletions < maxDeletion) {
                        addInstanceToPattern(extendedPattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge,
                                edgeIndex, error, deletions + 1);
                    }
                }
            }

        } else {//Edge is not null, the substring ends at the middle of the instanceEdge, at index edgeIndex
            WordArray label = instanceEdge.getLabel();
            //check the next char on the label, at edgeIndex+1
            nextEdgeIndex++;
            int nextCh = label.getLetter(nextEdgeIndex);

            //If we reached the end of the label by incrementing edgeIndex, get next nodeInstance
            if (nextEdgeIndex == label.getLength() - 1) {
                nextNodeInstance = instanceEdge.getDest();
                nextEdgeInstance = null;
                nextEdgeIndex = -1;
            }

            if (insertions < maxInsertion && instance.getLength() > 0) {
                if (nextCh != ch) {
                    String extendedInstanceString = "";
                    Instance nextInstance = new Instance(nextNodeInstance, nextEdgeInstance, nextEdgeIndex,
                            error, deletions, instance.getInsertions() + 1, extendedInstanceString,
                            instance.getLength() + 1, instance.getMinimalInstanceIndex());

                    extendInstance(patternGenes, extendedPattern, nextInstance, ch);
                    countNodesInDataTree++;
                }
            }

            //if the char is equal addGene anyway
            if (nextCh == ch) {
                exactInstanceCount = instanceEdge.getDest().getCountInstancePerGenome();
                addInstanceToPattern(extendedPattern, instance, nextCh, nextNodeInstance, nextEdgeInstance, nextEdgeIndex, error,
                        deletions);
            } else {

                if (error < maxError) {//check if the error is not maximal, to addGene not equal char
                    addInstanceToPattern(extendedPattern, instance, nextCh, nextNodeInstance, nextEdgeInstance, nextEdgeIndex,
                            error + 1, deletions);
                }
                //extend instance by deletions char
                if (deletions < maxDeletion) {
                    addInstanceToPattern(extendedPattern, instance, Alphabet.GAP_CHAR_INDEX, nodeInstance, instanceEdge, edgeIndex, error, deletions + 1);
                }

            }
        }
        if (error > 0 || deletions > 0 || insertions > 0) {
            exactInstanceCount = 0;
        }
        return exactInstanceCount;
    }

    /**
     * Go over all outgoing edges of instance node
     *
     * @param instance
     * @param instanceEdges edge set of instanceNode
     * @param deletions
     * @param error
     * @param instanceNode
     * @param edgeIndex
     * @param ch
     * @param patternNode
     */
    private int addAllInstanceEdges(Boolean makeInsertion, Instance instance, Map<Integer, Edge> instanceEdges,
                                    int deletions, int error, InstanceNode instanceNode, int edgeIndex, int ch,
                                    PatternNode patternNode, Gene[] patternGenes) {
        int curr_error = error;
        int nextEdgeIndex;
        int exact_instance_count = 0;

        //go over all outgoing edges
        for (Map.Entry<Integer, Edge> entry : instanceEdges.entrySet()) {
            int nextCh = entry.getKey();
            Edge nextEdge = entry.getValue();
            InstanceNode nextNode = instanceNode;

            if (ch == nextCh) {
                curr_error = error;
                exact_instance_count = nextEdge.getDest().getCountInstancePerGenome();
            } else {
                //Substitution - the chars are different, increment error
                curr_error = error + 1;
            }

            //The label contains only 1 char, go to next instanceNode
            if (nextEdge.getLabel().getLength() == 1) {
                nextNode = nextEdge.getDest();
                nextEdge = null;
                nextEdgeIndex = -1;
            } else {//label contains more the 1 char, increment edgeIndex
                nextEdgeIndex = edgeIndex + 1;
            }

            if (makeInsertion) {
                if (ch != nextCh) {
                    int minimalInstanceIndex = instance.getMinimalInstanceIndex();
                    Gene gene = patternGenes[minimalInstanceIndex];
                    //Gene instanceGene = gi.getLetter(nextCh);
                    if (gi.getLetter(gene) == nextCh) {
                        minimalInstanceIndex += 1;
                    }

                    if (minimalInstanceIndex < patternGenes.length) {
                        String extendedInstanceString = "";
                        Instance nextInstance = new Instance(nextNode, nextEdge, nextEdgeIndex, error, deletions,
                                instance.getInsertions() + 1, extendedInstanceString,
                                instance.getLength() + 1, minimalInstanceIndex);
                        extendInstance(patternGenes, patternNode, nextInstance, ch);
                        countNodesInDataTree++;
                    }
                }
            } else {
                addInstanceToPattern(patternNode, instance, nextCh, nextNode, nextEdge, nextEdgeIndex, curr_error, deletions);
            }
        }
        return exact_instance_count;
    }

    /**
     * @param extended_pattern
     * @param instance
     * @param next_ch
     * @param nextNode
     * @param nextEdge
     * @param nextEdgeIndex
     * @param nextError
     * @param nextDeletions
     */
    private void addInstanceToPattern(PatternNode extended_pattern, Instance instance, int next_ch, InstanceNode nextNode,
                                      Edge nextEdge, int nextEdgeIndex, int nextError, int nextDeletions) {

        String extendedInstanceString = "";
        Instance nextInstance = new Instance(nextNode, nextEdge, nextEdgeIndex, nextError, nextDeletions,
                instance.getInsertions(), extendedInstanceString, instance.getLength() + 1,
                instance.getMinimalInstanceIndex());
        extended_pattern.addInstance(nextInstance);

        countNodesInDataTree++;
    }


    /**
     * @return
     */
    public List<Pattern> getPatterns() {
        return new ArrayList<>(patterns.values());
    }


}
