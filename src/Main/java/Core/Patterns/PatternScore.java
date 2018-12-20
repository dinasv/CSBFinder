package Core.Patterns;

import org.apache.commons.math3.special.Beta;

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

    /**
     * for each cog, a set of genomes (bac_index) in which the cog appears
     */
    public Map<Integer, Set<Integer>> cogToContainingGenomes;

    public Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount;

    public PatternScore(int max_genome_size, int numberOfGenomes, int dataset_length_sum,
                        Map<Integer, Set<Integer>> cogToContainingGenomes,
                        Map<Integer, Map<Integer, Integer>> genomeToCogParalogCount){

        pValues = new double[max_genome_size+1];
        this.numberOfGenomes = numberOfGenomes;
        this.cogToContainingGenomes = cogToContainingGenomes;
        this.genomeToCogParalogCount = genomeToCogParalogCount;

        avgGenomeSize = 1;
        if (numberOfGenomes > 0 ) {
            avgGenomeSize = dataset_length_sum / numberOfGenomes;
        }
    }

    public double computePatternScore(List<Integer> patternLetters, int max_insertions, int pattern_occs_keys_size){

        Set<Integer> intersectionOfGenomesWithPatternChars = new HashSet<>(cogToContainingGenomes.get(patternLetters.get(0)));
        for (int ch: patternLetters) {
            intersectionOfGenomesWithPatternChars.retainAll(cogToContainingGenomes.get(ch));
        }

        int paralog_count_product_sum = 0;
        int paralog_count_product;
        for (int seq_key: intersectionOfGenomesWithPatternChars) {

            Map<Integer, Integer> curr_seq_paralog_count = genomeToCogParalogCount.get(seq_key);
            paralog_count_product = 1;
            for (int cog : patternLetters) {
                int curr_cog_paralog_count = curr_seq_paralog_count.get(cog);
                paralog_count_product *= curr_cog_paralog_count;
            }
            paralog_count_product_sum += paralog_count_product;
        }

        int average_paralog_count = paralog_count_product_sum/intersectionOfGenomesWithPatternChars.size();

        return pval_cross_genome(patternLetters.size(), max_insertions,
                average_paralog_count, pattern_occs_keys_size);
    }

    /**
     * Computes a ranking score for a given pattern
     * @param w pattern length
     * @param k maximal number of allowed insertions
     * @param h product of average paralog frequency for each gene in the pattern
     * @param g number of genomes containing an instance of the pattern
     * @return ranking score
     */
    private double pval_cross_genome(int w, int k, int h, int g){
        int G = numberOfGenomes;
        int n = avgGenomeSize;
        double result = 0;

        double q = q_homologs(n, w, k, h);

        double a = g/(double)G;
        if ( a == 1){
            result = -G*Math.log(q);
        } else if (q < a && q > 0 && a < 1){
            result = G*H(a, q);
        }else{
            result = binomialCDF(G, g, q);
            //base e
            result = -Math.log(result);
        }

        if ( (Double.isNaN(result)) || (result < 0) ){
            result = 0;
        }

        return result;
    }
    private static double H(double a, double p){
        return a*Math.log(a/p) + (1-a)*Math.log((1-a)/(1-p));
    }

    public double q_homologs(int n, int w, int k, int h){
        double q_result = q_insert(n, w, k);

        /*if (error_type.equals("insert")) {
            q_result = q_insert(n, w, k, q_val);
        }else if(error_type.equals("mismatch")){
            q_result = q_mismatch(n, w, k, q_val);
        }else if(error_type.equals("deletion")){
            q_result =  q_deletion(n, w, k, q_val);
        }*/

        return q_result*h;

    }
    public static double q_mismatch(int n, int w, int k, double[] q_val){
        double result = 0;
        if ( q_val[w] != 0){
            result = q_val[w];
        }else {
            for (int i = 0; i < k + 1; i++) {
                long numerator = (n-w+1) * binomialCoefficient(w, i);
                result += divide_by_product(numerator, n - w + i + 1, n);
            }
            q_val[w] = result;
        }
        return result;
    }

    public static double q_deletion(int n, int w, int k, double[] q_val){
        double result = 0;
        if ( q_val[w] != 0){
            result = q_val[w];
        }else {
            for (int i = 0; i < k + 1; i++) {
                long numerator = binomialCoefficient(w, w-i);
                result += divide_by_product(numerator, n - w + 2 + i, n);
            }
            q_val[w] = result;
        }
        return result;
    }

    private double q_insert(int n, int w, int k){
        double result = 0;
        if (pValues[w] != 0){
            result = pValues[w];
        }else {
            for (int i = 0; i < k + 1; i++) {
                long numerator = (n - w - i + 1) * binomialCoefficient(w+i-2, w-2);
                result += divide_by_product(numerator, n - w + 1, n);
            }
            pValues[w] = result;
        }
        return result;
    }


    public static double divide_by_product(long val, int start, int end){
        double result = val;
        for (int i = start; i < end + 1; i++) {
            result /= (double)i;
        }
        return result;
    }

    //P(x>=k)
    private static double binomialCDF(int n, int k, double p){
        return Beta.regularizedBeta(p, k, n-k+1);
    }

    public static double computePatternScore(PatternScore patternScore, List<Integer> patternLetters, int maxInsertions,
                                             int maxError, int maxDeletions, int patternOccsKeysSize){

        if (patternScore != null){
            return patternScore.computePatternScore(patternLetters, maxInsertions, patternOccsKeysSize);
        }
        return -1;
    }

}
