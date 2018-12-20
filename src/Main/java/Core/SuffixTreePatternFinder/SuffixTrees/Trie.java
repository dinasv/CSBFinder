package Core.SuffixTreePatternFinder.SuffixTrees;
import Core.Genomes.WordArray;

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

    public Trie(TreeType type){
        root = new PatternNode(type);

        debug = false;
        this.type = type;

        lastKey = 0;
    }

    public PatternNode getRoot(){
        return root;
    }

    public void put(WordArray str, int key, int UNK_CHAR_INDEX){
        PatternNode lastNode = put(str, root, false, UNK_CHAR_INDEX);
        if (lastNode != null && type == TreeType.STATIC){
            lastNode.setKey(key);
        }
    }

    public PatternNode put(WordArray str, int UNK_CHAR_INDEX){
        return put(str, root, false, UNK_CHAR_INDEX);
    }

    public PatternNode addNode(int ch, PatternNode src_node){
        PatternNode target_node = src_node.getTargetNode(ch);
        if (target_node == null){
            target_node = new PatternNode(type);
            src_node.addTargetNode(ch, target_node);
        }
        return target_node;
    }

    public PatternNode put(WordArray str, PatternNode srcNode, boolean includeUnknownChar, int UNK_CHAR_INDEX){
        PatternNode currNode = null;
        if (str.getLength() > 0) {
            currNode = srcNode;
            for (int i = 0; i < str.getLength(); i++) {
                int strChar = str.getLetter(i);
                if (includeUnknownChar || (!includeUnknownChar && strChar != UNK_CHAR_INDEX )) {
                    currNode = addNode(strChar, currNode);

                    if (currNode.getPatternKey() <= 0) {
                        currNode.setKey(++lastKey);
                    }
                }
            }
        }

        if (str.getLength() > 1) {
            //put the suffix of str
            str = new WordArray(str);
            str.addToStartIndex(1);

            put(str, root, includeUnknownChar, UNK_CHAR_INDEX);
        }
        return currNode;
    }

    /**
     * Adds str to the Trie under the given key.
     *
     * @param str the string added to the tree
     * @param key the key of that string
     * @throws IllegalStateException if an invalid index is passed as input
     */
    public PatternNode put(WordArray str, int key, PatternNode extendedStrNode) throws IllegalStateException {
        if (str.getLength() > 0) {
            PatternNode currNode = root;
            int index = 0;
            //as long as the infix of str is in the tree, go on
            for (int i = 0; i < str.getLength(); i++) {
                int letter = str.getLetter(i);

                PatternNode target_node = currNode.getTargetNode(letter);
                //there is no outgoing edge with letter
                if (target_node == null) {
                    break;
                }
                currNode = target_node;

                if (currNode.getPatternKey() <= 0){
                    currNode.setKey(++lastKey);
                }

                //advance the node
                if (extendedStrNode != null) {
                    extendedStrNode = extendedStrNode.getTargetNode(letter);
                    extendedStrNode.setSuffix(currNode);
                }

                index++;
            }

            //insert to the tree the rest of str
            for (int i = index; i < str.getLength(); i++) {
                int letter = str.getLetter(i);

                PatternNode nextNode = new PatternNode(type);
                currNode.addTargetNode(letter, nextNode);

                currNode = nextNode;

                if (currNode.getPatternKey() <= 0){
                    currNode.setKey(++lastKey);
                }

                if (extendedStrNode != null) {
                    extendedStrNode = extendedStrNode.getTargetNode(letter);
                    extendedStrNode.setSuffix(currNode);
                }
            }


            if (extendedStrNode == null && type == TreeType.STATIC) {
                currNode.setKey(key);
            }


            //continue recursively adding all suffixes
            //if (type.equals("enumeration")) {
            if (index != str.getLength()) {
                //put the suffix of str
                extendedStrNode = root.getTargetNode(str.getLetter(0));
                extendedStrNode.setSuffix(root);
                str = new WordArray(str);
                str.addToStartIndex(1);

                put(str, key, extendedStrNode);
            }
            //}

            return currNode;
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
        for (int i = 0; i < str.getLength(); i++) {
            int str_char = str.getLetter(i);
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
