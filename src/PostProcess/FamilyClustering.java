package PostProcess;

import Main.Pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import Utils.Utils;

import Main.CommandLineArgs.ClusterBy;

/**
 * Created by Dina on 23/08/2017.
 * Clusters patterns to families using a greedy method.
 */
public class FamilyClustering {

    public static ArrayList<Family> Cluster(ArrayList<Pattern> patterns, double threshold, ClusterBy cluster_by,
                                            Utils utils){

        ArrayList<Family> families = greedyClustering(patterns, threshold, cluster_by, utils);

        for (Family family: families){
            family.sortPatternsAndSetScore();
        }

        Collections.sort(families, new Family.ScoreComparator());
        return families;
    }

    private static ArrayList<Family> greedyClustering(ArrayList<Pattern> patterns, double threshold, ClusterBy cluster_by,
                                                      Utils utils){

        if (cluster_by == ClusterBy.LENGTH){
            Collections.sort(patterns, new Pattern.LengthComparator());
        }else {
            Collections.sort(patterns, new Pattern.ScoreComparator());
        }

        ArrayList<Family> families = new ArrayList<>();
        Iterator<Pattern> it = patterns.iterator();
        if (it.hasNext()) {
            Pattern first_pattern = it.next();
            Family first_family = new Family("0", first_pattern, utils);
            families.add(first_family);

            while (it.hasNext()) {
                Pattern curr_pattern = it.next();

                HashSet<Integer> curr_pattern_gene_set = get_genes_set(curr_pattern.getPatternArr(), utils);
                boolean added_pattern = false;
                for (Family family : families) {
                    HashSet<Integer> family_hash_set = family.getGeneSet();
                    int minimal_set_size = Math.min(family_hash_set.size(), curr_pattern_gene_set.size());

                    HashSet<Integer> intersection = new HashSet<Integer>(curr_pattern_gene_set);
                    intersection.retainAll(family_hash_set);

                    double thresh = threshold;
                    if (minimal_set_size >= 1 && minimal_set_size <= 2) {
                        thresh = 1;
                    }
                    if (intersection.size() / (double) minimal_set_size >= thresh) {
                        family.addPattern(curr_pattern);
                        added_pattern = true;

                        break;
                    }
                }

                if (!added_pattern) {
                    Family new_family = new Family(Integer.toString(families.size()), curr_pattern, utils);
                    families.add(new_family);
                }
            }
        }

        return families;
    }

    private static HashSet<Integer> get_genes_set(String[] genes, Utils utils){
        HashSet<Integer> gene_set = new HashSet<>();
        for (String cog: genes) {
            int cog_index = utils.char_to_index.get(cog);
            gene_set.add(cog_index);
        }
        return gene_set;
    }

}
