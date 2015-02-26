/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Math;

import Console.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author Aviad
 */
public class MathCalc {

    public static Map<String, Double> m_entropies = new HashMap<>();

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
     * Calculates the entropy of the given features values
     *
     * @param values values represent the occurrences number of each class
     * @return the entropy of the given features values
     */
    public static double Get_Entropy(ArrayList<Double> values) {
        AreEntropyValuesSumOne(values);
        String values_code = GetEntropyValuesCode(values);

        double entropy = 0.0;
        if (m_entropies.containsKey(values_code)) {
            entropy = m_entropies.get(values_code);
        } else {
            for (Double value : values) {
                entropy -= value * Log(value, 2);
            }
            m_entropies.put(values_code, entropy);
        }
        return entropy;
    }

    private static String GetEntropyValuesCode(ArrayList<Double> values) {
        Double a = values.get(0);
        Double b = values.get(1);
        return (a >= b) ? a + b + "" : b + a + "";
    }

    /**
     * Check if the values for the entropy calculations are summed to 1
     *
     * @param values values represent the occurrences number of each class
     * @return true if the the entropy calculations are summed to 1
     */
    private static boolean AreEntropyValuesSumOne(ArrayList<Double> values) {
        double values_sum = 0;
        for (Double value : values) {
            values_sum += value;
        }

        if (FastMath.round(values_sum) == 1) {
            return true;
        } else {
            Console.Print_To_Console("Error: Get_Entropy() was provided with value that do not sum to 1!", true, false);
            return false;
        }
    }

    /**
     * Return Probability P with Laplace Correction
     *
     * @param part part value
     * @param total total values
     * @param parts_num number of parts
     * @return Probability P with Laplace Correction
     */
    public static double Get_P_with_Laplace_Correction(int part, int total, int parts_num) {
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
    public static double Get_TFIDF(int feature_occurrences_in_element, int num_of_occurrences_of_most_common_feature, int num_of_elements, int num_of_elements_contain_the_feature) {
        double TF = ((double) feature_occurrences_in_element) / ((double) num_of_occurrences_of_most_common_feature);
        double IDF = MathCalc.Log((((double) num_of_elements) / ((double) num_of_elements_contain_the_feature)), 2);
        return TF * IDF;
    }
}
