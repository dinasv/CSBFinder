package Utils;

/**
 * Represents a gene
 */
public class Gene {

    private String cog_id;
    private String strand; //+ or -

    public Gene(String cog_id, String strand){
        this.cog_id = cog_id;
        this.strand = strand;
    }

    public String getCog_id() {
        return cog_id;
    }

    public String getStrand() {
        return strand;
    }

    public String toString(){
        return cog_id+strand;
    }
}
