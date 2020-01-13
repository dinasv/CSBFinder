package io;

import mvc.view.graphics.GeneColors;
import model.cogs.CogInfo;
import model.genomes.*;
import model.patterns.Pattern;
import model.postprocess.Family;

import java.awt.*;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class SessionWriter implements PatternsWriter {

    private PrintWriter file;

    private GenomesInfo genomesInfo;

    private int countPrintedPatterns;

    private String path;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public SessionWriter(String path, GenomesInfo genomesInfo){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        countPrintedPatterns = 0;

        this.genomesInfo = genomesInfo;

        this.path = path;
        file = Writer.createOutputPrintWriter(path);
    }

    public void writeGenomes(){

        Map<String, Genome> genomesMap = genomesInfo.getGenomesByName();
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

    public void writeColors(GeneColors colors){

        for (Iterator<Map.Entry<String, Color>> it = colors.getGeneToColor(); it.hasNext(); ) {
            Map.Entry<String, Color> entry = it.next();

        }

        Iterator<Map.Entry<String, Color>> it = colors.getGeneToColor();
        file.write("<colors>\n");

        it.forEachRemaining( entry -> {
            String line = String.format("%s=%s", entry.getKey(), entry.getValue().getRGB());
            file.println(line);
        });

        file.println("<\\colors>");
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

    @Override
    public String getPath() {
        return path;
    }
}
