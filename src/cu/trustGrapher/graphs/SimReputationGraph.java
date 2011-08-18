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

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class SimReputationGraph extends SimAbstractGraph {

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
    @Override
    public void graphEvent(TrustLogEvent event, boolean forward) {
        ReputationAlgorithm alg = (ReputationAlgorithm) graphPair.getAlgorithm();
        ((EigenTrust) alg).setMatrixFilled(false);
        ((EigenTrust) alg).setIterations(((EigenTrust) alg).getIterations() + (forward ? 1 : -1));
        Collection<Agent> fbVertices = graphPair.getGraphs().get(0).getDynamicGraph().getVertices();
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
                    ensureAgentExists(src.id);
                    ensureAgentExists(sink.id);
                    ((SimReputationEdge) graphPair.getFullGraph().findEdge(src, sink)).setReputation(trustScore);
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

