package IO;

import Core.OrthologyGroups.CogInfo;
import Core.Genomes.*;
import Core.Patterns.Pattern;
import Core.PostProcess.Family;

import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SessionWriter implements PatternsWriter {

    private PrintWriter file;

    GenomesInfo genomesInfo;

    private boolean includeFamilies;

    private int countPrintedPatterns;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public SessionWriter(boolean includeFamilies, String path, GenomesInfo genomesInfo){
        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.genomesInfo = genomesInfo;

        this.includeFamilies = includeFamilies;

        String catalogPath = path + "_export_session.txt";
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

        file.println("<\\genomes>");
    }

    @Override
    public void write(List<Family> families, CogInfo cogInfo) {
        file.println("<instances>");
        for (Family family: families) {
            for (Pattern pattern : family.getPatterns()) {
                countPrintedPatterns++;
                Writer.printInstances(pattern, family.getFamilyId(), genomesInfo, file);
            }
        }
    }

    @Override
    public void writeHeader(String header) {
        file.println(header);
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
