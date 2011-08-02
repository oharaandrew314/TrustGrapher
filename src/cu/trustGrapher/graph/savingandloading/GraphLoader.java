//////////////////////////////////GraphLoader//////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.trustGrapher.TrustGrapher;
import cu.trustGrapher.graph.*;

import java.util.ArrayList;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.ChatterBox;

/**
 * Reads an .arff file and parses it into a collection of TrustLogEvents
 * Also creates the necessary graphs and adds every event to a full graph so
 * that the event player knows where to add each object
 * @author Matthew Smith (I think)
 * @author Andrew O'Hara
 */
public class GraphLoader{

    public static final int DYNAMIC = TrustGrapher.DYNAMIC, FULL = TrustGrapher.FULL;
    private ArrayList<SimGraph[]> graphs;
    
//////////////////////////////////Constructor///////////////////////////////////
    public GraphLoader(TrustPropertyManager config) {
        graphs = new ArrayList<SimGraph[]>();       
        ArrayList<Integer> trustAlgs = new ArrayList<Integer>();
        for (int i = 0 ; i <= AlgorithmLoader.MAX_ALGS ; i++){
            if (config.containsKey("alg" + i)){
                String[] entry = config.getAlg((Integer) i);
                boolean display = (entry[AlgorithmLoader.DISPLAY].equals(AlgorithmLoader.TRUE)) ? true : false;
                if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.FB)){
                    addFeedbackGraph(display);
                }else if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.REP)){
                    addReputationGraph(entry, (Integer) i, display);
                }else if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.TRUST)){
                    trustAlgs.add((Integer) i);
                }else{
                    ChatterBox.error("TrustEventLoader", "TrustEventLoader()", "Uncaught graph type.");
                }
            }
        }
        for (Integer index : trustAlgs){ //Trust graphs are made last because their base graph might not be made yet
            String[] entry = config.getAlg(index);
            boolean display = false;
            if (entry[AlgorithmLoader.DISPLAY].equals(AlgorithmLoader.TRUE)){
                display = true;
            }
            addTrustGraph(entry, (Integer) index, display);
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public ArrayList<SimGraph[]> getGraphs() {
        return graphs;
    }

///////////////////////////////////Methods//////////////////////////////////////

    private void addFeedbackGraph(boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        graphSet[DYNAMIC] = new SimFeedbackGraph(DYNAMIC, display);
        graphSet[FULL] = new SimFeedbackGraph(FULL, display);
        graphs.add(graphSet.clone());
    }

    private void addReputationGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm dynEigenAlg = (ReputationAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        ReputationAlgorithm fulEigenAlg = (ReputationAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);

        ((FeedbackHistoryGraph)getBase(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(dynEigenAlg); //The algorithm will then add the graphs
        ((FeedbackHistoryGraph)getBase(FULL, entry[AlgorithmLoader.BASE])).addObserver(fulEigenAlg);

        graphSet[FULL] = new SimReputationGraph((SimFeedbackGraph)graphs.get(0)[FULL], index, display); //This automatically turns the full feedbackGraph into the full reputationGraph
        graphSet[DYNAMIC] = new SimReputationGraph((SimFeedbackGraph)graphs.get(0)[DYNAMIC], dynEigenAlg, index, display);
        graphs.add(graphSet.clone());
    }

    private void addTrustGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm dynRankAlg = (TrustAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        TrustAlgorithm fulRankAlg = (TrustAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);

        ((ReputationGraph) getBase(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(dynRankAlg); //The algorithm will then add the graphs
        ((ReputationGraph) getBase(FULL, entry[AlgorithmLoader.BASE])).addObserver(fulRankAlg);

        graphSet[FULL] = new SimTrustGraph(index, display);
        graphSet[DYNAMIC] = new SimTrustGraph(dynRankAlg, index, display);
        graphs.add(graphSet.clone());
    }

    private SimpleDirectedGraph getBase(int type, String baseID){
        for (SimGraph[] graph : graphs){
            if (graph != null){
                if (graph[type].getID() == Integer.parseInt(baseID.replace("alg", ""))){
                    return graph[type].getInnerGraph();
                }
            }
        }
        ChatterBox.debug(this, "getBase()", "Could not find a graph with id " + baseID);
        return null;
    }
}
////////////////////////////////////////////////////////////////////////////////
