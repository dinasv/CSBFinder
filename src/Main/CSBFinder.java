package Main;

import Words.WordArray;
import SuffixTrees.*;

import java.util.*;
import Utils.*;

/**
 * Suffix Tree based algorithm for CSB pattern discovery
 * A CSB is a substring of at least quorum1 input sequences and must have instance in at least quorum2 input sequences
 * An instance can differ from a CSB by at most k insertions
 */
public class CSBFinder {
    public static long count_nodes_in_pattern_tree;
    public static long count_nodes_in_data_tree;
    private static final String DELIMITER = "-";

    private static int max_error;
    private static int max_wildcards;
    private static int max_deletion;
    private static int max_insertion;
    private int q1;
    private int q2;
    private int min_pattern_length;
    private int max_pattern_length;

    private GeneralizedSuffixTree data_tree;

    //contains all extracted patterns
    private HashMap<String, Pattern> patterns;

    private int gap_char;
    private int wildcard_char;
    private int unkown_cog_char;
    private boolean count_by_keys;

    private int last_pattern_key;
    private boolean memory_saving_mode;

    private boolean debug;

    int total_chars_in_data;
    Utils utils;
    Writer writer;

    /**
     *
     * @param max_error
     * @param max_wildcards
     * @param max_deletion
     * @param max_insertion maximal number of insertions allowed in an instance of a pattern
     * @param quorum1 the pattern must be a substring of at least quorum1 input sequences
     * @param quorum2 the pattern should have an instance in at least quorum2 input sequences
     * @param min_pattern_length
     * @param max_pattern_length
     * @param gap_char
     * @param wildcard_char
     * @param unkown_cog_char
     * @param data_t GST representing all input sequences
     * @param pattern_trie
     * @param count_by_keys if true, counts one instance in each input sequence
     * @param utils
     * @param memory_saving_mode
     * @param writer
     * @param debug
     */
    public CSBFinder(int max_error, int max_wildcards, int max_deletion, int max_insertion, int quorum1, int quorum2,
                     int min_pattern_length, int max_pattern_length, int gap_char, int wildcard_char, int unkown_cog_char,
                     GeneralizedSuffixTree data_t, Trie pattern_trie, boolean count_by_keys, Utils utils,
                     boolean memory_saving_mode, Writer writer, boolean debug){

        patterns = new HashMap<>();
        this.max_error = max_error;
        this.max_wildcards = max_wildcards;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        data_tree = data_t;
        q1 = quorum1;
        q2 = quorum2;
        this.min_pattern_length = min_pattern_length;
        this.max_pattern_length = max_pattern_length;
        this.gap_char = gap_char;
        this.wildcard_char = wildcard_char;
        this.unkown_cog_char = unkown_cog_char;
        this.count_by_keys = count_by_keys;
        total_chars_in_data = -1;
        this.utils = utils;
        last_pattern_key = 0;
        this.memory_saving_mode = memory_saving_mode;
        this.writer = writer;
        this.debug = debug;

        count_nodes_in_pattern_tree = 0;
        count_nodes_in_data_tree = 0;

        PatternNode pattern_tree_root;
        if (pattern_trie == null){//all patterns will be extracted from the data tree
            pattern_tree_root = new PatternNode(TreeType.VIRTUAL);
            pattern_tree_root.setKey(++last_pattern_key);
        }else {//if we were given patterns as input
            pattern_tree_root = pattern_trie.getRoot();
        }
        findPatterns(pattern_tree_root);
    }

    public int getPatternsCount(){
        return patterns.size();
    }

    /**
     * Calls the recursive function spellPatterns
     * @param pattern_node a node in the pattern tree, the pattern tree traversal begins from this node
     */
    private void findPatterns(PatternNode pattern_node) {

        data_tree.computeCount();
        total_chars_in_data = ((InstanceNode) data_tree.getRoot()).getCount_by_indexes();

        InstanceNode data_tree_root = (InstanceNode) data_tree.getRoot();
        //the instance of an empty string is the root of the data tree
        Instance empty_instance = new Instance(data_tree_root, null, -1, 0, 0);
        count_nodes_in_data_tree ++;

        pattern_node.addInstance(empty_instance, max_insertion);
        if (pattern_node.getType()== TreeType.VIRTUAL){
            spellPatternsVirtually(pattern_node, data_tree_root, -1, null, "", 0, 0);
        }else {
            spellPatterns(pattern_node, "", 0, 0);
        }
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
        return new WordArray(seq.wordArray, seq.get_start_index() + start_index,
                seq.get_start_index() + end_index);
    }

    /**
     * Remove patterns that are suffixes of existing patterns, and has the same number of instances
     * If a pattern passes the quorum1, all its sub-patterns also pass the quorum1
     * If a (sub-pattern instance count = pattern instance count) : the sub-pattern is always a part of the larger
     * pattern
     * Therefore it is sufficient to remove each pattern suffix if it has the same instance count
     */
    public void removeRedundantPatterns() {
        ArrayList<String> patterns_to_remove = new ArrayList<String>();
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {

            Pattern pattern = entry.getValue();
            String[] pattern_arr = pattern.getPatternArr();
            //String str = entry.getKey();

            //String suffix_str = str.substring(5);
            String[] suffix_arr = Arrays.copyOfRange(pattern_arr, 1, pattern_arr.length);
            String suffix_str = String.join(DELIMITER, suffix_arr) + DELIMITER;
            Pattern suffix = patterns.get(suffix_str);

            if (suffix != null){
                int pattern_count = pattern.getInstanceCount();
                int suffix_count = suffix.getInstanceCount();
                if (suffix_count == pattern_count){
                    patterns_to_remove.add(suffix_str);
                }
            }
        }
        patterns.keySet().removeAll(patterns_to_remove);
    }

    /**
     * Add to node an edge with label = gap. edge.dest = copy of node, its edged are deep copied
     *
     * @param node
     */
    private void addWildcardEdge(PatternNode node, Boolean copy_node) {
        PatternNode targetNode = node.getTargetNode(wildcard_char);
        if (targetNode == null) {
            int[] wildcard = {wildcard_char};
            //create a copy of node
            PatternNode newnode = new PatternNode(node);
            node.addTargetNode(wildcard_char, newnode);
        } else {
            PatternNode newnode = node.getTargetNode(wildcard_char);
            newnode = new PatternNode(newnode);
            targetNode.addTargetNode(wildcard_char, newnode);
        }
    }

    private Boolean starts_with_wildcard(String pattern) {
        if (pattern.length() > 0) {
            if (pattern.charAt(0) == '*') {
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
    private String appendChar(String str, int ch) {
        String cog = utils.index_to_char.get(ch);
        //System.out.println(cog);
        String extended_string = str + cog + DELIMITER;
        extended_string.intern();
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
    private int spellPatterns(PatternNode pattern_node, String pattern, int pattern_length, int pattern_wildcard_count) {
        if (pattern_wildcard_count < max_wildcards && pattern_node.getType().equals("enumeration")) {
            //add to pattern_node an edge with "_", pointing to a new node that will save the instances
            addWildcardEdge(pattern_node, true);
        }

        ArrayList<Instance> instances = pattern_node.getInstances();

        HashMap<Integer, PatternNode> target_nodes = pattern_node.getTarget_nodes();

        //the maximal number of different instances, of one of the extended patterns
        int max_num_of_diff_instances = -1;
        int num_of_diff_instance = 0;

        PatternNode target_node;
        for (Map.Entry<Integer, PatternNode> entry : target_nodes.entrySet()) {
            int alpha = entry.getKey();
            String alpha_ch = utils.index_to_char.get(alpha);
            target_node = entry.getValue();

            //go over edges that are not wild cards
            if (alpha!=wildcard_char) {
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
        if (pattern_node.getType().equals("pattern") || pattern_wildcard_count < max_wildcards) {
            target_node = pattern_node.getTargetNode(wildcard_char);
            if (target_node != null) {
                num_of_diff_instance = extendPattern(wildcard_char, -1, null, null,
                            pattern_wildcard_count + 1, pattern, target_node, pattern_node, instances, pattern_length);
                if (num_of_diff_instance > max_num_of_diff_instances) {
                    max_num_of_diff_instances = num_of_diff_instance;
                }
            }
        }
        count_nodes_in_pattern_tree++;

        return max_num_of_diff_instances;
    }


    /**
     * * Recursive function that traverses over the subtree rooted at pattern_node, which is a node of a suffix tree.
     * This operation 'spells' all possible strings with infix 'pattern', that have enough instances (q1 exact instances
     * and q2 approximate instances)
     * It is same as spellPatterns, only that the suffix tree of patterns is not saved in memory, it is created virtually
     * from data suffix tree.
     *
     * @param pattern_node
     * @param data_node
     * @param data_edge_index
     * @param data_edge
     * @param pattern
     * @param pattern_length
     * @param wildcard_count
     * @return
     */
    private int spellPatternsVirtually(PatternNode pattern_node, InstanceNode data_node, int data_edge_index,
                                       Edge data_edge,
                                       String pattern, int pattern_length, int wildcard_count) {

        ArrayList<Instance> instances = pattern_node.getInstances();
        //the maximal number of different instances, of one of the extended patterns
        int max_num_of_diff_instances = -1;
        int num_of_diff_instances = 0;

        HashMap<Integer, Edge> data_node_edges = null;

        WordArray data_edge_label;
        if (data_edge != null) {
            data_edge_label = data_edge.getLabel();
            if (data_edge_index >= data_edge_label.get_length()) {//we reached to the end of the edge
                data_node = (InstanceNode) data_edge.getDest();
                data_edge_index = -1;
                data_edge = null;
            }
        }

        PatternNode target_node;

        if (data_edge_index == -1){
            data_edge_index ++;
            data_node_edges = data_node.getEdges();

            for (Map.Entry<Integer, Edge> entry : data_node_edges.entrySet()) {
                int alpha = entry.getKey();
                String alpha_ch = utils.index_to_char.get(alpha);
                data_edge = entry.getValue();
                InstanceNode data_tree_target_node = (InstanceNode) data_edge.getDest();

                if (data_tree_target_node.getCount_by_keys() >= q1) {

                    if (alpha == unkown_cog_char) {
                        if (q1 == 0 && !pattern.startsWith("X")) {
                            spellPatternsVirtually(pattern_node, data_node, data_edge_index + 1, data_edge,
                            pattern, pattern_length, wildcard_count);
                        }
                    } else {

                        target_node = new PatternNode(TreeType.VIRTUAL);
                        target_node.setKey(++last_pattern_key);

                        num_of_diff_instances = extendPattern(alpha, data_edge_index + 1, data_node, data_edge,
                                wildcard_count, pattern, target_node, pattern_node, instances, pattern_length);

                        if (num_of_diff_instances > max_num_of_diff_instances) {
                            max_num_of_diff_instances = num_of_diff_instances;
                        }
                    }
                }
            }
        }else{//data_edge_index>=1 && data_edge_index < data_edge_label.get_length()
            data_edge_label = data_edge.getLabel();
            int alpha = data_edge_label.get_index(data_edge_index);

            InstanceNode data_tree_target_node = (InstanceNode) data_edge.getDest();

            if (data_tree_target_node.getCount_by_keys() >= q1) {
                if (alpha == unkown_cog_char) {
                    //spellPatternsVirtually(pattern_node, data_node, data_edge_index + 1, data_edge,
                            //pattern, pattern_length, wildcard_count);
                } else {

                    target_node = new PatternNode(TreeType.VIRTUAL);
                    target_node.setKey(++last_pattern_key);

                    num_of_diff_instances = extendPattern(alpha, data_edge_index + 1, data_node, data_edge,
                            wildcard_count, pattern, target_node, pattern_node, instances, pattern_length);

                    if (num_of_diff_instances > max_num_of_diff_instances) {
                        max_num_of_diff_instances = num_of_diff_instances;
                    }
                }
            }
        }

        count_nodes_in_pattern_tree++;

        return max_num_of_diff_instances;
    }



    /**
     * Extend pattern recursively by one character, if it passes the q1 and q2 - add to pattern list
     *
     * @param alpha                the char to append
     * @param wildcard_count how many wildcard in the pattern
     * @param pattern                previous pattern string, before adding alpha. i.e. COG1234|COG2000|
     * @param target_node          node the extended pattern
     * @param pattern_node           node of pattern
     * @param Instances            the instances of pattern
     * @param pattern_length
     * @return num of different instances of extended pattern
     */

    private int extendPattern(int alpha, int data_edge_index, InstanceNode data_node, Edge data_edge,
                              int wildcard_count, String pattern, PatternNode target_node,
                              PatternNode pattern_node, ArrayList<Instance> Instances, int pattern_length) {

        String extended_pattern = appendChar(pattern, alpha);
        PatternNode extended_pattern_node = target_node;
        int extended_pattern_length = pattern_length + 1;

        //if there is a wildcard in the current pattern, have to create a copy of the subtree
        if (wildcard_count > 0 && alpha!=wildcard_char) {
            extended_pattern_node = new PatternNode(extended_pattern_node);
            pattern_node.addTargetNode(alpha, extended_pattern_node);
        }

        extended_pattern_node.setSubstring(extended_pattern);
        extended_pattern_node.setSubstring_length(extended_pattern_length);

        int exact_instances_count = 0;
        //go over all instances of the pattern
        for (Instance instance : Instances) {
            int curr_exact_instance_count = extendInstance(extended_pattern_node, instance, alpha);
            if (curr_exact_instance_count > 0){
                exact_instances_count = curr_exact_instance_count;
            }
        }
        extended_pattern_node.setExact_instance_count(exact_instances_count);

        int diff_instances_count;
        if (count_by_keys){
            diff_instances_count = extended_pattern_node.getInstanceKeysSize();
        }else {
            diff_instances_count = extended_pattern_node.getInstanceIndexCount();
        }

        if (exact_instances_count >= q1 && diff_instances_count >= q2 &&
                (extended_pattern_length - wildcard_count <= max_pattern_length)) {
            TreeType type = extended_pattern_node.getType();
            int ret;
            if (type == TreeType.VIRTUAL){
                ret = spellPatternsVirtually(extended_pattern_node, data_node, data_edge_index, data_edge,
                        extended_pattern, extended_pattern_length, wildcard_count);
            }else {
                ret = spellPatterns(extended_pattern_node, extended_pattern, extended_pattern_length, wildcard_count);
            }

            if (extended_pattern_length - wildcard_count >= min_pattern_length) {
                if (type == TreeType.STATIC) {
                    if (extended_pattern_node.getPatternKey()>0) {
                        Pattern new_pattern = new Pattern(extended_pattern_node.getPatternKey(), extended_pattern,
                                extended_pattern.split(DELIMITER), extended_pattern_length,
                                extended_pattern_node.getInstanceKeys(), extended_pattern_node.getInstances(),
                                extended_pattern_node.getExact_instance_count());

                        if (memory_saving_mode){
                            new_pattern.calculateScore(utils, max_insertion, max_error, max_deletion);
                            new_pattern.calculateMainFunctionalCategory(utils);
                            writer.printPattern(new_pattern, utils);
                        }else {
                            patterns.put(extended_pattern, new_pattern);
                        }
                    }
                } else if (type == TreeType.VIRTUAL) {
                    if (alpha != wildcard_char) {
                        if (!(starts_with_wildcard(extended_pattern))) {
                            //make sure that extended_pattern is right maximal, if extended_pattern has the same number of
                            // instances as the longer pattern, prefer the longer pattern
                            if (diff_instances_count > ret || debug) {// diff_instances_count >= ret always
                                Pattern new_pattern = new Pattern(extended_pattern_node.getPatternKey(), extended_pattern,
                                        extended_pattern.split(DELIMITER), extended_pattern_length,
                                        extended_pattern_node.getInstanceKeys(), extended_pattern_node.getInstances(),
                                        extended_pattern_node.getExact_instance_count());

                                if (memory_saving_mode){
                                    new_pattern.calculateScore(utils, max_insertion, max_error, max_deletion);
                                    new_pattern.calculateMainFunctionalCategory(utils);
                                    writer.printPattern(new_pattern, utils);
                                }else {
                                    patterns.put(extended_pattern, new_pattern);
                                }
                            }
                        }
                    } else {
                        if (ret <= 0) {
                            diff_instances_count = -1;
                        } else {
                            diff_instances_count = ret;
                        }
                    }
                }
            }
        }
        return diff_instances_count;
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
        InstanceNode node_instance = instance.getNodeInstance();
        Edge edge_instance = instance.getEdge();
        int edge_index = instance.getEdgeIndex();
        int error = instance.getError();
        int deletions = instance.getDeletions();
        int insertions = instance.getInsertions();

        //values of the extended instance
        int next_edge_index = edge_index;
        Edge next_edge_instance = edge_instance;
        InstanceNode next_node_instance = node_instance;

        int exact_instance_count = 0;

        //The substring ends at the current node_instance, edge_index = -1
        if (edge_instance == null) {
            //Go over all the edges from node_instance, see if the instance can be extended
            HashMap<Integer, Edge> instance_edges = node_instance.getEdges();

            //we can extend the instance using all outgoing edges, increment error if needed
            if (ch == wildcard_char) {
                exact_instance_count = addAllInstanceEdges(false, instance, instance_edges, deletions, error, node_instance,
                        edge_index, ch, extended_pattern);
                //extend instance by deletions char
                if (deletions < max_deletion) {
                    addInstanceToPattern(extended_pattern, instance, gap_char, node_instance, edge_instance, edge_index,
                            error, deletions + 1);
                }
            } else {
                if (insertions < max_insertion && instance.getLength() > 0){
                    addAllInstanceEdges(true, instance, instance_edges, deletions, error, node_instance,
                            edge_index, ch, extended_pattern);
                }
                if (error < max_error) {
                    //go over all outgoing edges
                    exact_instance_count = addAllInstanceEdges(false, instance, instance_edges, deletions,
                            error, node_instance, edge_index, ch, extended_pattern);
                    //extend instance by deletions char
                    if (deletions < max_deletion) {
                        addInstanceToPattern(extended_pattern, instance, gap_char, node_instance, edge_instance, edge_index,
                                error, deletions + 1);
                    }
                } else {//error = max error, only edge_instance starting with ch can be added, or deletions
                    next_edge_index++;
                    next_edge_instance = node_instance.getEdge(ch);
                    next_node_instance = node_instance;
                    //Exists an edge_instance starting with ch, add it to instances
                    if (next_edge_instance != null) {
                        exact_instance_count = ((InstanceNode)next_edge_instance.getDest()).getCount_by_keys();
                        //The label contains only 1 char, go to next node_instance
                        if (next_edge_instance.getLabel().get_length() == 1) {
                            next_node_instance = (InstanceNode) next_edge_instance.getDest();
                            next_edge_instance = null;
                            next_edge_index = -1;
                        }
                        addInstanceToPattern(extended_pattern, instance, ch, next_node_instance, next_edge_instance, next_edge_index, error,
                                deletions);
                    } else {
                        //extend instance by deletions char
                        if (deletions < max_deletion) {
                            addInstanceToPattern(extended_pattern, instance, gap_char, node_instance, edge_instance, edge_index, error,
                                    deletions + 1);
                        }
                    }
                }
            }
        } else {//Edge is not null, the substring ends at the middle of the edge_instance, at index edge_index
            WordArray label = edge_instance.getLabel();
            //check the next char on the label, at edge_index+1
            next_edge_index++;
            int next_ch = label.get_index(next_edge_index);

            //If we reached the end of the label by incrementing edge_index, get next node_instance
            if (next_edge_index == label.get_length() - 1) {
                next_node_instance = (InstanceNode) edge_instance.getDest();
                next_edge_instance = null;
                next_edge_index = -1;
            }

            if (insertions < max_insertion && instance.getLength() > 0){
                if (next_ch != ch) {
                    String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
                    Instance next_instance = new Instance(next_node_instance, next_edge_instance, next_edge_index, error, deletions, instance.get_insertion_indexes(), extended_instance_string, instance.getLength() + 1);
                    next_instance.add_insertion_index(instance.getLength());
                    next_instance.add_all_insertion_indexes(instance.get_insertion_indexes());
                    extendInstance(extended_pattern, next_instance, ch);
                    count_nodes_in_data_tree++;
                }
            }

            //if the char is equal add anyway
            if (next_ch == ch) {
                exact_instance_count = ((InstanceNode)edge_instance.getDest()).getCount_by_keys();
                addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, next_edge_index, error,
                        deletions);
            } else {
                if (ch == wildcard_char) {
                    addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, next_edge_index, error,
                            deletions);
                } else {
                    if (error < max_error) {//check if the error is not maximal, to add not equal char
                        addInstanceToPattern(extended_pattern, instance, next_ch, next_node_instance, next_edge_instance, next_edge_index,
                                error + 1, deletions);
                    }
                    //extend instance by deletions char
                    if (deletions < max_deletion) {
                        addInstanceToPattern(extended_pattern, instance, gap_char, node_instance, edge_instance, edge_index, error, deletions + 1);
                    }
                }
            }
        }
        if (error > 0 || deletions > 0 || insertions > 0){
            exact_instance_count = 0;
        }
        return exact_instance_count;
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
    private int addAllInstanceEdges(Boolean make_insertion, Instance instance, HashMap<Integer, Edge> instance_edges,
                                    int deletions, int error, InstanceNode instance_node, int edge_index, int ch,
                                    PatternNode patternNode) {
        int curr_error = error;
        int next_edge_index;
        int exact_instance_count = 0;

        //boolean exist_equal_char = false;

        //go over all outgoing edges
        for (Map.Entry<Integer, Edge> entry : instance_edges.entrySet()) {
            int next_ch = entry.getKey();
            Edge next_edge = entry.getValue();
            InstanceNode next_node = instance_node;

            if (ch == next_ch) {
                curr_error = error;
                exact_instance_count = ((InstanceNode)next_edge.getDest()).getCount_by_keys();
            } else {
                if (ch != wildcard_char) {//Substitution - the chars are different, increment error
                    curr_error = error + 1;
                }
            }

            //The label contains only 1 char, go to next instance_node
            if (next_edge.getLabel().get_length() == 1) {
                next_node = (InstanceNode) next_edge.getDest();
                next_edge = null;
                next_edge_index = -1;
            } else {//label contains more the 1 char, increment edge_index
                next_edge_index = edge_index + 1;
            }

            if (make_insertion) {
                if (ch != next_ch) {
                    String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
                    Instance next_instance = new Instance(next_node, next_edge, next_edge_index, error, deletions,
                            instance.get_insertion_indexes(), extended_instance_string, instance.getLength() + 1);
                    next_instance.add_insertion_index(instance.getLength());
                    extendInstance(patternNode, next_instance, ch);
                    count_nodes_in_data_tree++;
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
        String extended_instance_string = appendChar(instance.getSubstring(), next_ch);
        Instance next_instance = new Instance(next_node, next_edge, next_edge_index, next_error, next_deletions,
                instance.get_insertion_indexes(), extended_instance_string, instance.getLength()+1);
        extended_pattern.addInstance(next_instance, max_insertion);

        count_nodes_in_data_tree++;
    }


    /**
     * @return
     */
    public ArrayList<Pattern> getPatterns() {
        return new ArrayList<Pattern>(patterns.values());
    }


}
