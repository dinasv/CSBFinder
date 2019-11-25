package IO;

import Model.OrthologyGroups.CogInfo;
import Model.PostProcess.Family;

import java.util.List;

/**
 */
public interface PatternsWriter {

    void write(List<Family> families, CogInfo cogInfo);

    void writeHeader(String header);

    void closeFile();

    int getCountPrintedPatterns();

    String getPath();
}
