package Core.Genomes;

import java.util.Arrays;

/**
 * WordArray object represents a "word". The chars of the word are integers.
 * It contains a pointer to wordArray (the "word"), and indexes that represent a substring of this "word"
 */
public class WordArray{

    private int[] wordArray;
    //start index in wordArray
    private int startIndex;
    //end index in wordArray, not included
    private int endIndex;
    // length = endIndex - startIndex
    private int length;

    WordArray(){
        this.wordArray = new int[]{};
        startIndex = 0;
        endIndex = 0;
        length = 0;
    }

    /**
     * Shallow copy
     * @param other
     */
    public WordArray(WordArray other){
        this.wordArray = other.wordArray;
        startIndex = other.get_start_index();
        endIndex = other.get_end_index();
        length = other.get_length();
    }

    public WordArray(int[] wordArray){
        this.wordArray = wordArray;
        startIndex = 0;
        endIndex = wordArray.length;
        length = wordArray.length;
    }

    public WordArray(int[] wordArray, int startIndex) throws ArrayIndexOutOfBoundsException{
        this.wordArray = wordArray;
        endIndex = wordArray.length;
        if (startIndex >=0 && startIndex <= endIndex) {
            this.startIndex = startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }

        length = endIndex - startIndex;
    }

    public WordArray(int[] wordArray, int startIndex, int endIndex) throws ArrayIndexOutOfBoundsException{
        this.wordArray = wordArray;
        if (startIndex >=0 && startIndex <= endIndex && endIndex <= wordArray.length) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
        length = endIndex - startIndex;
    }

    public int[] getWordArray(){
        return wordArray;
    }

    //return the index relative to the startIndex
    public int get_index(int index){
        return wordArray[startIndex + index];
    }

    public int get_start_index(){
        return startIndex;
    }

    public int get_end_index(){
        return endIndex;
    }

    public int get_length(){
        return  length;
    }

    public void set_index(int index, int ch){
        wordArray[startIndex + index] = ch;
    }

    public void set_start_index(int start_index) throws ArrayIndexOutOfBoundsException{
        if(start_index >= 0 && start_index <= endIndex) {
            this.startIndex = start_index;
            length = endIndex - start_index;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    //adds diff to startIndex, if it's legal, else throws exception
    public void add_to_start_index(int diff) throws ArrayIndexOutOfBoundsException{
        int new_start_index = startIndex + diff;
        if(new_start_index >= 0 && new_start_index <= endIndex) {
            this.startIndex = new_start_index;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void set_end_index(int end_index) throws ArrayIndexOutOfBoundsException{
        end_index = startIndex + end_index;
        if(end_index >= startIndex && end_index <= wordArray.length) {
            this.endIndex = end_index;
            length = end_index - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Adds diff to endIndex, if it's legal, else throws exception
     * @param diff
     * @throws ArrayIndexOutOfBoundsException
     */
    public void add_to_end_index(int diff) throws ArrayIndexOutOfBoundsException{
        int new_end_index = endIndex + diff;
        if(new_end_index >= startIndex && new_end_index <= wordArray.length) {
            this.endIndex = new_end_index;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Deep copy
     * @return
     */
    public WordArray get_copy(){
         int[] word_copy = Arrays.copyOfRange(wordArray, startIndex, length);
         return new WordArray(word_copy, startIndex, endIndex);
    }

    /**
     *
     * @param from is relative to startIndex
     * @param to is absolute
     */
    public void substring(int from, int to){
        set_start_index(startIndex +from);
        set_end_index(to);
    }

    /**
     *
     * @param from is relative to startIndex
     */
    public void substring(int from){
        set_start_index(startIndex +from);
        endIndex = wordArray.length;
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
     * Compares between 2 bitsetArrays. Compares this.wordArray[startIndex, startIndex + lenToMatch] to bs2[0, lenToMatch]
     * @param bs2
     * @param start_i for this wordArray
     * @param lenToMatch number of cells compared
     * @return true if the subarrays are equal, false otherwise
     */
    public Boolean compare_to(WordArray bs2, int start_i, int lenToMatch){
        start_i = this.startIndex + start_i;
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


}
