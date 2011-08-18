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

import utilities.ChatterBox;

/**
 * A graph that shows whether each agent trusts the other
 * @author Andrew O'Hara
 */
public class SimTrustGraph extends SimAbstractGraph {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a Trust Graph.
     * @param graphPair The graphPair that is to hold this graph
     */
    public SimTrustGraph(GraphPair graphPair) {
        super(graphPair, (SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()));
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
        TrustAlgorithm alg = (TrustAlgorithm) graphPair.getAlgorithm();
        Collection<Agent> vertices = forward ? alg.getReputationGraph().vertexSet() : getVertices();
        for (Agent src : vertices) {
            for (Agent sink : vertices) {
                try {
                    if (!src.equals(sink)) {
                        if (alg.trusts(src, sink)) { //Ensure an edge exists between the two agents that trust eachother
                            ensureAgentExists(src.id);
                            ensureAgentExists(sink.id);
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

