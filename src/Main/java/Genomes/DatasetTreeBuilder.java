package Genomes;

import Core.SuffixTrees.GeneralizedSuffixTree;
import Core.SuffixTrees.Trie;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 */
public class DatasetTreeBuilder {

    /**
     * Builds a Trie of patterns, given in a file.
     * @param input_patterns_file_name path to input file containing patterns
     * @param pattern_tree the patterns are inserted to this GST
     * @return True if succesful, False if exception occurred
     */
    public static boolean buildPatternsTreeFromFile(String input_patterns_file_name, Trie pattern_tree, GenomesInfo gi) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(input_patterns_file_name));

            String line = br.readLine();

            int pattern_id = 0;
            while (line != null) {
                if (line.charAt(0) == '>'){
                    try {
                        pattern_id = Integer.parseInt(line.substring(1));
                    }catch (NumberFormatException e){
                        System.out.println("Pattern id should be an integer, found " + line);
                        return false;
                    }
                }else {
                    String[] pattern = line.split("-");

                    if (pattern.length > 1) {
                        WordArray word = createWordArray(pattern, gi);
                        pattern_tree.put(word, pattern_id, gi.UNK_CHAR_INDEX);
                    }
                }
                line = br.readLine();
            }

            try {
                br.close();
            } catch (IOException e) {
                System.out.println("A problem occurred while reading file " +input_patterns_file_name);
                return false;
            }

        } catch (IOException e) {
            System.out.println("A problem occurred while reading file " +input_patterns_file_name);
            return false;
        }
        return true;
    }


    /**
     * Converts an array of strings to wordArray, using char_to_index
     * @param str contains the characters comprising this str
     * @return WordArray representing this str
     */
    public static WordArray createWordArray(String[] str, GenomesInfo gi){
        int[] word = new int[str.length];
        int i = 0;
        for(String ch: str){
            int char_index = -1;
            if (gi.char_to_index.containsKey(ch)) {
                char_index = gi.char_to_index.get(ch);
            } else {
                char_index = gi.index_to_char.size();
                gi.index_to_char.add(ch);
                gi.char_to_index.put(ch, char_index);
            }

            word[i] = char_index;
            i++;
        }

        return new WordArray(word);
    }


    /**
     * Turns a genomicSegment (replicon or directon) into an array of genes and puts in in the @dataset_gst
     * @param genomicSegment
     * @param dataset_gst
     * @param curr_genome_index
     */
    private static void putWordInDataTree(GenomicSegmentInterface genomicSegment, GeneralizedSuffixTree dataset_gst, int curr_genome_index, GenomesInfo gi){
        String[] genes = genomicSegment.getGenesIDs();
        WordArray cog_word = createWordArray(genes, gi);
        InstanceLocation instance_info = new InstanceLocation(genomicSegment.getId(), genomicSegment.getStartIndex(),
                genomicSegment.getStrand());
        dataset_gst.put(cog_word, curr_genome_index, instance_info);

        gi.countParalogsInSeqs(genes, curr_genome_index);
    }

    /**
     * Insert replicon, or split the replicon to directons and then insert
     * @param non_directons
     * @param replicon
     * @param dataset_gst
     * @param curr_genome_index
     * @return
     */
    private static int updateDataTree(boolean non_directons, Replicon replicon, GeneralizedSuffixTree dataset_gst,
                               int curr_genome_index, GenomesInfo gi){

        int replicon_length = 0;
        if (non_directons) {

            putWordInDataTree(replicon, dataset_gst, curr_genome_index, gi);

            //reverse replicon
            replicon.reverse();
            putWordInDataTree(replicon, dataset_gst, curr_genome_index, gi);

            replicon_length += replicon.size() * 2;

        }else{

            List<Directon> directons = replicon.splitRepliconToDirectons(gi.UNK_CHAR);

            for (Directon directon: directons){
                replicon_length += directon.size();
                putWordInDataTree(directon, dataset_gst, curr_genome_index, gi);
            }

        }
        return replicon_length;
    }

    public static void buildTree(GeneralizedSuffixTree dataset_gst, boolean non_directons, GenomesInfo gi){
        for (Map.Entry<String, Map<String, Replicon>> entry : gi.genomeToRepliconsMap.entrySet()) {
            String genome_name = entry.getKey();
            int genome_index = gi.genome_name_to_id.get(genome_name);
            for (Replicon replicon: entry.getValue().values()) {
                updateDataTree(non_directons, replicon, dataset_gst, genome_index, gi);
            }
        }
    }

}
