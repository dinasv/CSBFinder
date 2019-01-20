package Core.SuffixTreePatternFinder.SuffixTrees;

import Core.Patterns.InstanceLocation;
import Core.Genomes.WordArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A modified version of the Suffix Tree code from https://github.com/abahgat/suffixtree
 * Copyright 2012 Alessandro Bahgat Shehata
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * A Generalized Suffix Tree, based on the Ukkonen's paper "On-line construction of suffix trees"
 * http://www.cs.helsinki.fi/u/ukkonen/SuffixT1withFigs.pdf
 *
 * Allows for fast storage and fast(er) retrieval by creating a tree-based index out of a set of strings.
 * Unlike common suffix trees, which are generally used to build an index out of one (very) long string,
 * a Generalized Suffix Tree can be used to build an index over many strings.

 * Its Core operations are putWithSuffix and search:
 * Put adds the given key to the index, allowing for later retrieval of the given value.
 * Search can be used to retrieve the set of all the values that were putWithSuffix in the index with keys that contain a given input.
 *
 * In particular, after putWithSuffix(K, V), search(H) will return a set containing V for any string H that is substring of K.
 *
 * The overall complexity of the retrieval operation (search) is O(m) where m is the length of the string to search within the index.
 *
 * Although the implementation is based on the original design by Ukkonen, there are a few aspects where it differs significantly.
 * 
 * The tree is composed of a set of nodes and labeled edges. The labels on the edges can have any length as long as it's greater than 0.
 * The only constraint is that no two edges going out from the same node will start with the same character.
 * 
 * Because of this, a given (startNode, stringSuffix) pair can denote a unique path within the tree, and it is the path (if any) that can be
 * composed by sequentially traversing all the edges (e1, e2, ...) starting from startNode such that (e1.label + e2.label + ...) is equal
 * to the stringSuffix.
 * See the search method for details.
 * 
 * The union of all the edge labels from the root to a given leaf node denotes the set of the strings explicitly contained within the GST.
 * In addition to those Strings, there are a set of different strings that are implicitly contained within the GST, and it is composed of
 * the strings built by concatenating e1.label + e2.label + ... + $end, where e1, e2, ... is a proper path and $end is prefix of any of
 * the labels of the edges starting from the last node of the path.
 *
 * This kind of "implicit path" is important in the testAndSplit method.
 *  
 */
public class GeneralizedSuffixTree  implements Serializable{
    /**
     * The index of the last item that was added to the GST
     */
    private int last = 0;
    /**
     * The root of the suffix tree
     */
    private final InstanceNode root;
    /**
     * The last leaf that was added during the update operation
     */
    private InstanceNode activeLeaf ;

    //The string from the root to this node is the full last added string (using "putWithSuffix")
    private InstanceNode fullStringNode;

    public GeneralizedSuffixTree() {
        root = new InstanceNode();

        activeLeaf = root;
    }

    /**
     * Searches for the given word within the GST.
     *
     * Returns all the indexes for which the key contains the <tt>word</tt> that was
     * supplied as input.
     *
     * @param word the key to search for
     * @return the collection of indexes associated with the input <tt>word</tt>
     */
    /*public Collection<Integer> search(WordArray word) {
        return search(word, -1);
    }*/

    /**
     * Searches for the given word within the GST and returns a map of locations of the word
     * Must be called after computeCount()
     *
     * @param word the key to search for
     * @return
     */
    public Map<Integer, List<InstanceLocation>> search(WordArray word) throws IllegalStateException{
        InstanceNode tmpNode = searchNode(word);
        if (tmpNode == null) {
            return Collections.EMPTY_MAP;
        }
        return tmpNode.getGenomeToLocationsInSubtree();
    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches.
     *
     * @param word the key to search for
     * @param to the max number of results to return
     * @return at most <tt>results</tt> values for the given word
     * @see GeneralizedSuffixTree#ResultInfo
     */
    /*public ResultInfo searchWithCount(WordArray word, int to) {
        Node tmpNode = searchNode(word);
        if (tmpNode == null) {
            return new ResultInfo(Collections.EMPTY_LIST, 0);
        }

        return new ResultInfo(tmpNode.getData(to), tmpNode.getCountInstancePerGenome());
    }*/

    /**
     * Returns the tree node (if present) that corresponds to the given string.
     */
    private InstanceNode searchNode(WordArray word) {
        /*
         * Verifies if exists a path from the root to a node such that the concatenation
         * of all the labels on the path is a superstring of the given word.
         * If such a path is found, the last node on it is returned.
         */
        InstanceNode currentNode = root;
        Edge currentEdge;

        for (int i = 0; i < word.getLength(); ++i) {
            int bs = word.getLetter(i);
            // follow the edge corresponding to this char
            currentEdge = currentNode.getEdge(bs);
            if (null == currentEdge) {
                // there is no edge starting with this char
                return null;
            } else {
                WordArray label = currentEdge.getLabel();
                int word_length = word.getLength();
                int lenToMatch = Math.min(word_length - i, label.getLength());

                if (!word.compareTo(label, i, lenToMatch)) {
                    // the label on the edge does not correspond to the one in the string to search
                    return null;
                }

                if (label.getLength() >= word.getLength() - i) {
                    return currentEdge.getDest();
                } else {
                    // advance to next node
                    currentNode = currentEdge.getDest();
                    i += lenToMatch - 1;
                }
            }
        }

        return null;
    }


    /**
     * Adds the specified <tt>subsstring</tt> to the GST under the given <tt>genomeId</tt>.
     *
     *
     * @param substring the substring (word) that will be added to the tree
     * @param genomeId a unique id of the genome in which {@code substring} is a substring
     * @param instanceLocation information regarding the location of substring in the genome
     */
    public void put(WordArray substring, InstanceLocation instanceLocation) {

        // reset activeLeaf
        activeLeaf = root;

        fullStringNode = null;

        InstanceNode s = root;

        // proceed with tree construction (closely related to procedure in
        // Ukkonen's paper)
        WordArray text = new WordArray(substring.getWordArray(), 0, 0);

        // iterate over the string, one char at a time
        for (int i = 0; i < substring.getLength(); i++) {
            // line 6
            text.addToEndIndex(1);

            // line 7: update the tree with the new transitions due to this new char
            WordArray rest = new WordArray(substring.getWordArray(), i, substring.getEndIndex());
            WordArray text_copy = new WordArray(text.getWordArray(), text.getStartIndex(), text.getEndIndex());


            Pair<InstanceNode, WordArray> active = update(s, text_copy, rest);
            // line 8: make sure the active pair is canonical
            active = canonize(active.getFirst(), active.getSecond());

            //check what canonize returns
            text = active.getSecond();
            s = active.getFirst();

        }

        // add leaf suffix link, if necessary
        if (null == activeLeaf.getSuffix() && activeLeaf != root && activeLeaf != s) {
            activeLeaf.setSuffix(s);
        }

        //add recursively the genomeId and indexes to the nodes corresponding to this string

        fullStringNode.addLocationToGenome(instanceLocation);


    }

    /**
     * Tests whether the string stringPart + t is contained in the subtree that has inputs as root.
     * If that's not the case, and there exists a path of edges e1, e2, ... such that
     *     e1.label + e2.label + ... + $end = stringPart
     * and there is an edge g such that
     *     g.label = stringPart + rest
     * 
     * Then g will be split in two different edges, one having $end as label, and the other one
     * having rest as label.
     *
     * @param inputs the starting node
     * @param stringPart the string to search
     * @param t the following character
     * @param remainder the remainder of the string to addGene to the index
     * @return a pair containing
     * true/false depending on whether (stringPart + t) is contained in the subtree starting in inputs
     * the last node that can be reached by following the path denoted by stringPart starting from inputs
     *         
     */
    private Pair<Boolean, InstanceNode> testAndSplit(final InstanceNode inputs, final WordArray stringPart, final int t,
                                                   final WordArray remainder) {

        // descend the tree as far as possible
        Pair<InstanceNode, WordArray> ret = canonize(inputs, stringPart);
        InstanceNode s = ret.getFirst();
        WordArray str = ret.getSecond();

        if (str.getLength() > 0) {
            Edge g = s.getEdge(str.getLetter(0));

            WordArray label = g.getLabel();
            // must see whether "str" is substring of the label of an edge
            if ((label.getLength() > str.getLength()) && (label.getLetter(str.getLength()) == t)) {
                return new Pair<>(true, s);
            } else {
                // need to split the edge

                WordArray newlabel = getSubstring(label, str.getLength(), label.getLength());
                WordArray str_copy = str;

                assert (label.startsWith(str_copy) );

                // build a new node
                InstanceNode r = new InstanceNode();
                // build a new edge
                Edge newedge = new Edge(str_copy, r);

                g.setLabel(newlabel);

                // link s -> r
                r.addEdge(newlabel.getLetter(0), g);
                s.addEdge(str.getLetter(0), newedge);

                return new Pair<>(false, r);
            }

        } else {
            Edge e = s.getEdge(t);
            if (null == e) {
                // if there is no t-transtion from s
                return new Pair<>(false, s);
            } else {

                if (remainder.equal(e.getLabel())) {

                    // update payload of destination node
                    if (fullStringNode == null){
                        fullStringNode = e.getDest();
                    }
                    return new Pair<>(true, s);
                } else if (remainder.startsWith(e.getLabel())) {
                    return new Pair<>(true, s);
                } else if (e.getLabel().startsWith(remainder)){
                    // need to split as above
                    InstanceNode newNode = new InstanceNode();

                    if (fullStringNode == null){
                        fullStringNode = newNode;
                    }
                    
                    Edge newEdge = new Edge(copySeq(remainder), newNode);

                    WordArray edgeLabel = e.getLabel();

                    WordArray edgeLabelSubstr = getSubstring(edgeLabel, remainder.getLength(), edgeLabel.getLength());

                    e.setLabel( edgeLabelSubstr );
                    newNode.addEdge(edgeLabelSubstr.getLetter(0), e);

                    s.addEdge(t, newEdge);

                    return new Pair<>(false, s);
                } else {
                    // they are different words. No prefix. but they may still share some common substr
                    return new Pair<>(true, s);
                }
            }
        }

    }



    /**
     * Return a (Node, String) (n, remainder) pair such that n is a farthest descendant of
     * s (the input node) that can be reached by following a path of edges denoting
     * a prefix of inputstr and remainder will be string that must be
     * appended to the concatenation of labels from s to n to get inpustr.
     */
    private Pair<InstanceNode, WordArray> canonize(final InstanceNode s, final WordArray inputstr) {

        if (inputstr.getLength()==0) {
            return new Pair<>(s, inputstr);
        } else {
            InstanceNode currentNode = s;

            WordArray str = copySeq(inputstr);
            Edge g = s.getEdge(str.getLetter(0));
            // descend the tree as long as a proper label is found
            while (g != null && str.startsWith(g.getLabel())) {
                str.addToStartIndex(g.getLabel().getLength());
                currentNode = g.getDest();
                if (str.getLength() > 0) {
                    g = currentNode.getEdge(str.getLetter(0));
                }
            }

            return new Pair<>(currentNode, str);
        }
    }


        /**
         * Updates the tree starting from inputNode and by adding stringPart.
         *
         * Returns a reference (Node, String) pair for the string that has been added so far.
         * This means:
         * - the Node will be the Node that can be reached by the longest path string (S1)
         *   that can be obtained by concatenating consecutive edges in the tree and
         *   that is a substring of the string added so far to the tree.
         * - the String will be the remainder that must be added to S1 to get the string
         *   added so far.
         *
         * @param inputNode the node to start from
         * @param stringPart the string to addGene to the tree
         * @param rest the rest of the string
         */
        private Pair<InstanceNode, WordArray> update (InstanceNode inputNode, WordArray stringPart, WordArray rest){
            InstanceNode s = inputNode;

            WordArray tempstr = stringPart;

            int newChar = stringPart.getLetter(stringPart.getLength() - 1);

            // line 1
            InstanceNode oldroot = root;

            Pair<Boolean, InstanceNode> ret = testAndSplit(s, getSubstring(tempstr, 0, tempstr.getLength()-1), newChar, rest/*, key, index*/);

            InstanceNode r = ret.getSecond();
            boolean endpoint = ret.getFirst();

            InstanceNode leaf;
            // line 2
            while (!endpoint) {
                // line 3
                Edge tempEdge = r.getEdge(newChar);
                if (null != tempEdge) {
                    // such a node is already present. This is one of the Core differences from Ukkonen's case:
                    // the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
                    leaf = tempEdge.getDest();
                } else {
                    // must build a new leaf
                    leaf = new InstanceNode();

                    if (fullStringNode == null){
                        fullStringNode = leaf;
                    }

                    Edge newedge = new Edge(rest, leaf);
                    r.addEdge(newChar, newedge);
                }

                // update suffix link for newly created leaf
                if (activeLeaf != root) {
                    activeLeaf.setSuffix(leaf);
                }
                activeLeaf = leaf;

                // line 4
                if (oldroot != root) {
                    oldroot.setSuffix(r);
                }

                // line 5
                oldroot = r;

                // line 6
                if (null == s.getSuffix()) { // root node
                    assert (root == s);
                    // this is a special case to handle what is referred to as node _|_ on the paper
                    tempstr.setStartIndex(tempstr.getStartIndex() + 1);

                } else {
                    // cut last char from tempstr and canonize
                    Pair<InstanceNode, WordArray> canret = canonize(s.getSuffix(), safeCutLastChar(tempstr));
                    s = canret.getFirst();
                    tempstr = canret.getSecond();
                    tempstr.addToEndIndex(1);
                }

                ret = testAndSplit(s, safeCutLastChar(tempstr), newChar, rest/*, key, index*/);
                r = ret.getSecond();
                endpoint = ret.getFirst();

            }

            // line 8
            if (oldroot != root) {
                oldroot.setSuffix(r);
            }
            oldroot = root;

            return new Pair<>(s, tempstr);
        }


    public InstanceNode getRoot() { return root; }

    public int computeCount() {
        return root.computeAndCacheCount();
    }


    private WordArray copySeq(WordArray seq){
        return new WordArray(seq.getWordArray(), seq.getStartIndex(), seq.getEndIndex());
    }
    //works as the regular substring
    private WordArray getSubstring(WordArray seq, int start_index, int end_index) {
        return new WordArray(seq.getWordArray(), seq.getStartIndex()+start_index, seq.getStartIndex() + end_index );
    }

    private WordArray safeCutLastChar(WordArray seq) {
        WordArray seq_substr = new WordArray(seq.getWordArray(), seq.getStartIndex(), seq.getEndIndex());
        if (seq_substr.getLength() > 0) {
            seq_substr.addToEndIndex(-1);
        }
        return seq_substr;
    }


    /**
     * A private class used to return a tuples of two elements
     */
    private class Pair<A, B> {

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
}
