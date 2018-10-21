package Core.Genomes;

/**
 */
public enum Strand {

    FORWARD(1), REVERSE(-1), INVALID(0);

    final public int strand;

    Strand(int strand){
        this.strand = strand;
    }

    public String toString() {
        if (this == Strand.FORWARD) {
            return "+";
        } else if (this == Strand.REVERSE){
            return "-";
        }
        return "";
    }
}
