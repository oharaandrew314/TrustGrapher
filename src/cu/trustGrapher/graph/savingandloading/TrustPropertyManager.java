////////////////////////////////TrustPropertyManager//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;

/**
 * Extends my PropertyManager wrapper for added functionality specific to TrustGrapher.
 * This PropertyManager extension is extended to deal with Algorithms in array format 
 * and class paths needed by the AlgorithmLoader.
 * @author Andrew O'Hara
 */
public class TrustPropertyManager extends utilities.PropertyManager {

//////////////////////////////////Constructor///////////////////////////////////
    public TrustPropertyManager(File propertiesFile) {
        super(propertiesFile);
    }

    public TrustPropertyManager(String propertiesPath) {
        this(new File(propertiesPath));
    }
/////////////////////////////Algorithm Methods//////////////////////////////////

    /**
     * Gets an array of the display names for the algorithms loaded in the AlgorithmLoader
     * If there are no algorithms, returns an empty array
     * @return An array of algorithm display names
     */
    public Object[] getAlgDisplayNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i <= AlgorithmLoader.MAX_ALGS; i++) {
            if (this.hasAlg(i)) {
                names.add(i + "-" + getAlg(i)[AlgorithmLoader.NAME]);
            }
        }
        return names.toArray();
    }

    /**
     * Returns an array of the algorithms in array format required by the AlgorithLoader
     * @return The List of algorithms in array format
     */
    public ArrayList<String[]> getAlgs() {
        ArrayList<String[]> algs = new ArrayList<String[]>();
        for (int i = 0; i <= AlgorithmLoader.MAX_ALGS; i++) {
            if (this.containsKey("alg" + i)) {
                algs.add(getAlg(i));
            }
        }
        return algs;
    }

    /**
     * Gets the algorithm in array format with the given index.
     * Returns null if no algorithm can be found.
     * @param index
     * @return 
     */
    public String[] getAlg(int index) {
        return this.getProperty("alg" + index).split(",");
    }

    /**
     * Checks if the Properties contains the algorithm with the given index
     * @param index The index of the algorithm to find
     * @return true or false depending on whether the algorithm was found
     */
    public boolean hasAlg(int index) {
        return containsKey("alg" + index);
    }

    /**
     * Modifies an existing algorithm in the properties
     * @param index The index of the algorithm to modify
     * @param element The index of the element to change (algorithm array format)
     * @param newElement The String that will replace the specified element
     */
    public void modifyAlg(int index, int element, String newElement) {
        String[] alg = getAlg(index);
        alg[element] = newElement;
        String s = "";
        for (int i = 0; i < alg.length - 1; i++) {
            s = s + alg[i] + ",";
        }
        s = s + alg[alg.length - 1]; //Add the last element without putting a comma at the end
        //Split to get rid of the key in the name.  That has no use in the properties file
        setProperty("alg" + index, s);
    }

///////////////////////////////////Class Methods////////////////////////////////

    /**
     * Returns the class path of the class property with the given index
     * Returns null if no class was found
     * @param index The index of the Class property to find
     * @return The classPath of the property that was found
     */
    public String getClassPath(int index){
        return getProperty("class" + index);
    }

}
////////////////////////////////////////////////////////////////////////////////
