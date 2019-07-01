package Model.Patterns;

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
    private double[] pValues;
    /**
     * Total number of input genomes
     */
    private int numberOfGenomes;
    private int avgGenomeSize;

    private Expm1 expm1 = new Expm1();

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

    private double computeLogMaxParalogCount(Set<Integer> intersectionOfGenomesWithPatternChars, List<Integer> patternLetters){
        //int paralogCountProductSum = 0;
        double maxLogParalogCount = 0;
        for (int genomeId: intersectionOfGenomesWithPatternChars) {

            Map<Integer, Integer> currGenomeParalogCount = genomeToCogParalogCount.get(genomeId);
            double paralogCountLogSum = patternLetters.stream()
                    .map(currGenomeParalogCount::get)
                    .mapToDouble(Math::log).sum();

            if (paralogCountLogSum > paralogCountLogSum) {
                maxLogParalogCount = paralogCountLogSum;
            }
        }

        return maxLogParalogCount;
    }

    public double computePatternScore(List<Integer> patternLetters, int maxInsertions, int genomesWithInstance){

        Set<Integer> intersectionOfGenomesWithPatternChars = genomesWithPatternChars(patternLetters);

        double averageParalogCount = computeLogMaxParalogCount(intersectionOfGenomesWithPatternChars, patternLetters);

        return pvalCrossGenome(patternLetters.size(), maxInsertions, averageParalogCount, genomesWithInstance);
    }

    /**
     * Computes a ranking score for a given pattern
     * @param patternLength pattern length
     * @param maxInsertions maximal number of allowed insertions
     * @param maxLogParalogFrequency the log of the maximal paralog frequency for each gene in the pattern
     * @param genomesWithInstance number of genomes containing an instance of the pattern
     * @return ranking score
     */
    public double pvalCrossGenome(int patternLength, int maxInsertions, double maxLogParalogFrequency,
                                  int genomesWithInstance){

        int G = numberOfGenomes;
        int n = avgGenomeSize;
        double result = 0;

        double logPval = Math.min(logPvalInsertions(n, patternLength, maxInsertions) + maxLogParalogFrequency, 0);

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
