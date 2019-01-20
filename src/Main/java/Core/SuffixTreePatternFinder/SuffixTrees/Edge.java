package Core.SuffixTreePatternFinder.SuffixTrees;

import Core.Genomes.WordArray;

import java.io.Serializable;

/**
 * Represents an Edge in the Suffix Tree and a Trie.
 * It has a label and a destination Node
 */
public class Edge{
    private WordArray label;
    private InstanceNode dest;

    public WordArray getLabel() {
        return label;
    }

    public void setLabel(WordArray label) {
        this.label = label;
    }

    public InstanceNode getDest() {
        return dest;
    }

    public void setDest(InstanceNode dest) {
        this.dest = dest;
    }

    public Edge(WordArray label, InstanceNode dest) {
        this.label =  new WordArray(label);
        this.dest = dest;
    }

    public Edge(Edge other){
        this.label = new WordArray(other.label);
        this.dest = other.dest;
    }

}
