package IO;

import Model.OrthologyGroups.CogInfo;
import Model.Genomes.*;
import Model.OutputType;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;

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

    private GenomesInfo genomesInfo;

    private int countPrintedPatterns;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public SessionWriter(String path, GenomesInfo genomesInfo){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.genomesInfo = genomesInfo;

        //String catalogPath = path + "_" + OutputType.SESSION.toString().toLowerCase() + ".txt";
        file = Writer.createOutputPrintWriter(path);
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
