////////////////////////////////SimTrustGraph//////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import cu.trustGrapher.graph.savingandloading.Algorithm;
import org.jgrapht.graph.SimpleDirectedGraph;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;
import java.util.ArrayList;
import java.util.Set;
import utilities.ChatterBox;

/**
 * A graph that shows whether each agent trusts the other
 * @author Andrew O'Hara
 */
public class SimTrustGraph extends SimGraph {
    private TrustAlgorithm alg;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a Trust Graph. The edges on this graph signify that one peers trust the other
     */
    public SimTrustGraph(GraphManager graphManager, int type, Algorithm algConfig) {
        super(graphManager, (SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()), type, algConfig);
        alg = (TrustAlgorithm) algConfig.getAlgorithm();
    }

//////////////////////////////////Accessors/////////////////////////////////////

    /**
     * This String returned by this is the String displayed on the viewer border
     * This can only be called on a DYNAMIC graph because it is the only one with the algorithm
     */
    public String getDisplayName(){
        if (type != DYNAMIC){
            ChatterBox.error(this, "getDisplayName()", "This graph is not a dynamic graph.  Illegal method call.");
            return null;
        }
        return graphID + "-" + alg.getClass().getSimpleName();
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds any necessary agents to the graph and then checks if every agent trusts the other.
     * If the agents trust eachother, then this ensures that there is a Trust Edge between them.
     * @param gev The TrustLogEvent that is being processed
     * @param fullGraph Any new edges will be added to this graph
     */
    @Override
    protected void forwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        ensureAgentExists(gev.getAssessor());
        ensureAgentExists(gev.getAssessee());
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
     * Checks all the vertices in this graph.
     * If the algorithm freaks out because one of those vertices don't exist in the reputation graph,
     * the edge between them is removed.
     * @param gev The TrustLogEvent that is being processed
     * @param fullGraph Any new edges will be added to this graph
     */
    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {        
        java.util.Collection<Agent> vertices = graphManager.get(GraphManager.FEEDBACK, DYNAMIC).getVertices();
        for (Agent src : vertices){
            for (Agent sink : vertices){
                try{
                    alg.trusts(src, sink);
                }catch(ArrayIndexOutOfBoundsException ex){ //One of the agents doesn't exist in the feedback graph anymore, so remove the edge between the two agents
                    removeEdgeAndVertices(findEdge(src, sink));
                    vertices = getVertices();
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
