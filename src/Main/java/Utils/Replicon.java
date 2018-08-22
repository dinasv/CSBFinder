package Utils;

import Utils.Gene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Created by Dina on 27/06/2018.
 */
public class Replicon {

    protected ArrayList<Gene> genes;
    private int strand;
    private int start_index;
    public static int index = -1;

    public Replicon(){
        genes = new ArrayList<>();
        strand = 0;
        start_index = 0;
    }

    public Replicon(int strand){
        this();
        this.strand = strand;
    }

    public int size(){
        return genes.size();
    }
    public void add(Gene gene){
        genes.add(gene);
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public int getStartIndex() {
        return start_index;
    }

    public void setStartIndex(int start_index) {
        this.start_index = start_index;
    }

    public ArrayList<Gene> getGenes(){
        return genes;
    }

    private static String reverseStrand(String strand){
        return strand.equals("-") ? "+" : "-";
    }

    public void reverse(){
        Collections.reverse(genes);
        strand = strand * -1;
        start_index = strand == -1 ? start_index + size() - 1 : start_index - size() + 1;
    }


    public String[] getGenesIDs(){
        if (strand == 1) {
            return genes.stream().map(gene -> gene.getCog_id() + gene.getStrand())
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }else{
            return genes.stream().map(gene -> gene.getCog_id() + reverseStrand(gene.getStrand()))
                    .collect(Collectors.toList())
                    .toArray(new String[genes.size()]);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
