package Core.SuffixTrees;

import Core.Genomes.*;

import java.util.List;

/**
 */
public class DatasetTree {

    private GeneralizedSuffixTree datasetTree;
    public final boolean nonDirectons;

    public DatasetTree(boolean nonDirectons, GenomesInfo gi){
        datasetTree = new GeneralizedSuffixTree();

        this.nonDirectons = nonDirectons;

        buildTree(gi);
    }

    public GeneralizedSuffixTree getSuffixTree(){
        return datasetTree;
    }

    /**
     * Builds a Trie of patterns, given in a file.
     * @param pattern_tree the patterns are inserted to this GST
     * @return True if succesful, False if exception occurred
     */
    public void buildPatternsTree(List<Pattern> patterns, Trie pattern_tree, GenomesInfo gi) {

        for (Pattern pattern: patterns){
            WordArray word = createWordArray(pattern.getPatternGenes(), gi);
            pattern_tree.put(word, pattern.getPatternId(), gi.UNK_CHAR_INDEX);
        }
    }


    /**
     * Converts an array of strings to wordArray, using charToIndex
     * @param genes contains the characters comprising this str
     * @return WordArray representing this str
     */
    private WordArray createWordArray(List<Gene> genes, GenomesInfo gi){
        int[] word = new int[genes.size()];
        int i = 0;
        for(Gene gene: genes){
            Gene ch;

            if (nonDirectons){
                ch = gene;
            }else{
                ch = new Gene(gene.getCogId(), Strand.FORWARD);
            }

            int char_index = -1;
            if (gi.charToIndex.containsKey(ch)) {
                char_index = gi.charToIndex.get(ch);
            } else {
                char_index = gi.indexToChar.size();
                gi.indexToChar.add(ch);
                gi.charToIndex.put(ch, char_index);
            }

            word[i] = char_index;
            i++;
        }

        return new WordArray(word);
    }


    /**
     * Turns a genomicSegment (replicon or directon) into an array of genes and puts in in the @datasetTree
     * @param genomicSegment
     * @param datasetTree
     * @param currGenomeIndex
     */
    private void putWordInDataTree(GenomicSegment genomicSegment, //GeneralizedSuffixTree datasetTree,
                                          int currGenomeIndex, GenomesInfo genomesInfo){

        List<Gene> genes = genomicSegment.getGenes();
        WordArray cog_word = createWordArray(genes, genomesInfo);
        InstanceLocation instanceLocation = new InstanceLocation(genomicSegment.getId(), currGenomeIndex,
                genomicSegment.getStartIndex(),
                genomicSegment.getStartIndex() + genomicSegment.size(), genomicSegment.getStrand());
        if (genomicSegment.getStrand() == Strand.REVERSE){
            instanceLocation.switchStartEndIndex();
        }
        datasetTree.put(cog_word, currGenomeIndex, instanceLocation);

        genomesInfo.countParalogsInSeqs(cog_word, currGenomeIndex);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param replicon
     * @param datasetTree
     * @param currGenomeIndex
     * @return
     */
    private int updateDataTree(Replicon replicon, int currGenomeIndex, GenomesInfo gi){

        int replicon_length = 0;
        if (nonDirectons) {//put replicon and its reverseCompliment compliment

            putWordInDataTree(replicon, currGenomeIndex, gi);

            //reverseCompliment replicon
            replicon = new Replicon(replicon);
            replicon.reverseCompliment();
            putWordInDataTree(replicon, currGenomeIndex, gi);

            replicon_length += replicon.size() * 2;

        }else{//split replicon to directons

            List<Directon> directons = replicon.splitRepliconToDirectons(gi.UNK_CHAR);

            for (Directon directon: directons){
                replicon_length += directon.size();
                putWordInDataTree(directon, currGenomeIndex, gi);
            }

        }
        return replicon_length;
    }

    private void buildTree(GenomesInfo gi){
        for (Genome genome : gi.getGenomes()) {

            int genome_index = genome.getId();
            for (Replicon replicon: genome.getReplicons()) {
                updateDataTree(replicon, genome_index, gi);
            }
        }
    }

}
