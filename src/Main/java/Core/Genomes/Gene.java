package Core.Genomes;

/**
 * Represents a gene
 */
public class Gene {

    private String cogId;
    private String strand; //+ or -

    public Gene(String cogId, String strand){
        this.cogId = cogId;
        this.strand = strand;
    }

    public String getCogId() {
        return cogId;
    }

    public String getStrand() {
        return strand;
    }

    public String toString(){
        return cogId +strand;
    }
}
