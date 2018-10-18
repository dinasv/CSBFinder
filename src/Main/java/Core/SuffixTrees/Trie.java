package Core.SuffixTrees;
import Genomes.WordArray;

/**
 * Represents a Generalized Trie - each edge has only 1 char
 * A string inserted to the Trie is the concatenation of labels from the root to a node with a key of that string
 */
public class Trie {

    /**
     * The root of the suffix tree
     */
    public final PatternNode root;
    private boolean debug;
    private TreeType type;
    private int lastKey;
    private int UNK_CHAR_INDEX;

    public Trie(TreeType type){
        root = new PatternNode(type);

        debug = false;
        this.type = type;

        lastKey = 0;
    }

    public PatternNode getRoot(){
        return root;
    }

    public PatternNode put(WordArray str, int key, int UNK_CHAR_INDEX){
        PatternNode last_node = put(str, root, true, UNK_CHAR_INDEX);
        if (last_node != null && type == TreeType.STATIC){
            last_node.setKey(key);
        }
        return last_node;
    }

    public PatternNode put(WordArray str, int UNK_CHAR_INDEX){
        return put(str, root, true, UNK_CHAR_INDEX);
    }

    public PatternNode addNode(int ch, PatternNode src_node){
        PatternNode target_node = src_node.getTargetNode(ch);
        if (target_node == null){
            target_node = new PatternNode(type);
            src_node.addTargetNode(ch, target_node);
        }
        return target_node;
    }

    public PatternNode put(WordArray str, PatternNode src_node, boolean include_unknown_char, int UNK_CHAR_INDEX){
        if (str.get_length() > 0) {
            PatternNode curr_node = src_node;
            for (int i = 0; i < str.get_length(); i++) {
                int str_char = str.get_index(i);
                if (include_unknown_char || (!include_unknown_char && str_char != UNK_CHAR_INDEX )) {
                    curr_node = addNode(str_char, curr_node);

                    if (type == TreeType.VIRTUAL && curr_node.getPatternKey() <= 0) {
                        curr_node.setKey(++lastKey);
                    }
                }
            }
            return curr_node;
        }
        return null;
    }

    /**
     * Adds str to the Trie under the given key.
     *
     * @param str the string added to the tree
     * @param key the key of that string
     * @throws IllegalStateException if an invalid index is passed as input
     */
    public PatternNode put(WordArray str, int key, PatternNode extended_str_node) throws IllegalStateException {
        if (str.get_length() > 0) {
            PatternNode curr_node = root;
            int index = 0;
            //as long as the infix of str is in the tree, go on
            for (int i = 0; i < str.get_length(); i++) {
                int str_char = str.get_index(i);

                PatternNode target_node = curr_node.getTargetNode(str_char);
                //there is no outgoing edge with str_char
                if (target_node == null) {
                    break;
                }
                curr_node = target_node;

                if (type == TreeType.VIRTUAL && curr_node.getPatternKey() <= 0){
                    curr_node.setKey(++lastKey);
                }

                //advance the node
                if (extended_str_node != null) {
                    extended_str_node = extended_str_node.getTargetNode(str_char);
                    extended_str_node.setSuffix(curr_node);

                }

                index++;
            }

            //insert to the tree the rest of str
            for (int i = index; i < str.get_length(); i++) {
                int str_char = str.get_index(i);

                PatternNode next_node = new PatternNode(type);
                curr_node.addTargetNode(str_char, next_node);

                curr_node = next_node;

                if (type == TreeType.VIRTUAL && curr_node.getPatternKey() <= 0){
                    curr_node.setKey(++lastKey);
                }

                if (extended_str_node != null) {
                    extended_str_node = extended_str_node.getTargetNode(str_char);
                    extended_str_node.setSuffix(curr_node);
                }
            }


            if (extended_str_node == null && type == TreeType.STATIC) {
                curr_node.setKey(key);
            }

            /*
            //continue recursively adding all suffixes
            if (type.equals("enumeration")) {
                if (index != str.get_length()) {
                    //put the suffix of str
                    extended_str_node = root.getTargetNode(str.get_index(0));
                    extended_str_node.setSuffix(root);
                    str = new WordArray(str);
                    str.add_to_start_index(1);

                    put(str, key, extended_str_node);
                }
            }*/

            return curr_node;
        }
        return null;
    }
    public Pair<PatternNode, Integer> search(WordArray str){
        return search(str, root);
    }

    /**
     * Returns the deepest node of which the concatenation of labels to this node is an infix of str (str[0:index])
     * @param str
     * @return
     */
    public Pair<PatternNode, Integer> search(WordArray str, PatternNode src_node){
        PatternNode curr_node = src_node;
        int index = 0;
        for (int i = 0; i < str.get_length(); i++) {
            int str_char = str.get_index(i);
            PatternNode target_node = curr_node.getTargetNode(str_char);

            if (target_node == null){
                break;
            }
            curr_node = target_node;
            index++;
        }
        return new Pair(curr_node, index);
    }


    /**
     * A private class used to return a tuples of two elements
     */
    public class Pair<A, B> {

        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }

    public TreeType getType(){
        return type;
    }
}
