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

    private String directoryPath;

    private boolean debug;

    private static final DecimalFormat DF = new DecimalFormat("#.####");


    public Writer(boolean debug, String instancesFileName, String outputPath,
                  PatternsWriter patternsWriter){

        this(debug, patternsWriter);

        directoryPath = outputPath;
        catalogInstancesPath = directoryPath + instancesFileName + ".fasta";
    }

    public Writer(boolean debug,
                  PatternsWriter patternsWriter){

        this.patternsWriter = patternsWriter;

        this.debug = debug;

        instancesFile = null;

        catalogInstancesPath = null;

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
            return new PrintWriter(path, "UTF-8");
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
                deleteFile(catalogInstancesPath);
                deleteFile(patternsWriter.getPath());
                deleteFile(directoryPath);
            }
        }
    }

    private void deleteFile(String path){
        File file = new File(path);
        if(file.delete()){
            System.out.println(path + " deleted");
        }else{
            System.out.println(path + " not deleted");
        }
    }

    public static void printInstances(Pattern pattern, int familyId, GenomesInfo gi, PrintWriter instancesFile){

        if (instancesFile == null) {
            return;
        }
        String catalogLine = pattern.getPatternId() + "\t" + pattern.getLength() + "\t";

        catalogLine += DF.format(pattern.getScore()) + "\t"
                + pattern.getInstancesPerGenomeCount() + "\t"
                + pattern.toString() + "\t";

        catalogLine += familyId;

        instancesFile.println(">" + catalogLine);

        List<String> replicons = new ArrayList<>();
        int genomeId = -1;
        String genomeName = "";
        Genome genome = null;

        for (InstanceLocation instanceLocation : pattern.getPatternLocations().getSortedLocations()) {

            if (instanceLocation.getGenomeId() != genomeId) {

                if (genomeId != -1) {
                    printGenomeInstancesLine(genomeName, replicons, instancesFile);
                    replicons.clear();
                }

                genome = gi.getGenome(instanceLocation.getGenomeId());
                genomeId = genome.getId();
                genomeName = genome.getName();

            }

            if (genome == null){
                continue;
            }

            String repliconName = genome.getReplicon(instanceLocation.getRepliconId()).getName();
            replicons.add(String.format("\t%s|[%d,%d]", repliconName,
                    instanceLocation.getActualStartIndex(), instanceLocation.getActualEndIndex()));

        }

        printGenomeInstancesLine(genomeName, replicons, instancesFile);
    }

    private static void printGenomeInstancesLine(String genomeName, List<String> replicons, PrintWriter instancesFile){
        instancesFile.print(genomeName);

        for (String replicon : replicons) {
            instancesFile.print(replicon);
        }
        instancesFile.println();
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
