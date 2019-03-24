package IO;

import Model.OrthologyGroups.CogInfo;
import Model.Genomes.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;

/**
 * Writes the output files:
 *      catalogFile: CSBs catalog
 *      instancesFile: The strings in which each CSB has an instance
 */
public class Writer {

    private PatternsWriter patternsWriter;

    private PrintWriter instancesFile;

    private String catalogInstancesPath;

    private boolean debug;

    private static final DecimalFormat DF = new DecimalFormat("#.####");


    public Writer(boolean debug, String instancesFileName, String outputPath,
                  PatternsWriter patternsWriter){

        this.patternsWriter = patternsWriter;

        this.debug = debug;

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

    public static void printInstances(Pattern pattern, int familyId, GenomesInfo gi, PrintWriter instancesFile){

        if (instancesFile != null) {

            String catalogLine = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

            catalogLine += DF.format(pattern.getScore()) + "\t"
                    + pattern.getInstancesPerGenome() + "\t"
                    + pattern.toString() + "\t";

            catalogLine += familyId;

            instancesFile.println(">" + catalogLine);

            int genomeId = -1;
            for (InstanceLocation instanceLocation : pattern.getPatternLocations().getSortedLocations()) {

                Genome genome = null;
                String genomeName;

                if (instanceLocation.getGenomeId() != genomeId) {
                    genome = gi.getGenome(instanceLocation.getGenomeId());
                    genomeName = genome.getName();

                    instancesFile.print(genomeName);
                }

                if (genome == null){
                    continue;
                }

                String repliconName = genome.getReplicon(instanceLocation.getRepliconId()).getName();
                instancesFile.print(String.format("\t%s|[%d,%d]", repliconName,
                        instanceLocation.getActualStartIndex(), instanceLocation.getActualEndIndex()));


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

    private void printInstances(Family family, GenomesInfo gi){
        createInstancesFile();
        family.getPatterns().forEach(pattern -> printInstances(pattern, family.getFamilyId(), gi, instancesFile));
    }

    public void printInstances(List<Family> families, GenomesInfo gi){
        createInstancesFile();
        families.forEach(family -> printInstances(family, gi));
    }

}
