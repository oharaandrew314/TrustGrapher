////////////////////////////////SimReputationGraph///////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.graph.edges.SImReputationEdge;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;

import java.util.Collection;
import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * A  graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class SimReputationGraph extends SimGraph {
    private ReputationAlgorithm alg;
    private SimFeedbackGraph feedbackGraph;

//////////////////////////////////Constructor///////////////////////////////////

    /**
     * Creates a FULL graph.  The components of this graph are actually the ones being displayed.
     * @param feedbackGraph The graph that this graph will listen to for changes
     * @param id The id of this graph and its dynamic partner
     * @param display Whether or not this graph will be shown in a TrustGraphViewer
     */
    public SimReputationGraph(int id, boolean display){
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()), FULL, id);
        this.display = display;
    }

    /**
     * Creates a DYNAMIC graph.  This graph is only used by the full graph to see which of it's own components should be displayed.
     * @param feedbackGraph The graph that this graph will listen to for changes
     * @param alg The algorithm that this graph will use to update the reputation values of the full graph
     * @param id The id of this graph and its full partner
     */
    public SimReputationGraph(SimFeedbackGraph feedbackGraph, ReputationAlgorithm alg, int id) {
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()), DYNAMIC, id);
        this.feedbackGraph = feedbackGraph;
        this.alg = alg;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    /**
     * This String returned by this is the String displayed on the viewer border
     * This can only be called on a DYNAMIC graph because it is the only one with the algorithm
     */
    public String getDisplayName(){
        if (type != DYNAMIC){
            ChatterBox.error(this, "getDisplayName", "This graph is not a dynamic graph.  Illegal method call.");
            return null;
        }
        return graphID + "-" + alg.getClass().getSimpleName();
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds any new Agents that are necessary, then looks up the trustscore for every agent in the graph.
     * If an edge doesn't exist, it is  created.  The reputation is actually changed on the matching edge on the full graph
     * @param gev The TrustLogEvent that is being processed
     * @param fullGraph     Any reputation changes will be done to the edges on this graph
     */
    @Override
    protected void forwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        ensureAgentExists(gev.getAssessor());
        ensureAgentExists(gev.getAssessee());
        ((EigenTrust)alg).setMatrixFilled(false);
        ((EigenTrust)alg).setIterations(((EigenTrust)alg).getIterations() + 1);
        Collection<Agent> vertices = feedbackGraph.getVertices();
        for (Agent src : vertices){
            for (Agent sink : vertices){
                if (!src.equals(sink)){
                    try{
                        double trustScore = alg.calculateTrustScore(src, sink);
                        SImReputationEdge fullEdge = (SImReputationEdge)fullGraph.findEdge(src, sink);
                        fullEdge.setReputation(trustScore);
                        ensureEdgeExists(src, sink, fullGraph);
                    }catch(Exception ex){
                        ChatterBox.debug(this, "forwardEvent()", ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Re-Evaluate the trust between the agents this graaph.
     * If the algorithm can't find one of those agents in the feedback graph, then remove the edge between those agents
     * @param gev The TrustLogEvent that is being processed
     * @param fullGraph     Any reputation changes will be done to the edges on this graph
     */
    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        ((EigenTrust)alg).setMatrixFilled(false);
        ((EigenTrust)alg).setIterations(((EigenTrust)alg).getIterations() - 1);
        Collection<Agent> vertices = getVertices();
        for (Agent src: vertices){
            for (Agent sink : vertices){
                if (!src.equals(sink)){
                    try{
                        double trustScore = alg.calculateTrustScore(src, sink);
                        SImReputationEdge fullEdge = (SImReputationEdge)fullGraph.findEdge(src, sink);
                        ((SImReputationEdge) ensureEdgeExists(src, sink, fullGraph)).setReputation(trustScore);
                        fullEdge.setReputation(trustScore);
                    }catch(NullPointerException ex){ //One of the agents doesn't exist in the feedback graph anymore, so remove the edge between the two agents
                        removeEdgeAndVertices(findEdge(src, sink));
                        vertices = getVertices();
                    }catch(Exception ex){
                        //I must ignore exceptions right now, because EigenTrust throws dumb exceptions, since I didn't write it
//                        ChatterBox.debug(this, "backwardEvent()", type + " " + ex.getMessage());
                    }
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

