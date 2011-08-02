////////////////////////////////TrustPropertyManager//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;

/**
 * Extends my PropertyManager wrapper for added functionality specific to TrustGrapher
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

    public Object[] getAlgDisplayNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i <= AlgorithmLoader.MAX_ALGS; i++) {
            if (this.hasAlg(i)) {
                names.add(i + "-" + getAlg(i)[AlgorithmLoader.NAME]);
            }
        }
        return names.toArray();
    }

    public ArrayList<String[]> getAlgs() {
        ArrayList<String[]> algs = new ArrayList<String[]>();
        for (int i = 0; i <= AlgorithmLoader.MAX_ALGS; i++) {
            if (this.containsKey("alg" + i)) {
                algs.add(getAlg(i));
            }
        }
        return algs;
    }

    public String[] getAlg(int index) {
        return this.getProperty("alg" + index).split(",");
    }

    public boolean hasAlg(int index) {
        return containsKey("alg" + index);
    }

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

    public String getClassPath(int index){
        return getProperty("class" + index);
    }

}
////////////////////////////////////////////////////////////////////////////////
