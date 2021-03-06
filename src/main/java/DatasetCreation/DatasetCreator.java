/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatasetCreation;

import Assistants.General;
import static Assistants.General.GetStringNumber;
import Assistants.StopWatch;
import DataStructures.MapDB;
import FeatureExtraction.IFeatureExtractor;
import FeatureExtraction.MasterFeatureExtractor;
import FeatureRepresentation.FeatureRepresentor.FeatureRepresentation;
import FeatureSelection.AFeatureSelector;
import Framework.DBFramework.Classification;
import IO.Console;
import IO.Directories;
import IO.FileWriter;
import IO.Serializer;
import java.util.ArrayList;
import java.util.Map;
import javafx.util.Pair;

/**
 *
 * @author Aviadjo
 */
public class DatasetCreator {

    public static String m_datasetFilename = "";

    /**
     * Return CSV string which represent the dataset
     *
     * @param fileType the file type of the files in the collection
     * @param ClassAdirectory folder of elements from class A
     * @param ClassBdirectory folder of elements from class B
     * @param featureExtractor The feature extractor to use
     * @param datasetFilenameFormat the format of the destination file
     * @param featureSelector The feature selector to use
     * @param topFeatures top features to select
     * @param featureRepresentation feature representation method (for example:
     * Boolea, TFIDF)
     * @param addElementIDColumn whether to add element ID column to the dataset
     * CSV
     * @param addClassificationColumn whether to add classification column to
     * the dataset CSV
     * @param destinationFolderPath destination folder path
     * @param printFileFeaturesFrequencies whether to print the features'
     * document frequencies
     * @param createDatabaseCSV whether to create the dataset record
     * @param printSelectedFeaturesScore whether to print the score of the
     * selected features
     * @param printFileSelectedFeatures whether to print file with the selected
     * features
     * @return dataset CSV
     */
    public static StringBuilder BuildDatasetCSV(
            String fileType,
            String ClassAdirectory,
            String ClassBdirectory,
            String destinationFolderPath,
            String datasetFilenameFormat,
            IFeatureExtractor<String> featureExtractor,
            AFeatureSelector featureSelector,
            int topFeatures,
            FeatureRepresentation featureRepresentation,
            boolean createDatabaseCSV,
            boolean addElementIDColumn,
            boolean addClassificationColumn,
            boolean printFileFeaturesFrequencies,
            boolean printSelectedFeaturesScore
    ) {
        StopWatch.Start();

        ArrayList<String> classAelements = Directories.GetDirectoryFilesPaths(ClassAdirectory);
        ArrayList<String> classBelements = Directories.GetDirectoryFilesPaths(ClassBdirectory);
        int totalElementsNum = classAelements.size() + classBelements.size();

        Console.PrintLine(String.format("File type: %s", fileType));
        Console.PrintLine(String.format("Benign folder: %s", ClassAdirectory));
        Console.PrintLine(String.format("Malicious folder: %s", ClassBdirectory));
        Console.PrintLine(String.format("Benign elements: %s", GetStringNumber(classAelements.size())));
        Console.PrintLine(String.format("Malicious elements: %s", GetStringNumber(classBelements.size())));
        Console.PrintLine(String.format("Total elements: %s", GetStringNumber(totalElementsNum)));
        Console.PrintLine(String.format("Feature Extraction: %s", featureExtractor.GetName()));
        Console.PrintLine(String.format("Feature Selection: %s", featureSelector.GetName()));
        Console.PrintLine(String.format("Feature Representation: %s", featureRepresentation.toString()));
        Console.PrintLine(String.format("Top features selection: %s", GetStringNumber(topFeatures)));

        //FEATURE EXTRACTION
        MasterFeatureExtractor<String> MFE = new MasterFeatureExtractor<>();
        Map<String, Integer> classAfeatures = MFE.ExtractFeaturesFrequenciesFromElements(classAelements, featureExtractor);
        Console.PrintLine(String.format("Benign unique features: %s", GetStringNumber(classAfeatures.size())));
        Map<String, Integer> classBfeatures = MFE.ExtractFeaturesFrequenciesFromElements(classBelements, featureExtractor);
        Console.PrintLine(String.format("Malicious unique features: %s", GetStringNumber(classBfeatures.size())));
        Map<String, int[]> classesABfeatures = MFE.GatherClassAClassBFeatureFrequency(classAfeatures, classBfeatures);
        Console.PrintLine(String.format("Total unique features: %s", GetStringNumber(classesABfeatures.size())));
        MapDB.m_db_off_heap_FE.commit();

        m_datasetFilename = String.format(datasetFilenameFormat, General.GetTimeStamp(), fileType, classAelements.size(), classBelements.size(), featureExtractor.GetName(), featureSelector.GetName(), featureRepresentation.toString());

        //PRINT FILE - Features Document Frequencies
        if (printFileFeaturesFrequencies) {
            PrintCSVFileFeaturesDocumentFrequencies(classesABfeatures, destinationFolderPath);
        }

        //FEATURE SELECTION
        Console.PrintLine(String.format("Selecting top %s features using %s", topFeatures, featureSelector.GetName()));
        ArrayList<Pair<String, Integer>> selectedFeatures = featureSelector.SelectTopFeatures(classesABfeatures, classAelements.size(), classBelements.size(), topFeatures, printSelectedFeaturesScore);

        //PRINT FILE - SELECTED FEATURES
        PrintCSVFileSelectedFeatures(selectedFeatures, classesABfeatures, destinationFolderPath);
        SerializeSelectedFeatures(selectedFeatures, destinationFolderPath);

        //DATASET CREATION
        StringBuilder datasetCSV = new StringBuilder();
        if (createDatabaseCSV) {
            Console.PrintLine(String.format("Building dataset..."));
            Console.PrintLine(String.format("Feature representation: %s", featureRepresentation.toString()));
            //****************
            DatasetCSVBuilder<String> datasetBuilder = new DatasetCSVBuilder<>();
            StringBuilder datasetHeaderCSV = datasetBuilder.GetDatasetHeaderCSV(selectedFeatures.size(), addElementIDColumn, addClassificationColumn);
            StringBuilder datasetClassACSV = datasetBuilder.BuildDatabaseCSV(classAelements, featureExtractor, selectedFeatures, totalElementsNum, featureRepresentation, Classification.Benign, addElementIDColumn, addClassificationColumn);
            StringBuilder datasetClassBCSV = datasetBuilder.BuildDatabaseCSV(classBelements, featureExtractor, selectedFeatures, totalElementsNum, featureRepresentation, Classification.Malicious, addElementIDColumn, addClassificationColumn);
            datasetCSV.append(datasetHeaderCSV).append("\n").append(datasetClassBCSV).append("\n").append(datasetClassACSV);
            StopWatch.Stop();

            //OUTPUTS
            String datasetPath = destinationFolderPath + "\\" + m_datasetFilename + ".csv";
            FileWriter.WriteFile(datasetCSV.toString(), datasetPath);
            Console.PrintLine(String.format("Running time: %s", StopWatch.GetTimeSecondsString()));
            Console.PrintLine(String.format("Dataset saved to: %s", datasetPath));
            //Console.PrintLine(String.format("Entropy Values: %s", Entropy.m_memoEntropies.size()), true, false);
            //Console.PrintLine(String.format("InfoGain Values: %s", featureSelector.m_memo.size()), true, false);
        }
        return datasetCSV;
    }

    /**
     * Print CSV file contain list of features and their document frequencies
     *
     * @param featuresDocumentFrequencies features document frequencies
     * @param destinationFolderPath path of the destination folder to print the
     * selected features file to
     */
    private static void PrintCSVFileFeaturesDocumentFrequencies(Map<String, int[]> featuresDocumentFrequencies, String destinationFolderPath) {
        String featuresDocumentFrequenciesFilePath = destinationFolderPath + "\\" + m_datasetFilename + "_FeaturesDF" + ".csv";
        StringBuilder sb = DatasetCSVBuilder.GetFeaturesDocumentFrequenciesCSV(featuresDocumentFrequencies);
        FileWriter.WriteFile(sb.toString(), featuresDocumentFrequenciesFilePath);
        Console.PrintLine(String.format("Features Document Frequencies saved to: %s", featuresDocumentFrequenciesFilePath));
    }

    /**
     * Print CSV file contain list of selected features
     *
     * @param selectedFeatures ArrayList of selected features selected features
     * @param featuresDocumentFrequencies all features document frequencies
     * (Benign, Malicious)
     * @param destinationFolderPath path of the destination folder to print the
     * selected features file to
     */
    private static void PrintCSVFileSelectedFeatures(ArrayList<Pair<String, Integer>> selectedFeatures, Map<String, int[]> featuresDocumentFrequencies, String destinationFolderPath) {
        StringBuilder sb = DatasetCSVBuilder.GetSelectedFeaturesCSV(selectedFeatures, featuresDocumentFrequencies);
        String featuresFilePath = String.format("%s\\%s__%s(%s).csv", destinationFolderPath, m_datasetFilename, "SelectedFeatures", selectedFeatures.size());
        FileWriter.WriteFile(sb.toString(), featuresFilePath);
        Console.PrintLine(String.format("Selected Features saved to: %s", featuresFilePath));
    }

    /**
     * Print SERIALIZED file contain list of selected features
     *
     * @param selectedFeatures ArrayList of selected features selected features
     * @param destinationFolderPath path of the destination folder to print the
     * selected features file to
     */
    private static void SerializeSelectedFeatures(ArrayList<Pair<String, Integer>> selectedFeatures, String destinationFolderPath) {
        String serializedFilePath = String.format("%s\\%s__SelectedFeaturesArrayList(%s)", destinationFolderPath, m_datasetFilename, selectedFeatures.size());
        Serializer.Serialize(selectedFeatures, serializedFilePath);
    }
}
