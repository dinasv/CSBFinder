package PostProcess;

import Utils.Pattern;

import java.util.*;

import Utils.Utils;

import CLI.CommandLineArgs.ClusterBy;

/**
 * Created by Dina on 23/08/2017.
 * Clusters patterns to families using a greedy method.
 */
public class FamilyClustering {

    public static List<Family> Cluster(List<Pattern> patterns, double threshold, ClusterBy cluster_by,
                                            Utils utils, boolean non_directons){

        List<Family> families = greedyClustering(patterns, threshold, cluster_by, utils, non_directons);

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

    private static List<Family> greedyClustering(List<Pattern> patterns, double threshold, ClusterBy cluster_by,
                                                      Utils utils, boolean non_directons){

        if (cluster_by == ClusterBy.LENGTH){
            Collections.sort(patterns, new Pattern.LengthComparator());
        }else {
            Collections.sort(patterns, new Pattern.ScoreComparator());
        }

        List<Family> families = new ArrayList<>();
        Iterator<Pattern> it = patterns.iterator();
        if (it.hasNext()) {
            Pattern first_pattern = it.next();
            Family first_family = new Family("0", first_pattern, utils, non_directons);

            families.add(first_family);

            while (it.hasNext()) {
                Pattern curr_pattern = it.next();

                Set<Integer> curr_pattern_gene_set = get_genes_set(curr_pattern.getPatternArr(), utils);

                boolean added_pattern = false;
                for (Family family : families) {
                    added_pattern = addToFamily(family, threshold, curr_pattern, curr_pattern_gene_set);

                    if (added_pattern){
                        break;
                    }
                }

                if (!added_pattern) {
                    Family new_family = new Family(Integer.toString(families.size()), curr_pattern, utils, non_directons);
                    families.add(new_family);
                }
            }
        }

        return families;
    }

    private static Set<Integer> get_genes_set(String[] genes, Utils utils){
        Set<Integer> gene_set = new HashSet<>();
        for (String cog: genes) {
            int cog_index = utils.char_to_index.get(cog);
            gene_set.add(cog_index);
        }
        return gene_set;
    }

}
