package Model.MatchPointPatternFinder;

import Model.Genomes.GenomicSegment;

/**
 */
public class MatchPoint {

    private final GenomicSegment genomicSegment;
    private final int position;

    public MatchPoint(GenomicSegment genomicSegment, int position) {
        this.genomicSegment = genomicSegment;
        this.position = position;
    }

    public GenomicSegment getGenomicSegment() {
        return genomicSegment;
    }

    public int getGenomicSegmentId(){
        return genomicSegment.getId();
    }

    public int getPosition() {
        return position;
    }

}
