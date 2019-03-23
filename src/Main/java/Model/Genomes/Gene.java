package Model.Genomes;

import java.util.Objects;

/**
 * Represents a gene
 */
public class Gene {

    private final String cogId;
    private final Strand strand; //+ or -

    public Gene(String cogId, Strand strand){
        this.cogId = cogId;
        this.strand = strand;
    }

    public Gene(Gene other){
        this.cogId = other.cogId;
        this.strand = other.strand;
    }

    public String getCogId() {
        return cogId;
    }

    public Strand getStrand() {
        return strand;
    }

    public static Strand reverseStrand(Strand strand){
        return strand == Strand.FORWARD ? Strand.REVERSE : Strand.FORWARD;
    }

    public String toString(){
        return (cogId + strand).intern();
    }

    @Override
    public int hashCode() {
        return Objects.hash(cogId, strand);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Gene))
            return false;
        if (obj == this)
            return true;

        Gene otherGene = (Gene) obj;

        return strand == otherGene.strand && cogId.equals(otherGene.getCogId());

    }
}
