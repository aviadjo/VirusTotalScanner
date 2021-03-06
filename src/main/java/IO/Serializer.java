/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IO;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Aviad
 */
public class Serializer {

    private static final String m_serializedFileExtension = "ser";

    /**
     * Returns true if the given object was serialized successfully
     *
     * @param object the object to serialize
     * @param destinationFolder the destination folder to save the serialized
     * object
     * @param destinationFilename destination file name (serialized object file)
     * @return true if the given object was serialized successfully
     */
    public static boolean Serialize(Object object, String serializedFilePath) {
        boolean serializedSuccessfully = false;
        serializedFilePath += "." + m_serializedFileExtension;
        try (FileOutputStream fos = new FileOutputStream(serializedFilePath); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(object);
            serializedSuccessfully = true;
            Console.PrintLine(String.format("%s was serialized and saved to: %s", object.getClass().getSimpleName(), serializedFilePath));
        } catch (IOException ex) {
            Console.PrintException(String.format("Error serilizing object '%s'", serializedFilePath), ex);
        }
        return serializedSuccessfully;
    }

    /**
     * Returns Deserialized object
     *
     * @param sourceFolder source folder in which the serialized file exist
     * @param objectFilename serialized filename
     * @param deleteSerializedFile whether to delete the serialized file
     * @return Deserialized object
     */
    public static Object Deserialize(String serializedFilePath/*, boolean deleteSerializedFile*/) {
        Object serialized = null;
        if (Files.IsFile(serializedFilePath)) {
            try (FileInputStream fin = new FileInputStream(serializedFilePath); ObjectInputStream ois = new ObjectInputStream(fin)) {
                serialized = ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                Console.PrintException(String.format("Error deserilizing file '%s'", serializedFilePath), ex);
            }
            /*if (deleteSerializedFile) {
             Files.DeleteFile(serializedFilePath);
             }*/
        }
        return serialized;
    }
}
