package PostProcess;

import Main.Motif;
import Utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by Dina on 23/08/2017.
 */
public class Family {
    private String family_id;
    private int family_rank;
    private ArrayList<Motif> motifs;
    private HashSet<Integer> gene_set;
    private double score;
    private Utils utils;

    public Family(String family_id, Motif first_motif, String[] genes, Utils utils){
        this.utils = utils;
        score = -1;
        this.family_id = family_id;
        motifs = new ArrayList<>();
        motifs.add(first_motif);
        gene_set = new HashSet<>();
        for (String cog: genes) {
            int cog_index = utils.cog_to_index.get(cog);
            gene_set.add(cog_index);
        }
    }

    public HashSet<Integer> getGeneSet(){
        return gene_set;
    }

    public void addMotif(Motif motif){
        motifs.add(motif);
    }

    public String getFamilyId(){
        return family_id;
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void sortMotifs(){
        Collections.sort(motifs, new Motif.ScoreComparator());
        score = motifs.get(0).getScore();
    }

    public ArrayList<Motif> getMotifs(){
        return motifs;
    }

    public static class ScoreComparator implements Comparator<Family> {
        @Override
        public int compare(Family o1, Family o2) {
            if (o2.getScore() < o1.getScore()) return -1;
            if (o2.getScore() > o1.getScore()) return 1;
            return 0;
        }
    }
}
