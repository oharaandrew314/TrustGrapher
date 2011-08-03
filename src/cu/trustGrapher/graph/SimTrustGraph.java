////////////////////////////////SimTrustGraph//////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import java.util.ArrayList;
import org.jgrapht.graph.SimpleDirectedGraph;
import cu.trustGrapher.graph.edges.MyTrustEdge;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * Description
 * @author Andrew O'Hara
 */
public class SimTrustGraph extends SimGraph {
    private TrustAlgorithm alg;

//////////////////////////////////Constructor///////////////////////////////////
    public SimTrustGraph(int id, boolean display) {
        super((SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()), FULL, id, display);
    }

    public SimTrustGraph(TrustAlgorithm alg, int id, boolean display) {
        super((SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()), DYNAMIC, id, display);
        this.alg = alg;
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    protected void forwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        if (type != DYNAMIC){
            ChatterBox.error(this, "feedback()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        ensureAgentExists(gev.getAssessor());
        ensureAgentExists(gev.getAssessee());
        for (Agent src : alg.getReputationGraph().vertexSet()){
            for (Agent sink : alg.getReputationGraph().vertexSet()){
                try{
                    if (!src.equals(sink) && alg.trusts(src, sink) && findEdge(src,sink) == null){
                        ensureEdgeExists(src, sink,((MyTrustEdge)fullGraph.findEdge(src, sink)).getID(), this);
                    }
                }catch(Exception ex){
                    ChatterBox.error(this, "forwardEvent()", ex.getMessage());
                }
            }
        }
    }

    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        if (type != DYNAMIC){
            ChatterBox.error(this, "feedback()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        for (Agent src : getVertices()){
            for (Agent sink : getVertices()){
                try{
                    alg.trusts(src, sink);
                }catch(Exception ex){
                    MyTrustEdge edge = (MyTrustEdge) findEdge(src, sink);
                    if (edge != null){
                        removeEdge(edge);
                    }
                }
            }
        }

        //remove unnecessary vertices
        ArrayList<Agent> toRemove = new ArrayList<Agent>();
        for (Agent a : getVertices()){
            if (!alg.getReputationGraph().vertexSet().contains(a)){
                toRemove.add(a);
            }
        }
        for (Agent a : toRemove){
            removeVertex(a);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
