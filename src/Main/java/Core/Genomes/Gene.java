package Core.Genomes;

/**
 * Represents a gene
 */
public class Gene {

    private String cogId;
    private Strand strand; //+ or -

    public Gene(String cogId, Strand strand){
        this.cogId = cogId;
        this.strand = strand;
    }

    public String getCogId() {
        return cogId;
    }

    public Strand getStrand() {
        return strand;
    }

    public String toString(){
        return cogId +strand;
    }
}
