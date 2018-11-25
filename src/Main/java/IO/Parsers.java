package IO;

import Core.Genomes.*;
import Core.PostProcess.Family;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Parsers {

    final static String CSB_DELIMITER = ",";

    public static List<Pattern> parsePatternsFile(String inputPatternsFilePath)
            throws IOException, IllegalArgumentException{

        List<Pattern> patterns = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputPatternsFilePath))) {

            String line = br.readLine();
            int lineNumber = 0;

            int patternId = 0;
            while (line != null) {
                if (line.charAt(0) == '>') {

                    patternId = castToInteger(line.substring(1), "id", lineNumber, inputPatternsFilePath);

                } else {
                    List<Gene> genes = parseGenes(line, lineNumber, inputPatternsFilePath);
                    Pattern pattern = new Pattern(patternId, genes);
                    patterns.add(pattern);
                }
                line = br.readLine();
                lineNumber++;
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + inputPatternsFilePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + inputPatternsFilePath);
        }
        return patterns;
    }

    private static List<Gene> parseGenes(String line, int lineNumber, String inputPatternsFilePath) throws IllegalArgumentException{
        String[] patternArr = line.split(CSB_DELIMITER);
        List<Gene> genes = new ArrayList<>();
        if (patternArr.length > 1) {
            for (String gene: patternArr){
                if (gene.length() > 0) {
                    String lastChar = gene.substring(gene.length() - 1);
                    Strand strand = determineStrand(lastChar);

                    if (strand != Strand.INVALID){
                        gene = gene.substring(0, gene.length()-1);
                    }

                    genes.add(new Gene(gene, strand));
                }
            }
        }else{
            throw new IllegalArgumentException(errorMessage(String.format("Genes delimited by \"%s\"", CSB_DELIMITER),
                    line, lineNumber, inputPatternsFilePath));
        }
        return genes;
    }


    /**
     * Parse {@code rawLine} containing a gene and its numericValue separated by TAB, and create {@link Genomes.Gene}.
     * A numericValue must be "+" or "-"
     * <p>
     * Format: [gene id][TAB][numericValue]
     * <p>
     * Valid examples:
     * COG1234[TAB]+
     * abc[TAB]-
     *
     * @param rawLine
     * @param lineNumber of {@code rawLine} in {@code filePath}
     * @param filePath
     */
    private static Gene parseGeneLine(String rawLine, int lineNumber, String filePath) throws IllegalArgumentException {

        Objects.requireNonNull(rawLine, "rawLine is null");
        Objects.requireNonNull(filePath, "filePath is null");

        String[] splitLine = rawLine.trim().split("\t");
        if (splitLine.length < 2) {
            throw new IllegalArgumentException(errorMessage("[gene id][TAB][numericValue]", rawLine, lineNumber, filePath));
        }

        String geneId = splitLine[0];
        String rawStrand = splitLine[1];

        Strand strand = determineStrand(rawStrand);
        if (strand == Strand.INVALID) {
            throw new IllegalArgumentException(errorMessage("numericValue to be + or -",
                    rawStrand, lineNumber, filePath));
        }

        return new Gene(geneId, strand);
    }

    /**
     * Parse rawTitle containing a genome name and a replicon name
     * replicon name must be unique
     * <p>
     * Format: [genome name]|[replicon name]
     * <p>
     * Valid examples:
     * Acaryochloris_marina_MBIC11017_uid58167|NC_009927
     *
     * @param rawTitle
     * @param replicon
     */
    private static String[] parseGenomeTitle(String rawTitle, int lineNumber, String filePath) throws IllegalArgumentException{

        Objects.requireNonNull(rawTitle, "rawTitle is null");
        Objects.requireNonNull(filePath, "filePath is null");

        String rawTitleSuffix = rawTitle.substring(1); //remove ">"
        String[] title = rawTitleSuffix.trim().split("\\|");
        if (title.length < 2) {
            throw new IllegalArgumentException(errorMessage(">[Genome name][TAB][Replicon id]",
                    rawTitle, lineNumber, filePath));
        }

        return title;
    }

    private static Strand determineStrand(String rawStrand) {

        Strand strand;

        switch (rawStrand) {
            case "+":
                strand = Strand.FORWARD;
                break;
            case "-":
                strand = Strand.REVERSE;
                break;
            default:
                strand = Strand.INVALID;
                break;
        }

        return strand;
    }

    private static String errorMessage(String expected, String recieved, int lineNumber, String path) {
        return String.format("Expected %s, got \"%s\" in file %s line %d", expected, recieved, path, lineNumber);
    }

    private static Genome getNewOrExistingGenome(GenomesInfo genomesInfo, String currGenomeName) {
        Genome genome;
        if (genomesInfo.genomeExists(currGenomeName)) {
            genome = genomesInfo.getGenome(currGenomeName);
        } else {
            genome = new Genome(currGenomeName, genomesInfo.getNumberOfGenomes());
        }

        return genome;
    }


    public static List<Family> parseSessionFile(String filePath, GenomesInfo genomesInfo)
        throws IOException, IllegalArgumentException {

        if ( filePath == null) {
            throw new IllegalArgumentException();
        }

        int lineNumber = 0;
        List<Family> families = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String rawLine = br.readLine();
            lineNumber++;
            while (rawLine != null) {
                if (rawLine.startsWith("<genomes>")){

                    lineNumber = readGenomes(br, genomesInfo, filePath, "<\\genomes>", lineNumber);
                }else if (rawLine.startsWith("<instances>")){
                    families = readInstances(br, genomesInfo, filePath, "<\\instances>", lineNumber);
                }
                rawLine = br.readLine();
                lineNumber++;
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + filePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + filePath);
        }

        return families;
    }

    /**
     * @param input_file_path path to input file with input sequences
     * @return number of input sequences
     */
    public static GenomesInfo parseGenomesFile(String filePath)
            throws IOException, IllegalArgumentException {

        if ( filePath == null) {
            throw new IllegalArgumentException();
        }

        GenomesInfo genomesInfo = new GenomesInfo();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            readGenomes(br, genomesInfo, filePath, null);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + filePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + filePath);
        }

        return genomesInfo;

    }

    private static List<Family> readInstances(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end,
                                      int lineNumber)
            throws IOException{

        HashMap<String, Family> families = new HashMap<>();

        Pattern pattern = new Pattern();

        String rawLine = br.readLine();
        while (!rawLine.equals(end)) {
            lineNumber++;

            if (rawLine.startsWith(">")) {

                pattern = parsePattern(rawLine, lineNumber, filePath);

                Family family;
                if (families.containsKey(pattern.getFamilyId())){
                    family = families.get(pattern.getFamilyId());
                    family.addPattern(pattern);
                }else{
                    family = new Family(pattern.getFamilyId(), pattern, genomesInfo);
                }
                families.put(pattern.getFamilyId(), family);

            } else {
                String[] locationsLine = rawLine.trim().split("\t");
                String genomeName = locationsLine[0];
                Genome genome = genomesInfo.getGenome(genomeName);
                int genomeId = genome.getId();
                for (int i = 1; i < locationsLine.length; i++) {
                    String[] location = locationsLine[i].split("\\|");;
                    String repliconName = location[0];
                    int repliconId = genome.getReplicon(repliconName).getId();
                    String[] indexes = location[1].substring(1, location[1].length()-1).split(",");
                    int startIndex = castToInteger(indexes[0], "start index", lineNumber, filePath);
                    int endIndex = castToInteger(indexes[1], "end index", lineNumber, filePath);

                    InstanceLocation instanceLocation = new InstanceLocation(repliconId, genomeId, startIndex, endIndex,
                            Strand.FORWARD);
                    pattern.addInstanceLocation(instanceLocation);
                }
            }

            rawLine = br.readLine();
        }

        return new ArrayList<Family>(families.values());
    }

    private static Pattern parsePattern(String rawLine, int lineNumber, String filePath) throws IllegalArgumentException{
        String[] patternLine = rawLine.trim().substring(1).split("\t");
        if (patternLine.length < 7) {
            throw new IllegalArgumentException(
                    errorMessage(">[ID][TAB][Length][TAB][Score][TAB][Count][TAB][Exact Count][TAB][Genes][TAB][Family ID]",
                            rawLine, lineNumber, filePath));
        }

        int id = castToInteger(patternLine[0], "id", lineNumber, filePath);
        int length = castToInteger(patternLine[1], "length", lineNumber, filePath);
        double score = castToDouble(patternLine[2], "score", lineNumber, filePath);
        int count = castToInteger(patternLine[3], "count", lineNumber, filePath);
        int exactCount = castToInteger(patternLine[4], "exact count", lineNumber, filePath);
        List<Gene> genes = parseGenes(patternLine[5], lineNumber, filePath);
        String familyId = patternLine[6];

        Pattern pattern = new Pattern(id, genes, count, exactCount);
        pattern.setScore(score);
        pattern.setFamilyId(familyId);

        return pattern;
    }

    private static int castToInteger(String value, String type, int lineNumber, String filePath)
            throws IllegalArgumentException{
        int result = -1;
        try {
            result = Integer.valueOf(value);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException(
                    errorMessage(type + " type double",
                            value, lineNumber, filePath));
        }
        return result;
    }

    private static double castToDouble(String value, String type, int lineNumber, String filePath)
            throws IllegalArgumentException{
        double result = -1;
        try {
            result = Double.valueOf(value);
        }catch(NumberFormatException e){
            throw new IllegalArgumentException(
                    errorMessage(type + " type integer",
                            value, lineNumber, filePath));
        }
        return result;
    }

    private static void readGenomes(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end)
            throws IOException{
        readGenomes(br, genomesInfo, filePath, end, 0);
    }


    private static int readGenomes(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end,
                                    int lineNumber)
                                throws IOException{

        String repliconName;
        String currGenomeName = "";

        Genome genome = new Genome();
        Replicon replicon = new Replicon(Strand.FORWARD, -1, "");

        String rawLine = br.readLine();
        while (rawLine != null && !rawLine.equals(end)) {
            lineNumber++;

            if (rawLine.startsWith(">")) {

                if (replicon.size() > 0) {

                    genome.addReplicon(replicon);

                    genomesInfo.addGenome(genome);
                    genomesInfo.addReplicon(replicon);

                }

                String[] title = parseGenomeTitle(rawLine, lineNumber, filePath);

                currGenomeName = title[0];
                repliconName = title[1];

                genome = getNewOrExistingGenome(genomesInfo, currGenomeName);
                replicon = new Replicon(Strand.FORWARD, genomesInfo.getNumberOfReplicons(), repliconName);

            } else {
                Gene gene = parseGeneLine(rawLine, lineNumber, filePath);
                replicon.add(gene);
            }

            rawLine = br.readLine();
        }

        genome.addReplicon(replicon);

        genomesInfo.addGenome(genome);
        genomesInfo.addReplicon(replicon);

        return lineNumber;
    }

    /**
     * Parse {@code cogInfoFilePath} containg functional information of the genes in the input genomes
     * For each cog that is used in our data, save information of the functional category
     * <p>
     * Format of a line in the file:
     * [COG ID];[COG description];[Functional letter X],[Functional letter Y],...;[Description of X];[Description of Y];...;[Gene ID]
     * <p>
     * Optional: Functional letters and their description, Gene ID
     * <p>
     * Valid examples:
     * COG0001;Glutamate-1-semialdehyde aminotransferase;H;Coenzyme transport and metabolism;HemL;
     * COG0129;Dihydroxyacid dehydratase/phosphogluconate dehydratase;E,G;Amino acid transport and metabolism;Carbohydrate transport and metabolism;IlvD;
     * COG0001;Glutamate-1-semialdehyde aminotransferase;
     * COG0001;Glutamate-1-semialdehyde aminotransferase;HemL;
     *
     * @throws FileNotFoundException
     */
    public static Map<String, COG> parseCogInfoTable(String cogInfoFilePath)
            throws IOException, IllegalArgumentException{
        Map<String, COG> cogInfo = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cogInfoFilePath))) {

            String line = br.readLine();
            int lineNumber = 0;

            while (line != null) {
                lineNumber++;

                String[] cogLine = line.split(";");
                if (cogLine.length < 2) {
                    throw new IllegalArgumentException(errorMessage("[COG ID];[COG description];",
                            line, lineNumber, cogInfoFilePath));
                }

                String cog_id = cogLine[0];
                String cog_desc = cogLine[1];

                COG cog = new COG(cog_id, cog_desc);

                if (cogLine.length == 3) {

                    String geneName = cogLine[2];
                    cog.setGeneName(geneName);

                }else if (cogLine.length > 3){

                    String[] functionalLetters = cogLine[2].split(",");

                    if (cogLine.length < 3 + functionalLetters.length) {
                        throw new IllegalArgumentException(errorMessage(
                                String.format("%d functional category descriptions", functionalLetters.length),
                                String.format("%d in line %s", cogLine.length-3, line), lineNumber, cogInfoFilePath));
                    }

                    String[] functional_categories = getFunctionalCategories(functionalLetters.length, 3, cogLine);

                    String geneName = "";
                    int valuesParsedSoFar = 2 + functionalLetters.length*2;
                    if (cogLine.length == valuesParsedSoFar + 1){
                        geneName = cogLine[valuesParsedSoFar];
                    }

                    cog.setGeneName(geneName);
                    cog.setFunctionalCategories(functional_categories);
                    cog.setFunctionalLetters(functionalLetters);
                }

                cogInfo.put(cog_id, cog);

                line = br.readLine();
            }
        }catch(FileNotFoundException e){
            throw new FileNotFoundException("File " + cogInfoFilePath + " was not found.");
        }
        catch (IOException e) {
            throw new IOException("An exception occurred while reading " + cogInfoFilePath);
        }
        return cogInfo;
    }

    private static String[] getFunctionalCategories(int numberOfFunctionalLetters, int startIndex, String[] cogLine){
        String[] functionalCategories = new String[numberOfFunctionalLetters];

        for (int i = 0; i < numberOfFunctionalLetters; i++) {
            functionalCategories[i] = cogLine[startIndex + i];
        }

        return functionalCategories;
    }
}
