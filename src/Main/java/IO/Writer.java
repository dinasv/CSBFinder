package IO;

import Core.CogInfo;
import Core.Genomes.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import Core.PostProcess.Family;

import java.util.logging.Logger;

/**
 * Writes the output files:
 *      catalogFile: CSBs catalog
 *      instancesFile: The strings in which each CSB has an instance
 */
public class Writer {

    private PatternsWriter patternsWriter;

    private PrintWriter instancesFile;

    private String catalogInstancesPath;

    private int countPrintedPatterns;
    private boolean cogInfoExists;
    private boolean debug;

    private static final DecimalFormat DF = new DecimalFormat("#.####");

    public Logger logger = null;

    //String header;

    public Writer(boolean debug, String catalogFileName,
                  String instancesFileName, boolean includeFamilies,
                  boolean cogInfoExists, String outputPath,
                  PatternsWriter patternsWriter){

        this.patternsWriter = patternsWriter;

        this.debug = debug;
        countPrintedPatterns = 0;

        this.cogInfoExists = cogInfoExists;
        catalogInstancesPath = outputPath + instancesFileName + ".fasta";

        instancesFile = null;
    }

    public void writeHeader(String header){
        this.patternsWriter.writeHeader(header);

    }

    private void createInstancesFile(){
        if (instancesFile == null) {
            instancesFile = createOutputPrintWriter(catalogInstancesPath);
        }
    }

    public static PrintWriter createOutputPrintWriter(String path){
        try {
            PrintWriter output_file = new PrintWriter(path, "UTF-8");

            return output_file;
        } catch (Exception e) {
            System.out.println("Cannot create file " + path);
            System.exit(1);
        }
        return null;
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


    public int getCountPrintedPatterns(){
        return patternsWriter.getCountPrintedPatterns();
    }

    public void closeFiles(){

        patternsWriter.closeFile();

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

    public static void printInstances(Pattern pattern, String familyId, GenomesInfo gi, PrintWriter instancesFile){

        if (instancesFile != null) {

            String catalogLine = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

            catalogLine += DF.format(pattern.getScore()) + "\t"
                    + pattern.getInstancesPerGenome() + "\t"
                    + pattern.toString() + "\t";

            catalogLine += familyId;

            instancesFile.println(">" + catalogLine);

            for (Map.Entry<Integer, PatternLocationsInGenome> entry : pattern.getPatternLocations().entrySet()) {

                Genome genome = gi.getGenome(entry.getKey());
                String genomeName = genome.getName();
                String repliconName;

                instancesFile.print(genomeName);

                PatternLocationsInGenome patternLocationsInGenome = entry.getValue();
                for (List<InstanceLocation> instanceLocationsInReplicon : patternLocationsInGenome.getSortedLocations().values()){
                    for (InstanceLocation instanceLocation: instanceLocationsInReplicon) {

                        repliconName = genome.getReplicon(instanceLocation.getRepliconId()).getName();

                        instancesFile.print(String.format("\t%s|[%d,%d]", repliconName,
                                instanceLocation.getActualStartIndex(), instanceLocation.getActualEndIndex()));
                    }
                }

                instancesFile.println();
            }

        }
    }

    public void printFamilies(List<Family> families, CogInfo cogInfo){
        if (families == null | cogInfo == null){
            return;
        }

        if (cogInfo.cogInfoExists()) {
            families.forEach(family -> family.getPatterns()
                    .forEach(pattern -> pattern.calculateMainFunctionalCategory(cogInfo)));
        }
        patternsWriter.write(families, cogInfo);
    }

    public void printInstances(Family family, GenomesInfo gi, CogInfo cogInfo){
        createInstancesFile();
        family.getPatterns().forEach(pattern -> printInstances(pattern, family.getFamilyId(), gi, instancesFile));
    }

    public void printInstances(List<Family> families, GenomesInfo gi, CogInfo cogInfo){
        createInstancesFile();
        families.forEach(family -> printInstances(family, gi, cogInfo));
    }

}
