package model.genomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 */
public class Directon extends GenomicSegment {

    public Directon(int id, int repliconId, int genomeId){
        super(id, repliconId, genomeId);
    }

    public void removeUnkChars(String UNK_CHAR){
        removeXFromEnd(UNK_CHAR);
        removeXFromStart(UNK_CHAR);
    }

    private void removeXFromEnd(String UNK_CHAR){
        int i = size()-1;
        while (i>=0 && genes.get(i).getCogId().equals(UNK_CHAR)){
            i--;
        }
        if (i != size()-1) {
            genes = new ArrayList<Gene>(genes.subList(0, i + 1));
        }
    }

    private void removeXFromStart(String UNK_CHAR){
        int i = 0;
        while (i<size() && genes.get(i).getCogId().equals(UNK_CHAR)){
            i++;
        }
        if (i != 0) {
            genes = new ArrayList<Gene>(genes.subList(2, genes.size()));
        }
    }

    public void setStrand(Strand strand) {
        super.setStrand(strand);
    }

    public void addGene(Gene gene){
        if(getStrand() == Strand.REVERSE) {
            genes.add(0, gene);
        }else{
            genes.add(gene);
        }
    }

}
