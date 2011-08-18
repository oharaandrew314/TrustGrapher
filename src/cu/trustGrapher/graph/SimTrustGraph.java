////////////////////////////////SimTrustGraph//////////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.ArrayList;
import java.util.Set;

import utilities.ChatterBox;

/**
 * A graph that shows whether each agent trusts the other
 * @author Andrew O'Hara
 */
public class SimTrustGraph extends SimGraph {

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
    public String getDisplayName(){
        return graphPair.getID() + "-" + graphPair.getAlgorithm().getClass().getSimpleName();
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds any necessary agents to the graph and then checks if every agent trusts the other.
     * If the agents trust eachother, then this ensures that there is a Trust Edge between them.
     * @param event The TrustLogEvent that is being processed
     * @param fullGraph Any new edges will be added to this graph
     */
    @Override
    protected void forwardEvent(TrustLogEvent event, SimGraph fullGraph) {
        TrustAlgorithm alg = (TrustAlgorithm) graphPair.getAlgorithm();
        ensureAgentExists(event.getAssessor());
        ensureAgentExists(event.getAssessee());
        Set<Agent> vertices = alg.getReputationGraph().vertexSet();
        for (Agent src : vertices){
            for (Agent sink : vertices){
                try{
                    if (!src.equals(sink) && alg.trusts(src, sink)){
                        ensureEdgeExists(src, sink, this);
                    }
                }catch(Exception ex){
                    ChatterBox.error(this, "forwardEvent()", ex.getMessage());
                }
            }
        }
    }

    /**
     * Checks all the vertices in this graph if they trust eachother, and removes
     * the edge between them if they don't.
     * @param event The TrustLogEvent that is being processed
     * @param fullGraph Any new edges will be added to this graph
     */
    @Override
    protected void backwardEvent(TrustLogEvent event, SimGraph fullGraph) {
        TrustAlgorithm alg = (TrustAlgorithm) graphPair.getAlgorithm();
        Set<Agent> vertices = alg.getReputationGraph().vertexSet();
        for (Agent src : vertices){
            for (Agent sink : vertices){
                try{
                    alg.trusts(src, sink);
                }catch (Exception ex){
                    ChatterBox.debug(this, "backwardEvent()", ex.getMessage());
                }
            }
        }
        ArrayList<Agent> toRemove = new ArrayList<Agent>(getVertices());
        toRemove.removeAll(vertices);
        for (Agent a : toRemove){
            for (TestbedEdge e : this.getIncidentEdges(a)){
                this.removeEdge(e);
            }
            this.removeVertex(a);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
