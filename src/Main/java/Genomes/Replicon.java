package Genomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class Replicon extends GenomicSegmentClass{

    public Replicon(int strand, int id){
        super(strand, id);
    }

    private static String reverseStrand(String strand){
        return strand.equals("-") ? "+" : "-";
    }

    public void reverse(){
        Collections.reverse(genes);
        setStrand(getStrand() * -1);

        int start_index = getStartIndex();
        start_index = getStrand() == -1 ? start_index + size() - 1 : start_index - size() + 1;
        setStartIndex(start_index);
    }


    public String[] getGenesIDs(){
        if (getStrand() == 1) {
            return genes.stream().map(gene -> gene.getCog_id() + gene.getStrand())
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }else{
            return genes.stream().map(gene -> gene.getCog_id() + reverseStrand(gene.getStrand()))
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }
    }


    public List<Directon> splitRepliconToDirectons(String UNK_CHAR) {

        List<Directon> directons = new ArrayList<>();

        Directon directon = new Directon(getId());

        int gene_index = 0;
        for (Gene gene : getGenes()) {
            //end directon if it is the last gene in the regulon, or if next gene is on different strand
            boolean end_directon = (gene_index == size()-1) ||
                    !(gene.getStrand().equals(getGenes().get(gene_index+1).getStrand()));
            if (directon.size() == 0) {
                if (!gene.getCog_id().equals(UNK_CHAR) && !end_directon) {
                    directon.add(gene);
                }
            } else {
                directon.add(gene);

                if (end_directon){//directon.size()>0

                    if (directon.size() > 1) {

                        directon.setStartIndex(gene_index-directon.size()+1);

                        directon.removeXFromEnd(UNK_CHAR);
                        if (directon.size() > 1) {
                            directon.setStrand();

                            directons.add(directon);
                        }
                    }

                    directon = new Directon(getId());
                }
            }
            gene_index ++;
        }

        return directons;
    }

}
