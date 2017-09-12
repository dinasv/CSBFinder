package PostProcess;

import Main.Instance;
import Main.Motif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import Utils.Utils;

/**
 * Created by Dina on 23/08/2017.
 */
public class FamilyClustering {

    public static ArrayList<Family> Cluster(ArrayList<Motif> motifs, double threshold, Utils utils){

        ArrayList<Family> families = greedyClustering(motifs, threshold, utils);

        for (Family family: families){
            family.sortMotifs();
            for (Motif motif : family.getMotifs()){
                System.out.println(motif.getScore());
            }
        }

        Collections.sort(families, new Family.ScoreComparator());
        return families;
    }

    private static ArrayList<Family> greedyClustering(ArrayList<Motif> motifs, double threshold, Utils utils){

        Collections.sort(motifs, new Motif.LengthComparator());
        Motif longest_motif = motifs.get(0);
        Family first_family = new Family("0", longest_motif, longest_motif.getMotif_arr(), utils);
        ArrayList<Family> families = new ArrayList<>();
        families.add(first_family);

        for (int i = 1; i < motifs.size(); i++) {
            Motif curr_motif = motifs.get(i);
            HashSet<Integer> curr_motif_gene_set = get_genes_set(curr_motif.getMotif_arr(), utils);
            boolean added_motif = false;
            for (Family family : families) {
                HashSet<Integer> family_hash_set = family.getGeneSet();
                int minimal_set_size = Math.min(family_hash_set.size(), curr_motif_gene_set.size());

                HashSet<Integer> intersection = new HashSet<Integer>(curr_motif_gene_set);
                intersection.retainAll(family_hash_set);
                int intersection_size = intersection.size();

                double thresh = threshold;
                if (minimal_set_size >= 1 && minimal_set_size <= 3) {
                    thresh = 1;
                }
                if (intersection_size / (double) minimal_set_size >= thresh) {
                    family.addMotif(curr_motif);
                    added_motif = true;

                    break;
                }
            }

            if (!added_motif){
                Family new_family = new Family(Integer.toString(families.size()), curr_motif, curr_motif.getMotif_arr(),
                        utils);
                families.add(new_family);
            }
        }
        return families;
    }

    private static HashSet<Integer> get_genes_set(String[] genes, Utils utils){
        HashSet<Integer> gene_set = new HashSet<>();
        for (String cog: genes) {
            int cog_index = utils.cog_to_index.get(cog);
            gene_set.add(cog_index);
        }
        return gene_set;
    }

}
