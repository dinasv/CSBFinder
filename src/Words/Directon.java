package Words;

import Utils.Gene;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Dina on 27/06/2018.
 */
public class Directon extends Replicon{

    public ArrayList<Gene> removeXFromEnd(String UNK_CHAR){
        int i = size()-1;
        while (i>=0 && genes.get(i).getCog_id().equals(UNK_CHAR)){
            i--;
        }
        genes = new ArrayList<Gene>(genes.subList(0, i+1));
        return genes;
    }


    public void setStrand() {
        if (genes.size() > 0 ){
            int strand = genes.get(0).getStrand().equals("-") ? -1 : 1;
            if (strand == -1) {
                reverse();
            }
            super.setStrand(strand);
        }
    }

    public String[] getGenesIDs(){
        return genes.stream().map(gene_obj -> gene_obj.getCog_id())
                .collect(Collectors.toList())
                .toArray(new String[genes.size()]);
    }

}
