package Core.Genomes;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class GenomicSegment {

    private int id;
    private int genomeId;

    protected List<Gene> genes;
    private Strand strand;
    private int startIndex;

    public GenomicSegment(){
        genes = new ArrayList<>();
        strand = Strand.INVALID;
        startIndex = 0;
        id = -1;
        genomeId = -1;
    }
    public GenomicSegment(int id, int genomeId){
        this();
        this.id = id;
        this.genomeId = genomeId;
    }

    public GenomicSegment(Strand strand, int id, int genomeId){
        this(id, genomeId);
        this.strand = strand;
    }

    public GenomicSegment(GenomicSegment other){
        this(other.getStrand(), other.getId(), other.getGenomeId());
        genes.addAll(other.genes);
    }

    public int size(){
        return genes.size();
    }

    public void add(Gene gene){
        genes.add(gene);
    }

    public Strand getStrand() {
        return strand;
    }

    public void setStrand(Strand strand) {
        this.strand = strand;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int start_index) {
        this.startIndex = start_index;
    }

    public List<Gene> getGenes(){
        return genes;
    }

    public int getId() {
        return id;
    }

    public int getGenomeId() {
        return genomeId;
    }


}
