package model;

import model.genomes.GenomesInfo;
import model.patterns.Pattern;

import java.util.List;

/**
 */
public interface Algorithm {

    void setParameters(Parameters params);

    void setGenomesInfo(GenomesInfo gi);

    void setPatternsFromFile(List<Pattern> patternsFromFile);

    //void setRefGenomesAsPatterns(List<Pattern> refGenomesPatterns);

    void findPatterns();

    List<Pattern> getPatterns();

    int getPatternsCount();

    Parameters getParameters();

    void setNumOfThreads(int numOfThreads);
}
