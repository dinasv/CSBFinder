package Main;

import SuffixTrees.Edge;
import SuffixTrees.InstanceNode;
import Utils.*;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Created by Dina on 19/05/2017.
 * Writes the output files:
 *      catalog_file: OGMs catalog
 *      motif_instances_file: The strings in which each OGM has an instance
 */
public class Writer {
    //output files
    private PrintWriter catalog_file;
    private PrintWriter motif_instances_file;

    private SXSSFWorkbook catalog_workbook;
    private Sheet catalog_sheet;
    private Sheet filtered_motifs_sheet;
    String catalog_path;

    private DecimalFormat df;

    private int max_error;
    private int max_deletion;
    private int max_insertion;
    private int count_printed_motifs;
    private int count_printed_filtered_motifs;
    private boolean debug;


    public Writer(int max_error, int max_deletion, int max_insertion, boolean debug, String catalog_path,
                  String motif_instances_path, boolean include_families){
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);

        this.max_error = max_error;
        this.max_deletion = max_deletion;
        this.max_insertion = max_insertion;
        this.debug = debug;
        count_printed_motifs = 0;
        count_printed_filtered_motifs = 0;


        this.catalog_path = catalog_path;
        createOutputDirectory();
        createFiles(catalog_path, motif_instances_path, include_families);

    }

    private void createOutputDirectory(){
        try {
            new File("output").mkdir();
        }catch (SecurityException e){
            System.out.println("The directory 'output' could not be created, therefore no output is printed. " +
                    "Please create a directory named 'output' in the following path: " + System.getProperty("user.dir"));
        }
    }

    private void createFiles(String catalog_path, String motif_instances_path, boolean include_families){

        catalog_file = createOutputFile(catalog_path);
        catalog_workbook = new SXSSFWorkbook(10);
        catalog_sheet = catalog_workbook.createSheet("Catalog");
        createMotifHeader(catalog_sheet, include_families);
        if (include_families){
            filtered_motifs_sheet = catalog_workbook.createSheet("Filtered OGMs");
            createMotifHeader(filtered_motifs_sheet, include_families);
        }

        motif_instances_file = createOutputFile(motif_instances_path);


    }

    private void createMotifHeader(Sheet sheet, boolean include_families){
        String header = "ID\tLength\tScore\tInstance Count\tInstance Ratio\tExact Instance Count" +
                "\tOGM\tMain Category";
        if (include_families){
            header += "\tFamily ID";
        }
        Row row = sheet.createRow(0);
        int i = 0;
        for (String str: header.split("\t")){
            row.createCell(i++).setCellValue(str);
        }

        if (catalog_file != null) {
            catalog_file.write(header+ "\n");
        }
    }

    public int getCountPrintedMotifs(){
        return  count_printed_motifs;
    }

    public void closeFiles(){
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(catalog_path+".xlsx");
            catalog_workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (catalog_file != null) {
            catalog_file.close();
        }
        if (motif_instances_file != null) {
            motif_instances_file.close();
        }
    }

    private void printMotifInstances(Motif motif){
        if (motif_instances_file != null) {
            motif_instances_file.print("motif_" + motif.getMotif_id() + "\t");

            HashMap<Integer, String> instance_seq_and_location = new HashMap<>();
            for (Instance instance : motif.get_instances()) {
                InstanceNode instance_node = instance.getNodeInstance();
                if (instance.getEdge() != null) {
                    Edge edge = instance.getEdge();
                    instance_node = (InstanceNode) edge.getDest();
                }
                int instance_length = instance.getLength();
                for (Map.Entry<Integer, ArrayList<String>> entry : instance_node.getResults().entrySet()) {
                    int seq_id = entry.getKey();
                    String word_id = entry.getValue().get(0);
                    word_id += "_l" + instance_length;
                    instance_seq_and_location.put(seq_id, word_id);

                }
            }
            for (Map.Entry<Integer, String> entry : instance_seq_and_location.entrySet()) {
                int seq = entry.getKey();
                String word_id = entry.getValue();
                motif_instances_file.print("seq" + seq + "_" + word_id + "\t");
            }
            motif_instances_file.print("\n");
        }
    }

    public void printFilteredMotif(Motif motif, Utils utils, String family_id){
        if (motif != null) {
            count_printed_filtered_motifs++;
            printToExcelSheet(filtered_motifs_sheet, motif, count_printed_filtered_motifs, family_id, utils);
        }
    }

    private void printToExcelSheet(Sheet sheet, Motif motif, int row_num, String family_id, Utils utils){
        Row row = sheet.createRow(row_num);
        int col = 0;
        row.createCell(col++).setCellValue(motif.getMotif_id());
        row.createCell(col++).setCellValue(motif.getLength());
        row.createCell(col++).setCellValue(df.format(motif.getScore()));
        row.createCell(col++).setCellValue(motif.get_instance_count());
        row.createCell(col++).setCellValue(df.format(motif.get_instance_count() / (double) utils.datasets_size.get(0)));
        row.createCell(col++).setCellValue(motif.get_exact_instance_count());
        row.createCell(col++).setCellValue(String.join("-", motif.getMotif_arr()));
        row.createCell(col++).setCellValue(motif.getMain_functional_category());
        if (family_id != null){
            row.createCell(col++).setCellValue(family_id);
        }
    }

    public void printMotif(Motif motif, Utils utils, String family_id){
        if (motif != null) {
            count_printed_motifs++;
            printToExcelSheet(catalog_sheet, motif, count_printed_motifs, family_id, utils);

            printMotifInstances(motif);

            String motifs_catalog_line = motif.getMotif_id() + "\t" + motif.getLength() + "\t";

            motifs_catalog_line += df.format(motif.getScore()) + "\t"
                    + motif.get_instance_count() + "\t"
                    + df.format(motif.get_instance_count() / (double) utils.datasets_size.get(0)) + "\t"
                    + motif.get_exact_instance_count() + "\t";

            motifs_catalog_line += String.join("-", motif.getMotif_arr());

            if (catalog_file != null) {
                catalog_file.write(motifs_catalog_line + "\n");
            }
        }
    }

    public void printMotif(Motif motif, Utils utils){
        printMotif(motif, utils, null);
    }



    private PrintWriter createOutputFile(String path){

        try {
            new File("output").mkdir();
            try {

                PrintWriter output_file = new PrintWriter(path + ".txt", "UTF-8");
                return output_file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }catch (SecurityException e){
            System.out.println("The directory 'output' could not be created, therefore no output is printed. " +
                    "Please create a directory named 'output' in the following path: " + System.getProperty("user.dir"));
        }
        return null;
    }


}
