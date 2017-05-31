package Main;

import java.util.ArrayList;
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
    private int instance_count;

    private int length;
    private ArrayList<Instance> motif_instances;

    private int exact_instance_count;

    public Motif(int motif_id, String motif, String[] motif_arr, int length, HashSet<Integer> seq_keys,
                 ArrayList<Instance> instances, int exact_instance_count){
        this.motif_id = motif_id;
        this.motif = motif;
        this.motif_arr = motif_arr;
        this.length = length;
        this.motif_instances = instances;
        instance_count = seq_keys.size();
        this.exact_instance_count = exact_instance_count;
    }

    public void setMotif_id(int motif_id){
        this.motif_id = motif_id;
    }

    public int getMotif_id(){
        return motif_id;
    }

    public ArrayList<Instance> get_instances(){
        return motif_instances;
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

    public int get_instance_count(){
        return instance_count;
    }

    public String[] getMotif_arr() {
        return motif_arr;
    }

    public int get_exact_instance_count() {
        return exact_instance_count;
    }

}
