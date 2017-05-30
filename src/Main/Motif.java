package Main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 **/
public class Motif{
    /**
     * e.g. COG1234 COG5234
     */
    private String motif;
    /**
     * Each cell contains the COG id, e.g. [1234, 5234]
     */
    private String[] motif_arr;
    private int motif_id;
    private int occ_count;
    private double[] motif_score;

    private int length;
    private ArrayList<Occurrence> motif_occs;

    private int exact_occs_count;

    public Motif(int motif_id, String motif, String[] motif_arr, int length, HashSet<Integer> seq_keys,
                 ArrayList<Occurrence> occs, int exact_occs_count){
        this.motif_id = motif_id;
        this.motif = motif;
        this.motif_arr = motif_arr;
        this.length = length;
        this.motif_occs = occs;
        occ_count = seq_keys.size();
        this.exact_occs_count = exact_occs_count;
    }

    public void setMotif_id(int motif_id){
        this.motif_id = motif_id;
    }

    public int getMotif_id(){
        return motif_id;
    }

    public ArrayList<Occurrence> get_occs(){
        return motif_occs;
    }

    public int getLength(){
        return length;
    }

    public void setMotif(String motif){
        this.motif = motif;
    }
    public String getMotif(){
        return motif;
    }

    public int getOccCount(){
        return occ_count;
    }


    public String[] getMotif_arr() {
        return motif_arr;
    }

    public int getExact_occs_count() {
        return exact_occs_count;
    }

}
