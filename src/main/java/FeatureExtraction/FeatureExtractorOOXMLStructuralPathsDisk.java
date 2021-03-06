/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FeatureExtraction;

import IO.Console;
import IO.Directories;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Aviad. Check http://www.docx4java.org/trac/docx4j to analyze OLE
 * objects found in XML Based documents
 */
public class FeatureExtractorOOXMLStructuralPathsDisk<T> extends AFeatureExtractor<T> {

    private final long serialVersionUID = 1L;
    private String m_OfficeFileTempFolderPath = "";
    private final boolean m_ignoreNumbersInFeatures;

    public FeatureExtractorOOXMLStructuralPathsDisk(boolean ignoreNumbersInFeatures) {
        m_ignoreNumbersInFeatures = ignoreNumbersInFeatures;
    }

    @Override
    public Map<String, Integer> ExtractFeaturesFrequencyFromSingleElement(T element) {
        Map<String, Integer> structuralPaths = new HashMap<>();
        String filePath = (String) element;
        String destinationFolder = FileUtils.getTempDirectoryPath() + FilenameUtils.getName(filePath);
        m_OfficeFileTempFolderPath = destinationFolder + "\\";
        if (UnzipFileToFolder(filePath, destinationFolder)) {
            ExtractFolderStructuralPaths(destinationFolder, structuralPaths);
        }
        //Directories.DeleteDirectory(destinationFolder); //TODO
        return structuralPaths;
    }

    /**
     * Extracts structural paths from the given folder
     *
     * @param folderPath path of a folder
     * @param structuralPaths the Map to add the extracted features to
     */
    private void ExtractFolderStructuralPaths(String folderPath, Map<String, Integer> structuralPaths) {
        ArrayList<String> directoryPaths = Directories.GetDirectoryFilesPaths(folderPath);
        String fileExtension;
        for (String path : directoryPaths) {
            if (!path.equals(folderPath)) {
                AddStructuralPath(path, structuralPaths);
                if (Files.isRegularFile(Paths.get(path))) {
                    fileExtension = FilenameUtils.getExtension(path);
                    if (fileExtension.equals("rels") || fileExtension.equals("xml")) {
                        AddXMLStructuralPaths(path, structuralPaths);
                    }
                }
            }
        }
    }

    /**
     * Add structural paths from the given xml file into the local map
     *
     * @param xmlFilePath the path of a xml file
     * @param structuralPaths the Map to add the extracted features to
     */
    private void AddXMLStructuralPaths(String xmlFilePath, Map<String, Integer> structuralPaths) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document xml = db.parse(xmlFilePath);
            NodeList nodeList = xml.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                AddXMLStructuralPathsRecursively(nodeList.item(i), xmlFilePath, structuralPaths);
            }
        } catch (Exception ex) {
            //Console.PrintLine(String.format("Error traversing XML file: '%s'", xmlFilePath), true, false);
        }
    }

    /**
     * Add structural paths from the given xml file into the local map
     * recursively
     *
     * @param xmlNode xmlNode to look for its childs
     * @param parentNodePath the path of the parent node
     * @param structuralPaths the Map to add the extracted features to
     */
    private void AddXMLStructuralPathsRecursively(Node xmlNode, String parentNodePath, Map<String, Integer> structuralPaths) {
        String currentNodePath = String.format("%s\\%s", parentNodePath, xmlNode.getNodeName());
        AddStructuralPath(currentNodePath, structuralPaths);

        NodeList childNodes = xmlNode.getChildNodes();
        Node childNode;
        for (int i = 0; i < childNodes.getLength(); i++) {
            childNode = childNodes.item(i);
            AddXMLStructuralPathsRecursively(childNode, currentNodePath, structuralPaths);
        }
    }

    /**
     * Add structural path to local Map
     *
     * @param structuralPath the key to add to the map
     * @param structuralPaths the Map to add the feature to
     */
    private void AddStructuralPath(String structuralPath, Map<String, Integer> structuralPaths) {
        structuralPath = structuralPath.replace(m_OfficeFileTempFolderPath, "");

        if (m_ignoreNumbersInFeatures) {
            structuralPath = structuralPath.replaceAll("[0-9]", "");
        }

        if (!structuralPaths.containsKey(structuralPath)) {
            structuralPaths.put(structuralPath, 1);
        } else {
            structuralPaths.put(structuralPath, structuralPaths.get(structuralPath) + 1);
        }
    }

    /**
     * Unzip the given file to the given folder
     *
     * @param filePath the full path of the file to unzip
     * @param destinationFolder the folder to unzip the file to
     * @return true if the unzipping process done successfully
     */
    private boolean UnzipFileToFolder(String filePath, String destinationFolder) {
        boolean success = false;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(filePath);
            if (!zipFile.isEncrypted()) {
                zipFile.extractAll(destinationFolder);
                success = true;
            } else {
                Console.PrintException(String.format("Error unzipping file '%s' - password protected", filePath), null);
            }
        } catch (ZipException ex) {
            Console.PrintException(String.format("Error unzipping file '%s'", filePath), ex);
        }
        return success;
    }

    @Override
    public String GetName() {
        if (m_ignoreNumbersInFeatures) {
            return "OOXML Structural Paths Disk NN";
        } else {
            return "OOXML Structural Paths Disk";
        }
    }

}
