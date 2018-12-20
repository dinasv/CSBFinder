package Core.SuffixTreePatternFinder.SuffixTrees;

import Core.Genomes.*;
import Core.Patterns.Pattern;

import java.util.List;

/**
 */
public class PatternsTree {

    Trie patternsTree;
    GenomesInfo genomesInfo;
    boolean nonDirectons;

    public PatternsTree(List<Pattern> patterns, GenomesInfo gi, boolean nonDirectons){
        patternsTree = new Trie(TreeType.STATIC);
        this.genomesInfo = gi;
        this.nonDirectons = nonDirectons;
        buildPatternsTree(patterns);
    }

    /**
     * Builds a Trie of patterns, given in a list of patterns.
     * @param patterns the patterns are inserted to this GST
     */
    private void buildPatternsTree(List<Pattern> patterns) {

        for (Pattern pattern: patterns){
            if (pattern.getPatternId() != -1){
                putWordInTree(pattern.getPatternGenes(), pattern.getPatternId());
            }else {
                if (nonDirectons) {
                    putWordInTree(pattern.getPatternGenes());

                    Replicon replicon = new Replicon();
                    replicon.addAllGenes(pattern.getPatternGenes());
                    replicon.reverseCompliment();
                    putWordInTree(replicon.getGenes());
                } else {
                    Replicon replicon = new Replicon();
                    replicon.addAllGenes(pattern.getPatternGenes());
                    List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

                    for (Directon directon : directons) {
                        putWordInTree(directon.getGenes());
                    }
                }
            }
        }
    }

    private void putWordInTree(List<Gene> genes){
        WordArray word = genomesInfo.createWordArray(genes);
        patternsTree.put(word, Alphabet.UNK_CHAR_INDEX);
    }

    private void putWordInTree(List<Gene> genes, int id){
        WordArray word = genomesInfo.createWordArray(genes);
        patternsTree.put(word, id, Alphabet.UNK_CHAR_INDEX);
    }

    public Trie getTrie(){
        return patternsTree;
    }
}
