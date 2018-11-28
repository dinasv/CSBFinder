package Core.SuffixTrees;

import Core.Genomes.Alphabet;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;
import Core.Genomes.WordArray;

import java.util.List;

/**
 */
public class PatternsTree {

    Trie patternsTree;
    GenomesInfo genomesInfo;

    public PatternsTree(List<Pattern> patterns, GenomesInfo gi){
        patternsTree = new Trie(TreeType.STATIC);
        this.genomesInfo = gi;
        buildPatternsTree(patterns);
    }

    /**
     * Builds a Trie of patterns, given in a list of patterns.
     * @param patternTree the patterns are inserted to this GST
     */
    public void buildPatternsTree(List<Pattern> patterns) {

        for (Pattern pattern: patterns){
            WordArray word = genomesInfo.createWordArray(pattern.getPatternGenes(), true);
            patternsTree.put(word, pattern.getPatternId(), Alphabet.UNK_CHAR_INDEX);
        }
    }

    public Trie getTrie(){
        return patternsTree;
    }
}
