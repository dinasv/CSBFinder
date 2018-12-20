package IO;

import Core.OrthologyGroups.CogInfo;
import Core.OrthologyGroups.COG;
import Core.Genomes.Gene;
import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 */
public class ExcelWriter implements PatternsWriter{

    String path;

    private FileOutputStream catalogFile;
    private SXSSFWorkbook catalogWorkbook;
    //sheets
    private Sheet catalogSheet;
    private Sheet filteredPatternsSheet;
    private Sheet patternsDescriptionSheet;

    boolean cogInfoExists;
    boolean includeFamilies;

    private int countPrintedPatterns;
    private int countPrintedFilteredPatterns;
    private int nextLineIndexDescSheet;


    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public ExcelWriter(boolean cogInfoExists, boolean includeFamilies, String path){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        this.cogInfoExists = cogInfoExists;
        this.includeFamilies = includeFamilies;
        this.path = path + ".xlsx";

        countPrintedPatterns = 0;
        countPrintedFilteredPatterns = 0;
        nextLineIndexDescSheet = 0;

        init();
    }

    private void init(){

        try {
            catalogFile = new FileOutputStream(path);

            catalogWorkbook = new SXSSFWorkbook(10);
            catalogSheet = catalogWorkbook.createSheet("Catalog");

            if (includeFamilies) {
                filteredPatternsSheet = catalogWorkbook.createSheet("Filtered CSBs");
            }

            if (cogInfoExists){
                patternsDescriptionSheet = catalogWorkbook.createSheet("CSBs description");
            }
        } catch (Exception e) {
            System.out.println("Cannot create file " + path + ". Close the file first.");
        }

    }

    private void writeHeaderToSheet(String header, Sheet sheet){
        Row row = sheet.createRow(0);
        int i = 0;
        for (String str: header.split("\t")){
            row.createCell(i++).setCellValue(str);
        }
    }


    public void printFamily(Family family, CogInfo cogInfo){

        printTopScoringPattern(family.getTopScoringPattern(), family.getFamilyId(), cogInfo);

        for (Pattern pattern: family.getPatterns()) {

            countPrintedPatterns++;
            printPatternLineToExcelSheet(catalogSheet, pattern, countPrintedPatterns, family.getFamilyId(), cogInfo);

            if (patternsDescriptionSheet != null) {
                nextLineIndexDescSheet = printPatternDescToExcelSheet(patternsDescriptionSheet,
                        nextLineIndexDescSheet, pattern, cogInfo);
            }

        }
    }

    /**
     * Prints a pattern with the highest score in its family to a different sheet
     * @param pattern
     * @param gi
     * @param familyId
     */
    public void printTopScoringPattern(Pattern pattern, int familyId, CogInfo cogInfo){
        if (pattern != null) {

            countPrintedFilteredPatterns++;
            printPatternLineToExcelSheet(filteredPatternsSheet, pattern, countPrintedFilteredPatterns, familyId, cogInfo);
        }
    }


    private void printPatternLineToExcelSheet(Sheet sheet, Pattern pattern, int rowNum, int familyId,
                                               CogInfo cogInfo){
        Row row = sheet.createRow(rowNum);
        int col = 0;
        row.createCell(col++).setCellValue(pattern.getPatternId());
        row.createCell(col++).setCellValue(pattern.getLength());
        try{
            row.createCell(col++).setCellValue(Double.valueOf(DF.format(pattern.getScore())));
        }catch (Exception e){
            row.createCell(col).setCellValue(DF.format(pattern.getScore()));
        }
        row.createCell(col++).setCellValue(pattern.getInstancesPerGenome());

        String patternGenes = pattern.toString();

        row.createCell(col++).setCellValue(patternGenes);

        if (cogInfo.cogInfoExists()) {
            row.createCell(col++).setCellValue(pattern.getMainFunctionalCategory());
        }
        row.createCell(col++).setCellValue(familyId);
    }

    private int printPatternDescToExcelSheet(Sheet sheet, int rowNum, Pattern pattern, CogInfo cogInfo){
        try {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("FAMILY_ID=");
            row.createCell(1).setCellValue(pattern.getPatternId());
            row.createCell(2).setCellValue("Count=");
            row.createCell(3).setCellValue(pattern.getInstancesPerGenome());
            row.createCell(4).setCellValue("Score=");
            row.createCell(5).setCellValue(pattern.getScore());

            for (Gene gene : pattern.getPatternGenes()) {
                row = sheet.createRow(rowNum++);

                COG cog_obj = cogInfo.getCog(gene.getCogId());

                row.createCell(0).setCellValue(gene.getCogId());
                if (cog_obj != null) {
                    row.createCell(1).setCellValue(cog_obj.getCogDesc());
                } else {
                    row.createCell(1).setCellValue("-");
                }
            }
        }catch (IllegalArgumentException e){
            System.out.println(String.format("Can't write line number %d to excel sheet. " +
                    "Choose a different output file format", rowNum));
        }
        return rowNum+1;
    }


    public void write(List<Family> families, CogInfo cogInfo) {
        families.forEach(family -> printFamily(family, cogInfo));
    }

    @Override
    public void writeHeader(String header) {
        writeHeaderToSheet(header, catalogSheet);

        if (includeFamilies) {
            writeHeaderToSheet(header, filteredPatternsSheet);
        }
    }

    public void closeFile(){
        try {
            catalogWorkbook.write(catalogFile);
            catalogFile.close();
        } catch (Exception e) {
            System.out.println("A problem occurred while trying to write to file "+ path);
            System.exit(1);
        }
    }

    public int getCountPrintedPatterns(){
        return countPrintedPatterns;
    }
}
