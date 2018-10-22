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


    public InstanceLocation(int repliconId, int startIndex, int endIndex, Strand strand){
        this.repliconId = repliconId;
        this.strand = strand;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
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

    public int getEndIndex() {
        return endIndex;
    }

    public int getActualStartIndex(){
        if (startIndex >= endIndex){
            return endIndex + 1;
        }
        return startIndex;
    }

    public int getActualEndIndex(){
        if (startIndex >= endIndex){
            return startIndex + 1;
        }
        return endIndex;
    }

    public void changeInstanceLength(int length){
        if (startIndex <= endIndex){
            endIndex = startIndex + length;
        }else{
            endIndex = startIndex - length;
        }
    }
    /*
    public void setEndIndexUsingLength(int instance_length) {

        endIndex = startIndex + instance_length;

        if (strand == Strand.REVERSE){
            switchStartEndIndex();
        }
    }*/

    public void switchStartEndIndex(){
        int temp = startIndex;
        int increment = 1;
        if (startIndex <= endIndex) {
            increment = -1;
        }
        startIndex = endIndex + increment;
        endIndex = temp + increment;
    }

    public void setEndIndex(int endIndex){
        this.endIndex = endIndex;
    }

    public String getRepliconName() {
        return repliconName;
    }

    public void setRepliconName(String repliconName) {
        this.repliconName = repliconName;
    }

}
