package Core;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;

import java.util.List;

/**
 */
public interface Algorithm {

    void setParameters(Parameters params);

    void setGenomesInfo(GenomesInfo gi);

    void setPatternsFromFile(List<Pattern> patternsFromFile);

    void findPatterns();

    List<Pattern> getPatterns();

    int getPatternsCount();

    Parameters getParameters();

}
