package Words;

import java.io.Serializable;
import java.util.Arrays;
import Utils.*;

/**
 * Created by Dina on 6/2/2016.
 * WordArray object represents a "word". The chars of the word are integers.
 * It contains a pointer to wordArray (the "word"), and indexes that represent a substring of this "word"
 */
public class WordArray implements Serializable{

    public int[] wordArray;
    //start index in wordArray
    private int start_index;
    //end index in wordArray, not included
    private int end_index;
    // length = end_index - start_index
    private int length;

    WordArray(){
        this.wordArray = new int[]{};
        start_index = 0;
        end_index = 0;
        length = 0;
    }

    /**
     * Shallow copy
     * @param other
     */
    public WordArray(WordArray other){
        this.wordArray = other.wordArray;
        start_index = other.get_start_index();
        end_index = other.get_end_index();
        length = other.get_length();
    }

    public WordArray(int[] wordArray){
        this.wordArray = wordArray;
        start_index = 0;
        end_index = wordArray.length;
        length = wordArray.length;
    }

    public WordArray(int[] wordArray, int start_index) throws ArrayIndexOutOfBoundsException{
        this.wordArray = wordArray;
        end_index = wordArray.length;
        if (start_index >=0 && start_index <= end_index) {
            this.start_index = start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }

        length = end_index - start_index;
    }

    public WordArray(int[] wordArray, int start_index, int end_index) throws ArrayIndexOutOfBoundsException{
        this.wordArray = wordArray;
        if (start_index >=0 && start_index <= end_index && end_index <= wordArray.length) {
            this.start_index = start_index;
            this.end_index = end_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
        length = end_index - start_index;
    }
    //return the index relative to the start_index
    public int get_index(int index){
        return wordArray[start_index + index];
    }

    public int get_start_index(){
        return  start_index;
    }

    public int get_end_index(){
        return  end_index;
    }

    public int get_length(){
        return  length;
    }

    public void set_index(int index, int ch){
        wordArray[start_index + index] = ch;
    }

    public void set_start_index(int start_index) throws ArrayIndexOutOfBoundsException{
        if(start_index >= 0 && start_index <= end_index) {
            this.start_index = start_index;
            length = end_index - start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    //adds diff to start_index, if it's legal, else throws exception
    public void add_to_start_index(int diff) throws ArrayIndexOutOfBoundsException{
        int new_start_index = start_index + diff;
        if(new_start_index >= 0 && new_start_index <= end_index) {
            this.start_index = new_start_index;
            length = end_index - start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void set_end_index(int end_index) throws ArrayIndexOutOfBoundsException{
        end_index = start_index + end_index;
        if(end_index >= start_index && end_index <= wordArray.length) {
            this.end_index = end_index;
            length = end_index - start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Adds diff to end_index, if it's legal, else throws exception
     * @param diff
     * @throws ArrayIndexOutOfBoundsException
     */
    public void add_to_end_index(int diff) throws ArrayIndexOutOfBoundsException{
        int new_end_index = end_index + diff;
        if(new_end_index >= start_index && new_end_index <= wordArray.length) {
            this.end_index = new_end_index;
            length = end_index - start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Deep copy
     * @return
     */
    public WordArray get_copy(){
         int[] word_copy = Arrays.copyOfRange(wordArray, start_index, length);
         return new WordArray(word_copy, start_index, end_index);
    }

    /**
     *
     * @param from is relative to start_index
     * @param to is absolute
     */
    public void substring(int from, int to){
        set_start_index(start_index+from);
        set_end_index(to);
    }

    /**
     *
     * @param from is relative to start_index
     */
    public void substring(int from){
        set_start_index(start_index+from);
        end_index = wordArray.length;
    }

    /**
     * Checks if this wordArray starts with bs2
     * @param bs2
     * @return true if the this wordArray starts with bs2, false otherwise
     */
    public boolean starts_with(WordArray bs2){
        if (bs2.get_length() > length){
            return false;
        }
        return compare_to(bs2, 0, bs2.get_length());
    }
    /**
     * Checks if this wordArray is equal to bs2
     * @param bs2
     * @return true if the this wordArray is equal to bs2, false otherwise
     */
    public boolean equal(WordArray bs2){
        if (bs2.get_length() != length){
            return false;
        }
        return compare_to(bs2, 0, length);
    }

    /**
     * Compares between 2 bitsetArrays. Compares this.wordArray[start_index, start_index + lenToMatch] to bs2[0, lenToMatch]
     * @param bs2
     * @param start_i for this wordArray
     * @param lenToMatch number of cells compared
     * @return true if the subarrays are equal, false otherwise
     */
    public Boolean compare_to(WordArray bs2, int start_i, int lenToMatch){
        start_i = this.start_index + start_i;
        int end_i = start_i + lenToMatch;
        int j = bs2.get_start_index();
        for (int i = start_i; i < end_i; i++){
            if (wordArray[i]!=(bs2.wordArray[j])){
                return false;
            }
            j++;
        }
        return true;
    }

    public String to_string(Utils utils){
        String str = "";
        for (int i=start_index; i<end_index; i++){
            str += utils.index_to_cog.get(wordArray[i]) + "|";
        }

        return str;
    }

}
