package Model;

import Model.OrthologyGroups.COG;
import Model.OrthologyGroups.CogInfo;
import Model.Patterns.Pattern;
import IO.*;
import Model.PostProcess.Family;
import Model.Genomes.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import IO.Writer;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Controller {

    private Parameters params;
    private MyLogger logger;

    private Writer writer;
    private CogInfo cogInfo;

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

        logger = new MyLogger("output/", params.debug);

        run();
    }

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar CSBFinder.jar");
        jcommander.usage();
        System.exit(exitStatus);
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
    private List<Pattern> readPatternsFromFile() throws IOException, IllegalArgumentException {
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
     */

    private void pipeline(){

        long startTime = System.nanoTime();

        //read genomes
        GenomesInfo gi;
        String genomesFilePath = params.inputFilePath;

        try {
            printToScreen("Parsing input genomes file");
            gi = Parsers.parseGenomesFile(genomesFilePath);

        }catch (Exception e){
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
            Algorithm algorithm = params.algorithmType.getAlgorithm();
            workflow.setAlgorithm(algorithm);

            //read patterns from a file if a file is given
            List<Pattern> patternsFromFile = new ArrayList<>();
            List<Pattern> refGenomesAsPatterns = new ArrayList<>();
            try {
                patternsFromFile = readPatternsFromFile();
                if (patternsFromFile.size() == 0 ){
                    refGenomesAsPatterns = readPatternsReferenceGenomesFile(gi);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            workflow.setPatternsFromFile(patternsFromFile);
            workflow.setRefGenomesAsPatterns(refGenomesAsPatterns);

            printToScreen(String.format("Extracting CSBs from %d input sequences.", gi.getNumberOfGenomes()));

            workflow.run(params);

            printToScreen(String.format("%d CSBs found.", workflow.getPatternsCount()));

            printToScreen("Writing to files");

            //writer = writeExportFiles(families, cogInfoExists, gi, cogInfo);
            writer = WriteUtils.writeExportFiles(workflow.getFamilies(), gi, cogInfo, params, arguments);

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



