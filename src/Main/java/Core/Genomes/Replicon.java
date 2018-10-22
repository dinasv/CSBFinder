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
    }

    public Replicon(Strand strand, int id, String name){
        super(strand, id);
        this.name = name;
    }

    private static Strand reverseStrand(Strand strand){
        return strand == Strand.REVERSE ? Strand.FORWARD : Strand.REVERSE;
    }

    public void reverse(){
        Collections.reverse(genes);
        setStrand(getStrand() == Strand.FORWARD ? Strand.REVERSE : Strand.FORWARD);

        int start_index = getStartIndex();
        start_index = getStrand() == Strand.REVERSE ? start_index + size() - 1 : start_index - size() + 1;
        setStartIndex(start_index);
    }


    public String[] getGenesIDs(){
        if (getStrand() == Strand.FORWARD) {
            return genes.stream().map(gene -> gene.getCogId() + gene.getStrand().toString())
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }else{
            return genes.stream().map(gene -> gene.getCogId() + reverseStrand(gene.getStrand()).toString())
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }
    }


    public List<Directon> splitRepliconToDirectons(String UNK_CHAR) {

        List<Directon> directons = new ArrayList<>();

        Directon directon = new Directon(getId());

        int geneIndex = 0;
        for (Gene gene : getGenes()) {
            //end directon if it is the last gene in the replicon, or if next gene is on different strand
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
