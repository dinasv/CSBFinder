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

    }

    public GeneralizedSuffixTree getSuffixTree(){
        return datasetTree;
    }

    /**
     * Turns a genomicSegment (replicon or directon) into an array of genes and puts in in the @datasetTree
     * @param genomicSegment
     * @param datasetTree
     * @param currGenomeIndex
     */
    private void putWordInDataTree(GenomicSegment genomicSegment, int currGenomeIndex, boolean nonDirectons){

        List<Gene> genes = genomicSegment.getGenes();
        WordArray wordArray = genomesInfo.createWordArray(genes);
        InstanceLocation instanceLocation = new InstanceLocation(genomicSegment.getId(), currGenomeIndex,
                0,genomicSegment.size(), genomicSegment.getStrand(), genomicSegment.getStartIndex(),
                genomicSegment.size());
        /*
        if (genomicSegment.getStrand() == Strand.REVERSE){
            instanceLocation.switchStartEndIndex();
        }*/
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
    private void updateDataTree(Replicon replicon, int currGenomeIndex, boolean nonDirectons){

        if (nonDirectons) {//put replicon and its reverseCompliment

            putWordInDataTree(replicon, currGenomeIndex, nonDirectons);

            //reverseCompliment replicon
            replicon = new Replicon(replicon);
            replicon.reverseCompliment();
            putWordInDataTree(replicon, currGenomeIndex, nonDirectons);

        }else{//split replicon to directons

            List<Directon> directons = replicon.splitRepliconToDirectons(Alphabet.UNK_CHAR);

            for (Directon directon: directons){
                putWordInDataTree(directon, currGenomeIndex, nonDirectons);
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
