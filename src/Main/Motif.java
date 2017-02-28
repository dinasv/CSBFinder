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

    //private ArrayList<Occurrence> motif_occs;
    private ArrayList<Integer> motif_seq_keys;

    private String motif_main_cat;
    private String motif_letter_cat;
    private String motif_cat_letters;

    private String[][] majority_tax;
    private double[][] majority_tax_ratio;

    private ArrayList<HashMap<String, Integer>> motif_genus_count_in_dataset;
    private ArrayList<HashMap<String, Integer>> motif_class_count_in_dataset;
    private ArrayList<HashMap<String, Integer>> motif_phylum_count_in_dataset;
    //private String[] dataset_major_genus;
    private int exact_occs_count;

    public Motif(int motif_id, String motif, String[] motif_arr, int length, int number_of_datasets, String motif_main_cat, String motif_letter_cat, String motif_cat_letters){
        this.motif = motif;
        this.length = length;
        this.motif_id = motif_id;
        this.motif_arr = motif_arr;
        //motif_occs = new ArrayList<>(number_of_datasets);
        motif_seq_keys = new ArrayList<>();
        motif_genus_count_in_dataset = new ArrayList<>();
        motif_class_count_in_dataset = new ArrayList<>();
        motif_phylum_count_in_dataset = new ArrayList<>();


        this.motif_main_cat = motif_main_cat;
        this.motif_letter_cat = motif_letter_cat;
        this.motif_cat_letters = motif_cat_letters;

        occ_count = 0;

        motif_score = new double[number_of_datasets];
        //exact_occs_count = new int[number_of_datasets];

        majority_tax = new String[number_of_datasets][3];
        majority_tax_ratio = new double[number_of_datasets][3];

        //dataset_major_genus = new String[number_of_datasets];
    }

    public Motif(int motif_id, String motif, String[] motif_arr, int length, HashSet<Integer> seq_keys, int exact_occs_count){
        this.motif_id = motif_id;
        this.motif = motif;
        this.motif_arr = motif_arr;
        this.length = length;
        motif_seq_keys = new ArrayList<>(seq_keys);
        occ_count = seq_keys.size();
        this.exact_occs_count = exact_occs_count;
    }

    public void setMotif_id(int motif_id){
        this.motif_id = motif_id;
    }

    public int getMotif_id(){
        return motif_id;
    }

    /*
    public void add_dataset_occ(Occurrence occ,  int dataset_num){
        motif_occs.get(dataset_num).add(occ);
        occ_count[dataset_num] += 1;
    }

    public void add_dataset_occs(ArrayList<Occurrence> occs, int occs_count, int dataset_num){
        motif_occs.add(dataset_num, occs);
        occ_count[dataset_num] = occs_count;
    }*/


    public ArrayList<Integer> get_occs_keys(){
        return motif_seq_keys;
    }

    public void add_datasets_occs_keys(ArrayList<Integer> keys){
        motif_seq_keys.addAll(keys);
    }

/*
    public ArrayList<Occurrence> get_occs(int dataset_num){
        return motif_occs.get(dataset_num);
    }
*/
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



    public static Comparator<Motif> motif_pvalComperator = new Comparator<Motif>() {

        public int compare(Motif motif1, Motif motif2) {

            double comparedSize = motif2.getDataset_pval(0);
            if (motif1.getDataset_pval(0) < comparedSize) {
                return 1;
            } else if (motif1.getDataset_pval(0) == comparedSize) {
                return 0;
            } else {
                return -1;
            }
        }

    };


    public String[] getMotif_arr() {
        return motif_arr;
    }

    public void setMotif_arr(String[] motif_arr) {
        this.motif_arr = motif_arr;
    }

    public String getMotif_letter_cat() {
        return motif_letter_cat;
    }

    public String getMotif_cat_letters() {
        return motif_cat_letters;
    }

    public String getMotif_main_cat() {
        return motif_main_cat;
    }

    public void setDataset_pval(double value, int dataset_num){
        motif_score[dataset_num] = value;
    }
    public double getDataset_pval(int dataset_num){
        return motif_score[dataset_num];
    }


    public String getMajority_tax(int dataset_num, int tax_index) {
        return majority_tax[dataset_num][tax_index];
    }

    public void setMajority_tax(String majority_tax, int dataset_num, int tax_index) {
        this.majority_tax[dataset_num][tax_index] = majority_tax;
    }

    public double getMajority_tax_ratio(int dataset_num, int tax_index) {
        return majority_tax_ratio[dataset_num][tax_index];
    }

    public void setMajority_tax_ratio(double majority_tax_ratio, int dataset_num, int tax_index) {
        this.majority_tax_ratio[dataset_num][tax_index] = majority_tax_ratio;
    }

    public void setMotif_genus_count_in_dataset(HashMap<String, Integer> motif_genus_count_in_dataset, int dataset_num) {
        this.motif_genus_count_in_dataset.set(dataset_num, motif_genus_count_in_dataset);
    }

    public HashMap<String, Integer> getMotif_genus_count_in_dataset(int dataset_num) {
        return this.motif_genus_count_in_dataset.get(dataset_num);
    }

    public void setMotif_class_count_in_dataset(HashMap<String, Integer> motif_class_count_in_dataset, int dataset_num) {
        this.motif_class_count_in_dataset.set(dataset_num, motif_class_count_in_dataset);
    }

    public HashMap<String, Integer> getMotif_class_count_in_dataset(int dataset_num) {
        return motif_class_count_in_dataset.get(dataset_num);
    }

    public void setMotif_phylum_count_in_dataset(HashMap<String, Integer> motif_phylum_count_in_dataset, int dataset_num) {
        this.motif_phylum_count_in_dataset.set(dataset_num, motif_phylum_count_in_dataset);
    }

    public HashMap<String, Integer> getMotif_phylum_count_in_dataset(int dataset_num) {
        return this.motif_phylum_count_in_dataset.get(dataset_num);
    }

    public int getExact_occs_count() {
        return exact_occs_count;
    }

}
