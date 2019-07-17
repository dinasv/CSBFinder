package Model.OrthologyGroups;

import java.util.Arrays;

/**
 * Contains details about the COG
 */
public class COG{

    private String cogId;
    private String cogDesc;
    private String[] functionalLetters;
    private String[] functionalCategories;
    private String geneName;

    public COG(String cogId, String cogDesc) {
        this.cogId = cogId;
        this.cogDesc = cogDesc;

        functionalLetters = new String[0];
        functionalCategories = new String[0];
        geneName = "";
    }

    public String getCogId() {
        return cogId;
    }

    public void setFunctionalLetters(String[] functionalLetters){
        this.functionalLetters = functionalLetters;
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

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public void setFunctionalCategories(String[] functionalCategories) {
        this.functionalCategories = functionalCategories;
    }

    @Override
    public String toString() {
        return "COG{" +
                "cogId='" + cogId + '\'' +
                ", cogDesc='" + cogDesc + '\'' +
                ", functionalLetters=" + Arrays.toString(functionalLetters) +
                ", functionalCategories=" + Arrays.toString(functionalCategories) +
                ", geneName='" + geneName + '\'' +
                '}';
    }
}
