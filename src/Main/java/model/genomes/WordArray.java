package model.genomes;

import java.util.Arrays;

/**
 * WordArray object represents a "word". The letters of the word are integers.
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
        startIndex = other.getStartIndex();
        endIndex = other.getEndIndex();
        length = other.getLength();
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
    public int getLetter(int index){
        return wordArray[startIndex + index];
    }

    public int getStartIndex(){
        return startIndex;
    }

    public int getEndIndex(){
        return endIndex;
    }

    public int getLength(){
        return  length;
    }

    public void setIndex(int index, int letter){
        wordArray[startIndex + index] = letter;
    }

    public void setStartIndex(int startIndex) throws ArrayIndexOutOfBoundsException{
        if(startIndex >= 0 && startIndex <= endIndex) {
            this.startIndex = startIndex;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    //adds diff to startIndex, if it's legal, else throws exception
    public void addToStartIndex(int diff) throws ArrayIndexOutOfBoundsException{
        int newStartIndex = startIndex + diff;
        if(newStartIndex >= 0 && newStartIndex <= endIndex) {
            this.startIndex = newStartIndex;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void setEndIndex(int endIndex) throws ArrayIndexOutOfBoundsException{
        endIndex = startIndex + endIndex;
        if(endIndex >= startIndex && endIndex <= wordArray.length) {
            this.endIndex = endIndex;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Adds diff to endIndex, if it's legal, else throws exception
     * @param diff
     * @throws ArrayIndexOutOfBoundsException
     */
    public void addToEndIndex(int diff) throws ArrayIndexOutOfBoundsException{
        int newEndIndex = endIndex + diff;
        if(newEndIndex >= startIndex && newEndIndex <= wordArray.length) {
            this.endIndex = newEndIndex;
            length = endIndex - startIndex;
        }else{
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Deep copy
     * @return
     */
    public WordArray getCopy(){
         int[] wordCopy = Arrays.copyOfRange(wordArray, startIndex, length);
         return new WordArray(wordCopy, startIndex, endIndex);
    }

    /**
     *
     * @param from is relative to startIndex
     * @param to is absolute
     */
    public void substring(int from, int to){
        setStartIndex(startIndex +from);
        setEndIndex(to);
    }

    /**
     *
     * @param from is relative to startIndex
     */
    public void substring(int from){
        setStartIndex(startIndex +from);
        endIndex = wordArray.length;
    }

    /**
     * Checks if this wordArray starts with bs2
     * @param bs2
     * @return true if the this wordArray starts with bs2, false otherwise
     */
    public boolean startsWith(WordArray bs2){
        if (bs2.getLength() > length){
            return false;
        }
        return compareTo(bs2, 0, bs2.getLength());
    }
    /**
     * Checks if this wordArray is equal to bs2
     * @param bs2
     * @return true if the this wordArray is equal to bs2, false otherwise
     */
    public boolean equal(WordArray bs2){
        if (bs2.getLength() != length){
            return false;
        }
        return compareTo(bs2, 0, length);
    }

    /**
     * Compares between 2 bitsetArrays. Compares this.wordArray[startIndex, startIndex + lenToMatch] to bs2[0, lenToMatch]
     * @param bs2
     * @param start_i for this wordArray
     * @param lenToMatch number of cells compared
     * @return true if the subarrays are equal, false otherwise
     */
    public Boolean compareTo(WordArray bs2, int start_i, int lenToMatch){
        start_i = this.startIndex + start_i;
        int end_i = start_i + lenToMatch;
        int j = bs2.getStartIndex();
        for (int i = start_i; i < end_i; i++){
            if (wordArray[i]!=(bs2.wordArray[j])){
                return false;
            }
            j++;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WordArray{" +
                "wordArray=" + Arrays.toString(wordArray) +
                '}';
    }
}
