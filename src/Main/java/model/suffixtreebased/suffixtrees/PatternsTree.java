package model.suffixtreebased.suffixtrees;

import model.genomes.*;
import model.patterns.Pattern;

import java.util.Arrays;
import java.util.List;

/**
 */
public class PatternsTree {

    Trie patternsTree;
    GenomesInfo genomesInfo;
    boolean crossStrand;

    public PatternsTree(List<Pattern> patterns, GenomesInfo gi, boolean crossStrand){
        patternsTree = new Trie(TreeType.STATIC);
        this.genomesInfo = gi;
        this.crossStrand = crossStrand;
        buildPatternsTree(patterns);
    }

    /**
     * Builds a Trie of patterns, given in a list of patterns.
     * @param patterns the patterns are inserted to this GST
     */
    private void buildPatternsTree(List<Pattern> patterns) {

        for (Pattern pattern: patterns){
            List<Gene> patternGenes = Arrays.asList(pattern.getPatternGenes());
            if (pattern.getPatternId() != null){
                putWordInTree(patternGenes, pattern.getPatternId());
            }else {
                if (crossStrand) {
                    putWordInTree(patternGenes);

                    putWordInTree(Replicon.reverseComplementGenes(patternGenes));
                } else {
                    Replicon replicon = new Replicon();
                    replicon.addAllGenes(patternGenes);
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
        patternsTree.putWithSuffix(word, Alphabet.UNK_CHAR_INDEX);
    }

    private void putWordInTree(List<Gene> genes, String id){
        WordArray word = genomesInfo.createWordArray(genes);
        patternsTree.put(word, id, Alphabet.UNK_CHAR_INDEX);
    }

    public Trie getTrie(){
        return patternsTree;
    }
}
