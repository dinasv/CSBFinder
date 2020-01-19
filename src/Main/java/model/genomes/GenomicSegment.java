package model.genomes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public interface GenomicSegment {

    int size();

    void addGene(Gene gene);

    void addAllGenes(List<Gene> genes);

    void addAllGenes(Gene[] genes);

    Strand getStrand();

    void setStrand(Strand strand);

    int getStartIndex();

    void setStartIndex(int startIndex);

    List<Gene> getGenes();

    int getRepliconId();

    int getGenomeId();

    int getId();

}
