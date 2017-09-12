package Utils;

/**
 * Contains details about the COG
 */
public class COG{
    String cog_id;
    String cog_desc;
    String[] functional_letters;
    String[] functional_categories;


    public COG(String cog_id, String cog_desc, String[] functional_letters, String[] functional_categories){
        this.cog_id = cog_id;
        this.cog_desc = cog_desc;
        this.functional_letters = functional_letters;
        this.functional_categories = functional_categories;

    }

    public COG(COG other, String cog_desc){
        this.cog_id = cog_id;
        this.cog_desc = cog_desc;
    }

    public String getCogID(){
        return cog_id;
    }

    public String getCog_desc(){
        return cog_desc;
    }


    public String[] getFunctional_categories(){
        return functional_categories;
    }

    public String[] getFunctional_letters(){
        return functional_letters;
    }


}
