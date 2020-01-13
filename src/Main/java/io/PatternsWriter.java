package io;

import model.cogs.CogInfo;
import model.postprocess.Family;

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
