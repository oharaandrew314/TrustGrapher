////////////////////////////////PropertyManager//////////////////////////////////
package aohara.utilities;

import java.io.File;
import java.io.IOException;

/**
 * A Properties extension for loading, saving and retrieving properties from a java .properties file
 * @author Andrew O'Hara
 */
public class PropertyManager extends java.util.Properties{
    
    protected File propertyFile;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new property Manager.  The properties file given by the path is automatically loaded or created.
     * @param path The path to the properties file to load or create.
     */
    public PropertyManager(String path) {
        this(new File(path));
    }

    /**
     * Creates a new property Manager.  The given properties file is automatically loaded or created.
     * @param file The properties file to load or create
     */
    public PropertyManager(File file) {
        super();
        this.propertyFile = file;
        loadPropertyFile();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Gets the file that the properties are being saved to
     * @return The properties file
     */
    public File getPropertyFile() {
        return propertyFile;
    }

///////////////////////////////////Methods//////////////////////////////////////

    /**
     * Loads the properties file that was given in the constructor.  If it does nto exist, it is created.
     */
    public final void loadPropertyFile(){
        if (propertyFile.exists()){
             try{
                load(BitStylus.getFile(propertyFile));
            }catch(IOException ex){
                ChatterBox.debug("PropertyManager", "PropertyManager()", "The properties could not be loaded");
                ex.printStackTrace();
            }
        }else{
            BitStylus.saveTextToFile(propertyFile, null);
        }     
    }

    /**
     * removes the property with the given key.  returns true if it succeeded, or false otherwise.
     * @param key The key of the property to remove
     * @return Whether ot not the removal succeeded
     */
    public boolean remove(String key){
        if (containsKey(key)){
            super.remove(key);
            return true;
        }
        return false;
    }

    /**
     * @deprecated Use setProperty instead
     */
    @Deprecated
    public boolean updateProperty(String key, String value) {
        setProperty(key, value);
        return save();
    }

    /**
     * @deprecated use remove instead
     */
    @Deprecated
    public boolean removeProperty(String key){
        if (remove(key)){
            return save();
        }
        return false;
    }

    /**
     * Saves the current properties to the properties file.
     * @return whether or not the save succeeded
     */
    public boolean save(){
        if (BitStylus.saveProperties(this, propertyFile)) {
            return true;
        } else {
            ChatterBox.error(this, "updateConfig()", "The properties file was not saved properly");
            return false;
        }
    }

    /**
     * @deprecated use clear instead
     */
    @Deprecated
    public boolean deleteProperties() {
        return BitStylus.deleteFile(propertyFile);
    }
}
////////////////////////////////////////////////////////////////////////////////
