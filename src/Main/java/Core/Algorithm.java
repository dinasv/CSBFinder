package Core;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;

import java.util.List;

/**
 */
public interface Algorithm {

    public void setParameters(Parameters params);

    public void setGenomesInfo(GenomesInfo gi);

    public void setPatternsFromFile(List<Pattern> patternsFromFile);

    public void findPatterns();

    public List<Pattern> getPatterns();

    public int getPatternsCount();

    public Parameters getParameters();

}
