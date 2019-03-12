package Model;

import Model.Genomes.GenomesInfo;
import Model.Patterns.Pattern;

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

    void setNumOfThreads(int numOfThreads);
}
