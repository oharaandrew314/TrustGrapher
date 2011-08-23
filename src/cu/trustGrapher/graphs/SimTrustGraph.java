////////////////////////////////SimTrustGraph//////////////////////////////////
package cu.trustGrapher.graphs;

import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import java.util.Collection;

import org.jgrapht.graph.SimpleDirectedGraph;

import aohara.utilities.ChatterBox;
import cu.trustGrapher.loading.GraphConfig;

/**
 * A graph that shows whether each agent trusts the other
 * @author Andrew O'Hara
 */
public class SimTrustGraph extends SimAbstractGraph {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a Trust Graph.
     * @param graphConfig This object contains all of the configurations for this graph
     */
    public SimTrustGraph(GraphConfig graphConfig) {
        super(graphConfig, (SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()));
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
        TrustAlgorithm alg = (TrustAlgorithm) getAlgorithm();
        Collection<Agent> vertices = forward ? alg.getReputationGraph().vertexSet() : getVertices();
        for (Agent src : vertices) {
            for (Agent sink : vertices) {
                try {
                    if (!src.equals(sink)) {
                        if (alg.trusts(src, sink)) { //Ensure an edge exists between the two agents that trust eachother
                            ensureAgentExists(src.id, this);
                            ensureAgentExists(sink.id, this);
                            ensureEdgeExists(src, sink, this);
                        } else {
                            throw new IllegalArgumentException(); //Remove the edge
                        }
                    }
                } catch (IllegalArgumentException ex) { //If they don't trust eachother or exist in the rep graph, enusre that no edge exists between them
                    TestbedEdge edgeToRemove = findEdge(src, sink);
                    if (edgeToRemove != null) {
                        removeEdge(edgeToRemove);
                    }
                } catch (Exception ex) {
                    ChatterBox.error(this, "graphEvent()", ex.getMessage());
                }
            }
        }
        for (Object agent : getVertices().toArray()){
            if (getIncidentEdges((Agent) agent).isEmpty()){
                removeVertex((Agent) agent);
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

