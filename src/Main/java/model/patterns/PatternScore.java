package model.patterns;

import model.genomes.GenomesInfo;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.special.Beta;
import org.apache.commons.math3.analysis.function.Expm1;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficient;
import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientLog;


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
    Two genomes that have distance more than delta are considered close to each other
     */
    private double delta;

    /**
     * for each cog, a set of genomes (bac_index) in which the cog appears
     */
    private Map<Integer, Set<Integer>> cogToContainingGenomes;

    private Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount;

    private GenomesInfo genomesInfo;

    public PatternScore(GenomesInfo genomesInfo, double delta){

        this(genomesInfo.getMaxGenomeSize(), genomesInfo.getNumberOfGenomes(), genomesInfo.getDatasetLengthSum(),
                genomesInfo.cogToContainingGenomes, genomesInfo.genomeToCogParalogCount);

        this.genomesInfo = genomesInfo;
        this.delta = delta;


        int correctedNumOfGenomes = calcCorrectedNumOfGenomes(IntStream.range(0, numberOfGenomes).boxed()
                                                        .collect(Collectors.toList()));

        if (correctedNumOfGenomes != -1){
            this.numberOfGenomes = correctedNumOfGenomes;
        }

    }

    public PatternScore(int maxGenomeSize, int numberOfGenomes, int datasetLengthSum,
                        Map<Integer, Set<Integer>> cogToContainingGenomes,
                        Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount){

        pValues = new double[maxGenomeSize+1];
        this.numberOfGenomes = numberOfGenomes;
        this.cogToContainingGenomes = cogToContainingGenomes;
        this.genomeToCogParalogCount = genomeToCogParalogCount;

        avgGenomeSize = 1;
        if (numberOfGenomes > 0) {
            avgGenomeSize = datasetLengthSum / numberOfGenomes;
        }

        genomesInfo = null;
        delta = 1;
    }

    public int calcCorrectedNumOfGenomes(Collection<Integer> genomeIds){

        if (genomesInfo == null || delta == 1){
            return -1;
        }

        double correctedNumOfGenomes = genomeIds.stream().map(
                genomeId1 -> (int)genomeIds.stream().mapToDouble(
                genomeId2 -> genomesInfo.getGenomesDistance(genomeId1, genomeId2))
                        .filter(dist -> dist >= delta).count()
        ).mapToDouble(i -> 1/(double)i).sum();

        return (int)correctedNumOfGenomes;
    }

    private Set<Integer> genomesWithPatternChars(List<Integer> patternLetters){
        int firstLetter = patternLetters.size() > 0 ? patternLetters.get(0) : 0;
        Set<Integer> genomeIds = cogToContainingGenomes.get(firstLetter);

        if (genomeIds == null){
            return new HashSet<>();
        }

        Set<Integer> intersectionOfGenomesWithPatternChars = new HashSet<>(genomeIds);

        for (int ch: patternLetters) {
            intersectionOfGenomesWithPatternChars.retainAll(cogToContainingGenomes.get(ch));
        }
        return intersectionOfGenomesWithPatternChars;
    }

    private double computeLogMaxParalogCount(Set<Integer> intersectionOfGenomesWithPatternChars,
                                             List<Integer> patternLetters){

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

    public double computePatternScore(List<Integer> patternLetters, int maxInsertions,
                                      Collection<Integer> instanceGenomeIds){

        int correctedNumOfInstances = calcCorrectedNumOfGenomes(instanceGenomeIds);

        if (correctedNumOfInstances == -1){
            correctedNumOfInstances = instanceGenomeIds.size();
        }

        return computePatternScore(patternLetters,  maxInsertions, correctedNumOfInstances);
    }

    public double computePatternScore(List<Integer> patternLetters, int maxInsertions,
                                      int genomesWithInstance){

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

            try {
                double binomialLog = binomialCoefficientLog(patternLength + maxInsertions - 2, patternLength - 2);
                int instanceStartIndexes = n - patternLength + 1;
                double numerator = binomialLog + Math.log(instanceStartIndexes - maxInsertions);

                int denominator = instanceStartIndexes;
                double logMaxInsertions = maxInsertions > 0 ? maxInsertions : 1;
                result = logMaxInsertions + numerator - logSum(denominator, n);

                if (instanceStartIndexes <= 0 || instanceStartIndexes - maxInsertions <= 0){
                    result = 0;
                }
            }catch (MathArithmeticException e){
                System.out.println(String.format("Arithmetic exception for pattern of length %d, score set to 0",
                        patternLength));
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

    public int getNumberOfGenomes(){
        return numberOfGenomes;
    }

}
