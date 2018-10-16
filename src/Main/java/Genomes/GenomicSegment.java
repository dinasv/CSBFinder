package Genomes;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class GenomicSegment {

    private int id;
    protected List<Gene> genes;
    private int strand;
    private int start_index;

    public GenomicSegment(){
        genes = new ArrayList<>();
        strand = 0;
        start_index = 0;
        id = -1;
    }
    public GenomicSegment(int id){
        this();
        this.id = id;
    }

    public GenomicSegment(int strand, int id){
        this(id);
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

    public List<Gene> getGenes(){
        return genes;
    }

    public int getId() {
        return id;
    }

    public abstract String[] getGenesIDs();

}
