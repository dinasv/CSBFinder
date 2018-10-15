package Core.SuffixTrees;

import Genomes.WordArray;

import java.io.Serializable;

/**
 * Represents an Edge in the Suffix Tree and a Trie.
 * It has a label and a destination Node
 */
public class Edge implements Serializable{
    private WordArray label;
    private SuffixNode dest;

    public WordArray getLabel() {
        return label;
    }

    public void setLabel(WordArray label) {
        this.label = label;
    }

    public SuffixNode getDest() {
        return dest;
    }

    public void setDest(SuffixNode dest) {
        this.dest = dest;
    }

    public Edge(WordArray label, SuffixNode dest) {
        this.label =  new WordArray(label);
        this.dest = dest;
    }

    public Edge(Edge other){
        this.label = new WordArray(other.label);
        this.dest = other.dest;
    }

}
