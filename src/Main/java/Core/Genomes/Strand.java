package Core.Genomes;

/**
 */
public enum Strand {

    FORWARD(1), REVERSE(-1), INVALID(0);

    final public int numericValue;

    Strand(int numericValue){
        this.numericValue = numericValue;
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
