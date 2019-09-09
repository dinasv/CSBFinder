package IO;

import Model.Genomes.GenomesInfo;
import Model.OrthologyGroups.CogInfo;
import Model.OutputType;
import Model.Parameters;
import Model.PostProcess.Family;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 */
public class WriteUtils {

    public static Writer saveSessionFile(List<Family> families, GenomesInfo genomesInfo, CogInfo cogInfo,
                                         Parameters params,
                                         String arguments, File currSession){

        SessionWriter sessionWriter = new SessionWriter(currSession.getPath(), genomesInfo);
        sessionWriter.writeHeader(arguments);
        sessionWriter.writeGenomes(genomesInfo.getGenomesByName());

        Writer writer = new Writer(params.debug, sessionWriter);

        writer.printFamilies(families, cogInfo);
        writer.closeFiles();

        return writer;
    }

    public static Writer writeExportFiles(List<Family> families, GenomesInfo genomesInfo,
                                          CogInfo cogInfo, Parameters params, String arguments){

        String outputPath = createOutputPath(params.outputDir);

        String parameters = "_ins" + params.maxInsertion + "_q" + params.quorum2;
        String catalogFileName = params.datasetName + parameters;
        String instancesFileName = catalogFileName + "_instances";

        String catalogPath = outputPath + catalogFileName;
        //TODO: add as input parameter
        boolean includeFamilies = true;

        PatternsWriter patternsWriter = null;

        switch (params.outputFileType){
            case TXT:
                patternsWriter = new TextWriter(cogInfo.cogInfoExists(), includeFamilies, catalogPath);
                break;
            case XLSX:
                patternsWriter = new ExcelWriter(cogInfo.cogInfoExists(), includeFamilies, catalogPath);
                break;
            case SESSION:
                SessionWriter sessionWriter = new SessionWriter(catalogPath, genomesInfo);
                sessionWriter.writeHeader(arguments);
                sessionWriter.writeGenomes(genomesInfo.getGenomesByName());
                patternsWriter = sessionWriter;
                break;
        }

        Writer writer = new Writer(params.debug, instancesFileName, outputPath, patternsWriter);

        if (params.outputFileType != OutputType.SESSION){
            writer.printInstances(families, genomesInfo);
            writer.writeHeader(createHeader(cogInfo));
        }

        writer.printFamilies(families, cogInfo);
        writer.closeFiles();

        return writer;
    }

    private static String createHeader(CogInfo cogInfo){

        String header = "CSB_ID\tLength\tScore\tInstance_Count\tCSB";
        if (cogInfo.cogInfoExists()){
            header += "\tMain_Category";
        }

        header += "\tFamily_ID";


        return header;
    }

    private static String createOutputPath(String outputDir){
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("dd_MM_yyyy_hh_mm_ss_a");

        String path = outputDir;
        Writer.createOutputDirectory(path);
        path += "/"+ft.format(dNow)+"/";
        Writer.createOutputDirectory(path);

        return path;
    }
}
