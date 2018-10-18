package Core;

import IO.MyLogger;
import IO.Readers;
import IO.Writer;
import Core.PostProcess.Family;
import Genomes.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;


public class Controller {

    private Parameters params;
    private MyLogger logger;
    private String output_path;
    private Writer writer;
    private String INPUT_PATH = "input/";


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

        output_path = createOutputPath();
        logger = new MyLogger("output/", params.debug);


        run();
    }

    private static void printUsageAndExit(JCommander jcommander, int exitStatus){
        jcommander.setProgramName("java -jar MainAlgorithm.jar");
        jcommander.usage();
        System.exit(exitStatus);
    }


    private Writer createWriter(boolean cog_info_exists){
        String parameters = "_ins" + params.max_insertion + "_q" + params.quorum2;
        String catalog_file_name = "Catalog_" + params.dataset_name + parameters;
        String instances_file_name = catalog_file_name + "_instances";
        boolean include_families = true;

        Writer writer = new Writer(params.max_error, params.max_deletion, params.max_insertion, params.debug, catalog_file_name,
                instances_file_name,
                include_families, params.output_file_type, cog_info_exists, params.non_directons, output_path);

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
    private List<Pattern> readPatternsFromFile() {
        List<Pattern> patterns = null;
        if (params.input_patterns_file_name != null) {
            //these arguments are not valid when input patterns are give
            params.min_pattern_length = 2;
            params.max_pattern_length = Integer.MAX_VALUE;

            String path = INPUT_PATH + params.input_patterns_file_name;
            patterns = Readers.readPatternsFromFile(path);
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

        logger.writeLogger("Building Data tree");
        System.out.println("Building Data tree");

        //read genomes
        GenomesInfo gi = new GenomesInfo();
        String genomes_file_path = INPUT_PATH + params.input_file_name;
        int number_of_genomes = Readers.readGenomes(genomes_file_path, gi);


        //cog info
        Map<String, COG> cog_info = null;
        boolean cog_info_exists = (params.cog_info_file_name != null);
        if (cog_info_exists) {
            cog_info = Readers.read_cog_info_table(INPUT_PATH + params.cog_info_file_name);
        }

        CogInfo cogInfo = new CogInfo();
        cogInfo.setCogInfo(cog_info);
        //gi.setCogInfo(cog_info);


        MemoryUtils.measure();

        if (number_of_genomes != -1) {

            CSBFinderWorkflow workflow = new CSBFinderWorkflow(gi);

            //read patterns from a file if a file is given
            List<Pattern> patternsFromFile = readPatternsFromFile();
            List<Family> families;

            if (patternsFromFile != null){
                families = workflow.run(params, patternsFromFile);
            }else{
                families = workflow.run(params);
            }


            System.out.println("Extracting CSBs from " + number_of_genomes + " input sequences.");


            System.out.println("Writing to files");
            writer = createWriter(cog_info_exists);

            for (Family family : families) {
                writer.printTopScoringPattern(family.getPatterns().get(0), gi, family.getFamilyId(), cogInfo);
                for (Pattern pattern : family.getPatterns()) {
                    writer.printPattern(pattern, gi, family.getFamilyId(), cogInfo);
                }
            }
            MemoryUtils.measure();


            writer.closeFiles();

            System.out.println(writer.getCountPrintedPatterns() + " CSBs found");

            float estimatedTime = (float) (System.nanoTime() - startTime) / (float) Math.pow(10, 9);
            logger.writeLogger("Took " + estimatedTime + " seconds");

            System.out.println("Took " + estimatedTime + " seconds");

        }else{
            String msg = "Could not read input sequences";
            System.out.println(msg);
            logger.writeLogger(msg);
            System.exit(1);
        }
    }
}



