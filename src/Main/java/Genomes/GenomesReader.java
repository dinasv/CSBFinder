package Genomes;

import Core.SuffixTrees.GeneralizedSuffixTree;
import IO.MyLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Boris on 14/10/2018.
 */
public class GenomesReader {

    GenomesInfo gi;
    MyLogger logger;

    public GenomesReader(GenomesInfo gi, MyLogger logger){
        this.gi = gi;
        this.logger = logger;
    }

    private void writeLogInfo(){
        int genomesCount = gi.genome_id_to_name.size();
        logger.writeLogger("Average genome size: " + gi.dataset_length_sum / genomesCount);
        logger.writeLogger("Number of genomes " + genomesCount);
        logger.writeLogger("Alphabet size " + gi.char_to_index.size());
    }

    private void updateGenomeToRepliconsMap(String curr_genome_name, String replicon_id, Replicon replicon){
        if (!gi.genomeToRepliconsMap.containsKey(curr_genome_name)){
            gi.genomeToRepliconsMap.put(curr_genome_name, new HashMap<>());
        }
        Map<String, Replicon> genomeRepliconsMap = gi.genomeToRepliconsMap.get(curr_genome_name);

        genomeRepliconsMap.put(replicon_id, replicon);
    }

    private boolean updateGenomes(String curr_genome_name, int genome_size, int curr_genome_index){

        boolean is_updated = false;
        if (!gi.genome_name_to_id.containsKey(curr_genome_name)) {
            if (genome_size > 0 && curr_genome_index != -1) {
                gi.genome_id_to_name.put(curr_genome_index, curr_genome_name);
                gi.genome_name_to_id.put(curr_genome_name, curr_genome_index);
                is_updated = true;
            }
        }
        return is_updated;
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

            int length_sum = 0;
            int genome_size = 0;

            try {
                String line = br.readLine();

                String replicon_name = "";
                int curr_genome_index = -1;
                String curr_genome_name = "";

                Replicon replicon = new Replicon(1, -1);

                while (line != null) {
                    if (line.startsWith(">")) {

                        if (curr_genome_index != -1) {

                            length_sum += replicon.size();
                            genome_size += replicon.size();

                            updateGenomeToRepliconsMap(curr_genome_name, replicon_name, replicon);

                        }

                        line = line.substring(1); //remove ">"
                        //e.g. Acaryochloris_marina_MBIC11017_uid58167|NC_009927
                        String[] word_desc = line.trim().split("\\|");

                        if (word_desc.length > 0) {
                            //Replicon.index ++;
                            replicon = new Replicon(1, gi.replicon_id_to_name.size());

                            String next_genome_name = word_desc[0];

                            updateGenomes(curr_genome_name, genome_size, curr_genome_index);

                            if (!next_genome_name.equals(curr_genome_name)) {
                                curr_genome_index++;
                                if (genome_size > gi.max_genome_size){
                                    gi.max_genome_size = genome_size;
                                }
                                genome_size = 0;
                            }

                            curr_genome_name = next_genome_name;

                            if (word_desc.length > 1) {
                                replicon_name = word_desc[1];
                                gi.replicon_id_to_name.put(replicon.getId(), replicon_name);
                            }

                        }
                    } else {
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

                length_sum += replicon.size();
                genome_size += replicon.size();

                updateGenomes(curr_genome_name, genome_size, curr_genome_index);
                updateGenomeToRepliconsMap(curr_genome_name, replicon_name, replicon);

                if (genome_size > gi.max_genome_size){
                    gi.max_genome_size = genome_size;
                }

                gi.dataset_length_sum = length_sum;

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
