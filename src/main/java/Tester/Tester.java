/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tester;

import Assistants.General;
import static Assistants.General.Get_String_Number;
import Assistants.StopWatch;
import Console.Console;
import Data_Structures.MapDB;
import Dataset_Creation.DatasetCSVBuilder;
import Dataset_Creation.DatasetCSVBuilder.Clasification;
import Dataset_Creation.DatasetCSVBuilder.Feature_Representation;
import Feature_Extraction.AFeatureExtractor;
import Feature_Extraction.MasterFeatureExtractor;
import IO.Directories;
import Implementations.FeatureExtractorNgrams;
import Implementations.FeatureSelectorInfoGainRatio;
import IO.FileReader;
import IO.FileWriter;
import Implementations.FeatureExtractorDocxStructuralPaths;
import Math.Entropy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
import org.mapdb.*;

/**
 *
 * @author Aviad
 */
public class Tester {

    public static Map<String, Integer> m_structuralPaths = new HashMap<>();
    public static String m_PathOfficeFileTempFolder = "";
    public static ArrayList<String> m_ArrayList = new ArrayList<String>();

    public static void main(String[] args) {
        //TestNgram();
        TextDocx();
    }

    public static void TestNgram() {
        StopWatch.Start();

        //String folder_ClassA = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassA_20";
        //String folder_ClassB = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassB_20";
        String folder_ClassA = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\PDF_ClassA";
        String folder_ClassB = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\PDF_ClassB";
        //String folder_ClassA = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\TXT_ClassA";
        //String folder_ClassB = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\TXT_ClassB";
        ArrayList<String> ClassA_elements = Directories.GetDirectoryFilesPaths(folder_ClassA);
        ArrayList<String> ClassB_elements = Directories.GetDirectoryFilesPaths(folder_ClassB);
        //ArrayList<String> ClassA_elements = new ArrayList<>(Arrays.asList("a1b1c1d1", "d1e1f1g1h1", "h1i1j1k1l1","l1m1n1o1p1"));
        //ArrayList<String> ClassB_elements = new ArrayList<>(Arrays.asList("p1q1r1s1t1", "t1u1v1w1x1","x1y1z1"));
        int total_elements_num = ClassA_elements.size() + ClassB_elements.size();

        Console.Print_To_Console(String.format("ClassA folder: %s", folder_ClassA), true, false);
        Console.Print_To_Console(String.format("ClassB folder: %s", folder_ClassB), true, false);
        Console.Print_To_Console(String.format("ClassA elements: %s", Get_String_Number(ClassA_elements.size())), true, false);
        Console.Print_To_Console(String.format("ClassB elements: %s", Get_String_Number(ClassB_elements.size())), true, false);
        Console.Print_To_Console(String.format("Total elements: %s", Get_String_Number(total_elements_num)), true, false);

        //FEATURE EXTRACTION
        int gram = 3;
        int skip = 1;
        MasterFeatureExtractor<String> CFE = new MasterFeatureExtractor<>();
        AFeatureExtractor<String> ngram_extractor = new FeatureExtractorNgrams<>(gram, skip);
        Console.Print_To_Console(String.format("Feature Extraction: %s", ngram_extractor.GetName()), true, false);
        HTreeMap<String, Integer> ngrams_ClassA = CFE.Extract_Features_DF_From_Elements(ClassA_elements, ngram_extractor);
        Console.Print_To_Console(String.format("ClassA unique features: %s", Get_String_Number(ngrams_ClassA.size())), true, false);
        HTreeMap<String, Integer> ngrams_ClassB = CFE.Extract_Features_DF_From_Elements(ClassB_elements, ngram_extractor);
        Console.Print_To_Console(String.format("ClassB unique features: %s", Get_String_Number(ngrams_ClassB.size())), true, false);
        HTreeMap<String, int[]> ngrams_ClassesAB = CFE.Gather_ClassA_ClassB_DF(ngrams_ClassA, ngrams_ClassB);
        Console.Print_To_Console(String.format("Total unique features: %s", Get_String_Number(ngrams_ClassesAB.size())), true, false);
        ngrams_ClassA.clear();
        ngrams_ClassB.clear();
        MapDB.m_db_off_heap_FE.commit();

        //FEATURE SELECTION
        int top_features = 2000;
        double top_percent_features = 0.01;
        Console.Print_To_Console(String.format("Selecting features.."), true, false);
        FeatureSelectorInfoGainRatio fs_IG = new FeatureSelectorInfoGainRatio(ClassA_elements.size(), ClassB_elements.size(), false);
        ArrayList<Pair<String, Integer>> selected_features = fs_IG.Select_Features(ngrams_ClassesAB, top_features, top_percent_features,false);
        Console.Print_To_Console(String.format("Selected features: %s", selected_features.size()), true, false);

        //DATASET CREATION
        boolean add_preffix_element = false;
        boolean add_suffix_classification = true;
        Feature_Representation feature_representation = Feature_Representation.Binary;
        Console.Print_To_Console(String.format("Building dataset..."), true, false);
        Console.Print_To_Console(String.format("Feature representation: %s", feature_representation.toString()), true, false);
        //****************
        DatasetCSVBuilder<String> dataset_builder = new DatasetCSVBuilder<>();
        String dataset_header = dataset_builder.Get_Dataset_Header_CSV(selected_features, add_preffix_element, add_suffix_classification);
        String dataset_classA = dataset_builder.Build_Database_CSV(ClassA_elements, ngram_extractor, selected_features, total_elements_num, feature_representation, Clasification.Benign, add_preffix_element, add_suffix_classification);
        String dataset_classB = dataset_builder.Build_Database_CSV(ClassB_elements, ngram_extractor, selected_features, total_elements_num, feature_representation, Clasification.Malicious, add_preffix_element, add_suffix_classification);
        String dataset = dataset_header + "\n" + dataset_classB + "\n" + dataset_classA;
        StopWatch.Stop();

        //OUTPUTS
        String dataset_path = String.format("D:\\Dropbox\\DATASETS\\DATASET_%s_files(%s)_gram(%s)_Rep(%s).csv", General.Get_TimeStamp_String(), total_elements_num, gram, feature_representation.toString());
        FileWriter.WriteFile(dataset, dataset_path);
        Console.Print_To_Console(String.format("Dataset saved to: %s", dataset_path), true, false);
        Console.Print_To_Console(String.format("Running time: %s", StopWatch.GetTime()), true, false);
        Console.Print_To_Console(String.format("Entropy Values: %s", Entropy.m_memoEntropies.size()), true, false);
        Console.Print_To_Console(String.format("InfoGain Values: %s", fs_IG.m_memoInfoGain.size()), true, false);
    }

    private static void TextDocx() {
        StopWatch.Start();

        String folder_ClassA = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassA_10";
        String folder_ClassB = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassB_10";

        ArrayList<String> ClassA_elements = Directories.GetDirectoryFilesPaths(folder_ClassA);
        ArrayList<String> ClassB_elements = Directories.GetDirectoryFilesPaths(folder_ClassB);

        int total_elements_num = ClassA_elements.size() + ClassB_elements.size();

        Console.Print_To_Console(String.format("ClassA folder: %s", folder_ClassA), true, false);
        Console.Print_To_Console(String.format("ClassB folder: %s", folder_ClassB), true, false);
        Console.Print_To_Console(String.format("ClassA elements: %s", Get_String_Number(ClassA_elements.size())), true, false);
        Console.Print_To_Console(String.format("ClassB elements: %s", Get_String_Number(ClassB_elements.size())), true, false);
        Console.Print_To_Console(String.format("Total elements: %s", Get_String_Number(total_elements_num)), true, false);

        //FEATURE EXTRACTION
        MasterFeatureExtractor<String> CFE = new MasterFeatureExtractor<>();
        AFeatureExtractor<String> structuralFeaturesExtractorDOCX = new FeatureExtractorDocxStructuralPaths();
        Console.Print_To_Console(String.format("Feature Extraction: %s", structuralFeaturesExtractorDOCX.GetName()), true, false);
        HTreeMap<String, Integer> ClassA = CFE.Extract_Features_DF_From_Elements(ClassA_elements, structuralFeaturesExtractorDOCX);
        Console.Print_To_Console(String.format("ClassA unique features: %s", Get_String_Number(ClassA.size())), true, false);
        HTreeMap<String, Integer> ClassB = CFE.Extract_Features_DF_From_Elements(ClassB_elements, structuralFeaturesExtractorDOCX);
        Console.Print_To_Console(String.format("ClassB unique features: %s", Get_String_Number(ClassB.size())), true, false);
        HTreeMap<String, int[]> ClassesAB = CFE.Gather_ClassA_ClassB_DF(ClassA, ClassB);
        Console.Print_To_Console(String.format("Total unique features: %s", Get_String_Number(ClassesAB.size())), true, false);
        //ClassA.clear();
        //ClassB.clear();
        MapDB.m_db_off_heap_FE.commit();

        //FEATURE SELECTION
        int top_features = 500;
        double top_percent_features = 0.01;
        Console.Print_To_Console(String.format("Selecting features.."), true, false);
        FeatureSelectorInfoGainRatio fs_IG = new FeatureSelectorInfoGainRatio(ClassA_elements.size(), ClassB_elements.size(), false);
        ArrayList<Pair<String, Integer>> selected_features = fs_IG.Select_Features(ClassesAB, top_features, top_percent_features,true);
        Console.Print_To_Console(String.format("Selected features: %s", selected_features.size()), true, false);

        //DATASET CREATION
        boolean add_preffix_element = false;
        boolean add_suffix_classification = true;
        Feature_Representation feature_representation = Feature_Representation.Binary;
        Console.Print_To_Console(String.format("Building dataset..."), true, false);
        Console.Print_To_Console(String.format("Feature representation: %s", feature_representation.toString()), true, false);
        //****************
        DatasetCSVBuilder<String> dataset_builder = new DatasetCSVBuilder<>();
        String dataset_header = dataset_builder.Get_Dataset_Header_CSV(selected_features, add_preffix_element, add_suffix_classification);
        String dataset_classA = dataset_builder.Build_Database_CSV(ClassA_elements, structuralFeaturesExtractorDOCX, selected_features, total_elements_num, feature_representation, Clasification.Benign, add_preffix_element, add_suffix_classification);
        String dataset_classB = dataset_builder.Build_Database_CSV(ClassB_elements, structuralFeaturesExtractorDOCX, selected_features, total_elements_num, feature_representation, Clasification.Malicious, add_preffix_element, add_suffix_classification);
        String dataset = dataset_header + "\n" + dataset_classB + "\n" + dataset_classA;
        StopWatch.Stop();

        //OUTPUTS
        String dataset_path = String.format("D:\\Dropbox\\DATASETS\\DATASET_%s_DOCX_files(%s)_Rep(%s).csv", General.Get_TimeStamp_String(), total_elements_num, feature_representation.toString());
        FileWriter.WriteFile(dataset, dataset_path);
        Console.Print_To_Console(String.format("Dataset saved to: %s", dataset_path), true, false);
        Console.Print_To_Console(String.format("Running time: %s", StopWatch.GetTime()), true, false);
        Console.Print_To_Console(String.format("Entropy Values: %s", Entropy.m_memoEntropies.size()), true, false);
        Console.Print_To_Console(String.format("InfoGain Values: %s", fs_IG.m_memoInfoGain.size()), true, false);
    }

}
