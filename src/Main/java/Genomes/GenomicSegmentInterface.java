package Genomes;

import java.util.ArrayList;
import java.util.List;

/**
 */
public interface GenomicSegmentInterface {

    int getId();

    int size();

    void add(Gene gene);

    int getStrand();

    void setStrand(int strand);

    /**
     *
     * @return start index in the genome
     */
    int getStartIndex();

    void setStartIndex(int start_index);

    List<Gene> getGenes();

    /**
     *
     * @return Array of gene IDs, as in the input sequences
     */
    String[] getGenesIDs();
}
