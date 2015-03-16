/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import DatasetCreation.DatasetCSVBuilder;
import DatasetCreation.DatasetCreator;
import static DatasetCreation.DatasetCreator.BuildDataset;
import FeatureExtraction.AFeatureExtractor;
import FeatureSelection.AFeatureSelector;
import Implementations.FeatureExtractorDocStreamPaths;
import Implementations.FeatureExtractorDocxStructuralPaths;
import Implementations.FeatureExtractorNgrams;
import Implementations.FeatureSelectorInfoGainRatio;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Aviad
 */
public class Framework {

    public static enum Clasification {

        Benign,
        Malicious
    }

    public static enum FeatureRepresentation {

        Binary,
        TFIDF
    }

    public static String m_benignFolder = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassA_100";
    public static String m_maliciousFolder = "D:\\Dropbox\\TESTS\\FeatureExtractionData\\DocX_ClassB_20";
    public static String m_destinationFolder = "D:\\Dropbox\\DATASETS";
    public static String m_datasetFilenameFormat = "DATASET_%s_Files(B%s_M%s)_FE(%s)_FS(%s)_Rep(%s)";
    public static ArrayList<Integer> m_tops = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000));

    //public static String m_benignFolder = "D:\\Nir_Aviad_DT_Shared\\DATASETS\\Office\\_OFFICE_DATASET_MERGED_SHA256\\DOCX_Benign_16108";
    //public static String m_maliciousFolder = "D:\\Nir_Aviad_DT_Shared\\DATASETS\\Office\\_OFFICE_DATASET_MERGED_SHA256\\DOCX_Malicious_327";
    public static void CreateDataset() {
        //AFeatureExtractor<String> featureExtractor = new FeatureExtractorNgrams<>(3, 1);
        //AFeatureExtractor<String> featureExtractor = new FeatureExtractorDocStreamPaths();
        AFeatureExtractor<String> featureExtractor = new FeatureExtractorDocxStructuralPaths();
        AFeatureSelector featureSelector = new FeatureSelectorInfoGainRatio(false);
        int topFeatures = 2000;
        FeatureRepresentation featureRepresentation = FeatureRepresentation.Binary;
        boolean createDatabaseCSV = true;
        boolean addElementIDColumn = false;
        boolean addClassificationColumn = true;
        boolean printFeaturesDocumentFrequencies = false;
        boolean printSelectedFeaturesScore = true;
        boolean generateTops = true;

        String datasetCSV = BuildDataset(m_benignFolder,
                m_maliciousFolder,
                m_destinationFolder,
                m_datasetFilenameFormat,
                featureExtractor,
                featureSelector,
                topFeatures,
                featureRepresentation,
                createDatabaseCSV,
                addElementIDColumn,
                addClassificationColumn,
                printFeaturesDocumentFrequencies,
                printSelectedFeaturesScore
        );

        if (generateTops) {
            Console.Console.PrintLine("Generating tops:", true, false);
            DatasetCSVBuilder.GenerateTopDatasets(datasetCSV, m_tops, m_destinationFolder,DatasetCreator.m_datasetFilename ,addElementIDColumn, addClassificationColumn);
        }
    }

    public static void main(String[] args) {
        CreateDataset();
    }
}
