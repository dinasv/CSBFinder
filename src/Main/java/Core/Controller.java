package Core;

import IO.*;
import Core.PostProcess.Family;
import Core.Genomes.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Controller {

    private Parameters params;
    private MyLogger logger;
    private String outputPath;
    private Writer writer;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public Controller(String [ ] args){
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

        outputPath = createOutputPath();
        logger = new MyLogger("output/", params.debug);


        run();
    }

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar MainAlgorithm.jar");
        jcommander.usage();
        System.exit(exitStatus);
    }


    private Writer createWriter(boolean cog_info_exists, GenomesInfo genomesInfo){
        String parameters = "_ins" + params.maxInsertion + "_q" + params.quorum2;
        String catalogFileName = "Catalog_" + params.datasetName + parameters;
        String instancesFileName = catalogFileName + "_instances";

        String catalogPath = outputPath + catalogFileName;
        //TODO: add as input parameter
        boolean includeFamilies = true;

        PatternsWriter patternsWriter = null;

        switch (params.outputFileType){
            case TXT:
                patternsWriter = new TextWriter(cog_info_exists, includeFamilies, params.nonDirectons, catalogPath);
                break;
            case XLSX:
                patternsWriter = new ExcelWriter(cog_info_exists, includeFamilies, params.nonDirectons, catalogPath);
                break;
            case EXPORT:
                SessionWriter sessionWriter = new SessionWriter(includeFamilies, params.nonDirectons, catalogPath, genomesInfo);
                sessionWriter.writeGenomes(genomesInfo.getGenomesMap());
                patternsWriter = sessionWriter;
                break;
        }


        Writer writer = new Writer(params.debug, catalogFileName,
                instancesFileName, includeFamilies, cog_info_exists, params.nonDirectons,
                outputPath, patternsWriter);

        return writer;
    }

    private static String createOutputPath(){
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("dd_MM_yyyy_hh_mm_ss_a");

        String path = "output";
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
     * Read patterns from a file if a file is given, and put them in a suffix trie
     * @return
     */
    private List<Pattern> readPatternsFromFile() throws Exception{
        List<Pattern> patterns = null;
        if (params.inputPatternsFilePath != null) {
            //these arguments are not valid when input patterns are give
            params.minPatternLength = 2;
            params.maxPatternLength = Integer.MAX_VALUE;

            String path = params.inputPatternsFilePath;
            patterns = Parsers.parsePatternsFile(path);
        }
        return patterns;
    }

    /**
     * Executes MainAlgorithm and prints colinear synteny blocks
     *
     * @return
     * @throws Exception
     */

    private void pipeline(){

        long startTime = System.nanoTime();

        //read genomes
        GenomesInfo gi = new GenomesInfo();
        String genomes_file_path = params.inputFilePath;

        int number_of_genomes = -1;
        try {
            printToScreen("Parsing input genomes file");
            number_of_genomes = Parsers.parseGenomesFile(genomes_file_path, gi);
        }catch (IOException e){
            printToScreen("Input genome file is not valid. " + e.getMessage());
            return;
        }

        //cog info
        Map<String, COG> cog_info = null;
        boolean cog_info_exists = (params.cogInfoFilePath != null);
        if (cog_info_exists) {
            printToScreen("Parsing orthology group information file");

            try {
                cog_info = Parsers.parseCogInfoTable(params.cogInfoFilePath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        CogInfo cogInfo = new CogInfo();
        cogInfo.setCogInfo(cog_info);

        MemoryUtils.measure();

        if (number_of_genomes != -1) {

            logger.writeLogger("Executing workflow");
            printToScreen("Executing workflow");

            CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

            //read patterns from a file if a file is given
            List<Pattern> patternsFromFile = new ArrayList<>();
            try {
                patternsFromFile = readPatternsFromFile();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            List<Family> families;

            printToScreen(String.format("Extracting CSBs from %d input sequences.", number_of_genomes));

            if (patternsFromFile != null){
                families = workflow.run(params, patternsFromFile);
            }else{
                families = workflow.run(params);
            }

            printToScreen(String.format("%d CSBs found.", workflow.getPatternsCount()));

            printToScreen("Writing to files");

            writer = createWriter(cog_info_exists, gi);

            for (Family family : families) {
                writer.printFamily(family, gi, cogInfo);
            }
            MemoryUtils.measure();

            writer.closeFiles();

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



