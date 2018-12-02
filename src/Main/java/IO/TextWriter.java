package IO;

import Core.CogInfo;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;
import Core.OutputType;
import Core.PostProcess.Family;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 */
public class TextWriter implements PatternsWriter{

    private PrintWriter catalogFile;
    private boolean cogInfoExists;

    private boolean includeFamilies;

    private int countPrintedPatterns;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public TextWriter(boolean cogInfoExists, boolean includeFamilies, String path){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.cogInfoExists = cogInfoExists;
        this.includeFamilies = includeFamilies;

        String catalogPath = path + ".txt";
        catalogFile = Writer.createOutputPrintWriter(catalogPath);

    }

    @Override
    public void write(Family family, CogInfo cogInfo) {
        printFamily(family, cogInfo);
    }

    public void writeHeader(String header){
        if (catalogFile != null) {
            catalogFile.write(header + "\n");
        }
    }

    public void closeFile() {
        if (catalogFile != null) {
            catalogFile.close();
        }
    }

    public void printFamily(Family family, CogInfo cogInfo){
        for (Pattern pattern: family.getPatterns()) {
            countPrintedPatterns++;

            String catalogLine = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

            catalogLine += DF.format(pattern.getScore()) + "\t"
                    + pattern.getInstancesPerGenome() + "\t"
                    //+ pattern.getExactInstanceCount() + "\t"
                    + pattern.toString() + "\t";

            if (cogInfoExists) {
                catalogLine += pattern.getMainFunctionalCategory() + "\t";
            }
            //if (family. != null){
                catalogLine += family.getFamilyId();
            //}

            if (catalogFile != null) {
                catalogFile.println(catalogLine);
            }
        }
    }

    public int getCountPrintedPatterns(){
        return countPrintedPatterns;
    }

}
