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
    public static ArrayList<SimGraph[]> loadGraphs(AlgorithmList algorithms){
        graphs = new ArrayList<SimGraph[]>();
        ArrayList<Integer> trustAlgs = new ArrayList<Integer>();
        for (Algorithm alg : algorithms.getAlgs()){
            if (alg != null){                
                boolean display = alg.isDisplayed();
                if (alg.isFeedbackHistory()){
                    addFeedbackGraph(display);
                }else if (alg.isReputationAlg()){
                    addReputationGraph(alg, alg.getIndex(), display);
                }else if (alg.isTrustAlg()){
                    trustAlgs.add(alg.getIndex());
                }else{
                    ChatterBox.error("TrustEventLoader", "TrustEventLoader()", "Uncaught graph type.");
                }
            }
        }
        for (Integer index : trustAlgs){ //Trust graphs are made last because their base graph might not be made yet
            Algorithm alg = algorithms.getAlg(index);
            addTrustGraph(alg, (Integer) index, alg.isDisplayed());
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
    private static void addReputationGraph(Algorithm algDB, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm alg = (ReputationAlgorithm) algDB.getAlgorithm();
        ((FeedbackHistoryGraph)getInnerGraph(DYNAMIC, algDB.getBase())).addObserver(alg); //The algorithm will then add the graphs

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
    private static void addTrustGraph(Algorithm algDB, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm alg = (TrustAlgorithm) algDB.getAlgorithm();
        ((ReputationGraph) getInnerGraph(DYNAMIC, algDB.getBase())).addObserver(alg); //The algorithm will then add the graphs

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
    private static SimpleDirectedGraph getInnerGraph(int type, int index){
        for (SimGraph[] graph : graphs){
            if (graph != null){
                try{
                    if (graph[type].getID() == index){
                        return graph[type].getInnerGraph();
                    }
                }catch(NumberFormatException ex){
                    return null;
                }
            }
        }
        ChatterBox.error("GraphLoader", "getBase()", "Could not find a graph with id " + index);
        return null;
    }
}
////////////////////////////////////////////////////////////////////////////////
