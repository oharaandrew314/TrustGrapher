////////////////////////////////GraphManager//////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.trustGrapher.graph.savingandloading.Algorithm;
import cu.trustGrapher.graph.savingandloading.AlgorithmList;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;
import java.util.ArrayList;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.ChatterBox;

/**
 * Description
 * @author Andrew O'Hara
 */
public class GraphManager {
    public static final int DYNAMIC = 0, FULL = 1, FEEDBACK = 0;
    private ArrayList<SimGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public GraphManager(AlgorithmList algorithms) {
        graphs = new ArrayList<SimGraph[]>();
        ArrayList<Algorithm> trustAlgs = new ArrayList<Algorithm>();
        for (Algorithm alg : algorithms.getAlgs()){
            if (alg != null){                
                if (alg.isFeedbackHistory()){
                    addFeedbackGraph(alg);
                }else if (alg.isReputationAlg()){
                    addReputationGraph(alg);
                }else if (alg.isTrustAlg()){
                    trustAlgs.add(alg);
                }else{
                    ChatterBox.error("TrustEventLoader", "LoadGraphs()", "Uncaught graph type.");
                }
            }
        }
        for (Algorithm alg : trustAlgs){ //Trust graphs are made last because their base graph might not be made yet
            addTrustGraph(alg);
        }        
    }
//////////////////////////////////Accessors/////////////////////////////////////
    
    public SimGraph get(int index, int type){
        return graphs.get(index)[type];
    }
    
    public ArrayList<SimGraph[]> getGraphs(){
        return graphs;
    }
    
    /**
     * Searches the existing graphs for the graph with the correct ID and then returns that graphs inner jGrapht graph.
     * This is needed since the trustTestBed algorithms require trustTestBed graphs to be attached to them
     * @param type Whether the inner graph is to be a DYNAMIC or FULL graph
     * @param graphID The ID of the graph pair that contains the inner graph to return
     * @return The jGraphT SimpleDirectedGraph inside of the specified JungAdapterGraph
     */
    private SimpleDirectedGraph getInnerGraph(int type, int graphID){
        for (SimGraph[] graph : graphs){
            if (graph != null){
                try{
                    if (graph[type].getID() == graphID){
                        return graph[type].getInnerGraph();
                    }
                }catch(NumberFormatException ex){
                    return null;
                }
            }
        }
        ChatterBox.error("GraphLoader", "getBase()", "Could not find a graph with id " + graphID);
        return null;
    }
    
///////////////////////////////////Methods//////////////////////////////////////ndle
    public void handleGraphEvent(TrustLogEvent event, boolean isForward){
        for (SimGraph[] graphSet : graphs){
            graphSet[DYNAMIC].graphEvent(event, isForward, graphSet[FULL]);
        }
    }
    
    public void handleConstructionEvent(TrustLogEvent event){
        for (SimGraph[] graphSet : graphs){
            graphSet[FULL].graphConstructionEvent(event);
        }
    }
    
    /**
     * Creats a new feedback history graph
     */
    private void addFeedbackGraph(Algorithm algConfig){
        SimGraph[] graphSet = new SimGraph[2];
        graphSet[DYNAMIC] = new SimFeedbackGraph(this, DYNAMIC, algConfig);
        graphSet[FULL] = new SimFeedbackGraph(this, FULL, algConfig);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Reputation Graph
     */
    private void addReputationGraph(Algorithm algConfig){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm alg = (ReputationAlgorithm) algConfig.getAlgorithm();
        ((FeedbackHistoryGraph)getInnerGraph(DYNAMIC, algConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimReputationGraph(this, FULL, algConfig);
        graphSet[DYNAMIC] = new SimReputationGraph(this, DYNAMIC, algConfig);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Trust Graph
     */
    private void addTrustGraph(Algorithm algConfig){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm alg = (TrustAlgorithm) algConfig.getAlgorithm();
        ((ReputationGraph) getInnerGraph(DYNAMIC, algConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimTrustGraph(this, FULL, algConfig);
        graphSet[DYNAMIC] = new SimTrustGraph(this, DYNAMIC, algConfig);
        graphs.add(graphSet.clone());
    }
////////////////////////////////Static Methods//////////////////////////////////
}
////////////////////////////////////////////////////////////////////////////////
