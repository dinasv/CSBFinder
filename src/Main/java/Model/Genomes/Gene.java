package Model.Genomes;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        return cogId + strand;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(cogId).
                        append(strand).
                        toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Gene))
            return false;
        if (obj == this)
            return true;

        Gene rhs = (Gene) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                        append(cogId, rhs.cogId).
                        append(strand, rhs.strand).
                        isEquals();
    }
}
