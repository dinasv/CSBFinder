package Core;

import Core.OrthologyGroups.COG;
import Core.OrthologyGroups.CogInfo;
import Core.Patterns.Pattern;
import IO.*;
import Core.PostProcess.Family;
import Core.Genomes.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import IO.Writer;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Controller {

    private Parameters params;
    private MyLogger logger;
    private String outputPath;
    private Writer writer;
    CogInfo cogInfo;

    private String arguments;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Controller(String[] args){
        JCommander jcommander = null;
        try {
            params = new Parameters();

            jcommander = JCommander.newBuilder().addObject(params).build();
            jcommander.parse(args);
            if (params.help){
                printUsageAndExit(jcommander, 0);
            }

        }catch (ParameterException e){
            System.err.println(e.getMessage());

            jcommander = JCommander.newBuilder().addObject(params).build();
            printUsageAndExit(jcommander, 1);
        }

        arguments = String.join(" ", args);
        cogInfo = new CogInfo();
        outputPath = createOutputPath();
        logger = new MyLogger("output/", params.debug);

        run();
    }

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar CSBFinder.jar");
        jcommander.usage();
        System.exit(exitStatus);
    }


    private Writer writeFamiliesToFiles(List<Family> families, boolean cog_info_exists, GenomesInfo genomesInfo,
                                                CogInfo cogInfo){

        String parameters = "_ins" + params.maxInsertion + "_q" + params.quorum2;
        String catalogFileName = "Catalog_" + params.datasetName + parameters;
        String instancesFileName = catalogFileName + "_instances";

        String catalogPath = outputPath + catalogFileName;
        //TODO: addGene as input parameter
        boolean includeFamilies = true;

        PatternsWriter patternsWriter = null;

        switch (params.outputFileType){
            case TXT:
                patternsWriter = new TextWriter(cog_info_exists, includeFamilies, catalogPath);
                break;
            case XLSX:
                patternsWriter = new ExcelWriter(cog_info_exists, includeFamilies, catalogPath);
                break;
            case EXPORT:
                SessionWriter sessionWriter = new SessionWriter(includeFamilies, catalogPath, genomesInfo);
                sessionWriter.writeHeader(arguments);
                sessionWriter.writeGenomes(genomesInfo.getGenomesByName());
                patternsWriter = sessionWriter;
                break;
        }

        Writer writer = new Writer(params.debug, catalogFileName,
                instancesFileName, includeFamilies, cog_info_exists,
                outputPath, patternsWriter);

        if (params.outputFileType != OutputType.EXPORT){
            writer.printInstances(families, genomesInfo, cogInfo);
            writer.writeHeader(createHeader(includeFamilies));
        }

        writer.printFamilies(families, cogInfo);
        writer.closeFiles();

        return writer;
    }

    private String createHeader(boolean include_families){

        String header = "FAMILY_ID\tLength\tScore\tInstance_Count\tCSB";
        if (cogInfo.cogInfoExists()){
            header += "\tMain_Category";
        }
        if (include_families){
            header += "\tFamily_ID";
        }

        return header;
    }


    private String createOutputPath(){
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("dd_MM_yyyy_hh_mm_ss_a");

        String path = params.outputDir;
        Writer.createOutputDirectory(path);
        path += "/"+ft.format(dNow)+"/";
        Writer.createOutputDirectory(path);

        return path;
    }

    public void run() {

        pipeline();

        if (params.debug){
            MemoryUtils.measure();
            long actualMemUsed = MemoryUtils.getActualMaxUsedMemory();

            System.out.println("Memory used: " + actualMemUsed);
            logger.writeLogger("Memory used: " + actualMemUsed);

        }

    }

    /**
     * Read patterns from a file if a file is given
     * @return
     */
    private List<Pattern> readPatternsFromFile() throws Exception{
        List<Pattern> patterns = new ArrayList<>();
        String path = params.inputPatternsFilePath;

        if (path != null) {

            patterns = Parsers.parsePatternsFile(path);
        }
        return patterns;
    }

    private List<Pattern> readPatternsReferenceGenomesFile(GenomesInfo genomesInfo) throws Exception{
        List<Pattern> patterns = new ArrayList<>();
        String path = params.referenceGenomesPath;

        if (path != null) {

            patterns = Parsers.parseReferenceGenomesFile(genomesInfo, path);
        }
        return patterns;
    }

    /**
     * Executes CSBFinder and prints colinear synteny blocks
     *
     * @return
     * @throws Exception
     */

    private void pipeline(){

        long startTime = System.nanoTime();

        //read genomes
        GenomesInfo gi;
        String genomesFilePath = params.inputFilePath;

        try {
            printToScreen("Parsing input genomes file");
            gi = Parsers.parseGenomesFile(genomesFilePath);

        }catch (IOException e){
            printToScreen("Input genome file is not valid. " + e.getMessage());
            return;
        }

        //cog info
        Map<String, COG> cogInfoMap = null;
        boolean cogInfoExists = (params.cogInfoFilePath != null);
        if (cogInfoExists) {
            printToScreen("Parsing orthology group information file");

            try {
                cogInfoMap = Parsers.parseCogInfoTable(params.cogInfoFilePath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        cogInfo.setCogInfo(cogInfoMap);

        MemoryUtils.measure();

        if (gi.getNumberOfGenomes() >= 0) {

            logger.writeLogger("Executing workflow");
            printToScreen("Executing workflow");

            CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);
            Algorithm algorithm = params.algorithmType.algorithm;
            workflow.setAlgorithm(algorithm);

            //read patterns from a file if a file is given
            List<Pattern> patternsFromFile = new ArrayList<>();
            try {
                patternsFromFile = readPatternsFromFile();
                if (patternsFromFile.size() == 0 ){
                    patternsFromFile = readPatternsReferenceGenomesFile(gi);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            workflow.setPatternsFromFile(patternsFromFile);

            printToScreen(String.format("Extracting CSBs from %d input sequences.", gi.getNumberOfGenomes()));

            List<Family> families = workflow.run(params);

            printToScreen(String.format("%d CSBs found.", workflow.getPatternsCount()));

            printToScreen("Writing to files");

            writer = writeFamiliesToFiles(families, cogInfoExists, gi, cogInfo);

            MemoryUtils.measure();

            printToScreen(String.format("%d CSBs written to files", writer.getCountPrintedPatterns()));

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            logger.writeLogger("Took " + estimatedTime + " seconds");

            printToScreen(String.format("Took %f seconds", estimatedTime));

        }else{
            String msg = "Could not read input sequences";
            System.out.println(msg);
            logger.writeLogger(msg);
            System.exit(1);
        }
    }

    private void printToScreen(String msg){
        Date date = new Date();

        System.out.println(DATE_FORMAT.format(date) + ": " + msg);
    }
}



