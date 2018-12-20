package Core.Patterns;

import Core.Genomes.Gene;
import Core.Genomes.Strand;

import java.util.ArrayList;
import java.util.List;

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
    private String genomeName;
    private List<Gene> genes;

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
        genomeName = "";
        genes = new ArrayList<>();
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

    public String getGenomeName() {
        return genomeName;
    }
    public List<Gene> getGenes() {
        return genes;
    }

    public void setRepliconName(String value) {
        this.repliconName = value;
    }

    public void setGenomeName(String value) {
        this.genomeName = value;
    }

    public void setGenes(List<Gene> genes) {
        this.genes = genes;
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
