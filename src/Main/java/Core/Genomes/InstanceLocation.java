package Core.Genomes;

/**
 *
 */
public class InstanceLocation{

    private int repliconId;
    private int genomeId;
    private Strand strand;

    private int relativeStartIndex;
    private int instanceLength;

    private int genomicSegmentLength;
    private int genomicSegmentStartIndex;
   // private int endIndex;

    private String repliconName;

    public InstanceLocation(int repliconId, int genomeId, int relativeStartIndex, int length, Strand strand,
                            int genomicSegmentStartIndex, int genomicSegmentLength){
        this.repliconId = repliconId;
        this.genomeId = genomeId;
        this.strand = strand;
        this.relativeStartIndex = relativeStartIndex;
        this.instanceLength = length;
        this.genomicSegmentLength = genomicSegmentLength;
        this.genomicSegmentStartIndex = genomicSegmentStartIndex;

        //this.endIndex = relativeStartIndex + length;

        /*
        if (strand == Strand.REVERSE) {
            switchStartEndIndex();
        }*/
        repliconName = "";
    }

    public InstanceLocation(InstanceLocation other){
        repliconId = other.repliconId;
        genomeId = other.genomeId;
        strand = other.strand;
        relativeStartIndex = other.relativeStartIndex;
        instanceLength = other.instanceLength;
        genomicSegmentLength = other.genomicSegmentLength;
        //endIndex = other.endIndex;
        repliconName = other.repliconName;
        genomicSegmentStartIndex = other.genomicSegmentStartIndex;
    }

    public int getRepliconId() {
        return repliconId;
    }

    public int getGenomeId() {
        return genomeId;
    }

    public Strand getStrand() {
        return strand;
    }

    public int getRelativeStartIndex() {
        return relativeStartIndex;
    }

/*
    public int getEndIndex() {
        return endIndex;
    }
*/
    public int getActualStartIndex(){
        int startIndex = genomicSegmentStartIndex + relativeStartIndex;
        if (strand == Strand.REVERSE){
            startIndex = genomicSegmentStartIndex + genomicSegmentLength - (relativeStartIndex + instanceLength);
        }
        return startIndex;
        /*
        if (relativeStartIndex >= endIndex){
            return endIndex + 1;
        }
        return relativeStartIndex;*/
    }

    public int getActualEndIndex(){
        return getActualStartIndex() + instanceLength;
        /*
        if (relativeStartIndex >= endIndex){
            return relativeStartIndex + 1;
        }
        return endIndex;*/

    }

    public void setInstanceLength(int length){
        this.instanceLength = length;
        /*
        if (strand == Strand.REVERSE){
            endIndex = relativeStartIndex - length;

        }else{
            endIndex = relativeStartIndex + length;
        }*/
        /*
        if (relativeStartIndex <= endIndex){
            endIndex = relativeStartIndex + length;
        }else{
            endIndex = relativeStartIndex - length;
        }*/
    }

    /*
    public void switchStartEndIndex(){
        int temp = relativeStartIndex;
        int increment = 1;
        if (relativeStartIndex <= endIndex) {
            increment = -1;
        }
        relativeStartIndex = endIndex + increment;
        endIndex = temp + increment;
    }*/

    /*
    public void setEndIndex(int endIndex){
        this.endIndex = endIndex;
    }
*/
    public String getRepliconName() {
        return repliconName;
    }

    public void setRepliconName(String repliconName) {
        this.repliconName = repliconName;
    }

    public int getLength(){
        return instanceLength;
    }

    public void setRelativeStartIndex(int relativeStartIndex){
        this.relativeStartIndex = relativeStartIndex;
    }

    public void incrementRelativeStartIndex(){
        if (instanceLength > 0) {
            relativeStartIndex++;
            instanceLength--;
        }
    }

}
