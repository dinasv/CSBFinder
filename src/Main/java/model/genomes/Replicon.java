package model.genomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class Replicon extends GenomicSegment {

    private String name;

    public Replicon(){
        super();
    }

    public Replicon(Strand strand){
        super(strand);
        this.name = "";
    }

    public Replicon(Strand strand, int repliconId, String name, int genomeId){
        super(strand.numericValue, strand, repliconId, genomeId);
        this.name = name;
    }

    public Replicon(Replicon other){
        super(other);
        name = other.name;
    }

    private static Strand reverseStrand(Strand strand){
        return strand == Strand.REVERSE ? Strand.FORWARD : Strand.REVERSE;
    }

    public void reverseCompliment(){
        List<Gene> reversedGenes = new ArrayList<>();

        Collections.reverse(genes);
        setStrand(reverseStrand(getStrand()));

        for(Gene gene: genes){
            reversedGenes.add(new Gene(gene.getCogId().intern(), Gene.reverseStrand(gene.getStrand())));
        }

        int start_index = getStartIndex();
        setStartIndex(start_index);

        genes = reversedGenes;
    }


    public List<Directon> splitRepliconToDirectons(String UNK_CHAR) {

        List<Directon> directons = new ArrayList<>();

        int directonId = 1;
        Directon directon = new Directon(directonId++, getRepliconId(), getGenomeId());

        int geneIndex = 0;
        for (Gene gene : getGenes()) {
            //end directon if it is the last gene in the replicon, or if next gene is on different strand
            boolean endDirecton = (geneIndex == size()-1) ||
                    !(gene.getStrand().equals(getGenes().get(geneIndex+1).getStrand()));
            if (directon.size() == 0) {
                if (!gene.getCogId().equals(UNK_CHAR) && !endDirecton) {
                    directon.setStrand(gene.getStrand());
                    directon.addGene(new Gene(gene.getCogId(), Strand.INVALID));
                }
            } else {
                directon.addGene(new Gene(gene.getCogId(), Strand.INVALID));

                if (endDirecton){//directon.size()>0

                    if (directon.size() > 1) {

                        directon.setStartIndex(geneIndex-directon.size()+1);

                        directon.removeUnkChars(UNK_CHAR);
                        if (directon.size() > 1) {

                            directons.add(directon);
                        }
                    }

                    directon = new Directon(directonId++, getRepliconId(), getGenomeId());
                }
            }
            geneIndex ++;
        }

        return directons;
    }

    public String getName() {
        return name;
    }

    public void setStrand(Strand strand) {
        super.setStrand(strand);
        super.setId(strand.numericValue);
    }
}
