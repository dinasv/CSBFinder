package IO;

import Genomes.*;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import Core.Parameters.OutputType;

import java.util.logging.Logger;

/**
 * Created by Dina on 19/05/2017.
 * Writes the output files:
 *      catalog_file: CSBs catalog
 *      instances_file: The strings in which each CSB has an instance
 */
public class Writer {
    //output files
    private FileOutputStream catalog_file_xls;
    private PrintWriter catalog_file;
    private PrintWriter instances_file;

    private SXSSFWorkbook catalog_workbook;
    private Sheet catalog_sheet;
    private Sheet filtered_patterns_sheet;
    private Sheet patterns_description_sheet;
    private String catalog_path;
    private String catalog_instances_path;

    private int max_error;
    private int max_deletion;
    private int max_insertion;
    private int count_printed_patterns;
    private int count_printed_filtered_patterns;
    private int next_line_index_desc_sheet;
    private boolean cog_info_exists;
    private boolean debug;
    private boolean non_directons;

    private static final String DELIMITER = "|";
    private static final DecimalFormat DF = new DecimalFormat("#.####");

    private OutputType output_file_type;

    public Logger logger = null;

    public Writer(int max_error, int max_deletion, int max_insertion, boolean debug, String catalog_file_name,
                  String instances_file_name, boolean include_families, OutputType output_file_type,
                  boolean cog_info_exists, boolean non_directons, String output_path){

        DF.setRoundingMode(RoundingMode.HALF_UP);

        this.max_error = max_error;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        this.debug = debug;
        count_printed_patterns = 0;
        count_printed_filtered_patterns = 0;
        next_line_index_desc_sheet = 0;

        this.output_file_type = output_file_type;
        this.cog_info_exists = cog_info_exists;
        this.non_directons = non_directons;

        catalog_sheet = null;
        filtered_patterns_sheet = null;
        patterns_description_sheet = null;

        init(catalog_file_name, instances_file_name, include_families, output_path);

    }
/*
    public void writeLogger(String msg){
        if (debug && logger != null){
            logger.info(msg);
        }
    }
*/
    private void init(String catalog_file_name, String instances_file_name, boolean include_families,
                      String output_path){

        catalog_path = output_path + catalog_file_name;
        catalog_instances_path = output_path +instances_file_name;
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

        if (output_file_type == OutputType.TXT) {
            catalog_path += ".txt";
            catalog_file = createOutputPrintWriter(catalog_path);
        } else if (output_file_type == OutputType.XLSX) {
            catalog_path += ".xlsx";
            try {
                catalog_file_xls = new FileOutputStream(catalog_path);
            } catch (Exception e) {
                System.out.println("Cannot create file " + catalog_path + ". Close the file first.");
                System.exit(1);
            }
        }

        catalog_instances_path += ".fasta";
        instances_file = createOutputPrintWriter(catalog_instances_path);
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
        if (cog_info_exists){
            header += "\tMain_Category";
        }
        if (include_families){
            header += "\tFamily_ID";
        }

        if (output_file_type == OutputType.TXT) {
            if (catalog_file != null) {
                catalog_file.write(header + "\n");
            }
        }else if(output_file_type == OutputType.XLSX) {
            catalog_workbook = new SXSSFWorkbook(10);
            catalog_sheet = catalog_workbook.createSheet("Catalog");
            writeHeaderToSheet(header, catalog_sheet);
            if (include_families) {
                filtered_patterns_sheet = catalog_workbook.createSheet("Filtered CSBs");
                writeHeaderToSheet(header, filtered_patterns_sheet);
            }
            if (cog_info_exists){
                patterns_description_sheet = catalog_workbook.createSheet("CSBs description");
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
        return count_printed_patterns;
    }

    public void closeFiles(){

        if (output_file_type == OutputType.TXT) {
            if (catalog_file != null) {
                catalog_file.close();
            }
        }else if(output_file_type == OutputType.XLSX){
            try {
                catalog_workbook.write(catalog_file_xls);
                catalog_file_xls.close();
                if (debug){
                    File file = new File(catalog_path);
                    if(file.delete()){
                        System.out.println(catalog_path + " deleted");
                    }
                }
            } catch (Exception e) {
                System.out.println("A problem occurred while trying to write to file "+catalog_path);
                System.exit(1);
            }
        }
        if (instances_file != null) {
            instances_file.close();
            if (debug){
                File file = new File(catalog_instances_path);
                if(file.delete()){
                    System.out.println(catalog_instances_path + " deleted");
                }else{
                    System.out.println(catalog_instances_path + " not deleted");

                }
            }
        }
    }

    private void printInstances(Pattern pattern, GenomesInfo gi){

        if (instances_file != null) {

            instances_file.println(">" + pattern.getPatternId() + "\t" + pattern.getPattern());

            for (Map.Entry<Integer, List<InstanceLocation>> entry : groupSameSeqInstances(pattern).entrySet()) {

                String seq_name = gi.genome_id_to_name.get(entry.getKey());
                instances_file.print(seq_name);

                List<InstanceLocation> instances_locations = entry.getValue();
                for (InstanceLocation instance_location : instances_locations){
                    String replicon_name = gi.replicon_id_to_name.get(instance_location.getRepliconId());

                    instances_file.print("\t" + replicon_name + "|[" + instance_location.getStartIndex() + ","
                            + instance_location.getEndIndex() + "]");
                }

                instances_file.print("\n");
            }

        }
    }

    /**
     * Group all instances of pattern that are located in the same sequence
     * @param pattern
     * @return
     */
    private Map<Integer, List<InstanceLocation>> groupSameSeqInstances(Pattern pattern){
        Map<Integer, List<InstanceLocation>> instance_seq_to_location = new HashMap<>();
        for (Instance instance : pattern.get_instances()) {

            int instance_length = instance.getLength();
            for (Map.Entry<Integer, List<InstanceLocation>> entry : instance.getInstanceLocations().entrySet()) {
                int seq_key = entry.getKey();

                if (!instance_seq_to_location.containsKey(seq_key)) {
                    instance_seq_to_location.put(seq_key, new ArrayList<InstanceLocation>());
                }
                List<InstanceLocation> instances_locations = instance_seq_to_location.get(seq_key);
                for (InstanceLocation instance_location : entry.getValue()) {
                    instance_location.setEndIndex(instance_length);
                    instances_locations.add(instance_location);
                }
            }
        }
        return instance_seq_to_location;
    }


    /**
     * Prints a pattern with the highest score in its family to a different sheet
     * @param pattern
     * @param gi
     * @param family_id
     */
    public void printFilteredCSB(Pattern pattern, GenomesInfo gi, String family_id){
        if(output_file_type == OutputType.XLSX){
            if (pattern != null) {
                count_printed_filtered_patterns++;
                printCSBLineToExcelSheet(filtered_patterns_sheet, pattern, count_printed_filtered_patterns, family_id, gi);
            }
        }
    }

    private void printCSBLineToExcelSheet(Sheet sheet, Pattern pattern, int row_num, String family_id, GenomesInfo gi){
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
        row.createCell(col++).setCellValue(String.join(DELIMITER, pattern.getPatternArr()));
        if (gi.cog_info != null) {
            row.createCell(col++).setCellValue(pattern.getMainFunctionalCategory());
        }
        if (family_id != null){
            row.createCell(col++).setCellValue(family_id);
        }
    }

    private int printPatternDescToExcelSheet(Sheet sheet, int row_num, Pattern pattern, GenomesInfo gi){
        Row row = sheet.createRow(row_num++);
        row.createCell(0).setCellValue("ID=");
        row.createCell(1).setCellValue(pattern.getPatternId());
        row.createCell(2).setCellValue("Count=");
        row.createCell(3).setCellValue(pattern.getInstanceCount());
        row.createCell(4).setCellValue("Score=");
        row.createCell(5).setCellValue(pattern.getScore());

        for (String cog : pattern.getPatternArr()){
            row = sheet.createRow(row_num++);
            if (non_directons){
                cog = cog.substring(0, cog.length()-1);
            }
            COG cog_obj = gi.cog_info.get(cog);

            row.createCell(0).setCellValue(cog);
            if (cog_obj!=null) {
                row.createCell(1).setCellValue(cog_obj.getCog_desc());
            }else{
                row.createCell(1).setCellValue("-");
            }
        }
        return row_num+1;
    }

    public void printPattern(Pattern pattern, GenomesInfo gi, String family_id){
        if (pattern != null) {
            count_printed_patterns++;
            if(output_file_type == OutputType.XLSX) {
                printCSBLineToExcelSheet(catalog_sheet, pattern, count_printed_patterns, family_id, gi);
                if (patterns_description_sheet != null) {
                    next_line_index_desc_sheet = printPatternDescToExcelSheet(patterns_description_sheet,
                            next_line_index_desc_sheet, pattern, gi);
                }
            }else if(output_file_type == OutputType.TXT){
                String catalog_line = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

                catalog_line += DF.format(pattern.getScore()) + "\t"
                        + pattern.getInstanceCount() + "\t"
                        + DF.format(pattern.getInstanceCount() / (double) gi.getNumberOfGenomes()) + "\t"
                        + pattern.getExactInstanceCount() + "\t";

                catalog_line += String.join(DELIMITER, pattern.getPatternArr()) + "\t";

                if (cog_info_exists) {
                    catalog_line += pattern.getMainFunctionalCategory() + "\t";
                }
                if (family_id != null){
                    catalog_line += family_id;
                }

                if (catalog_file != null) {
                    catalog_file.write(catalog_line + "\n");
                }
            }
            printInstances(pattern, gi);

        }
    }

    public void printPattern(Pattern pattern, GenomesInfo gi){
        printPattern(pattern, gi, null);
    }

}
