package IO;

import Core.CogInfo;
import Core.Genomes.*;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import Core.OutputType;

import java.util.logging.Logger;

/**
 * Created by Dina on 19/05/2017.
 * Writes the output files:
 *      catalogFile: CSBs catalog
 *      instancesFile: The strings in which each CSB has an instance
 */
public class Writer {
    //output files
    private FileOutputStream catalogFileXls;
    private PrintWriter catalogFile;
    private PrintWriter instancesFile;

    private SXSSFWorkbook catalogWorkbook;
    private Sheet catalogSheet;
    private Sheet filteredPatternsSheet;
    private Sheet patternsDescriptionSheet;
    private String catalogPath;
    private String catalogInstancesPath;

    private int maxError;
    private int maxDeletion;
    private int maxInsertion;
    private int countPrintedPatterns;
    private int countPrintedFilteredPatterns;
    private int nextLineIndexDescSheet;
    private boolean cogInfoExists;
    private boolean debug;
    private boolean nonDirectons;

    private static final String DELIMITER = "|";
    private static final DecimalFormat DF = new DecimalFormat("#.####");

    private OutputType outputType;

    public Logger logger = null;

    public Writer(int maxError, int maxDeletion, int maxInsertion, boolean debug, String catalog_file_name,
                  String instances_file_name, boolean include_families, OutputType outputType,
                  boolean cogInfoExists, boolean nonDirectons, String output_path){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        this.maxError = maxError;
        this.maxDeletion = maxDeletion;
        this.maxInsertion = maxInsertion;
        this.debug = debug;
        countPrintedPatterns = 0;
        countPrintedFilteredPatterns = 0;
        nextLineIndexDescSheet = 0;

        this.outputType = outputType;
        this.cogInfoExists = cogInfoExists;
        this.nonDirectons = nonDirectons;

        catalogSheet = null;
        filteredPatternsSheet = null;
        patternsDescriptionSheet = null;

        init(catalog_file_name, instances_file_name, include_families, output_path);

    }

    private void init(String catalog_file_name, String instances_file_name, boolean include_families,
                      String output_path){

        catalogPath = output_path + catalog_file_name;
        catalogInstancesPath = output_path +instances_file_name;
        createOutputFiles();
        createHeaders(include_families);
    }

    private PrintWriter createOutputPrintWriter(String path){
        try {
            PrintWriter output_file = new PrintWriter(path, "UTF-8");

            return output_file;
        } catch (Exception e) {
            System.out.println("Cannot create file " + path);
            System.exit(1);
        }
        return null;
    }


    private void createOutputFiles() {

        if (outputType == OutputType.TXT) {
            catalogPath += ".txt";
            catalogFile = createOutputPrintWriter(catalogPath);
        } else if (outputType == OutputType.XLSX) {
            catalogPath += ".xlsx";
            try {
                catalogFileXls = new FileOutputStream(catalogPath);
            } catch (Exception e) {
                System.out.println("Cannot create file " + catalogPath + ". Close the file first.");
                System.exit(1);
            }
        }

        catalogInstancesPath += ".fasta";
        instancesFile = createOutputPrintWriter(catalogInstancesPath);
    }

    public static void createOutputDirectory(String path){
        try {
            new File(path).mkdir();
        }catch (SecurityException e){
            System.out.println("The directory \'"+path+"\' could not be created, therefore no output is printed. " +
                    "Please create a directory named 'output' in the following path: " + System.getProperty("user.dir"));
            System.exit(1);
        }
    }

    private void createHeaders(boolean include_families){

        String header = "ID\tLength\tScore\tInstance_Count\tInstance_Ratio\tExact_Instance_Count\tCSB";
        if (cogInfoExists){
            header += "\tMain_Category";
        }
        if (include_families){
            header += "\tFamily_ID";
        }

        if (outputType == OutputType.TXT) {
            if (catalogFile != null) {
                catalogFile.write(header + "\n");
            }
        }else if(outputType == OutputType.XLSX) {
            catalogWorkbook = new SXSSFWorkbook(10);
            catalogSheet = catalogWorkbook.createSheet("Catalog");
            writeHeaderToSheet(header, catalogSheet);
            if (include_families) {
                filteredPatternsSheet = catalogWorkbook.createSheet("Filtered CSBs");
                writeHeaderToSheet(header, filteredPatternsSheet);
            }
            if (cogInfoExists){
                patternsDescriptionSheet = catalogWorkbook.createSheet("CSBs description");
            }
        }

    }

    private void writeHeaderToSheet(String header, Sheet sheet){
        Row row = sheet.createRow(0);
        int i = 0;
        for (String str: header.split("\t")){
            row.createCell(i++).setCellValue(str);
        }
    }

    public int getCountPrintedPatterns(){
        return countPrintedPatterns;
    }

    public void closeFiles(){

        if (outputType == OutputType.TXT) {
            if (catalogFile != null) {
                catalogFile.close();
            }
        }else if(outputType == OutputType.XLSX){
            try {
                catalogWorkbook.write(catalogFileXls);
                catalogFileXls.close();
                if (debug){
                    File file = new File(catalogPath);
                    if(file.delete()){
                        System.out.println(catalogPath + " deleted");
                    }
                }
            } catch (Exception e) {
                System.out.println("A problem occurred while trying to write to file "+ catalogPath);
                System.exit(1);
            }
        }
        if (instancesFile != null) {
            instancesFile.close();
            if (debug){
                File file = new File(catalogInstancesPath);
                if(file.delete()){
                    System.out.println(catalogInstancesPath + " deleted");
                }else{
                    System.out.println(catalogInstancesPath + " not deleted");

                }
            }
        }
    }

    private void printInstances(Pattern pattern, GenomesInfo gi){

        if (instancesFile != null) {

            instancesFile.println(String.format(">%s\t%s", pattern.getPatternId(), pattern.getPattern()));

            for (Map.Entry<Integer, PatternLocationsInGenome> entry : pattern.getPatternLocations().entrySet()) {

                String genomeName = gi.getGenomeName(entry.getKey());
                instancesFile.print(genomeName);

                PatternLocationsInGenome patternLocationsInGenome = entry.getValue();
                for (List<InstanceLocation> instanceLocationsInReplicon : patternLocationsInGenome.getSortedLocations().values()){
                    for (InstanceLocation instanceLocation: instanceLocationsInReplicon) {
                        String replicon_name = gi.getRepliconName(instanceLocation.getRepliconId());

                        instancesFile.print("\t" + replicon_name + "|[" + instanceLocation.getActualStartIndex() + ","
                                + instanceLocation.getActualEndIndex() + "]");
                    }
                }

                instancesFile.print("\n");
            }

        }
    }


    /**
     * Prints a pattern with the highest score in its family to a different sheet
     * @param pattern
     * @param gi
     * @param family_id
     */
    public void printTopScoringPattern(Pattern pattern, GenomesInfo gi, String family_id, CogInfo cogInfo){
        if(outputType == OutputType.XLSX){
            if (pattern != null) {
                pattern.calculateMainFunctionalCategory(cogInfo, false);

                countPrintedFilteredPatterns++;
                printCSBLineToExcelSheet(filteredPatternsSheet, pattern, countPrintedFilteredPatterns, family_id, gi, cogInfo);
            }
        }
    }

    private void printCSBLineToExcelSheet(Sheet sheet, Pattern pattern, int row_num, String family_id, GenomesInfo gi, CogInfo cogInfo){
        Row row = sheet.createRow(row_num);
        int col = 0;
        row.createCell(col++).setCellValue(pattern.getPatternId());
        row.createCell(col++).setCellValue(pattern.getLength());
        try{
            row.createCell(col++).setCellValue(Double.valueOf(DF.format(pattern.getScore())));
        }catch (Exception e){
            row.createCell(col).setCellValue(DF.format(pattern.getScore()));
        }
        row.createCell(col++).setCellValue(pattern.getInstanceCount());
        row.createCell(col++).setCellValue(Double.valueOf(DF.format(pattern.getInstanceCount() /
                (double) gi.getNumberOfGenomes())));
        row.createCell(col++).setCellValue(pattern.getExactInstanceCount());
        row.createCell(col++).setCellValue(pattern.getPattern());
        if (cogInfo.cogInfoExists()) {
            row.createCell(col++).setCellValue(pattern.getMainFunctionalCategory());
        }
        if (family_id != null){
            row.createCell(col++).setCellValue(family_id);
        }
    }

    private int printPatternDescToExcelSheet(Sheet sheet, int rowNum, Pattern pattern, GenomesInfo gi, CogInfo cogInfo){
        try {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("ID=");
            row.createCell(1).setCellValue(pattern.getPatternId());
            row.createCell(2).setCellValue("Count=");
            row.createCell(3).setCellValue(pattern.getInstanceCount());
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

    public void printPattern(Pattern pattern, GenomesInfo gi, String family_id, CogInfo cogInfo){
        if (pattern != null) {
            pattern.calculateMainFunctionalCategory(cogInfo, false);

            countPrintedPatterns++;
            if(outputType == OutputType.XLSX) {
                printCSBLineToExcelSheet(catalogSheet, pattern, countPrintedPatterns, family_id, gi, cogInfo);
                if (patternsDescriptionSheet != null) {
                    nextLineIndexDescSheet = printPatternDescToExcelSheet(patternsDescriptionSheet,
                            nextLineIndexDescSheet, pattern, gi, cogInfo);
                }
            }else if(outputType == OutputType.TXT){
                String catalog_line = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

                catalog_line += DF.format(pattern.getScore()) + "\t"
                        + pattern.getInstanceCount() + "\t"
                        + DF.format(pattern.getInstanceCount() / (double) gi.getNumberOfGenomes()) + "\t"
                        + pattern.getExactInstanceCount() + "\t";

                catalog_line += pattern.getPattern() + "\t";

                if (cogInfoExists) {
                    catalog_line += pattern.getMainFunctionalCategory() + "\t";
                }
                if (family_id != null){
                    catalog_line += family_id;
                }

                if (catalogFile != null) {
                    catalogFile.write(catalog_line + "\n");
                }
            }
            printInstances(pattern, gi);

        }
    }

    public void printPattern(Pattern pattern, GenomesInfo gi, CogInfo cogInfo){
        printPattern(pattern, gi, null, cogInfo);
    }

}
