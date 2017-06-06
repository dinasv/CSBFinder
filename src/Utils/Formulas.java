package Utils;

import java.util.HashMap;

import org.apache.commons.math3.special.Beta;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficient;


/**
 * Created by Dina on 10/26/2016.
 */
public class Formulas {

    /**
     * Computes a ranking score for a given motif
     * @param n the minimal number of input genomes where one of the motif genes appear
     * @param w motif length
     * @param k number of insertions/deletion/mismatches
     * @param h product of average paralog frequency for each gene in the motif
     * @param G minimal number of input genomes, where one of the cog appears
     * @param g motif occurrence count
     * @param error_type mismatch/insert/deletion
     * @return ranking score
     */
    public static double pval_cross_genome(int n, int w, int k, int h, int G, int g, String error_type, double[] q_val
                                            , int motif_id){
        double result = 0;

        double q = q_homologs(n, w, k, h, error_type, q_val);

        double a = g/(double)G;
        if ( a == 1){
            result = -G*Math.log(q);
        } else if (q < a && q > 0 && a < 1){
            result = G*H(a, q);
            //double res = -Math.log(binomialCDF(G, g, q));
        }else{
            result = binomialCDF(G, g, q);

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

    public static double q_homologs(int n, int w, int k, int h, String error_type, double[] q_val){
        double q_result = 0;
        if (error_type.equals("insert")) {
            q_result = q_insert(n, w, k, q_val);
        }else if(error_type.equals("mismatch")){
            q_result = q_mismatch(n, w, k, q_val);
        }else if(error_type.equals("deletion")){
            q_result =  q_deletion(n, w, k, q_val);
        }

        double result = q_result*h;
        return result;

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

    public static double q_insert(int n, int w, int k, double[] q_val){
        double result = 0;
        if ( q_val[w] != 0){
            result = q_val[w];
        }else {
            for (int i = 0; i < k + 1; i++) {
                long numerator = (n - w - i + 1) * binomialCoefficient(w+i-2, w-2);
                result += divide_by_product(numerator, n - w + 1, n);
            }
            q_val[w] = result;
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

}
