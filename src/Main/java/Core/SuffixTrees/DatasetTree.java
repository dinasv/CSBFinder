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

            Gene ch = gene;
            if (!nonDirectons){
                ch = new Gene(gene.getCogId(), Strand.INVALID);
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
    private void putWordInDataTree(GenomicSegment genomicSegment,
                                          int currGenomeIndex, GenomesInfo genomesInfo){

        List<Gene> genes = genomicSegment.getGenes();
        WordArray wordArray = createWordArray(genes, genomesInfo);
        InstanceLocation instanceLocation = new InstanceLocation(genomicSegment.getId(), currGenomeIndex,
                genomicSegment.getStartIndex(),
                genomicSegment.getStartIndex() + genomicSegment.size(), genomicSegment.getStrand());
        if (genomicSegment.getStrand() == Strand.REVERSE){
            instanceLocation.switchStartEndIndex();
        }
        datasetTree.put(wordArray, currGenomeIndex, instanceLocation);

        genomesInfo.countParalogsInSeqs(wordArray, currGenomeIndex);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param replicon
     * @param datasetTree
     * @param currGenomeIndex
     * @return
     */
    private int updateDataTree(Replicon replicon, int currGenomeIndex, GenomesInfo gi){

        int repliconLength = 0;
        if (nonDirectons) {//put replicon and its reverseCompliment

            putWordInDataTree(replicon, currGenomeIndex, gi);

            //reverseCompliment replicon
            replicon = new Replicon(replicon);
            replicon.reverseCompliment();
            putWordInDataTree(replicon, currGenomeIndex, gi);

            repliconLength += replicon.size() * 2;

        }else{//split replicon to directons

            List<Directon> directons = replicon.splitRepliconToDirectons(gi.UNK_CHAR);

            for (Directon directon: directons){
                repliconLength += directon.size();
                putWordInDataTree(directon, currGenomeIndex, gi);
            }

        }
        return repliconLength;
    }

    private void buildTree(GenomesInfo genomesInfo){
        for (Genome genome : genomesInfo.getGenomes()) {

            int genomeId = genome.getId();
            for (Replicon replicon: genome.getReplicons()) {
                updateDataTree(replicon, genomeId, genomesInfo);
            }
        }
    }

}
