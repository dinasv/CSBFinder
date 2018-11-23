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
    private boolean nonDirectons;

    private int countPrintedPatterns;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public TextWriter(boolean cogInfoExists, boolean includeFamilies, boolean nonDirectons, String path){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.cogInfoExists = cogInfoExists;
        this.includeFamilies = includeFamilies;
        this.nonDirectons = nonDirectons;

        String catalogPath = path + ".txt";
        catalogFile = createOutputPrintWriter(catalogPath);

        //init(path, header);
    }

    @Override
    public void write(Family family, GenomesInfo gi, CogInfo cogInfo) {
        printFamily(family, gi, cogInfo);
    }

    public void writeHeader(String header){
        if (catalogFile != null) {
            catalogFile.write(header + "\n");
        }
    }


    private PrintWriter createOutputPrintWriter(String path){

        PrintWriter output_file = null;

        try {
            output_file = new PrintWriter(path, "UTF-8");
        } catch (Exception e) {
            System.out.println("Cannot create file " + path);
            System.exit(1);
        }
        return output_file;
    }

    public void closeFile() {
        if (catalogFile != null) {
            catalogFile.close();
        }
    }

    public void printFamily(Family family, GenomesInfo gi, CogInfo cogInfo){
        for (Pattern pattern: family.getPatterns()) {
            countPrintedPatterns++;
            //pattern.calculateMainFunctionalCategory(cogInfo, false);

            String catalogLine = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

            catalogLine += DF.format(pattern.getScore()) + "\t"
                    + pattern.getInstanceCount() + "\t"
                    + DF.format(pattern.getInstanceCount() / (double) gi.getNumberOfGenomes()) + "\t"
                    + pattern.getExactInstanceCount() + "\t";

            String patternGenes;
            if(nonDirectons){
                patternGenes = pattern.toString();
            }else{
                patternGenes = pattern.toStringWithNoStrand();
            }

            catalogLine += patternGenes + "\t";

            if (cogInfoExists) {
                catalogLine += pattern.getMainFunctionalCategory() + "\t";
            }
            //if (family. != null){
                catalogLine += family.getFamilyId();
            //}

            if (catalogFile != null) {
                catalogFile.write(catalogLine + "\n");
            }
        }
    }

    public int getCountPrintedPatterns(){
        return countPrintedPatterns;
    }

}
