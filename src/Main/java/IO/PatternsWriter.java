package IO;

import Core.CogInfo;
import Core.Genomes.GenomesInfo;
import Core.PostProcess.Family;

/**
 */
public interface PatternsWriter {

    void write(Family family, CogInfo cogInfo);

    void writeHeader(String header);

    void closeFile();

    int getCountPrintedPatterns();
}
