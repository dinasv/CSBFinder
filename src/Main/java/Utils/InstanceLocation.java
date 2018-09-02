package Utils;

/**
 *
 */
public class InstanceLocation {
    private int replicon_id;
    private int strand;
    private int start_index;
    private int end_index;
    private String repliconName;


    public InstanceLocation(int replicon_id, int start_index, int strand){
        this.replicon_id = replicon_id;
        this.strand = strand;
        this.start_index = start_index;
        end_index = -1;
        repliconName = "";
    }

    public int getRepliconId() {
        return replicon_id;
    }

    public int getStrand() {
        return strand;
    }

    public int getStartIndex() {
        return start_index;
    }

    public int getActualStartIndex() {
        if (strand == -1){
            return end_index;
        }
        return start_index;
    }


    public int getEndIndex() {
        return end_index;
    }

    public int getActualEndIndex() {
        if (strand == -1){
            return start_index;
        }
        return end_index;
    }

    public void setEndIndex(int instance_length) {
        instance_length -= 1;
        instance_length *= strand;
        end_index = start_index + instance_length;
    }

    public String getRepliconName() {
        return repliconName;
    }

    public void setRepliconName(String repliconName) {
        this.repliconName = repliconName;
    }
}
