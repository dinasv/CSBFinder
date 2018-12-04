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

        repliconName = "";
    }

    public InstanceLocation(InstanceLocation other){
        repliconId = other.repliconId;
        genomeId = other.genomeId;
        strand = other.strand;
        relativeStartIndex = other.relativeStartIndex;
        instanceLength = other.instanceLength;
        genomicSegmentLength = other.genomicSegmentLength;
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

    public int getRelativeEndIndex() {
        return relativeStartIndex + instanceLength;
    }


    public int getActualStartIndex(){
        int startIndex = genomicSegmentStartIndex + relativeStartIndex;
        if (strand == Strand.REVERSE){
            startIndex = genomicSegmentStartIndex + genomicSegmentLength - (relativeStartIndex + instanceLength);
        }
        return startIndex;
    }

    public int getActualEndIndex(){
        return getActualStartIndex() + instanceLength;
    }

    public void setInstanceLength(int length){
        this.instanceLength = length;
    }

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
