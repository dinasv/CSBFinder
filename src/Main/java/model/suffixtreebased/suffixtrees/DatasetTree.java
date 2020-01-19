package model.suffixtreebased.suffixtrees;

import model.genomes.*;
import model.patterns.InstanceLocation;

import java.util.List;

/**
 */
public class DatasetTree {

    private GeneralizedSuffixTree datasetTree;
    private GenomesInfo genomesInfo;
    public boolean nonDirectons;

    public DatasetTree(GenomesInfo gi){
        datasetTree = null;

        genomesInfo = gi;

        nonDirectons = false;

    }

    public GeneralizedSuffixTree getSuffixTree(){
        return datasetTree;
    }

    /**
     * Turns a genomicSegment (replicon or directon) into an array of genes and puts in in the @datasetTree
     * @param genomicSegment
     * @param currGenomeIndex
     */
    private void putWordInDataTree(GenomicSegment genomicSegment, int currGenomeIndex){

        List<Gene> genes = genomicSegment.getGenes();
        WordArray wordArray = genomesInfo.createWordArray(genes);
        InstanceLocation instanceLocation = new InstanceLocation(genomicSegment.getRepliconId(), currGenomeIndex,
                0,genomicSegment.size(), genomicSegment.getStrand(), genomicSegment.getStartIndex(),
                genomicSegment.size(), genomicSegment.getId());

        datasetTree.put(wordArray, instanceLocation);

        genomesInfo.countParalogsInSeqs(wordArray, currGenomeIndex);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param replicon
     * @param currGenomeIndex
     * @return
     */
    private void updateDataTree(Replicon replicon, int currGenomeIndex, boolean nonDirectons){

        if (nonDirectons) {//putWithSuffix replicon and its reverseComplement

            putWordInDataTree(replicon, currGenomeIndex);

            replicon = replicon.reverseComplement();
            putWordInDataTree(replicon, currGenomeIndex);

        }else{//split replicon to directons

            List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

            for (Directon directon: directons){
                putWordInDataTree(directon, currGenomeIndex);
            }

        }
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
