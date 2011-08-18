////////////////////////////////SimReputationGraph///////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.graph.edges.SimReputationEdge;
import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TestbedEdge;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;

import utilities.ChatterBox;

/**
 * A graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class SimReputationGraph extends SimGraph {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a Reputation Graph.
     * @param graphPair The graphPair that is to hold this graph
     */
    public SimReputationGraph(GraphPair graphPair) {
        super(graphPair, (SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * This String returned by this is the String displayed on the viewer border
     */
    public String getDisplayName() {
        return graphPair.getID() + "-" + graphPair.getAlgorithm().getClass().getSimpleName();
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds any new Agents that are necessary, then looks up the trustscore for every agent in the graph.
     * If an edge doesn't exist, it is  created.  The reputation is actually changed on the matching edge on the full graph
     * @param event The TrustLogEvent that is being processed
     * @param fullGraph     Any reputation changes will be done to the edges on this graph
     */
    @Override
    protected void forwardEvent(TrustLogEvent event, SimGraph fullGraph) {
        ReputationAlgorithm alg = (ReputationAlgorithm) graphPair.getAlgorithm();
        ensureAgentExists(event.getAssessor());
        ensureAgentExists(event.getAssessee());
        ((EigenTrust) alg).setMatrixFilled(false);
        ((EigenTrust) alg).setIterations(((EigenTrust) alg).getIterations() + 1);
        Collection<Agent> vertices = graphPair.getGraphs().get(0).getDynamicGraph().getVertices();
        for (Agent src : vertices) {
            for (Agent sink : vertices) {
                if (!src.equals(sink)) {
                    double trustScore = -1;
                    try {
                        trustScore = alg.calculateTrustScore(src, sink);

                    } catch (Exception ex) {
                        ChatterBox.debug(this, "forwardEvent()", ex.getMessage());
                    }
                    ((SimReputationEdge) fullGraph.findEdge(src, sink)).setReputation(trustScore);
                    ((SimReputationEdge) ensureEdgeExists(src, sink, this)).setReputation(trustScore);
                }
            }
        }
    }

    /**
     * Re-Evaluate the trust between the agents this graaph.
     * If the algorithm can't find one of those agents in the feedback graph, then remove the edge between those agents
     * @param event The TrustLogEvent that is being processed
     * @param fullGraph     Any reputation changes will be done to the edges on this graph
     */
    @Override
    protected void backwardEvent(TrustLogEvent event, SimGraph fullGraph) {
        ReputationAlgorithm alg = (ReputationAlgorithm) graphPair.getAlgorithm();
        ((EigenTrust) alg).setMatrixFilled(false);
        ((EigenTrust) alg).setIterations(((EigenTrust) alg).getIterations() - 1);
        Collection<Agent> vertices = graphPair.getGraphs().get(0).getDynamicGraph().getVertices();
        for (Agent src : vertices) {
            for (Agent sink : vertices) {
                if (!src.equals(sink)) {
                    try {
                        double trustScore = alg.calculateTrustScore(src, sink);
                        ((SimReputationEdge) fullGraph.findEdge(src, sink)).setReputation(trustScore);
                        ((SimReputationEdge) findEdge(src, sink)).setReputation(trustScore);
                    } catch (Exception ex) { //One of the agents doesn't exist in the feedback graph anymore, so remove the edge between the two agents
                    }
                }
            }
        }
        ArrayList<Agent> toRemove = new ArrayList<Agent>(getVertices());
        toRemove.removeAll(vertices);
        for (Agent a : toRemove) {
            for (TestbedEdge e : this.getIncidentEdges(a)) {
                this.removeEdge(e);
            }
            this.removeVertex(a);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

