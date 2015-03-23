/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Math;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;

/**
 *
 * @author Aviad
 */
public class MathCalc {

    /**
     * Return Logarithm of the given number in the given base
     *
     * @param number the number to calculate to Logarithm on
     * @param base base of the Logarithm
     * @return Logarithm of the given number in the given base
     */
    public static double Log(double number, double base) {
        return FastMath.log(base, number);
    }

    /**
     * Return Probability P with Laplace Correction
     *
     * @param part part value
     * @param total total values
     * @param parts_num number of parts
     * @return Probability P with Laplace Correction
     */
    public static double GetPwithLaplaceCorrection(int part, int total, int parts_num) {
        return ((double) part + (double) 1.0) / ((double) total + (double) parts_num);
    }

    /**
     * Return TD*IDF calculation
     *
     * @param num_of_elements total number of elements in dataset (Malicious +
     * Benign)
     * @param num_of_occurrences_of_most_common_feature add prefix column
     * identifying the record
     * @param feature_occurrences_in_element count the number of times a
     * specific feature appear in element
     * @param num_of_elements_contain_the_feature add suffix column identifying
     * the class of the record
     * @return TD*IDF calculation
     */
    public static double GetTFIDF(int feature_occurrences_in_element, int num_of_occurrences_of_most_common_feature, int num_of_elements, int num_of_elements_contain_the_feature) {
        double TF = ((double) feature_occurrences_in_element) / ((double) num_of_occurrences_of_most_common_feature);
        double IDF = MathCalc.Log((((double) num_of_elements) / ((double) num_of_elements_contain_the_feature)), 2);
        return TF * IDF;
    }

    /**
     * Return rounded double number
     *
     * @param num the number to round
     * @param decimalPlaces decimal places to cut the number
     * @return rounded double number
     */
    public static double Round(double num, int decimalPlaces) {
        return Precision.round(num, decimalPlaces);
    }
}
