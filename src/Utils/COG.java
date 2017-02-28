package Utils;

import Main.Motif;

/**
 * Contains details about the COG
 */
public class COG implements Comparable<COG>{
    String cog_id;
    String functional_category_letters;
    String functional_category_desc;
    String letter_desc;
    String sub_cat_desc;
    //"phage" or "bacteria"
    String cog_type;

    int count;

    public COG(String cog_id, String functional_category_letters, String functional_category_desc, String letter_desc, String sub_cat_desc, String cog_type){
        this.cog_id = cog_id;
        this.functional_category_letters = functional_category_letters;
        this.functional_category_desc = functional_category_desc;
        this.letter_desc = letter_desc;
        this.sub_cat_desc = sub_cat_desc;
        this.cog_type = cog_type;
        count = 0;
    }

    public COG(COG other, int count){
        this.functional_category_letters = other.getFunctional_category_letters();
        this.functional_category_desc = other.getFunctional_category_desc();
        this.letter_desc = other.getLetter_desc();
        this.count = count;
    }

    public String getSub_cat_desc(){
        return  sub_cat_desc;
    }

    public String getFunctional_category_letters(){
        return functional_category_letters;
    }

    public String getCogType(){
        return cog_type;
    }

    public void setCog_type(String cog_type){
        this.cog_type = cog_type;
    }

    public String getLetter_desc(){
        return letter_desc;
    }

    public String getFunctional_category_desc(){
        return functional_category_desc;
    }

    public void increment_count(int val){
        count += val;
    }

    public int getCount(){
        return  count;
    }


    @Override
    public int compareTo(COG o) {
        int comparedSize = o.getCount();
        if (count < comparedSize) {
            return 1;
        } else if (count == comparedSize) {
            return 0;
        } else {
            return -1;
        }
    }
}
