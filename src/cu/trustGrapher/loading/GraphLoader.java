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

 * 
 * @author Andrew O'Hara
 */
public class GraphLoader {

    public static List<SimAbstractGraph> loadGraphs(List<GraphConfig> graphConfigs) {
        List<SimAbstractGraph> graphs = new ArrayList<SimAbstractGraph>();
        ArrayList<GraphConfig> trustGraphs = new ArrayList<GraphConfig>();
        for (GraphConfig graphConfig : graphConfigs) {
            if (graphConfig.isTrustGraph()) {
                trustGraphs.add(graphConfig);
            } else if (graphConfig.isFeedbackGraph()) {
                graphs.add(new SimFeedbackGraph(graphConfig));
            } else if (graphConfig.isReputationGraph()) {
                graphs.add(new SimReputationGraph(graphConfig, (SimFeedbackGraph) graphs.get(0)));
                ReputationAlgorithm alg = (ReputationAlgorithm) graphConfig.getAlgorithm();
                alg.setFeedbackHistoryGraph((FeedbackHistoryGraph) getInnerGraph(graphConfig.getBase(), graphs));
            } else {
                ChatterBox.criticalError("SimGraphPair", "<init>", "Unsupported graph type");
            }
        }
        for (GraphConfig graphConfig : trustGraphs) {
            graphs.add(new SimTrustGraph(graphConfig));
            TrustAlgorithm alg = (TrustAlgorithm) graphConfig.getAlgorithm();
            alg.setReputationGraph((ReputationGraph) getInnerGraph(graphConfig.getBase(), graphs));
        }
        return graphs;
    }

    /**
     * Searches the existing graphs for the graph with the correct ID and then returns that graphs inner jGrapht graph.
     * This is needed since the trustTestBed algorithms require trustTestBed graphs to be attached to them.
     * @param graphID The ID of the graph pair that contains the inner graph to return
     * @return The jGraphT SimpleDirectedGraph inside of the specified JungAdapterGraph
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

