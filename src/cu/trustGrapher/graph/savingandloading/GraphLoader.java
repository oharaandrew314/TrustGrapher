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
 * Creates the necessary graphs specified by the algorithms in the given Properties object
 * @author Andrew O'Hara
 */
public class GraphLoader{

    public static final int DYNAMIC = TrustGrapher.DYNAMIC, FULL = TrustGrapher.FULL;
    private static ArrayList<SimGraph[]> graphs;    

///////////////////////////////////Methods//////////////////////////////////////

    /**
     * Creates all of the graphs specified by the algorithms in the config
     * @param config The PropertyManager that contains all the info on the algorithms
     */
    public static ArrayList<SimGraph[]> loadGraphs(TrustPropertyManager config){
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
        return graphs;
    }

    /**
     * Creats a new feedback history graph
     * @param display Whether or not the graph will be displayed in a TrustGraphViewer
     */
    private static void addFeedbackGraph(boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        graphSet[DYNAMIC] = new SimFeedbackGraph(DYNAMIC, display);
        graphSet[FULL] = new SimFeedbackGraph(FULL, display);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Reputation Graph
     * @param entry The algorithm entry array
     * @param index The algorithm number
     * @param display Whether or not the graph will be displayed in a TrustGraphViewer
     */
    private static void addReputationGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm alg = (ReputationAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        ((FeedbackHistoryGraph)getInnerGraph(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimReputationGraph(index, display); //This automatically turns the full feedbackGraph into the full reputationGraph
        graphSet[DYNAMIC] = new SimReputationGraph((SimFeedbackGraph)graphs.get(0)[DYNAMIC], alg, index);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Trust Graph
     * @param entry The algorithm entry array
     * @param index The algorithm number
     * @param display Whether or not the graph will be displayed in a TrustGraphViewer
     */
    private static void addTrustGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm alg = (TrustAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        ((ReputationGraph) getInnerGraph(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimTrustGraph(index, display);
        graphSet[DYNAMIC] = new SimTrustGraph(alg, index);
        graphs.add(graphSet.clone());
    }

    /**
     * Searches the existing graphs for the graph with the correct ID and then returns that graphs inner jGrapht graph.
     * This is needed since the trustTestBed algorithms require trustTestBed graphs to be attached to them
     * @param type Whether the inner graph is to be a DYNAMIC or FULL graph
     * @param graphID The ID of the graph pair that contains the inner graph to return
     * @return The jGrapht graph inside of the specified graph
     */
    private static SimpleDirectedGraph getInnerGraph(int type, String graphID){
        for (SimGraph[] graph : graphs){
            if (graph != null){
                if (graph[type].getID() == Integer.parseInt(graphID.replace("alg", ""))){
                    return graph[type].getInnerGraph();
                }
            }
        }
        ChatterBox.error("GraphLoader", "getBase()", "Could not find a graph with id " + graphID);
        return null;
    }
}
////////////////////////////////////////////////////////////////////////////////
