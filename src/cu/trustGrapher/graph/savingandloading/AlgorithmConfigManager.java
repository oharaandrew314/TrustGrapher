////////////////////////////////AlgorithmList//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;
import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 * Description
 * @author Andrew O'Hara
 */
public class AlgorithmConfigManager {

    public static int ALG_COUNT = 0, VISIBLE_COUNT = 0, MAX_ALGS = 13, MAX_VISIBLE = 6;
    private ArrayList<AlgorithmConfig> algorithms;
    private PropertyManager config;

//////////////////////////////////Constructor///////////////////////////////////
    public AlgorithmConfigManager(PropertyManager config) {
        algorithms = new ArrayList<AlgorithmConfig>(MAX_ALGS);
        this.config = config;
    }
//////////////////////////////////Accessors/////////////////////////////////////
    
    public ArrayList<AlgorithmConfig> getAlgs(){
        return algorithms;
    }

    public AlgorithmConfig getAlg(int i) {
        for (AlgorithmConfig alg : algorithms){
            if (i == alg.getIndex()){
                return alg;
            }
        }
        return null;
    }

    public Object[] getAlgDisplayNames() {
        ArrayList<String> names = new ArrayList<String>(MAX_ALGS);
        for (AlgorithmConfig alg : algorithms) {
            names.add(alg.getDisplayName());
        }
        return names.toArray();
    }
    
    public boolean hasReputationAlgorithm(){
        for (AlgorithmConfig alg : algorithms){
            if (alg != null){
                if (alg.getType().equals(AlgorithmConfig.REP)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public int size(){
        return algorithms.size();
    }

///////////////////////////////////Methods//////////////////////////////////////    
    public boolean addAlgorithm(AlgorithmConfig alg) {
        if (size() == MAX_ALGS) {
            ChatterBox.alert("Cannot have more than " + MAX_ALGS + " algorithms at one time.");
            return false;
        }
        algorithms.add(alg);
        ALG_COUNT++;
        return true;
    }

    public boolean newAlg(int index, boolean display, int classIndex) {
        String classPath = (classIndex != -1) ? config.getProperty(AlgorithmLoader.CLASS + classIndex) : null;
        AlgorithmConfig alg = new AlgorithmConfig(index, display, -1, classIndex, classPath, null);
        if (alg.isTrustAlg() && !hasReputationAlgorithm()){
            ChatterBox.alert("There must be an existing Reputation Algorithm\nbefore you can add a Trust Algorithm.");
            return false;
        }
        return addAlgorithm(alg);
    }

    /**
     * 
     * @param index
     * @param property { display, baseIndex, classFile, configFile
     * @return 
     */
    public boolean newAlgFromProperty(int index, String[] property) {
        int baseIndex = -1;
        try{
            baseIndex = Integer.parseInt(property[1]);
        }catch (NumberFormatException ex){}
        int classIndex = Integer.parseInt(property[2]);
        String classPath = (classIndex == -1) ? null : config.getProperty(AlgorithmLoader.CLASS + classIndex);
        //Algorithm property format                     display                    base    classIndex  classPath      properties
        AlgorithmConfig alg = new AlgorithmConfig(index, Boolean.parseBoolean(property[0]), baseIndex, classIndex, classPath, new File(property[3]));
        return addAlgorithm(alg);
    }
    
    public void removeAlg(int index){
        if (getAlg(index).isDisplayed()){
            VISIBLE_COUNT--;
        }
        ALG_COUNT--;
        
        config.remove(AlgorithmLoader.ALG + index);
        algorithms.remove(index);
    }
}
////////////////////////////////////////////////////////////////////////////////
