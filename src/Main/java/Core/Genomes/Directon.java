package Core.Genomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dina on 27/06/2018.
 */
public class Directon extends GenomicSegment {

    public Directon(int id){
        super(id);
    }
    public List<Gene> removeXFromEnd(String UNK_CHAR){
        int i = size()-1;
        while (i>=0 && genes.get(i).getCogId().equals(UNK_CHAR)){
            i--;
        }
        genes = new ArrayList<Gene>(genes.subList(0, i+1));
        return genes;
    }


    public void setStrand() {
        if (genes.size() > 0 ){
            int strand = genes.get(0).getStrand().equals("-") ? -1 : 1;

            super.setStrand(strand);
            if (strand == -1) {
                Collections.reverse(genes);
                setStartIndex(getStartIndex() + size() - 1);
            }
        }
    }

    public String[] getGenesIDs(){
        return genes.stream().map(gene_obj -> gene_obj.getCogId())
                .collect(Collectors.toList())
                .toArray(new String[genes.size()]);
    }

}
