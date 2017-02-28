package Main;
/**
 * Created by ASUS on 7/27/2016.
 */
public class MotifOcc {
    private String str;
    int gap_count;
    int word_id;
    int word_start_index;

    public MotifOcc(String str, int gap_count, int word_id, int word_start_index){
        this.str = str;
        this.gap_count = gap_count;
        this.word_id = word_id;
        this.word_start_index = word_start_index;
    }

    public String getStr(){
        return str;
    }
    public int getGap_count(){
        return gap_count;
    }
    public int getWord_id(){
        return word_id;
    }
    public int getWord_start_index(){
        return word_start_index;
    }
}
