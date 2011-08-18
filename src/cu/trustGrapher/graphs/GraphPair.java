////////////////////////////////SImGraphPair//////////////////////////////////
package cu.trustGrapher.graphs;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.trustGrapher.graph.savingandloading.GraphConfig;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import java.util.List;
import org.apache.commons.collections15.Predicate;
import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * The graphs are all attached to GraphPair Objects which act as wrappers.
 * To interact with the graphs, you should use its GraphPair to interact with it for you
 * 
 * The dynamic graph is not shown, but components in the full graph will only be displayed if they exist in the dynamic graph.
 * As events occur, they are added to the dynamic graphs through their graphEvent() method.
 * 
 * The full graph is the one that is shown in the GraphViewer, but all vertices and edges that are ever shown must be on the 
 * graph before the events start playing.  Their graphConstructionEvent() method is used as the events are parsed to add 
 * all components to the graph.
 * @author Andrew O'Hara
 */
public class GraphPair implements Predicate<Context<Graph<Agent, TestbedEdge>, Object>>{

    private SimAbstractGraph fullGraph, dynamicGraph;
    private GraphConfig graphConfig; //The Object containing all of the configuration information for the graphs in this pair
    private static List<GraphPair> graphs; //Refers to the list of GraphPairs.  I don't like having to use this.  Find another way

//////////////////////////////////Constructor///////////////////////////////////
    public GraphPair(GraphConfig graphConfig, List<GraphPair> graphs) {
        this.graphConfig = graphConfig;
        GraphPair.graphs = graphs;
        if (graphConfig.isFeedbackHistory()) {
            fullGraph = new SimFeedbackGraph(this);
            dynamicGraph = new SimFeedbackGraph(this);
        } else if (graphConfig.isReputationAlg()) {
            fullGraph = new SimReputationGraph(this);
            dynamicGraph = new SimReputationGraph(this);
            ReputationAlgorithm alg = (ReputationAlgorithm) graphConfig.getAlgorithm();
            ((FeedbackHistoryGraph) getInnerGraph(graphConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs
        } else if (graphConfig.isTrustAlg()) {
            fullGraph = new SimTrustGraph(this);
            dynamicGraph = new SimTrustGraph(this);
            TrustAlgorithm alg = (TrustAlgorithm) graphConfig.getAlgorithm();
            ((ReputationGraph) getInnerGraph(graphConfig.getBase())).addObserver(alg); //The algorithm will then add the graphs
        } else {
            ChatterBox.criticalError("SimGraphPair", "<init>", "Unsupported graph type");
        }
    }
//////////////////////////////////Accessors/////////////////////////////////////

    /**
     * Gets the list of GraphPairs so that other GraphPairs can be found
     * @return the list of GraphPairs
     */
    public List<GraphPair> getGraphs() {
        return graphs;
    }

    public SimAbstractGraph getFullGraph() {
        return fullGraph;
    }

    public SimAbstractGraph getDynamicGraph() {
        return dynamicGraph;
    }

    /**
     * Gets the display name for this graph.
     * The Display name consits of the graphID and the name of the algorithm attached to it.
     * @return The display name
     */
    public String getDisplayName() {
        return dynamicGraph.getDisplayName();
    }

    public Object getAlgorithm() {
        return graphConfig.getAlgorithm();
    }

    /**
     * Returns the id of this graph.  It is currently used to generate the name of the graph
     * @return The graph id
     */
    public int getID() {
        return graphConfig.getIndex();
    }

    /**
     * Whether or not this graph will have a viewer built for it
     * @return the displayed boolean
     */
    public boolean isDisplayed() {
        return graphConfig.isDisplayed();
    }
    
    /**
     * This is the predicate for whether to show the graph entities in the GraphViewer.
     * It is called by the GraphViewer during every repaint for every entity.
     * If the entity exists in the dynamicGraph, return true.  Otherwise false.
     * @param context The fullGraph and element that is being checked
     * @return Whether to display the element in the context for the current repaint
     */
    public boolean evaluate(Context<Graph<Agent, TestbedEdge>, Object> context) {
        if (context.element instanceof TestbedEdge){
            return dynamicGraph.containsEdge((TestbedEdge) context.element);
        }else if (context.element instanceof Agent){
            return dynamicGraph.containsVertex((Agent) context.element);
        }else{
            ChatterBox.criticalError(this, "evaluate()", "Uncaught predicate");
            return false;
        }
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Whenever a graph event is processed by the EventPlayer, 
     * this method passes that event on to the dynamic graph contained within.
     * @param event The TrustLogEvent that is being processed
     * @param isForward Whether the simulator is being played forward or not
     */
    public void handleGraphEvent(TrustLogEvent event, boolean isForward) {
        if (event != null){
            dynamicGraph.graphEvent(event, isForward);
        }
    }

    /**
     * Whenvever a graph construction event is processed by the EventPlayer, 
     * this method passes that event on to the full graph contained within.
     * @param event The TrustLogEvent that is being processed
     */
    public void handleConstructionEvent(TrustLogEvent event) {
        fullGraph.graphConstructionEvent(event);
    }
////////////////////////////////Static Methods//////////////////////////////////

    /**
     * Searches the existing graphs for the graph with the correct ID and then returns that graphs inner jGrapht graph.
     * This is needed since the trustTestBed algorithms require trustTestBed graphs to be attached to them.
     * @param graphID The ID of the graph pair that contains the inner graph to return
     * @return The jGraphT SimpleDirectedGraph inside of the specified JungAdapterGraph
     */
    private static SimpleDirectedGraph getInnerGraph(int graphID) {
        for (GraphPair graphPair : graphs) {
            if (graphPair != null) {
                try {
                    if (graphPair.getID() == graphID) {
                        return graphPair.getDynamicGraph().getInnerGraph();
                    }
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        ChatterBox.error("GraphLoader", "getBase()", "Could not find a graph with id " + graphID);
        return null;
    }
}
////////////////////////////////////////////////////////////////////////////////
