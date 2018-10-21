package Core.Genomes;

/**
 *
 */
public class InstanceLocation{
    private int repliconId;
    private Strand strand;
    private int startIndex;
    private int endIndex;
    private String repliconName;


    public InstanceLocation(int repliconId, int startIndex, Strand strand){
        this.repliconId = repliconId;
        this.strand = strand;
        this.startIndex = startIndex;
        endIndex = -1;
        repliconName = "";
    }

    public int getRepliconId() {
        return repliconId;
    }

    public Strand getStrand() {
        return strand;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getActualStartIndex() {
        if (strand == Strand.REVERSE){
            return endIndex;
        }
        return startIndex;
    }


    public int getEndIndex() {
        return endIndex;
    }

    public int getActualEndIndex() {
        if (strand == Strand.REVERSE){
            return startIndex;
        }
        return endIndex;
    }

    public void setEndIndex(int instance_length) {
        instance_length -= 1;
        instance_length *= strand.strand;
        endIndex = startIndex + instance_length;
    }

    public String getRepliconName() {
        return repliconName;
    }

    public void setRepliconName(String repliconName) {
        this.repliconName = repliconName;
    }

}
