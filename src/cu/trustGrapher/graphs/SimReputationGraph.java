////////////////////////////////SimReputationGraph///////////////////////////////
package cu.trustGrapher.graphs;

import cu.trustGrapher.graphs.edges.SimReputationEdge;
import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.loading.GraphConfig;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class SimReputationGraph extends SimAbstractGraph {
    private SimFeedbackGraph feedbackGraph;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a Reputation Graph.
     * @param graphConfig This object contains all of the configurations for this graph
     * @param feedbackGraph The feedbackGraph is needed to find what Agents are currently on it.
     */
    public SimReputationGraph(GraphConfig graphConfig, SimFeedbackGraph feedbackGraph) {
        super(graphConfig, (SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
        this.feedbackGraph = feedbackGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  It is assumed that this graph is a dynamic graph.
     * This method handles the addition or subtraction of edges and agents from the dynamic graph, and edge labels for both
     * based on the event that is currently being processed.
     * @param event The TrustLogEvent that is being processed
     * @param forward Whether or not the graph is being played forward
     */
    @Override
    public void graphEvent(TrustLogEvent event, boolean forward) {
        ReputationAlgorithm alg = (ReputationAlgorithm) getAlgorithm();
        ((EigenTrust) alg).setMatrixFilled(false);
        ((EigenTrust) alg).setIterations(((EigenTrust) alg).getIterations() + (forward ? 1 : -1));
        Collection<Agent> fbVertices = feedbackGraph.getVertices();
        ArrayList<Agent> toRemove = new ArrayList<Agent>();
        for (Agent src : fbVertices) {
            for (Agent sink : fbVertices) {
                if (!src.equals(sink)) {
                    double trustScore = -1;
                    try {
                        trustScore = alg.calculateTrustScore(src, sink);
                    } catch (Exception ex) {
                        continue;
                    }
                    ensureAgentExists(src.id, this);
                    ensureAgentExists(sink.id, this);
                    ((SimReputationEdge) referenceGraph.findEdge(src, sink)).setReputation(trustScore);
                    ((SimReputationEdge) ensureEdgeExists(src, sink, this)).setReputation(trustScore);
                }
            }
        }
        if (getVertexCount() > fbVertices.size()){
            toRemove.clear();
            toRemove.addAll(getVertices());
            toRemove.removeAll(fbVertices);
            for (Agent a : toRemove) {
                for (TestbedEdge e : getIncidentEdges(a)) {
                    removeEdge(e);
                }
                removeVertex(a);
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

