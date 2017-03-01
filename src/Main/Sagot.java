package Main;

import COGAlphabet.WordArray;
import SuffixTrees.*;

import java.util.*;
import Utils.*;

/**
 * Extension of SPELLER algorithm described in "Spelling approximate repeated or common motifs using a suffix tree"
 * by M. Sagot, 1998
 * Search for approximate motifs appearing in at least q2 input sequences.
 * The motif must occur with no errors in q1 input sequences.
 */
public class Sagot {
    private static int max_error;
    private static int max_motif_wildcard;
    private static int max_deletion;
    private static int max_insertion;
    private int q1;
    private int q2;
    private int min_motif_length;

    private GeneralizedSuffixTree data_tree;

    private HashMap<String, Motif> motifs;

    private int gap_char;
    private int wildcard_char;
    private int unkown_cog_char;
    private boolean count_by_keys;

    private int last_motif_key;
    private boolean memory_saving_mode;

    int total_chars_in_data;
    Utils utils;

    public Sagot(int max_error, int max_motif_wildcard, int max_deletion, int max_insertion, int quorum1, int quorum2,
                 int min_motif_length, int gap_char, int wildcard_char, int unkown_cog_char,
                 GeneralizedSuffixTree data_t, Trie motif_trie, boolean count_by_keys, Utils utils, boolean memory_saving_mode){

        motifs = new HashMap<>();
        this.max_error = max_error;
        this.max_motif_wildcard = max_motif_wildcard;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        data_tree = data_t;
        q1 = quorum1;
        q2 = quorum2;
        this.min_motif_length = min_motif_length;
        this.gap_char = gap_char;
        this.wildcard_char = wildcard_char;
        this.unkown_cog_char = unkown_cog_char;
        this.count_by_keys = count_by_keys;
        total_chars_in_data = -1;
        this.utils = utils;
        last_motif_key = 0;
        this.memory_saving_mode = memory_saving_mode;

        MotifNode motif_tree_root;
        if (memory_saving_mode){
            motif_tree_root = new MotifNode("enumeration");
            motif_tree_root.setKey(++last_motif_key);
        }else {
            motif_tree_root = motif_trie.getRoot();
        }
        findMotifs(motif_tree_root);
    }

    /**
     * Calls the recursive function spellMotifs
     * @param motif_node a node in the motif tree, the motif tree traversal begins from this node
     */
    private void findMotifs(MotifNode motif_node) {

        data_tree.computeCount();
        total_chars_in_data = ((OccurrenceNode) data_tree.getRoot()).getCount_by_indexes();

        OccurrenceNode data_tree_root = (OccurrenceNode) data_tree.getRoot();
        //occurrence of empty string
        Occurrence empty_occ = new Occurrence(data_tree_root, null, -1, 0, 0);

        motif_node.addOcc(empty_occ, max_insertion);
        if (memory_saving_mode){
            spellMotifsVirtually(motif_node, data_tree_root, -1, null, "", 0, 0);
        }else {
            spellMotifs(motif_node, "", 0, 0);
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
     * Remove motifs that are suffixes of existing motifs, and has the same number of occurrences
     * Makes sure that the motifs are left maximal
     * If a motif passes the quorum1, all its sub-motifs also pass the quorum1
     * If a (sub-motif occs count = motif occs count) : the sub-motif is always a part of the larger motif
     * Therefore it is sufficient to remove each motif suffix if it has the same occ count
     */
    public void removeRedundantMotifs() {
        ArrayList<String> motifs_to_remove = new ArrayList<String>();
        for (Map.Entry<String, Motif> entry : motifs.entrySet()) {

            Motif motif = entry.getValue();
            String str = entry.getKey();

            String suffix_str = str.substring(5);
            Motif suffix = motifs.get(suffix_str);

            if (suffix != null){
                int motif_count = motif.getOccCount();
                int suffix_count = suffix.getOccCount();
                if (suffix_count == motif_count){
                    motifs_to_remove.add(suffix_str);
                }
            }
        }
        motifs.keySet().removeAll(motifs_to_remove);
    }

    /**
     * Add to node an edge with label = gap. edge.dest = copy of node, its edged are deep copied
     *
     * @param node
     */
    private void addWildcardEdge(MotifNode node, Boolean copy_node) {
        MotifNode targetNode = node.getTargetNode(wildcard_char);
        if (targetNode == null) {
            int[] wildcard = {wildcard_char};
            //create a copy of node
            MotifNode newnode = new MotifNode(node);
            node.addTargetNode(wildcard_char, newnode);
        } else {
            MotifNode newnode = node.getTargetNode(wildcard_char);
            newnode = new MotifNode(newnode);
            targetNode.addTargetNode(wildcard_char, newnode);
        }
    }

    private Boolean starts_with_wildcard(String motif) {
        if (motif.length() > 0) {
            if (motif.charAt(0) == '*') {
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
        String cog = utils.index_to_cog.get(ch);
        //System.out.println(cog);
        String extended_string = str + cog + "|";
        extended_string.intern();
        return extended_string;
    }


    /**
     * Recursive function that spells all possible motifs given max_error and q2
     *
     * @param motif_node node in the enumeration tree that represents the current motif
     * @param motif     represents concatenation of edged from root to motif_node
     * @param motif_length
     * @param motif_wildcard_count  number of wildcards in the motif
     * @return The maximal number of different string indexes that one of the extended motifs by a char appear in
     */
    private int spellMotifs(MotifNode motif_node, String motif, int motif_length, int motif_wildcard_count) {
        if (motif_wildcard_count < max_motif_wildcard && motif_node.getType().equals("enumeration")) {
            //add to motif_node an edge with "_", pointing to a new node that will save the occurrences
            addWildcardEdge(motif_node, true);
        }

        ArrayList<Occurrence> Occs = motif_node.getOccs();

        HashMap<Integer, MotifNode> target_nodes = motif_node.getTarget_nodes();

        //the maximal number of different occurrences, of one of the extended motifs
        int max_num_of_diff_occ = -1;
        int num_of_diff_occ = 0;

        MotifNode target_node;
        for (Map.Entry<Integer, MotifNode> entry : target_nodes.entrySet()) {
            int alpha = entry.getKey();
            String alpha_ch = utils.index_to_cog.get(alpha);
            target_node = entry.getValue();

            //go over edges that are not wild cards
            if (alpha!=wildcard_char) {
                num_of_diff_occ = extendMotif(alpha, -1, null, null,
                                    motif_wildcard_count, motif, target_node, motif_node, Occs, motif_length);

                if (num_of_diff_occ > max_num_of_diff_occ) {
                    max_num_of_diff_occ = num_of_diff_occ;
                }
                //For memory saving, remove pointer to target node
                motif_node.addTargetNode(alpha, null);
            }
        }

        //handle wild card edge
        if (motif_node.getType().equals("motif") || motif_wildcard_count < max_motif_wildcard) {
            target_node = motif_node.getTargetNode(wildcard_char);
            if (target_node != null) {
                num_of_diff_occ = extendMotif(wildcard_char, -1, null, null,
                            motif_wildcard_count + 1, motif, target_node, motif_node, Occs, motif_length);
                if (num_of_diff_occ > max_num_of_diff_occ) {
                    max_num_of_diff_occ = num_of_diff_occ;
                }
            }
        }

        return max_num_of_diff_occ;
    }

    private int spellMotifsVirtually(MotifNode motif_node, OccurrenceNode data_node, int data_edge_index,
                                     Edge data_edge,
                                     String motif, int motif_length, int motif_wildcard_count) {

        ArrayList<Occurrence> Occs = motif_node.getOccs();
        //the maximal number of different occurrences, of one of the extended motifs
        int max_num_of_diff_occ = -1;
        int num_of_diff_occ = 0;

        HashMap<Integer, Edge> data_node_edges = null;

        WordArray data_edge_label;
        if (data_edge != null) {
            data_edge_label = data_edge.getLabel();
            if (data_edge_index >= data_edge_label.get_length()) {//we reached to the end of the edge
                data_node = (OccurrenceNode) data_edge.getDest();
                data_edge_index = -1;
                data_edge = null;
            }
        }

        MotifNode target_node;

        if (data_edge_index == -1){
            data_edge_index ++;
            data_node_edges = data_node.getEdges();

            for (Map.Entry<Integer, Edge> entry : data_node_edges.entrySet()) {
                int alpha = entry.getKey();
                String alpha_ch = utils.index_to_cog.get(alpha);
                data_edge = entry.getValue();
                OccurrenceNode data_tree_target_node = (OccurrenceNode) data_edge.getDest();

                if (data_tree_target_node.getCount_by_keys() >= q1) {

                    if (alpha == unkown_cog_char) {
                        spellMotifsVirtually(motif_node, data_node, data_edge_index + 1, data_edge,
                                motif, motif_length, motif_wildcard_count);
                    } else {

                        target_node = new MotifNode("enumeration");
                        target_node.setKey(++last_motif_key);

                        num_of_diff_occ = extendMotif(alpha, data_edge_index + 1, data_node, data_edge,
                                motif_wildcard_count, motif, target_node, motif_node, Occs, motif_length);

                        if (num_of_diff_occ > max_num_of_diff_occ) {
                            max_num_of_diff_occ = num_of_diff_occ;
                        }
                    }
                }

            }
        }else{//data_edge_index>=1 && data_edge_index < data_edge_label.get_length()
            data_edge_label = data_edge.getLabel();
            int alpha = data_edge_label.get_index(data_edge_index);

            OccurrenceNode data_tree_target_node = (OccurrenceNode) data_edge.getDest();

            if (data_tree_target_node.getCount_by_keys() >= q1) {
                if (alpha == unkown_cog_char) {
                    spellMotifsVirtually(motif_node, data_node, data_edge_index + 1, data_edge,
                            motif, motif_length, motif_wildcard_count);
                } else {

                    target_node = new MotifNode("enumeration");
                    target_node.setKey(++last_motif_key);

                    num_of_diff_occ = extendMotif(alpha, data_edge_index + 1, data_node, data_edge,
                            motif_wildcard_count, motif, target_node, motif_node, Occs, motif_length);

                    if (num_of_diff_occ > max_num_of_diff_occ) {
                        max_num_of_diff_occ = num_of_diff_occ;
                    }
                }
            }
        }

        return max_num_of_diff_occ;
    }



    /**
     * Extend motif recursively, if it passes the q1 and q2 - add to motif list
     *
     * @param alpha                the char to append
     * @param motif_wildcard_count how many wildcard in the motif
     * @param motif                previous motif string, before adding alpha. i.e. COG1234|COG2000|
     * @param target_node          node the extended motif
     * @param motif_node            node of motif
     * @param Occ                  the occurrences of motif
     * @param motif_length
     * @return num of different occurrences of extended motif
     */

    private int extendMotif(int alpha, int data_edge_index, OccurrenceNode data_node, Edge data_edge,
                            int motif_wildcard_count, String motif, MotifNode target_node,
                            MotifNode motif_node, ArrayList<Occurrence> Occ, int motif_length) {

        String extended_motif = appendChar(motif, alpha);
        MotifNode extended_motif_node = target_node;
        int extended_motif_length = motif_length + 1;

        //if there is a wildcard in the current motif, have to create a copy of the subtree
        if (motif_wildcard_count > 0 && alpha!=wildcard_char) {
            extended_motif_node = new MotifNode(extended_motif_node);
            motif_node.addTargetNode(alpha, extended_motif_node);
        }

        extended_motif_node.setSubstring(extended_motif);
        extended_motif_node.setSubstring_length(extended_motif_length);

        int exact_occs_count = 0;
        //go over all occurrences of the motif
        for (Occurrence occ : Occ) {
            int curr_exact_occs_count = getExtendedOcc(extended_motif_node, occ, alpha);
            if (curr_exact_occs_count > 0){
                exact_occs_count = curr_exact_occs_count;
            }
        }
        extended_motif_node.setExact_occs_conut(exact_occs_count);

        int diff_occs_count;
        if (count_by_keys){
            diff_occs_count = extended_motif_node.getOccKeysSize();
        }else {
            diff_occs_count = extended_motif_node.getOccsIndexCount();
        }

        if (exact_occs_count >= q1 && diff_occs_count >= q2) {
            int ret;
            if (memory_saving_mode){
                ret = spellMotifsVirtually(extended_motif_node, data_node, data_edge_index, data_edge,
                        extended_motif, extended_motif_length, motif_wildcard_count);
            }else {
                ret = spellMotifs(extended_motif_node, extended_motif, extended_motif_length, motif_wildcard_count);
            }

            if (extended_motif_length - motif_wildcard_count >= min_motif_length) {
                String type = extended_motif_node.getType();
                if (type.equals("motif")) {
                    if (extended_motif_node.getMotifKey()>0) {
                        Motif new_motif = new Motif(extended_motif_node.getMotifKey(), extended_motif,
                                extended_motif.split("\\|"), extended_motif_length, extended_motif_node.getOccKeys(),
                                extended_motif_node.getExact_occs_conut());
                        motifs.put(extended_motif, new_motif);
                    }
                } else if (type.equals("enumeration")) {
                    if (alpha != wildcard_char) {
                        if (!(starts_with_wildcard(extended_motif))) {
                            //make sure that extended_motif is right maximal, if extended_motif has the same number of
                            // occurrences as the longer motif, prefer the longer motif
                            if (diff_occs_count > ret) {// diff_occ_count >= ret always
                                Motif new_motif = new Motif(extended_motif_node.getMotifKey(), extended_motif,
                                        extended_motif.split("\\|"), extended_motif_length,
                                        extended_motif_node.getOccKeys(), extended_motif_node.getExact_occs_conut());
                                motifs.put(extended_motif, new_motif);

                                if (motifs.size() % 1000 == 0){
                                    System.out.println("extracted " + motifs.size() + " so far");
                                }
                            }
                        }
                    } else {
                        if (ret <= 0) {
                            diff_occs_count = -1;
                        } else {
                            diff_occs_count = ret;
                        }
                    }
                }
            }
        }
        return diff_occs_count;
    }

    /**
     * Extends occ, increments error depending on ch
     *
     * @param extended_motif extended motif node
     * @param occ the current occurrence
     * @param ch  the character  of the motif, need to check if the next char on the occurrence is equal
     * @return list of all possible extended occurrences
     */
    private int getExtendedOcc(MotifNode extended_motif, Occurrence occ, int ch) {
        //values of current occurrence
        OccurrenceNode node_occ = occ.getNodeOcc();
        Edge edge_occ = occ.getEdge();
        int edge_index = occ.getEdgeIndex();
        int error = occ.getError();
        int deletions = occ.getDeletions();
        int insertions = occ.getInsertions();

        //values of the extended occurrence
        int next_edge_index = edge_index;
        Edge next_edge_occ = edge_occ;
        OccurrenceNode next_node_occ = node_occ;

        int exact_occs_count = 0;

        //The substring ends at the current node_occ, edge_index = -1
        if (edge_occ == null) {
            //Go over all the edges from node_occ, see if the occurrence can be extended
            HashMap<Integer, Edge> occ_edges = node_occ.getEdges();

            //we can extend the occurrence using all outgoing edges, increment error if needed
            if (ch == wildcard_char) {
                exact_occs_count = addAllOccEdges(false, occ, occ_edges, deletions, error, node_occ,
                        edge_index, ch, extended_motif);
                //extend occ by deletions char
                if (deletions < max_deletion) {
                    addOccToMotif(extended_motif, occ, gap_char, node_occ, edge_occ, edge_index, error, deletions + 1);
                }
            } else {
                if (insertions < max_insertion && occ.getLength() > 0){
                    addAllOccEdges(true, occ, occ_edges, deletions, error, node_occ, edge_index, ch, extended_motif);
                }
                if (error < max_error) {
                    //go over all outgoing edges
                    exact_occs_count = addAllOccEdges(false, occ, occ_edges, deletions, error, node_occ,
                            edge_index, ch, extended_motif);
                    //extend occ by deletions char
                    if (deletions < max_deletion) {
                        addOccToMotif(extended_motif, occ, gap_char, node_occ, edge_occ, edge_index, error,
                                deletions + 1);
                    }
                } else {//error = max error, only edge_occ starting with ch can be added, or deletions
                    next_edge_index++;
                    next_edge_occ = node_occ.getEdge(ch);
                    next_node_occ = node_occ;
                    //Exists an edge_occ starting with ch, add it to occurrences
                    if (next_edge_occ != null) {
                        exact_occs_count = ((OccurrenceNode)next_edge_occ.getDest()).getCount_by_keys();
                        //The label contains only 1 char, go to next node_occ
                        if (next_edge_occ.getLabel().get_length() == 1) {
                            next_node_occ = (OccurrenceNode) next_edge_occ.getDest();
                            next_edge_occ = null;
                            next_edge_index = -1;
                        }
                        addOccToMotif(extended_motif, occ, ch, next_node_occ, next_edge_occ, next_edge_index, error,
                                deletions);
                    } else {
                        //extend occ by deletions char
                        if (deletions < max_deletion) {
                            addOccToMotif(extended_motif, occ, gap_char, node_occ, edge_occ, edge_index, error,
                                    deletions + 1);
                        }
                    }
                }
            }

        } else {//Edge is not null, the substring ends at the middle of the edge_occ, at index edge_index
            WordArray label = edge_occ.getLabel();
            //check the next char on the label, at edge_index+1
            next_edge_index++;
            int next_ch = label.get_index(next_edge_index);

            //If we reached the end of the label by incrementing edge_index, get next node_occ
            if (next_edge_index == label.get_length() - 1) {
                next_node_occ = (OccurrenceNode) edge_occ.getDest();
                next_edge_occ = null;
                next_edge_index = -1;
            }

            if (insertions < max_insertion && occ.getLength() > 0){
                String extended_occ_string = appendChar(occ.getSubstring(), next_ch);
                Occurrence next_occ = new Occurrence(next_node_occ, next_edge_occ, next_edge_index, error, deletions, occ.get_insertion_indexes(), extended_occ_string, occ.getLength() + 1);
                next_occ.add_insertion_index(occ.getLength());
                next_occ.add_all_insertion_indexes(occ.get_insertion_indexes());
                getExtendedOcc(extended_motif, next_occ, ch);
            }

            //if the char is equal add anyway
            if (next_ch == ch) {
                exact_occs_count = ((OccurrenceNode)edge_occ.getDest()).getCount_by_keys();
                addOccToMotif(extended_motif, occ, next_ch, next_node_occ, next_edge_occ, next_edge_index, error,
                        deletions);
            } else {
                if (ch == wildcard_char) {
                    addOccToMotif(extended_motif, occ, next_ch, next_node_occ, next_edge_occ, next_edge_index, error,
                            deletions);
                } else {
                    if (error < max_error) {//check if the error is not maximal, to add not equal char
                        addOccToMotif(extended_motif, occ, next_ch, next_node_occ, next_edge_occ, next_edge_index,
                                error + 1, deletions);
                    }
                    //extend occ by deletions char
                    if (deletions < max_deletion) {
                        addOccToMotif(extended_motif, occ, gap_char, node_occ, edge_occ, edge_index, error, deletions + 1);
                    }
                }
            }
        }
        if (error > 0 || deletions > 0 || insertions > 0){
            exact_occs_count = 0;
        }
        return exact_occs_count;
    }

    /**
     * Go over all outgoing edges of occurrence node
     *
     * @param occ
     * @param occ_edges  edge set of node_occ
     * @param deletions
     * @param error
     * @param node_occ
     * @param edge_index
     * @param ch
     * @param motif
     */
    private int addAllOccEdges(Boolean make_insertion, Occurrence occ, HashMap<Integer, Edge> occ_edges, int deletions,
                               int error, OccurrenceNode node_occ, int edge_index, int ch, MotifNode motif) {
        int curr_error = error;
        int next_edge_index;
        int exact_occs_count = 0;

        //boolean exist_equal_char = false;

        //go over all outgoing edges
        for (Map.Entry<Integer, Edge> entry : occ_edges.entrySet()) {
            int next_ch = entry.getKey();
            Edge next_edge = entry.getValue();
            OccurrenceNode next_node = node_occ;

            if (ch == next_ch) {
                curr_error = error;
                exact_occs_count = ((OccurrenceNode)next_edge.getDest()).getCount_by_keys();
            } else {
                if (ch != wildcard_char) {//Substitution - the chars are different, increment error
                    curr_error = error + 1;
                }
            }

            //The label contains only 1 char, go to next node_occ
            if (next_edge.getLabel().get_length() == 1) {
                next_node = (OccurrenceNode) next_edge.getDest();
                next_edge = null;
                next_edge_index = -1;
            } else {//label contains more the 1 char, increment edge_index
                next_edge_index = edge_index + 1;
            }


            if (make_insertion) {
                String extended_occ_string = appendChar(occ.getSubstring(), next_ch);
                Occurrence next_occ = new Occurrence(next_node, next_edge, next_edge_index, error, deletions, occ.get_insertion_indexes(), extended_occ_string, occ.getLength() + 1);
                next_occ.add_insertion_index(occ.getLength());
                getExtendedOcc(motif, next_occ, ch);
            } else {
                addOccToMotif(motif, occ, next_ch, next_node, next_edge, next_edge_index, curr_error, deletions);
            }
        }
        return exact_occs_count;
    }

    /**
     * @param extended_motif
     * @param occ
     * @param next_ch
     * @param next_node
     * @param next_edge
     * @param next_edge_index
     * @param next_error
     * @param next_deletions
     * @throws Exception
     */
    private void addOccToMotif(MotifNode extended_motif, Occurrence occ, int next_ch, OccurrenceNode next_node,
                               Edge next_edge, int next_edge_index, int next_error, int next_deletions) {
        String extended_occ_string = appendChar(occ.getSubstring(), next_ch);
        Occurrence next_occ = new Occurrence(next_node, next_edge, next_edge_index, next_error, next_deletions, occ.get_insertion_indexes(), extended_occ_string, occ.getLength()+1);
        extended_motif.addOcc(next_occ, max_insertion);
    }


    /**
     * @return
     */
    public ArrayList<Motif> getMotifs() {
        return new ArrayList<Motif>(motifs.values());
    }


}
