package IO;

import Core.OrthologyGroups.CogInfo;
import Core.PostProcess.Family;

import java.util.List;

/**
 */
public interface PatternsWriter {

    void write(List<Family> families, CogInfo cogInfo);

    void writeHeader(String header);

    void closeFile();

    int getCountPrintedPatterns();
}
