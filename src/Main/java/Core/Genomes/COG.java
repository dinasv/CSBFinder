package Core.Genomes;

/**
 * Contains details about the COG
 */
public class COG{

    private String cogId;
    private String cogDesc;
    private String[] functionalLetters;
    private String[] functionalCategories;
    private String geneName;

    public COG(String cogId, String cogDesc, String geneName){
        this(cogId, cogDesc, new String[0], new String[0], geneName);

    }

    public COG(String cogId, String cogDesc, String[] functionalLetters, String[] functionalCategories){
        this(cogId, cogDesc, functionalLetters, functionalCategories, "");
    }
    public COG(String cogId, String cogDesc, String[] functionalLetters, String[] functionalCategories,
               String geneName) {

        this.cogId = cogId;
        this.cogDesc = cogDesc;
        this.geneName = geneName;
        this.functionalLetters = functionalLetters;
        this.functionalCategories = functionalCategories;
    }

    public COG(COG other, String cogDesc){
        this.cogId = cogId;
        this.cogDesc = cogDesc;
    }

    public String getCogID(){
        return cogId;
    }

    public String getCogDesc(){
        return cogDesc;
    }


    public String[] getFunctionalCategories(){
        return functionalCategories;
    }

    public String[] getFunctionalLetters(){
        return functionalLetters;
    }


    public String getGeneName() {
        return geneName;
    }
}
