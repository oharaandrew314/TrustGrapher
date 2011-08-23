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
    public PropertyManager(String path) {
        this(new File(path));
    }

    public PropertyManager(File file) {
        super();
        this.propertyFile = file;
        loadPropertyFile();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public File getPropertyFile() {
        return propertyFile;
    }

///////////////////////////////////Methods//////////////////////////////////////

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

    public boolean remove(String key){
        if (containsKey(key)){
            super.remove(key);
            return true;
        }
        return false;
    }

    @Deprecated
    public boolean updateProperty(String key, String value) {
        setProperty(key, value);
        return save();
    }

    @Deprecated
    public boolean removeProperty(String key){
        if (remove(key)){
            return save();
        }
        return false;
    }

    public boolean save(){
        if (BitStylus.saveProperties(this, propertyFile)) {
            return true;
        } else {
            ChatterBox.error(this, "updateConfig()", "The properties file was not saved properly");
            return false;
        }
    }

    @Deprecated
    public boolean deleteProperties() {
        return BitStylus.deleteFile(propertyFile);
    }
}
////////////////////////////////////////////////////////////////////////////////
