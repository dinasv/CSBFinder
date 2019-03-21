package IO;

import Model.Genomes.*;
import Model.OrthologyGroups.COG;
import Model.Patterns.InstanceLocation;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Parsers {

    final static String PATTERN_DELIMITER = ",";
    final static String[] INSTANCE_HEADER = {"[Family ID]", "[Length]", "[Score]", "[Count]", "[Genes]", "[Family FAMILY_ID]"};
    final static String INSTANCE_HEADER_DELIMITER = "\t";
    final static String[] GENOME_HEADER = {"[Genome name]", "[Replicon FAMILY_ID]"};
    final static String GENOME_HEADER_DELIMITER = "\\|";
    final static String[] GENE_LINE = {"[Gene Orthology Group ID]", "[Strand]"};
    final static String GENE_LINE_DELIMITER = "\t";
    final static String[] COG_LINE = {"[COG Orthology Group ID]", "[COG description]"};
    final static String COG_LINE_DELIMITER = ";";
    final static String[] LOCATIONS_LINE = {"[Genome Name]", "[Replicon Name|[Start Index, End Index]]"};
    final static String LOCATIONS_LINE_DELIMITER = "\t";

    final static String GENOMES_START = "<genomes>";
    final static String GENOMES_END = "<\\genomes>";
    final static String INSTANCES_START = "<instances>";
    final static String INSTANCES_END = "<\\instances>";

    public static List<Pattern> parseReferenceGenomesFile(GenomesInfo genomesInfo, String referenceGenomesPath)
            throws IOException {

        List<Pattern> patterns = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(referenceGenomesPath))) {
            String line = br.readLine();
            while (line != null) {
                String genomeName = line.trim();
                Genome genome = genomesInfo.getGenome(genomeName);
                if (genome != null){
                    for (Replicon replicon: genome.getReplicons()){
                        Pattern pattern = new Pattern(replicon.getGenes());
                        patterns.add(pattern);
                    }
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + referenceGenomesPath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + referenceGenomesPath);
        }

        return patterns;
    }

    public static List<Pattern> parsePatternsFile(String inputPatternsFilePath)
            throws IOException, IllegalArgumentException {

        List<Pattern> patterns = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputPatternsFilePath))) {

            String line = br.readLine();
            int lineNumber = 0;

            String patternId = "";
            while (line != null) {
                if (line.charAt(0) == '>') {

                    patternId = line.substring(1);

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

    private static List<Gene> parseGenes(String line, int lineNumber, String inputPatternsFilePath) throws IllegalArgumentException {
        String[] patternArr = line.split(PATTERN_DELIMITER);
        List<Gene> genes = new ArrayList<>();
        if (patternArr.length > 1) {
            for (String gene : patternArr) {
                if (gene.length() > 0) {
                    String lastChar = gene.substring(gene.length() - 1);
                    Strand strand = determineStrand(lastChar);

                    if (strand != Strand.INVALID) {
                        gene = gene.substring(0, gene.length() - 1);
                    }

                    genes.add(new Gene(gene, strand));
                }
            }
        } else {
            throw new IllegalArgumentException(errorMessage(String.format("Genes delimited by \"%s\"", PATTERN_DELIMITER),
                    line, lineNumber, inputPatternsFilePath));
        }
        return genes;
    }


    /**
     * Parse {@code rawLine} containing a gene and its numericValue separated by TAB, and create {@link Model.Genomes.Gene}.
     * A numericValue must be "+" or "-"
     * <p>
     * Format: [Gene FAMILY_ID][TAB][Strand]
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

        String[] splitLine = rawLine.trim().split(GENE_LINE_DELIMITER);
        if (splitLine.length < GENE_LINE.length) {
            throw new IllegalArgumentException(errorMessage(String.join(GENE_LINE_DELIMITER, GENE_LINE), rawLine,
                    lineNumber, filePath));
        }

        String geneId = splitLine[0];
        String rawStrand = splitLine[1];

        Strand strand = determineStrand(rawStrand);
        if (strand == Strand.INVALID) {
            throw new IllegalArgumentException(errorMessage("Strand must be + or -",
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
     */
    private static String[] parseGenomeTitle(String rawTitle, int lineNumber, String filePath) throws IllegalArgumentException {

        Objects.requireNonNull(rawTitle, "rawTitle is null");
        Objects.requireNonNull(filePath, "filePath is null");

        String rawTitleSuffix = rawTitle.substring(1); //remove ">"
        String[] title = rawTitleSuffix.trim().split(GENOME_HEADER_DELIMITER);
        if (title.length < GENOME_HEADER.length) {
            throw new IllegalArgumentException(errorMessage(">" + String.join(GENOME_HEADER_DELIMITER, GENOME_HEADER),
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

    public static String[] parseSessionFileFirstLine(String filePath) throws IOException, IllegalArgumentException{

        if (filePath == null) {
            throw new IllegalArgumentException();
        }
        String[] args = new String[0];

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            args = br.readLine().split(" ");

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + filePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + filePath);
        }

        return args;
    }


    public static void parseSessionFile(List<Family> families, String filePath, GenomesInfo genomesInfo)
            throws IOException, IllegalArgumentException {

        if (filePath == null) {
            throw new IllegalArgumentException();
        }

        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String args = br.readLine();

            lineNumber++;

            String rawLine = br.readLine();
            lineNumber++;
            while (rawLine != null) {
                if (rawLine.startsWith(GENOMES_START)) {

                    lineNumber = readGenomes(br, genomesInfo, filePath, GENOMES_END, lineNumber);
                } else if (rawLine.startsWith(INSTANCES_START)) {
                    readInstances(br, genomesInfo, filePath, INSTANCES_END, lineNumber, families);
                }
                rawLine = br.readLine();
                lineNumber++;
            }

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + filePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + filePath);
        }

    }


    /**
     * @param filePath path to input file with input sequences
     * @return all information obtained from the file, stored in GenomesInfo
     */
    public static GenomesInfo parseGenomesFile(String filePath)
            throws IOException, IllegalArgumentException {

        if (filePath == null) {
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

    private static void readInstances(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end,
                                      int lineNumber, List<Family> families)
            throws IOException {

        HashMap<Integer, Family> familiesMap = new HashMap<>();

        Pattern pattern = new Pattern();

        String rawLine = br.readLine();
        while (rawLine != null && !rawLine.equals(end)) {
            lineNumber++;

            if (rawLine.startsWith(">")) {

                pattern = parsePattern(rawLine, lineNumber, filePath);

                Family family = parseFamily(familiesMap, pattern, genomesInfo);
                familiesMap.put(family.getFamilyId(), family);

            } else {
                parseInstanceLocations(rawLine, genomesInfo, lineNumber, filePath, pattern);
            }

            rawLine = br.readLine();
        }

        familiesMap.values().forEach(Family::sortPatternsAndSetScore);

        families.addAll(familiesMap.values());
    }

    private static void parseInstanceLocations(String rawLine, GenomesInfo genomesInfo, int lineNumber, String filePath,
                                               Pattern pattern) {

        String[] locationsLine = rawLine.trim().split("\t");
        if (locationsLine.length < LOCATIONS_LINE.length) {
            throw new IllegalArgumentException(
                    errorMessage(String.join(LOCATIONS_LINE_DELIMITER, LOCATIONS_LINE),
                            rawLine, lineNumber, filePath));
        }

        String genomeName = locationsLine[0];
        Genome genome = genomesInfo.getGenome(genomeName);

        Objects.requireNonNull(genome, errorMessage("genome name to match one of the input genomes",
                rawLine, lineNumber, filePath));

        int genomeId = genome.getId();

        for (int i = 1; i < locationsLine.length; i++) {

            String[] location = locationsLine[i].split("\\|");

            String repliconName = location[0];
            Replicon replicon = genome.getReplicon(repliconName);

            Objects.requireNonNull(genome, errorMessage("replicon name to match one of the input genomes",
                    rawLine, lineNumber, filePath));

            int repliconId = replicon.getRepliconId();

            String[] indexes = location[1].substring(1, location[1].length() - 1).split(",");
            int startIndex = castToInteger(indexes[0], "start index", lineNumber, filePath);
            int endIndex = castToInteger(indexes[1], "end index", lineNumber, filePath);

            InstanceLocation instanceLocation = new InstanceLocation(repliconId, genomeId, startIndex,
                    endIndex - startIndex, Strand.FORWARD, 0, 0,
                    repliconId);
            pattern.addInstanceLocation(instanceLocation);

        }
    }

    private static Family parseFamily(HashMap<Integer, Family> familiesMap, Pattern pattern, GenomesInfo genomesInfo) {
        Family family;
        if (familiesMap.containsKey(pattern.getFamilyId())) {
            family = familiesMap.get(pattern.getFamilyId());
            family.addPattern(pattern);
        } else {
            family = new Family(pattern.getFamilyId(), pattern, genomesInfo);
        }
        return family;
    }

    private static Pattern parsePattern(String rawLine, int lineNumber, String filePath) throws IllegalArgumentException {
        String[] patternLine = rawLine.trim().substring(1).split(INSTANCE_HEADER_DELIMITER);
        if (patternLine.length < INSTANCE_HEADER.length) {
            throw new IllegalArgumentException(
                    errorMessage(">" + String.join(INSTANCE_HEADER_DELIMITER, INSTANCE_HEADER),
                            rawLine, lineNumber, filePath));
        }

        int i = 0;
        String id = patternLine[i];
        i++;
        int length = castToInteger(patternLine[i], INSTANCE_HEADER[i], lineNumber, filePath);
        i++;
        double score = castToDouble(patternLine[i], INSTANCE_HEADER[i], lineNumber, filePath);
        i++;
        int count = castToInteger(patternLine[i], INSTANCE_HEADER[i], lineNumber, filePath);
        i++;
        List<Gene> genes = parseGenes(patternLine[i], lineNumber, filePath);
        i++;
        int familyId = castToInteger(patternLine[i], INSTANCE_HEADER[i], lineNumber, filePath);

        Pattern pattern = new Pattern(id, genes);
        pattern.setScore(score);
        pattern.setFamilyId(familyId);

        return pattern;
    }

    private static int castToInteger(String value, String type, int lineNumber, String filePath)
            throws IllegalArgumentException {
        int result = -1;
        try {
            result = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    errorMessage(type + " type integer",
                            value, lineNumber, filePath));
        }
        return result;
    }

    private static double castToDouble(String value, String type, int lineNumber, String filePath)
            throws IllegalArgumentException {
        double result = Integer.MAX_VALUE;
        try {
            result = Double.valueOf(value);
        } catch (NumberFormatException e) {
            /*
            throw new IllegalArgumentException(
                    errorMessage(type + " type double",
                            value, lineNumber, filePath));
            */
        }
        return result;
    }

    private static void readGenomes(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end)
            throws IOException {
        readGenomes(br, genomesInfo, filePath, end, 0);
    }


    private static int readGenomes(BufferedReader br, GenomesInfo genomesInfo, String filePath, String end,
                                   int lineNumber)
            throws IOException {

        String repliconName;
        String currGenomeName = "";

        Genome genome = new Genome();
        Replicon replicon = new Replicon(Strand.FORWARD);

        String rawLine = br.readLine();
        lineNumber++;
        while (rawLine != null && !rawLine.equals(end)) {

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
                replicon = new Replicon(Strand.FORWARD, genomesInfo.getNumberOfReplicons(), repliconName,
                        genome.getId());

            } else {
                Gene gene = parseGeneLine(rawLine, lineNumber, filePath);
                replicon.addGene(gene);
            }

            rawLine = br.readLine();
            lineNumber++;
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
     * [COG FAMILY_ID];[COG description];[Functional letter X],[Functional letter Y],...;[Description of X];[Description of Y];...;[Gene FAMILY_ID]
     * <p>
     * Optional: Functional letters and their description, Gene FAMILY_ID
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
            throws IOException, IllegalArgumentException {

        Map<String, COG> cogInfo = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cogInfoFilePath))) {

            String line = br.readLine();
            int lineNumber = 0;

            while (line != null) {
                lineNumber++;

                String[] cogLine = line.split(COG_LINE_DELIMITER);
                if (cogLine.length < COG_LINE.length) {
                    throw new IllegalArgumentException(errorMessage(String.join(COG_LINE_DELIMITER, COG_LINE),
                            line, lineNumber, cogInfoFilePath));
                }

                String cogId = cogLine[0];
                String cogDesc = cogLine[1];

                COG cog = new COG(cogId, cogDesc);

                if (cogLine.length == 3) {

                    String geneName = cogLine[2];
                    cog.setGeneName(geneName);

                } else if (cogLine.length > 3) {

                    String[] functionalLetters = cogLine[2].split(",");

                    if (cogLine.length < 3 + functionalLetters.length) {
                        throw new IllegalArgumentException(errorMessage(
                                String.format("%d functional category descriptions", functionalLetters.length),
                                String.format("%d in line %s", cogLine.length - 3, line), lineNumber, cogInfoFilePath));
                    }

                    String[] functional_categories = getFunctionalCategories(functionalLetters.length, 3, cogLine);

                    String geneName = "";
                    int valuesParsedSoFar = 3 + functionalLetters.length;
                    if (cogLine.length == valuesParsedSoFar + 1) {
                        geneName = cogLine[valuesParsedSoFar];
                    }

                    cog.setGeneName(geneName);
                    cog.setFunctionalCategories(functional_categories);
                    cog.setFunctionalLetters(functionalLetters);
                }

                cogInfo.put(cogId, cog);

                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + cogInfoFilePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + cogInfoFilePath);
        }
        return cogInfo;
    }

    private static String[] getFunctionalCategories(int numberOfFunctionalLetters, int startIndex, String[] cogLine) {
        String[] functionalCategories = new String[numberOfFunctionalLetters];

        for (int i = 0; i < numberOfFunctionalLetters; i++) {
            functionalCategories[i] = cogLine[startIndex + i];
        }

        return functionalCategories;
    }
}
