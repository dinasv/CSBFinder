package Core.SuffixTrees;

import Core.Genomes.*;

import java.util.List;

/**
 */
public class DatasetTreeBuilder {

    /**
     * Builds a Trie of patterns, given in a file.
     * @param pattern_tree the patterns are inserted to this GST
     * @return True if succesful, False if exception occurred
     */
    public static void buildPatternsTree(List<Pattern> patterns, Trie pattern_tree, GenomesInfo gi) {

        for (Pattern pattern: patterns){
            WordArray word = createWordArray(pattern.getPatternArr(), gi);
            pattern_tree.put(word, pattern.getPatternId(), gi.UNK_CHAR_INDEX);
        }
    }


    /**
     * Converts an array of strings to wordArray, using charToIndex
     * @param str contains the characters comprising this str
     * @return WordArray representing this str
     */
    public static WordArray createWordArray(String[] str, GenomesInfo gi){
        int[] word = new int[str.length];
        int i = 0;
        for(String ch: str){
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
    private static void putWordInDataTree(GenomicSegment genomicSegment, GeneralizedSuffixTree datasetTree,
                                          int currGenomeIndex, GenomesInfo genomesInfo){

        String[] genes = genomicSegment.getGenesIDs();
        WordArray cog_word = createWordArray(genes, genomesInfo);
        InstanceLocation instanceLocation = new InstanceLocation(genomicSegment.getId(), currGenomeIndex,
                genomicSegment.getStartIndex(),
                genomicSegment.getStartIndex() + genomicSegment.size(), genomicSegment.getStrand());
        if (genomicSegment.getStrand() == Strand.REVERSE){
            instanceLocation.switchStartEndIndex();
        }
        datasetTree.put(cog_word, currGenomeIndex, instanceLocation);

        genomesInfo.countParalogsInSeqs(genes, currGenomeIndex);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param nonDirectons
     * @param replicon
     * @param datasetTree
     * @param currGenomeIndex
     * @return
     */
    private static int updateDataTree(boolean nonDirectons, Replicon replicon, GeneralizedSuffixTree datasetTree,
                                      int currGenomeIndex, GenomesInfo gi){

        int replicon_length = 0;
        if (nonDirectons) {

            putWordInDataTree(replicon, datasetTree, currGenomeIndex, gi);

            //reverse replicon
            replicon.reverse();
            putWordInDataTree(replicon, datasetTree, currGenomeIndex, gi);

            replicon_length += replicon.size() * 2;

        }else{

            List<Directon> directons = replicon.splitRepliconToDirectons(gi.UNK_CHAR);

            for (Directon directon: directons){
                replicon_length += directon.size();
                putWordInDataTree(directon, datasetTree, currGenomeIndex, gi);
            }

        }
        return replicon_length;
    }

    public static void buildTree(GeneralizedSuffixTree dataset_gst, boolean non_directons, GenomesInfo gi){
        for (Genome genome : gi.getGenomes()) {
            //String genome_name = genome.getName();
            int genome_index = genome.getId();
            for (Replicon replicon: genome.getReplicons()) {
                updateDataTree(non_directons, replicon, dataset_gst, genome_index, gi);
            }
        }
    }

}
