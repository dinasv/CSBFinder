package Utils;

/**
 *
 */
public class InstanceLocation {
    private int replicon_id;
    private int strand;
    private int start_index;
    private int end_index;


    public InstanceLocation(int replicon_id, int start_index, int strand){
        this.replicon_id = replicon_id;
        this.strand = strand;
        this.start_index = start_index;
        end_index = -1;
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


    public int getEndIndex() {
        return end_index;
    }

    public void setEndIndex(int instance_length) {
        instance_length *= strand;
        end_index = start_index + instance_length;
    }
}
