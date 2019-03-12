package Model.Patterns;
import Model.Genomes.Strand;
/**
 *
 */
public class InstanceLocation{

    //If the replicons are segmented to directons - each directon has a different id
    //Otherwise - the id is the same as the repliconId.
    private int genomicSegmentId;
    private int repliconId;
    private int genomeId;

    private Strand strand;

    /**
     * Start index relative to the (@code genomicSegmentStartIndex) if it is the forward strand,
     * or relative to the (@code genomicSegmentStartIndex) + (@code genomicSegmentLength) if it is the reverse strand
     */
    private int relativeStartIndex;
    private int instanceLength;

    private int genomicSegmentLength;
    private int genomicSegmentStartIndex;

    public InstanceLocation(int repliconId, int genomeId, int relativeStartIndex, int length, Strand strand,
                            int genomicSegmentStartIndex, int genomicSegmentLength, int genomicSegmentId){
        this.repliconId = repliconId;
        this.genomeId = genomeId;
        this.strand = strand;
        this.relativeStartIndex = relativeStartIndex;
        this.instanceLength = length;
        this.genomicSegmentLength = genomicSegmentLength;
        this.genomicSegmentStartIndex = genomicSegmentStartIndex;
        this.genomicSegmentId = genomicSegmentId;

    }

    public InstanceLocation(InstanceLocation other){
        repliconId = other.repliconId;
        genomeId = other.genomeId;
        strand = other.strand;
        relativeStartIndex = other.relativeStartIndex;
        instanceLength = other.instanceLength;
        genomicSegmentLength = other.genomicSegmentLength;
        genomicSegmentStartIndex = other.genomicSegmentStartIndex;
        genomicSegmentId = other.genomicSegmentId;
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

    public int getLength(){
        return instanceLength;
    }

    public void incrementRelativeStartIndex(){
        if (instanceLength > 0) {
            relativeStartIndex++;
            instanceLength--;
        }
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof InstanceLocation) {
            InstanceLocation otherInstanceLoc = (InstanceLocation) other;
            return repliconId == otherInstanceLoc.repliconId &&
                    genomeId == otherInstanceLoc.getGenomeId() &&
                    strand == otherInstanceLoc.getStrand() &&
                    relativeStartIndex == otherInstanceLoc.relativeStartIndex &&
                    instanceLength == otherInstanceLoc.instanceLength &&
                    genomicSegmentLength == otherInstanceLoc.genomicSegmentLength &&
                    genomicSegmentStartIndex == otherInstanceLoc.genomicSegmentStartIndex;
        }
        return false;
    }

    public int getGenomicSegmentId() {
        return genomicSegmentId;
    }

    public String toHashString(){
        return String.format("%d_%d", genomeId, repliconId);
    }
}
