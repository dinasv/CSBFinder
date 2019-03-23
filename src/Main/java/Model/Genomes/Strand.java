package Model.Genomes;

/**
 */
public enum Strand {

    FORWARD(1),
    REVERSE(-1),
    INVALID(0);

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

    public static Strand determineStrand(String rawStrand) {

        Strand strand;

        switch (rawStrand) {
            case "+":
                strand = Strand.FORWARD;
                break;
            case "-":
                strand = Strand.REVERSE;
                break;
            default:
                strand = Strand.INVALID;
                break;
        }

        return strand;
    }
}
