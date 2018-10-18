package IO;

import Core.SuffixTrees.Trie;
import Genomes.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Readers {

    public static List<Pattern> readPatternsFromFile(String input_patterns_file_name) {

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

    private static void readGene(String line, Replicon replicon) {
        String[] split_line = line.trim().split("\t");
        if (split_line.length > 1) {
            String gene_family = split_line[0];
            String strand = split_line[1];
            Gene gene = new Gene(gene_family, strand);
            replicon.add(gene);
        }
    }

    /**
     * @param input_file_path path to input file with input sequences
     * @return number of input sequences
     */
    public static int readGenomes(String input_file_path, GenomesInfo gi) {

        String file_name = input_file_path;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file_name));

            String line = br.readLine();

            String replicon_name;
            String curr_genome_name = "";

            Genome genome;
            Replicon replicon = new Replicon(1, -1, "");

            while (line != null) {
                if (line.startsWith(">")) {

                    if (replicon.size() > 0) {
                        genome = gi.addGenome(curr_genome_name);
                        gi.addReplicon(replicon, genome);
                    }

                    //get genome and replicon name
                    line = line.substring(1); //remove ">"
                    //e.g. Acaryochloris_marina_MBIC11017_uid58167|NC_009927
                    String[] title = line.trim().split("\\|");

                    if (title.length > 0) {
                        curr_genome_name = title[0];

                        replicon_name = "";
                        if (title.length > 1) {
                            replicon_name = title[1];
                        }

                        replicon = new Replicon(1, gi.getNumberOfReplicons(), replicon_name);
                    }
                } else {
                    readGene(line, replicon);
                }

                line = br.readLine();
            }

            genome = gi.addGenome(curr_genome_name);
            gi.addReplicon(replicon, genome);

        } catch (FileNotFoundException e) {
            System.err.println("File " + file_name + " was not found.");
        } catch (IOException e) {
            System.err.println("An exception occurred while reading " + file_name);
            return -1;
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                System.err.println("Cannot close file " + file_name);
                return -1;
            }
        }

        return gi.getNumberOfGenomes();

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
