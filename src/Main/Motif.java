package Main;

import java.util.*;

import Utils.*;

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

    private double score;

    private String main_functional_category;

    public Motif(int motif_id, String motif, String[] motif_arr, int length, HashSet<Integer> seq_keys,
                 ArrayList<Instance> instances, int exact_instance_count){
        this.motif_id = motif_id;
        this.motif = motif;
        this.motif_arr = motif_arr;
        this.length = length;
        this.motif_instances = instances;
        instance_count = seq_keys.size();
        this.exact_instance_count = exact_instance_count;
        score = 0;
        main_functional_category = "";
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

    public void calculateScore(Utils utils, int max_insertion, int max_error, int max_deletion){

        score = utils.computeMotifScore(motif_arr, max_insertion, max_error, max_deletion, 0,
                instance_count, motif_id);
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void calculateMainFunctionalCategory(Utils utils){
        if (utils.cog_info != null) {


            HashMap<String, Integer> functional_letter_count = new HashMap<>();
            HashMap<String, String> functional_letter_to_desc = new HashMap<>();
            for (String cog_id : motif_arr) {
                COG cog = utils.cog_info.get(cog_id);
                if (cog != null) {
                    String[] functional_letters = cog.getFunctional_letters();
                    String[] functional_categories = cog.getFunctional_categories();
                    for (int i = 0; i < functional_letters.length; i++) {
                        String letter = functional_letters[i];
                        if (!functional_letter_count.containsKey(letter)) {
                            functional_letter_count.put(letter, 0);
                        }
                        functional_letter_count.put(letter, functional_letter_count.get(letter) + 1);
                        functional_letter_to_desc.put(letter, functional_categories[i]);
                    }
                }
            }

            String main_categories = "";
            if (functional_letter_count.size() > 0) {
                List<Map.Entry<String, Integer>> list = new ArrayList<>(functional_letter_count.entrySet());

                Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

                main_categories = functional_letter_to_desc.get(list.get(0).getKey());
                int max_count = list.get(0).getValue();
                list.remove(0);
                for (Map.Entry<String, Integer> entry : list) {
                    String letter = entry.getKey();
                    int count = entry.getValue();
                    if (count == max_count) {
                        main_categories += "/" + functional_letter_to_desc.get(letter);
                    }
                }
            }

            main_functional_category = main_categories;
        }
    }

    public String getMain_functional_category() {
        return main_functional_category;
    }


    public static class LengthComparator implements Comparator<Motif> {

        @Override
        public int compare(Motif o1, Motif o2) {

            // descending order
            return o2.getLength() - o1.getLength();
        }
    }

    public static class ScoreComparator implements Comparator<Motif> {
        @Override
        public int compare(Motif o1, Motif o2) {
            if (o2.getScore() < o1.getScore()) return -1;
            if (o2.getScore() > o1.getScore()) return 1;
            return 0;
        }
    }

}

