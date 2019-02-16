package Core.MatchPointPatternFinder;

import Core.Genomes.GenomicSegment;

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

    public int getPosition() {
        return position;
    }

}
