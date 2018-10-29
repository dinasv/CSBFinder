package Core.Genomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class Replicon extends GenomicSegment {

    private String name;

    public Replicon(){
        super();
    }

    public Replicon(Strand strand, int id, String name){
        super(strand, id);
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
            reversedGenes.add(new Gene(gene.getCogId(), Gene.reverseStrand(gene.getStrand())));
        }

        int start_index = getStartIndex();
        setStartIndex(start_index);

        genes = reversedGenes;
    }


    /*
    public List<Gene> getGenesIDs(){
        return genes;
        /*
        if (getStrand() == Strand.FORWARD) {
            return genes;
            //genes.stream().map(gene -> gene.getCogId() + gene.getStrand().toString())
                    //.collect(Collectors.toList());
                    //.toArray(new String[genes.size()]);
        }else{
            return genes.stream().map(gene -> gene.getCogId() + reverseStrand(gene.getStrand()).toString())
                    .collect(Collectors.toList());
                    //.toArray(new String[genes.size()]);
        }*/
    //}


    public List<Directon> splitRepliconToDirectons(String UNK_CHAR) {

        List<Directon> directons = new ArrayList<>();

        Directon directon = new Directon(getId());

        int geneIndex = 0;
        for (Gene gene : getGenes()) {
            //end directon if it is the last gene in the replicon, or if next gene is on different numericValue
            boolean endDirecton = (geneIndex == size()-1) ||
                    !(gene.getStrand().equals(getGenes().get(geneIndex+1).getStrand()));
            if (directon.size() == 0) {
                if (!gene.getCogId().equals(UNK_CHAR) && !endDirecton) {
                    directon.add(gene);
                }
            } else {
                directon.add(gene);

                if (endDirecton){//directon.size()>0

                    if (directon.size() > 1) {

                        directon.setStartIndex(geneIndex-directon.size()+1);

                        directon.removeXFromEnd(UNK_CHAR);
                        if (directon.size() > 1) {
                            directon.setStrand();

                            directons.add(directon);
                        }
                    }

                    directon = new Directon(getId());
                }
            }
            geneIndex ++;
        }

        return directons;
    }

    public String getName() {
        return name;
    }
}
