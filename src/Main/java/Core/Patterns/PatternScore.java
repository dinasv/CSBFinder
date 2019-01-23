package Core.Patterns;

import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.analysis.function.Expm1;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficient;


/**
 * This class is used for computation of score for a pattern
 */
public class PatternScore {

    /**
     * Array used for memoization, as each patterns with the same length have the same basic q_val
     */
    double[] pValues;
    /**
     * Total number of input genomes
     */
    int numberOfGenomes;
    int avgGenomeSize;

    Expm1 expm1 = new Expm1();

    /**
     * for each cog, a set of genomes (bac_index) in which the cog appears
     */
    public Map<Integer, Set<Integer>> cogToContainingGenomes;

    public Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount;

    public PatternScore(int maxGenomeSize, int numberOfGenomes, int datasetLengthSum,
                        Map<Integer, Set<Integer>> cogToContainingGenomes,
                        Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount){

        pValues = new double[maxGenomeSize+1];
        this.numberOfGenomes = numberOfGenomes;
        this.cogToContainingGenomes = cogToContainingGenomes;
        this.genomeToCogParalogCount = genomeToCogParalogCount;

        avgGenomeSize = 1;
        if (numberOfGenomes > 0 ) {
            avgGenomeSize = datasetLengthSum / numberOfGenomes;
        }
    }

    private Set<Integer> genomesWithPatternChars(List<Integer> patternLetters){
        Set<Integer> intersectionOfGenomesWithPatternChars = new HashSet<>(cogToContainingGenomes.get(patternLetters.get(0)));
        for (int ch: patternLetters) {
            intersectionOfGenomesWithPatternChars.retainAll(cogToContainingGenomes.get(ch));
        }
        return intersectionOfGenomesWithPatternChars;
    }

    private int computeAverageParalogCount(Set<Integer> intersectionOfGenomesWithPatternChars, List<Integer> patternLetters){
        int paralogCountProductSum = 0;
        int paralogCountProduct;
        for (int seq_key: intersectionOfGenomesWithPatternChars) {

            Map<Integer, Integer> currSeqParalogCount = genomeToCogParalogCount.get(seq_key);
            paralogCountProduct = patternLetters.stream()
                    .map(cog -> currSeqParalogCount.get(cog))
                    .reduce(1, (a, b) -> a*b);

            paralogCountProductSum += paralogCountProduct;
        }

        return paralogCountProductSum/intersectionOfGenomesWithPatternChars.size();
    }

    public double computePatternScore(List<Integer> patternLetters, int maxInsertions, int genomesWithInstance){

        Set<Integer> intersectionOfGenomesWithPatternChars = genomesWithPatternChars(patternLetters);

        int averageParalogCount = computeAverageParalogCount(intersectionOfGenomesWithPatternChars, patternLetters);

        return pvalCrossGenome(patternLetters.size(), maxInsertions, averageParalogCount, genomesWithInstance);
    }

    /**
     * Computes a ranking score for a given pattern
     * @param patternLength pattern length
     * @param maxInsertions maximal number of allowed insertions
     * @param averageParalogFrequency product of average paralog frequency for each gene in the pattern
     * @param genomesWithInstance number of genomes containing an instance of the pattern
     * @return ranking score
     */
    public double pvalCrossGenome(int patternLength, int maxInsertions, int averageParalogFrequency,
                                  int genomesWithInstance){

        int G = numberOfGenomes;
        int n = avgGenomeSize;
        double result = 0;

        double logPval = logPvalInsertions(n, patternLength, maxInsertions) + Math.log(averageParalogFrequency);

        double a = (double)genomesWithInstance/G;
        if ( a == 1){
            result = -G*logPval;
        } else if (logPval < Math.log(a) && logPval < 0 && a < 1){
            result = G*H(a, logPval);
        }else{
            result = binomialCDF(G, genomesWithInstance, Math.exp(logPval));
            //base e
            result = result <= 0 ? 0 : -Math.log(result);
        }

        if ( (Double.isNaN(result)) || (result < 1) ){
            result = 0;
        }

        return result;
    }

    private double H(double a, double logP){
        return a*(Math.log(a) - logP) + (1-a)*(Math.log(1-a) - Math.log(-expm1.value(logP)));
    }

    private double logPvalInsertions(int n, int patternLength, int maxInsertions){
        double result = 0;
        if (pValues[patternLength] != 0){
            result = pValues[patternLength];
        }else {
            long binomial = binomialCoefficient(patternLength+maxInsertions-2, patternLength-2);
            int instanceStartIndexes = n - patternLength + 1;
            double numerator = Math.log(binomial) + Math.log(instanceStartIndexes - maxInsertions);

            int denominator = instanceStartIndexes;
            double logMaxInsertions = maxInsertions > 0 ? maxInsertions : 1;
            result = logMaxInsertions + numerator - logSum(denominator, n);

            if (binomial <= 0 || instanceStartIndexes <= 0 || instanceStartIndexes - maxInsertions <= 0){
                result = 0;
            }

            pValues[patternLength] = result;
        }

        return result;
    }

    private static double logSum(int start, int end){
        double result = 0;
        for (int i = start; i < end + 1; i++) {
            result += Math.log(i);
        }
        return result;
    }

    //P(x>=k)
    private static double binomialCDF(int n, int k, double p){
        return Beta.regularizedBeta(p, k, n-k+1);
    }

}
