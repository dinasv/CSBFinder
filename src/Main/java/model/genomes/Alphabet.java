package model.genomes;

import java.util.*;

/**
 */
public class Alphabet {

    private List<Gene> indexToLetter;
    private Map<Gene, Integer> letterToIndex;

    public static final int GAP_CHAR_INDEX = 0;
    public static final String GAP_CHAR = "_";
    public static final int UNK_CHAR_INDEX = 1;
    public static final String UNK_CHAR = "X";

    public Alphabet(){
        indexToLetter = new ArrayList<>();
        letterToIndex = new HashMap<>();

        //gap
        Gene gene = new Gene(GAP_CHAR, Strand.FORWARD);
        letterToIndex.put(gene, GAP_CHAR_INDEX);
        indexToLetter.add(gene);

        //unkown orthology group
        gene = new Gene(UNK_CHAR, Strand.FORWARD);
        letterToIndex.put(gene, UNK_CHAR_INDEX);
        indexToLetter.add(gene);

        gene = new Gene(UNK_CHAR, Strand.REVERSE);
        letterToIndex.put(gene, UNK_CHAR_INDEX);

        gene = new Gene(UNK_CHAR, Strand.INVALID);
        letterToIndex.put(gene, UNK_CHAR_INDEX);
    }

    public Gene getLetter(int index){
        return indexToLetter.get(index);
    }

    public int getLetter(Gene gene){
        if (!letterToIndex.containsKey(gene)){
            return -1;
        }
        return letterToIndex.get(gene);
    }

    public int alphabetSize(){
        return indexToLetter.size();
    }

    public int addLetter(Gene gene){

        int letterIndex = getLetter(gene);
        if (letterIndex == -1) {
            letterIndex = alphabetSize();
            indexToLetter.add(gene);
            letterToIndex.put(gene, letterIndex);
        }
        return letterIndex;
    }

    public Iterator<Gene> getAlphabetLetters(){
        return indexToLetter.iterator();
    }

    /**
     * Converts an array of strings to wordArray, using letterToIndex
     * @param genes each gene will we converted to a character with index
     * @return WordArray representing genes
     */
    public WordArray createWordArray(List<Gene> genes){
        int[] word = new int[genes.size()];
        int i = 0;
        for(Gene gene: genes){

            int letterIndex = getLetter(gene);

            word[i] = letterIndex;
            i++;
        }

        return new WordArray(word);
    }

}
