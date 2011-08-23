////////////////////////////////////Bit Stylus//////////////////////////////////
package aohara.utilities;

import com.java2s.utilities.ExtensionFileFilter;
import javax.swing.JFileChooser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * API for Swing framework to read, write and parse text files.
 * @author Andrew O'Hara
 */
public class BitStylus {

//////////////////////////////////Input/////////////////////////////////////////
    /**
     * Brings up a file chooser dialog and returns the chosen file.  The initial directory will be your personal directory.
     * @return The chosen file
     */
    public static File chooseFile() {
        return chooseFile("Choose a file", null, null);
    }

    /**
     * Brings up a file chooser dialog and returns the chosen file.  You must specify a message and initial directory.
     * @param title The title of the File Chooser window
     * @param defaultDirectory The directory that the File Chooser starts at
     * @return The chosen file
     * @param Extensions A String array of the file extensions (without the '.') that you want to be accepted
     */
    public static File chooseFile(String title, File defaultDirectory, String[] extensions) {
        JFileChooser fc = new JFileChooser(defaultDirectory);
        fc.setDialogTitle(title);
        fc.setFileFilter(createFileFilter(extensions)); //Adding the file filter
        fc.showOpenDialog(null);
        return fc.getSelectedFile();
    }

    /**
     * Creates a file filter for a JFileChooser based on the array of extensions
     * @param extensions A String array of the file extensions (without the '.') that you want to be accepted
     * @return The FileFilter from the given extension
     */
    private static ExtensionFileFilter createFileFilter(String[] extensions){
        if (extensions != null){  //Creating the text that is displayed in the file filter field
            String filterText = "." + extensions[0];
            for (int i = 1 ; i < extensions.length - 1 ; i++){ //Don't add the last extension yet
                filterText = filterText + ", ." + extensions[i];
            }
            if (extensions.length -1 != 0){ //If the last extension isn't the first extension, add the last extension
                filterText = filterText + " and ." + extensions[extensions.length - 1];
            }
            return new ExtensionFileFilter(filterText + " files only", extensions);
        }
        return null;
    }

    /**
     * Opens a File chooser that lets you choose where to save a file.
     * Returns null if no file is chosen.
     *
     * Calls chooseSaveLocation("Choose a location to save", null);
     * @return The save file
     */
    public static File chooseSaveLocation(){
        return chooseSaveLocation("Choose a location to save", null, null);
    }

    /**
     * Opens a File chooser that lets you choose where to save a file
     * Returns null if no file is chosen.
     *
     * @param title The title for the chooser to display
     * @param defaultDirectory The directory that the chooser is initially viewing
     * @param extensions A String array of the file extensions (without the '.') that you want to be accepted
     * @return The save file
     */
    public static File chooseSaveLocation(String title, File defaultDirectory, String[] extensions){
        JFileChooser fc = new JFileChooser(defaultDirectory);
        fc.setDialogTitle(title);
        fc.setFileFilter(createFileFilter(extensions)); //Adding the file filter
        fc.showSaveDialog(null);
        return fc.getSelectedFile();
    }

    /**
     * Returns a FileInputStream for the file at the given path
     * @param path The path to the file that you want to open
     * @return A FileInputstream for the given path
     */
    public static FileInputStream getFile(String path){
        try{
            return new FileInputStream(path);
        }catch (Exception ex){
            ChatterBox.error("ExoSkeleton", "getFile()", ex.getMessage());
            return null;
        }
    }

    /**
     * Returns a FileInputStream for the given file
     * Calls (getFile(path)
     * @param file The file that you want to open
     * @return A FileInputStream for tr given file
     */
    public static FileInputStream getFile(File file){
        return getFile(file.getPath());

    }

    /**
     * Takes a text file and returns the contents as a String
     * @param file The file to read
     * @return A string of the entire contents of the file
     */
    public static String readTextFile(File file) {
        Scanner s = null;
        try {
            s = new Scanner(file).useDelimiter("");
        } catch (FileNotFoundException ex) {
            ChatterBox.error("utilities.BitStylus", "splitTextFile", "Not a valid text file");
        }
        String text = "";
        while (s.hasNext()){
            text = text + s.next();
        }
        return text;
    }

////////////////////////////////////Output//////////////////////////////////////

    /**
     * Takes a String and saves it to the file referenced by the given path
     * @param path The path pertaining to the file to save the String to
     * @param text The String to be saved to the file
     */
    public static void saveTextToFile(String path, String text){
        saveTextToFile(new File(path), text);
    }

    /**
     * Takes a String and saves it to the given file
     * @param file The file to save the String to
     * @param text The String to be saved to the file
     */
    public static void saveTextToFile(File file, String text) {
        try {
            FileWriter outFile = new FileWriter(file);
            if (text != null){
                outFile.write(text);
            }
            outFile.close();
        } catch (IOException ex) {
            ChatterBox.error( "utilities.BitStylus", "saveTextToFile", "Invalid file: " + file.getPath());
        }
    }

/////////////////////////////////Manipulation///////////////////////////////////

    /**
     * Takes a text file and String delimiter, reads the file, and returns an ArrayList of the split contents.
     * @param file The file to read
     * @param delimiter The String to split the text with
     * @return An ArrayList of the contents of the file
     */
    public static ArrayList<String> splitTextFile(File file, String delimiter) {
        Scanner s = null;
        try {
            s = new Scanner(file).useDelimiter(delimiter);
        } catch (FileNotFoundException ex) {
            ChatterBox.error("utilities.BitStylus", "splitTextFile", "Not a valid text file");
            return null;
        } catch (NullPointerException ex){
            ChatterBox.error("utilities.BitStylus", "splitTextFile", "Not a valid text file");
            return null;
        }

        ArrayList<String> temp = new ArrayList<String>();
        while (s.hasNext()) {
            temp.add(s.next());
        }
        return temp;
    }

    /**
     * Deletes the file at the given path
     *
     * Calls deleteFile(new File(path))
     * @param path The path referring to the file to be deleted
     * @return true or false depending on whether the file was succesfully deleted or not
     */
    public static boolean deleteFile(String path){
        return deleteFile(new File(path));
    }

    /**
     * Deletes the given file
     * @param file The file to be deleted
     * @return true or false depending on whether the file was succesfully deleted or not
     */
    public static boolean deleteFile(File file){
        if (file.exists()){
            file.delete();
            if (!file.exists()){
                return true;
            }
            ChatterBox.error("utilities.BitStylus", "deleteFile", "The file could not be deleted.");
        }else{
            ChatterBox.error("utilities.BitStylus", "deleteFile", "The file does not exist.");
        }
        return false;
    }

///////////////////////////////////Property File////////////////////////////////

    /**
     * Saves the given Properties object to the given file
     *
     * Calls saveProperties(properties, new File(path))
     * @param properties The Properties object to be saved
     * @param file The file to save to Properties object to
     * @return true or false depending on whether or not the Properties object was succesfully saved
     */
    public static boolean saveProperties(Properties properties, String path){
        return saveProperties(properties, new File(path));
    }

    /**
     * Saves the given Properties object to the given file
     * @param properties The Properties object to be saved
     * @param file The file to save to Properties object to
     * @return true or false depending on whether or not the Properties object was succesfully saved
     */
    public static boolean saveProperties(Properties properties, File file){
        try{
            properties.store(new FileOutputStream(file), null);
        }catch (IOException ex){
            return false;
        }
        return true;
    }
}
////////////////////////////////////////////////////////////////////////////////

