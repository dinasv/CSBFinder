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
     * Parse {@code rawLine} containing a gene and its strand separated by TAB, and create {@link Gene}.
     * A strand must be "+" or "-"
     *
     * Format: [gene id][TAB][strand]
     *
     * Valid examples:
     * COG1234[TAB]+
     * abc[TAB]-
     * @param rawLine
     * @param lineNumber of {@code rawLine} in {@code filePath}
     * @param filePath
     */
    private static Gene parseGeneLine(String rawLine, int lineNumber, String filePath) {

        Objects.requireNonNull(rawLine, "rawLine is null");
        Objects.requireNonNull(filePath,"filePath is null");

        String[] splitLine = rawLine.trim().split("\t");
        if (splitLine.length < 2){
            throw new IllegalArgumentException(errorMessage("[gene id][TAB][strand]", rawLine,lineNumber, filePath));
        }

        String geneId = splitLine[0];
        String rawStrand = splitLine[1];

        Strand strand = determineStrand(rawStrand);
        if (strand == Strand.INVALID){
            throw new IllegalArgumentException(errorMessage("strand to be + or -",
                    rawStrand, lineNumber, filePath));
        }

        return new Gene(geneId, strand);
    }

    /**
     * Parse rawTitle containing a genome name and a replicon name
     * replicon name must be unique
     *
     * Format: [genome name]|[replicon name]
     *
     * Valid examples:
     * Acaryochloris_marina_MBIC11017_uid58167|NC_009927
     *
     * @param rawTitle
     * @param replicon
     */
    private static String[] parseGenomeTitle(String rawTitle, int lineNumber, String filePath) {

        Objects.requireNonNull(rawTitle, "rawTitle is null");
        Objects.requireNonNull(filePath,"filePath is null");

        String rawTitleSuffix = rawTitle.substring(1); //remove ">"
        String[] title = rawTitleSuffix.trim().split("\\|");
        if (title.length < 2){
            throw new IllegalArgumentException(errorMessage(">[Genome name][TAB][Replicon id]",
                    rawTitle, lineNumber, filePath));
        }

        return title;
    }

    private static Strand determineStrand(String rawStrand){

        Strand strand;

        switch (rawStrand){
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

    private static String errorMessage(String expected, String recieved, int lineNumber, String path){
        return String.format("Expected %s, got %s in file %s line %d", expected, recieved, path, lineNumber);
    }

    private static Genome getNewOrExistingGenome(GenomesInfo genomesInfo, String currGenomeName){
        Genome genome;
        if (genomesInfo.genomeExists(currGenomeName)) {
            genome = genomesInfo.getGenome(currGenomeName);
        }else{
            genome = new Genome(currGenomeName, genomesInfo.getNumberOfGenomes());
        }

        return genome;
    }

    /**
     * @param input_file_path path to input file with input sequences
     * @return number of input sequences
     */
    public static int parseGenomesFile(String filePath, GenomesInfo genomesInfo) {

        if (genomesInfo == null || filePath == null){
            throw new IllegalArgumentException();
        }

        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))){

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
            System.err.println("File " + filePath + " was not found.");
        } catch (IOException e) {
            System.err.println("An exception occurred while reading " + filePath);
            return -1;
        }

        return genomesInfo.getNumberOfGenomes();

    }

    /**
     * Read COG_INFO_TABLE.txt and fill cog_info. For each cog that is used in our data,
     * save information of functional category
     *
     * @throws FileNotFoundException
     */
    public static Map<String, COG> read_cog_info_table(String cog_info_file_name) {
        Map<String, COG> cog_info = new HashMap<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(cog_info_file_name));
            String line = br.readLine();
            while (line != null) {

                String[] cog_line = line.split(";");
                if (cog_line.length > 1) {
                    String cog_id = cog_line[0];
                    String cog_desc = cog_line[1];
                    if (cog_line.length > 2) {
                        String[] functional_letters = cog_line[2].split(",");
                        String[] functional_categories = new String[functional_letters.length];
                        if (cog_line.length > 3 + functional_letters.length) {
                            for (int i = 0; i < functional_letters.length; i++) {
                                functional_categories[i] = cog_line[3 + i];
                            }
                            String geneName = cog_line[cog_line.length - 1];
                            if (geneName.equals(functional_categories[functional_categories.length - 1])) {
                                geneName = "";
                            }
                            COG cog = new COG(cog_id, cog_desc, functional_letters, functional_categories, geneName);
                            cog_info.put(cog_id, cog);
                        }

                    } else {
                        String geneName = cog_line[cog_line.length - 1];
                        if (geneName.equals(cog_desc)) {
                            geneName = "";
                        }
                        COG cog = new COG(cog_id, cog_desc, geneName);
                        cog_info.put(cog_id, cog);
                    }
                }
                line = br.readLine();
            }
            br.close();

        } catch (IOException e) {
            System.out.println("File " + cog_info_file_name + " was not found");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cog_info;
    }
}
