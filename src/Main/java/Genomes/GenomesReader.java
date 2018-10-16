package Genomes;

import IO.MyLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 */
public class GenomesReader {

    GenomesInfo gi;
    MyLogger logger;

    public GenomesReader(GenomesInfo gi, MyLogger logger){
        this.gi = gi;
        this.logger = logger;
    }

    private void writeLogInfo(){
        int genomesCount = gi.getNumberOfGenomes();
        logger.writeLogger("Average genome size: " + gi.getDatasetLengthSum() / genomesCount);
        logger.writeLogger("Number of genomes " + genomesCount);
        logger.writeLogger("Alphabet size " + gi.char_to_index.size());
    }


    /**
     *
     * @param input_file_path path to input file with input sequences
     * @return number of input sequences that contains at least one valid direction
     */
    public int readGenomes(String input_file_path) {

        String file_name = input_file_path;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file_name));

            try {
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
                        //read gene
                        String[] split_line = line.trim().split("\t");
                        if (split_line.length > 1) {
                            String gene_family = split_line[0];
                            String strand = split_line[1];
                            Gene gene = new Gene(gene_family, strand);
                            replicon.add(gene);
                        }
                    }

                    line = br.readLine();
                }

                genome = gi.addGenome(curr_genome_name);
                gi.addReplicon(replicon, genome);


                writeLogInfo();

                int number_of_genomes = gi.getNumberOfGenomes();
                if (number_of_genomes == 0){
                    return -1;
                }

            } catch (IOException e) {
                System.out.println("An exception occured while reading " + file_name);
                return -1;
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Cannot close file " + file_name);
                    return -1;
                }
            }

            return gi.getNumberOfGenomes();
        } catch (FileNotFoundException e) {
            System.out.println("File " + file_name + " was not found.");
        }
        return -1;
    }

}
