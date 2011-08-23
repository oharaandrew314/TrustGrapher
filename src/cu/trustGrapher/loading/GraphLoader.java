////////////////////////////////SImGraphPair//////////////////////////////////
package cu.trustGrapher.loading;

import cu.trustGrapher.graphs.*;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;

import java.util.List;
import java.util.ArrayList;
import org.jgrapht.graph.SimpleDirectedGraph;

import aohara.utilities.ChatterBox;

/**
 * This takes the list of GraphConfigs and creates the Simulation graph classes based on them.
 * @author Andrew O'Hara
 */
public class GraphLoader {

    /**
     * Create the graphs and return them
     * @param graphConfigs The list of graphConfigs which contain the info necessary to create the graph
     * @return The list of graphs to be used in the simulation
     */
    public static List<SimAbstractGraph> loadGraphs(List<GraphConfig> graphConfigs) {
        List<SimAbstractGraph> graphs = new ArrayList<SimAbstractGraph>();
        ArrayList<GraphConfig> trustGraphs = new ArrayList<GraphConfig>();
        for (GraphConfig graphConfig : graphConfigs) {
            if (graphConfig.isTrustGraph()) { //Trust Algorithms cannot be made yet since the graph that they depend on may not be created yet
                trustGraphs.add(graphConfig);
            } else if (graphConfig.isFeedbackGraph()) { // add the Feedback Graph
                graphs.add(new SimFeedbackGraph(graphConfig));
            } else if (graphConfig.isReputationGraph()) { //Add a Reputation Graph
                graphs.add(new SimReputationGraph(graphConfig, (SimFeedbackGraph) graphs.get(0)));
                ReputationAlgorithm alg = (ReputationAlgorithm) graphConfig.getAlgorithm(); //get the algorithm and add it to the inner jGraphT graph
                alg.setFeedbackHistoryGraph((FeedbackHistoryGraph) getInnerGraph(graphConfig.getBaseIndex(), graphs));
            } else {
                ChatterBox.criticalError("SimGraphPair", "<init>", "Unsupported graph type");
            }
        }
        for (GraphConfig graphConfig : trustGraphs) { //Now add the Tryst Graphs
            graphs.add(new SimTrustGraph(graphConfig));
            TrustAlgorithm alg = (TrustAlgorithm) graphConfig.getAlgorithm(); //get the algorithm and add it to the inner jGraphT graph
            alg.setReputationGraph((ReputationGraph) getInnerGraph(graphConfig.getBaseIndex(), graphs));
        }
        return graphs;
    }

    /**
     * Searches the existing graphs for the graph with the correct ID and then returns that graphs inner jGrapht graph.
     * This is needed since the trustTestBed algorithms require trustTestBed graphs to be attached to them.
     * @param graphID The ID of the graph that contains the inner graph to return
     * @return The jGraphT SimpleDirectedGraph inside of the specified TrustGrapher SimAbstractGraph
     */
    private static SimpleDirectedGraph getInnerGraph(int graphID, List<SimAbstractGraph> graphs) {
        for (SimAbstractGraph graph : graphs) {
            if (graph.getID() == graphID) {
                return graph.getInnerGraph();
            }
        }
        ChatterBox.error("GraphLoader", "getBase()", "Could not find a graph with id " + graphID);
        return null;
    }
}
////////////////////////////////////////////////////////////////////////////////

