package IO;

import Core.CogInfo;
import Core.Genomes.*;
import Core.PostProcess.Family;

import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;

/**
 *
 */
public class SessionWriter implements PatternsWriter {

    private PrintWriter file;

    GenomesInfo genomesInfo;

    private boolean includeFamilies;
    private boolean nonDirectons;

    private int countPrintedPatterns;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public SessionWriter(boolean includeFamilies, boolean nonDirectons, String path, GenomesInfo genomesInfo){
        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.genomesInfo = genomesInfo;

        this.includeFamilies = includeFamilies;
        this.nonDirectons = nonDirectons;

        String catalogPath = path + ".txt";
        file = Writer.createOutputPrintWriter(catalogPath);
    }

    public void writeGenomes(Map<String, Genome> genomesMap){
        file.write("<genomes>\n");

        for (Genome genome: genomesMap.values()){
            for(Replicon replicon: genome.getReplicons()){
                file.write(String.format(">%s|%s%n", genome.getName(), replicon.getName()));
                for (Gene gene: replicon.getGenes()){
                    file.write(String.format("%s\t%s%n", gene.getCogId(), gene.getStrand()));
                }
            }
        }

        file.write("<\\genomes>\n");
    }

    @Override
    public void write(Family family, CogInfo cogInfo) {
        for (Pattern pattern: family.getPatterns()) {
            countPrintedPatterns++;

            Writer.printInstances(pattern, family.getFamilyId(), genomesInfo, nonDirectons, file);
        }
    }

    @Override
    public void writeHeader(String header) {
        file.println("<instances>");
    }

    @Override
    public void closeFile() {
        file.println("<\\instances>");
        if (file != null) {
            file.close();
        }
    }

    @Override
    public int getCountPrintedPatterns() {
        return countPrintedPatterns;
    }
}
