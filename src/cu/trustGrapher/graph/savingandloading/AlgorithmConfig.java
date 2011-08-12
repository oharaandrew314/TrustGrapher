////////////////////////////////Algorithm//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import java.io.File;
import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 * Description
 * @author Andrew O'Hara
 */
public class AlgorithmConfig {

    public static String NO_BASE = "noBase", NO_CONFIG = "noConfig";
    public static final String FB = "FeedbackHistory", REP = "ReputationAlgorithm", TRUST = "TrustAlgorithm";
    private String type;
    private File classFile;
    private int index, base, classIndex;
    private boolean display;
    private Object algorithm;
    private PropertyManager properties;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new Trust or Reputation algorithm.
     * @param index
     * @param name
     * @param display
     * @param base
     * @param classFile
     * @param propertyFile
     */
    public AlgorithmConfig(int index, boolean display, int base, int classIndex, String classPath, File propertyFile){
        this.index = index;
        setDisplay(display, true);
        this.base = base;
        setProperties(propertyFile);
        this.classIndex = classIndex;
        if (classPath != null){
            classFile = new File(classPath);
            algorithm = TrustClassLoader.newAlgorithm(classFile.getPath());
            type = (algorithm instanceof ReputationAlgorithm) ? REP : TRUST;
        }else{
            classFile = null;
            algorithm = null;
            type = FB;
        }
    }
//////////////////////////////////Accessors/////////////////////////////////////

    @Override
    public String toString() {
        String baseIndex = (base != -1) ? "" + base : NO_BASE;
        String configString = (properties != null) ? properties.getPropertyFile().getPath() : NO_CONFIG;
        return display + "," + baseIndex + "," + classIndex + "," + configString;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }
    
    public String getKey(){
        return AlgorithmLoader.ALG + index;
    }

    /**
     * @return the baseIndex
     */
    public int getBase() {
        return base;
    }

    /**
     * @return the classFile
     */
    public File getClassFile() {
        return classFile;
    }

    public String getDisplayName() {
        String name = (algorithm != null) ? algorithm.getClass().getSimpleName() : "FeedbackHistory";
        return index + "-" + name;
    }

    /**
     * @return the display
     */
    public boolean isDisplayed() {
        return display;
    }
    
    public Object getAlgorithm(){
        return algorithm;
    }
    
    public String getType(){
        return type;
    }
    
    public File getProperties(){
        return (properties != null) ?properties.getPropertyFile() : null;
    }
    
    public boolean isFeedbackHistory(){
        return type.equals(FB);
    }
    
    public boolean isReputationAlg(){
        return type.equals(REP);
    }
    
    public boolean isTrustAlg(){
        return type.equals(TRUST);
    }
    
///////////////////////////////////Methods//////////////////////////////////////

    public void setIndex(int index){
        this.index = index;
    }
    
    /**
     * @param baseIndex the baseIndex to set
     */
    public void setBase(int base) {
        this.base = base;
    }

    /**
     * @param propertyFile the configFile to set
     */
    public final void setProperties(File propertyFile) {
        if (propertyFile == null){
            properties= null;
        }else{
            properties = (propertyFile.exists()) ? new PropertyManager(propertyFile) : null;
        }
    }
    
    public void setDisplay(boolean display){
        setDisplay(display, false);
    }

    /**
     * @param display the display to set
     */
    private void setDisplay(boolean display, boolean constructing) {
        if (AlgorithmConfigManager.VISIBLE_COUNT >= AlgorithmConfigManager.MAX_VISIBLE) {
            ChatterBox.alert("You cannot have more than " + AlgorithmConfigManager.MAX_VISIBLE + " graphs shown at one time.");
        } else {
            this.display = display;
            if (display) {
                AlgorithmConfigManager.VISIBLE_COUNT++;
            } else if (!constructing){ //It is not necessary to decrement the visible count if called from the constructor
                AlgorithmConfigManager.VISIBLE_COUNT--;
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
