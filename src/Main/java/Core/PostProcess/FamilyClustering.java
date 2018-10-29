package Core.PostProcess;

import Core.Genomes.Gene;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;

import java.util.*;

import Core.ClusterBy;

/**
 * Created by Dina on 23/08/2017.
 * Clusters patterns to families using a greedy method.
 */
public class FamilyClustering {

    public static List<Family> Cluster(List<Pattern> patterns, double threshold, ClusterBy cluster_by,
                                            GenomesInfo gi, boolean non_directons){

        List<Family> families = greedyClustering(patterns, threshold, cluster_by, gi, non_directons);

        for (Family family: families){
            family.sortPatternsAndSetScore();
        }

        Collections.sort(families, new Family.ScoreComparator());
        return families;
    }

    private static boolean addToFamily(Family family, double threshold, Pattern curr_pattern,
                                       Set<Integer> curr_pattern_gene_set){

        Set<Integer> family_set = family.getGeneSet();
        int minimal_set_size = Math.min(family_set.size(), curr_pattern_gene_set.size());

        Set<Integer> intersection = new HashSet<Integer>(curr_pattern_gene_set);
        intersection.retainAll(family_set);

        //double thresh = threshold;
        if (minimal_set_size >= 1 && minimal_set_size <= 2) {
            threshold = 1;
        }

        if (intersection.size() / (double) minimal_set_size >= threshold) {
            family.addPattern(curr_pattern);
            return true;
        }
        return false;
    }

    private static List<Family> greedyClustering(List<Pattern> patterns, double threshold, ClusterBy clusterBy,
                                                      GenomesInfo gi, boolean non_directons){

        Collections.sort(patterns, clusterBy.patternComparator);

        List<Family> families = new ArrayList<>();
        Iterator<Pattern> it = patterns.iterator();
        if (it.hasNext()) {
            Pattern first_pattern = it.next();
            Family first_family = new Family("0", first_pattern, gi, non_directons);

            families.add(first_family);

            while (it.hasNext()) {
                Pattern curr_pattern = it.next();

                Set<Integer> curr_pattern_gene_set = getGenesSet(curr_pattern.getPatternGenes(), gi);

                boolean added_pattern = false;
                for (Family family : families) {
                    added_pattern = addToFamily(family, threshold, curr_pattern, curr_pattern_gene_set);

                    if (added_pattern){
                        break;
                    }
                }

                if (!added_pattern) {
                    Family new_family = new Family(Integer.toString(families.size()), curr_pattern, gi, non_directons);
                    families.add(new_family);
                }
            }
        }

        return families;
    }

    private static Set<Integer> getGenesSet(List<Gene> genes, GenomesInfo gi){
        Set<Integer> gene_set = new HashSet<>();
        for (Gene cog: genes) {
            int cog_index = gi.charToIndex.get(cog);
            gene_set.add(cog_index);
        }
        return gene_set;
    }

}
