import Model.Genomes.Strand;
import Model.Genomes.WordArray;
import Model.Patterns.InstanceLocation;
import Model.SuffixTreePatternFinder.SuffixTrees.GeneralizedSuffixTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 */

public class DatasetSuffixTreeTest {

    private int GENOME_ID = 1;
    private int REPLICON_ID = 1;

    private Pair<WordArray, InstanceLocation> wordLocationPair(int[] word){
        InstanceLocation LOCATION1 = location(word);
        return new Pair<>(wordArray(word), LOCATION1);
    }

    private InstanceLocation location(int[] word){
        return new InstanceLocation(REPLICON_ID, GENOME_ID, 0,
                word.length, Strand.FORWARD, 0, word.length, REPLICON_ID);
    }

    private WordArray wordArray(int[] word){
        return new WordArray(word);
    }

    private GeneralizedSuffixTree createGstWithWords(List<Pair<WordArray, InstanceLocation>> words){
        GeneralizedSuffixTree gst = new GeneralizedSuffixTree();

        for (Pair<WordArray, InstanceLocation> word : words) {
            gst.put(word.getFirst(), word.getSecond());
        }
        gst.computeCount();

        return gst;
    }

    @Test
    public void testGSTFindSingleWordSubstrings() throws Exception {

        int[] WORD = {1, 2, 3, 4};
        Pair<WordArray, InstanceLocation> WORD_LOC = wordLocationPair(WORD);

        List<Pair<WordArray, InstanceLocation>> words = new ArrayList<>();
        words.add(WORD_LOC);
        GeneralizedSuffixTree gst = createGstWithWords(words);

        for (int startIndex = 0; startIndex < WORD.length; startIndex++) {
            for (int endIndex = startIndex+1; endIndex < WORD.length+1; endIndex++) {

                WordArray WORD_ARRAY1 = wordArray(Arrays.copyOfRange(WORD, startIndex, endIndex));
                Map<Integer, List<InstanceLocation>> instances = gst.search(WORD_ARRAY1);

                Assert.assertEquals(1, instances.size());
                Assert.assertEquals(true, instances.containsKey(WORD_LOC.getSecond().getGenomeId()));

                InstanceLocation loc = instances.get(WORD_LOC.getSecond().getGenomeId()).get(0);
                Assert.assertEquals(startIndex, loc.getActualStartIndex());
            }
        }
    }

    @Test
    public void testGSTFindSingleRepeatedSubstring() throws Exception {
        int[] WORD = {1, 2, 1, 2};
        Pair<WordArray, InstanceLocation> WORD_LOC = wordLocationPair(WORD);

        List<Pair<WordArray, InstanceLocation>> words = new ArrayList<>();
        words.add(WORD_LOC);
        GeneralizedSuffixTree gst = createGstWithWords(words);

        WordArray WORD_SUBSTRING = wordArray(Arrays.copyOfRange(WORD, 0, 2));
        Map<Integer, List<InstanceLocation>> instances = gst.search(WORD_SUBSTRING);

        Set<Integer> startIndexes = instances.get(GENOME_ID).stream()
                .map(InstanceLocation::getActualStartIndex)
                .collect(Collectors.toSet());

        Set<Integer> expectedStartIndexes = new HashSet<>();
        expectedStartIndexes.add(0);
        expectedStartIndexes.add(2);

        Assert.assertEquals(expectedStartIndexes, startIndexes);

    }

    @Test
    public void testGSTFindSingleSubstringDuplicatedLetter() throws Exception {
        int[] WORD = {0, 1, 1};
        Pair<WordArray, InstanceLocation> WORD_LOC = wordLocationPair(WORD);

        List<Pair<WordArray, InstanceLocation>> words = new ArrayList<>();
        words.add(WORD_LOC);
        GeneralizedSuffixTree gst = createGstWithWords(words);

        Map<Integer, List<InstanceLocation>> instances = gst.search(wordArray(Arrays.copyOfRange(WORD, 1, WORD.length)));

        Set<Integer> startIndexes = instances.get(GENOME_ID).stream()
                .map(InstanceLocation::getActualStartIndex)
                .collect(Collectors.toSet());

        Set<Integer> expectedStartIndexes = new HashSet<>();
        expectedStartIndexes.add(1);

        Assert.assertEquals(expectedStartIndexes, startIndexes);

    }

    /**
     * A private class used to return a tuples of two elements
     */
    private class Pair<A, B> {

        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
}
