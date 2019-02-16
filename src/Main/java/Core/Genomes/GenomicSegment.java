package Core.Genomes;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class GenomicSegment {

    private int id;
    private int repliconId;
    private int genomeId;

    protected List<Gene> genes;
    private Strand strand;
    private int startIndex;

    public GenomicSegment(){
        genes = new ArrayList<>();
        strand = Strand.INVALID;
        startIndex = 0;
        repliconId = -1;
        genomeId = -1;
        id = -1;
    }

    public GenomicSegment(int id, int repliconId, int genomeId){
        this();
        this.repliconId = repliconId;
        this.genomeId = genomeId;
        this.id = id;
    }

    public GenomicSegment(Strand strand){
        this();
        this.strand = strand;
    }

    public GenomicSegment(int id, Strand strand, int repliconId, int genomeId){
        this(id, repliconId, genomeId);
        this.strand = strand;
    }

    public GenomicSegment(GenomicSegment other){
        this(other.id, other.strand, other.repliconId, other.genomeId);
        genes.addAll(other.genes);
    }

    public int size(){
        return genes.size();
    }

    public void addGene(Gene gene){
        genes.add(gene);
    }

    public void addAllGenes(List<Gene> genes){
        this.genes.addAll(genes);
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

    public int getRepliconId() {
        return repliconId;
    }

    public int getGenomeId() {
        return genomeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
