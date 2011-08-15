////////////////////////////////GraphManager//////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.trustGrapher.graph.savingandloading.AlgorithmConfig;
import cu.trustGrapher.graph.savingandloading.AlgorithmConfigManager;
import cu.trustGrapher.eventplayer.TrustLogEvent;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.ChatterBox;

/**
 * Wrapper for the list of graphs.  Builds, retrieves, and passes them the necessary events
 * This is created by TrustGrapher after the AlgorithmLoader 'ok' button is pressed.
 * @author Andrew O'Hara
 */
public class GraphManager {
    public static final int DYNAMIC = 0, FULL = 1, FEEDBACK = 0;
    private List<SimGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new GraphManager, and builds all the graphs required by the given AlgorithmConfigManager
     * @param algorithms The AlgorithmConfigManager which has all the algorithms and their configurations
     */
    public GraphManager(AlgorithmConfigManager algorithms) {
        graphs = new ArrayList<SimGraph[]>();
        ArrayList<AlgorithmConfig> trustAlgs = new ArrayList<AlgorithmConfig>();
        for (AlgorithmConfig alg : algorithms.getAlgs()){
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
        //Trust graphs are made last because their base graph might not have been made before
        for (AlgorithmConfig alg : trustAlgs){
            addTrustGraph(alg);
        }        
    }
//////////////////////////////////Accessors/////////////////////////////////////
    
    /**
     * Gets the graph with the given index and type
     * @param index The index corresponding to the the order that the graphs were added to the GraphManager in
     * @param type The type of the graph (FULL, DYNAMIC)
     * @return The graph with the given index and type
     */
    public SimGraph get(int index, int type){
        return graphs.get(index)[type];
    }
    
    /**
     * Gets the List of graphs held by this GraphManager
     * @return The list of graphs
     */
    public List<SimGraph[]> getGraphs(){
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
    
///////////////////////////////////Methods//////////////////////////////////////
    
    /**
     * Whenever a graph event is processed by the EventPlayer, 
     * this method passes that event on to the dynamic graphs contained within.
     * @param event The TrustLogEvent that is being processed
     * @param isForward Whether the simulator is being played forward or not
     */
    public void handleGraphEvent(TrustLogEvent event, boolean isForward){
        for (SimGraph[] graphSet : graphs){
            graphSet[DYNAMIC].graphEvent(event, isForward, graphSet[FULL]);
        }
    }
    
    /**
     * Whenvever a graph construction event is processed by the EventPlayer, 
     * this method passes that event on to the full graphs contained within.
     * @param event The TrustLogEvent that is being processed
     */
    public void handleConstructionEvent(TrustLogEvent event){
        for (SimGraph[] graphSet : graphs){
            graphSet[FULL].graphConstructionEvent(event);
        }
    }
    
    /**
     * Creats a new feedback history graph and adds it to the graph list
     * @param algConfig The class containing all of the configuration proeprties of an algorithm
     */
    private void addFeedbackGraph(AlgorithmConfig algConfig){
        SimGraph[] graphSet = new SimGraph[2];
        graphSet[DYNAMIC] = new SimFeedbackGraph(this, DYNAMIC, algConfig);
        graphSet[FULL] = new SimFeedbackGraph(this, FULL, algConfig);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Reputation Graph and adds it to the graph list
     * @param algConfig The class containing all of the configuration proeprties of an algorithm
     */
    private void addReputationGraph(AlgorithmConfig algConfig){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm alg = (ReputationAlgorithm) algConfig.getAlgorithm();
        ((FeedbackHistoryGraph)getInnerGraph(DYNAMIC, algConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimReputationGraph(this, FULL, algConfig);
        graphSet[DYNAMIC] = new SimReputationGraph(this, DYNAMIC, algConfig);
        graphs.add(graphSet.clone());
    }

    /**
     * Creates a new Trust Graph and adds it to the graph list
     * @param algConfig The class containing all of the configuration proeprties of an algorithm
     */
    private void addTrustGraph(AlgorithmConfig algConfig){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm alg = (TrustAlgorithm) algConfig.getAlgorithm();
        ((ReputationGraph) getInnerGraph(DYNAMIC, algConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs

        graphSet[FULL] = new SimTrustGraph(this, FULL, algConfig);
        graphSet[DYNAMIC] = new SimTrustGraph(this, DYNAMIC, algConfig);
        graphs.add(graphSet.clone());
    }
}
////////////////////////////////////////////////////////////////////////////////
