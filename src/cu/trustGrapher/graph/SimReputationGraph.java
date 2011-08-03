////////////////////////////////SimReputationGraph///////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.graph.edges.MyReputationEdge;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;

import java.util.ArrayList;
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
     * Creates a full graph.  The components of this graph are actually the ones being displayed.
     * @param baseGraph The graph that this graph will be based on
     */
    public SimReputationGraph(SimFeedbackGraph feedbackGraph, int id, boolean display){
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()), FULL, id, display);
        this.feedbackGraph = feedbackGraph;
    }

    /**
     * Creates a dynamic graph.  This graph is only used by the full graph to see which of it's own components should be displayed.
     * @param baseGraph The graph that this graph will be based on
     * @param fullGraph A reference to the fullGraph so that reputation can be changed
     */
    public SimReputationGraph(SimFeedbackGraph feedbackGraph, ReputationAlgorithm alg, int id, boolean display) {
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()), DYNAMIC, id, display);
        this.alg = alg;
        this.feedbackGraph = feedbackGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds any new Agents that are necessary, then looks up the trustscore for every agent in the graph.
     * If an edge doesn't exist, it is  created.  The reputation is actually changed on the matching edge on the full graph
     * @param fullGraph     Any reputation changes will be done to the edges on this graph
     */
    @Override
    protected void forwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        if (type != DYNAMIC){
            ChatterBox.error(this, "feedback()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        ensureAgentExists(gev.getAssessor());
        ensureAgentExists(gev.getAssessee());
        ((EigenTrust)alg).setMatrixFilled(false);
        ((EigenTrust)alg).setIterations(((EigenTrust)alg).getIterations() + 1);
        for (Agent src : feedbackGraph.getVertices()){
            for (Agent sink : feedbackGraph.getVertices()){
                if (!src.equals(sink)){
                    double trustScore = -1;
                    try{
                        trustScore = alg.calculateTrustScore(src, sink);
                    }catch(Exception ex){
                        ChatterBox.debug(this, "forwardEvent()", ex.getMessage());
                    }
                    MyReputationEdge fullEdge = (MyReputationEdge)fullGraph.findEdge(src, sink);
                    ((MyReputationEdge) ensureEdgeExists(src, sink, fullEdge.getID(), fullGraph)).setReputation(trustScore);
                    fullEdge.setReputation(trustScore);
                }
            }
        }
    }

    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        if (type != DYNAMIC){
            ChatterBox.error(this, "backwardEvent()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        ((EigenTrust)alg).setMatrixFilled(false);
        ((EigenTrust)alg).setIterations(((EigenTrust)alg).getIterations() - 1);
        for (Agent src: feedbackGraph.getVertices()){
            for (Agent sink : feedbackGraph.getVertices()){
                if (!src.equals(sink)){
                    try{
                        double trustScore = alg.calculateTrustScore(src, sink);
                        MyReputationEdge fullEdge = (MyReputationEdge)fullGraph.findEdge(src, sink);
                        ((MyReputationEdge) ensureEdgeExists(src, sink, fullEdge.getID(), fullGraph)).setReputation(trustScore);
                        fullEdge.setReputation(trustScore);
                    }catch(Exception ex){
                        ChatterBox.debug(this, "backwardEvent()", ex.getMessage());
                    }
                }
            }
        }

        //remove unnecessary vertices
        ArrayList<Agent> toRemove = new ArrayList<Agent>();
        for (Agent a : getVertices()){
            if (!feedbackGraph.getVertices().contains(a)){
                toRemove.add(a);
            }
        }
        for (Agent a : toRemove){
            removeVertex(a);
        }

    }

    
}
////////////////////////////////////////////////////////////////////////////////

