package IO;

import Core.Genomes.*;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Parsers {

    public static List<Pattern> parsePatternsFile(String input_patterns_file_name) {

        List<Pattern> patterns = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(input_patterns_file_name));

            String line = br.readLine();

            int pattern_id = 0;
            while (line != null) {
                if (line.charAt(0) == '>') {
                    try {
                        pattern_id = Integer.parseInt(line.substring(1));
                    } catch (NumberFormatException e) {
                        System.out.println("Pattern id should be an integer, found " + line);
                        return null;
                    }
                } else {
                    String[] patternArr = line.split("-");
                    if (patternArr.length > 1) {
                        Pattern pattern = new Pattern(pattern_id, line, patternArr);
                        patterns.add(pattern);
                    }
                }
                line = br.readLine();
            }

            try {
                br.close();
            } catch (IOException e) {
                System.out.println("A problem occurred while reading file " + input_patterns_file_name);
                return null;
            }

        } catch (IOException e) {
            System.out.println("A problem occurred while reading file " + input_patterns_file_name);
            return null;
        }
        return patterns;
    }


    /**
     * Parse {@code rawLine} containing a gene and its strand separated by TAB, and create {@link Genomes.Gene}.
     * A strand must be "+" or "-"
     * <p>
     * Format: [gene id][TAB][strand]
     * <p>
     * Valid examples:
     * COG1234[TAB]+
     * abc[TAB]-
     *
     * @param rawLine
     * @param lineNumber of {@code rawLine} in {@code filePath}
     * @param filePath
     */
    private static Gene parseGeneLine(String rawLine, int lineNumber, String filePath) {

        Objects.requireNonNull(rawLine, "rawLine is null");
        Objects.requireNonNull(filePath, "filePath is null");

        String[] splitLine = rawLine.trim().split("\t");
        if (splitLine.length < 2) {
            throw new IllegalArgumentException(errorMessage("[gene id][TAB][strand]", rawLine, lineNumber, filePath));
        }

        String geneId = splitLine[0];
        String rawStrand = splitLine[1];

        Strand strand = determineStrand(rawStrand);
        if (strand == Strand.INVALID) {
            throw new IllegalArgumentException(errorMessage("strand to be + or -",
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
    private static String[] parseGenomeTitle(String rawTitle, int lineNumber, String filePath) {

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
        return String.format("Expected %s, got %s in file %s line %d", expected, recieved, path, lineNumber);
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

    /**
     * @param input_file_path path to input file with input sequences
     * @return number of input sequences
     */
    public static int parseGenomesFile(String filePath, GenomesInfo genomesInfo) throws IOException {

        if (genomesInfo == null || filePath == null) {
            throw new IllegalArgumentException();
        }

        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String rawLine = br.readLine();

            String repliconName;
            String currGenomeName = "";

            Genome genome = new Genome();
            Replicon replicon = new Replicon(Strand.FORWARD, -1, "");

            while (rawLine != null) {
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

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File " + filePath + " was not found.");
        } catch (IOException e) {
            throw new IOException("An exception occurred while reading " + filePath);
        }

        return genomesInfo.getNumberOfGenomes();

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
    public static Map<String, COG> parseCogInfoTable(String cogInfoFilePath) {
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
        } catch (IOException e) {
            System.out.println("File " + cogInfoFilePath + " was not found");
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
