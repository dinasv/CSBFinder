package Core.SuffixTrees;

import Core.Genomes.*;

import java.util.List;

/**
 */
public class DatasetTree {

    private GeneralizedSuffixTree datasetTree;
    private GenomesInfo genomesInfo;
    public boolean nonDirectons;

    public DatasetTree(/*boolean nonDirectons, */GenomesInfo gi){
        datasetTree = null;

        genomesInfo = gi;

        nonDirectons = false;
        //this.nonDirectons = nonDirectons;

        //buildTree(gi);
    }

    public GeneralizedSuffixTree getSuffixTree(){
        return datasetTree;
    }


    /**
     * Converts an array of strings to wordArray, using charToIndex
     * @param genes each gene will we converted to a chracter with index
     * @return WordArray representing genes
     */
    /*
    private static WordArray createWordArray(List<Gene> genes, GenomesInfo gi, boolean nonDirectons){
        int[] word = new int[genes.size()];
        int i = 0;
        for(Gene gene: genes){

            Gene ch = gene;
            if (!nonDirectons){
                ch = new Gene(gene.getCogId(), Strand.INVALID);
            }

            int letterIndex = gi.getLetter(ch);
            if (gi.getLetter(ch) == -1) {
                letterIndex = gi.alphabetSize();
                gi.indexToChar.add(ch);
                gi.charToIndex.put(ch, letterIndex);
            }

            word[i] = letterIndex;
            i++;
        }

        return new WordArray(word);
    }*/


    /**
     * Turns a genomicSegment (replicon or directon) into an array of genes and puts in in the @datasetTree
     * @param genomicSegment
     * @param datasetTree
     * @param currGenomeIndex
     */
    private void putWordInDataTree(GenomicSegment genomicSegment,
                                          int currGenomeIndex, boolean nonDirectons){

        List<Gene> genes = genomicSegment.getGenes();
        WordArray wordArray = genomesInfo.createWordArray(genes, nonDirectons);
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
    private int updateDataTree(Replicon replicon, int currGenomeIndex, boolean nonDirectons){

        int repliconLength = 0;
        if (nonDirectons) {//put replicon and its reverseCompliment

            putWordInDataTree(replicon, currGenomeIndex, nonDirectons);

            //reverseCompliment replicon
            replicon = new Replicon(replicon);
            replicon.reverseCompliment();
            putWordInDataTree(replicon, currGenomeIndex, nonDirectons);

            repliconLength += replicon.size() * 2;

        }else{//split replicon to directons

            List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

            for (Directon directon: directons){
                repliconLength += directon.size();
                putWordInDataTree(directon, currGenomeIndex, nonDirectons);
            }

        }
        return repliconLength;
    }

    public void buildTree(boolean nonDirectons){
        if (datasetTree != null && this.nonDirectons == nonDirectons){
            return;
        }
        datasetTree = new GeneralizedSuffixTree();
        this.nonDirectons = nonDirectons;

        for (Genome genome : genomesInfo.getGenomes()) {

            int genomeId = genome.getId();
            for (Replicon replicon: genome.getReplicons()) {
                updateDataTree(replicon, genomeId, nonDirectons);
            }
        }
    }

}
