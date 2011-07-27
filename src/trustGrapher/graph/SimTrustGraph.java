////////////////////////////////SimTrustGraph//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import java.util.ArrayList;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.graph.edges.MyTrustEdge;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
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
    /**
     * For use by graphConstructionEvent() only.  Calls the createEdge(Agent, Agent, int) method.
     * Passes on the src and sink, but gives it an incrementing id starting at 0.
     * @param src
     * @param sink
     * @return a MyReputationEdge between the given agents
     */
    public MyTrustEdge createEdge(Agent src, Agent sink) {
        if (type == DYNAMIC) {
            ChatterBox.error(this, "createEdge()", "This graph is not a full graph.  Illegal method call.");
            return null;
        }
        return createEdge(src, sink, edgecounter++);
    }

    /**
     * Creates a MyReputatioEdge and returns a reference to it
     * @param src
     * @param sink
     * @param id
     * @return a MyReputationEdge between the given agents with the given id
     */
    public MyTrustEdge createEdge(Agent src, Agent sink, int id) {
        if (getVertexInGraph(src.id) == null) {
            addPeer(src.id);
        }
        if (getVertexInGraph(sink.id) == null) {
            addPeer(sink.id);
        }
        src = getVertexInGraph(src.id);
        sink = getVertexInGraph(sink.id);
        return new MyTrustEdge(src, sink, id);
    }

    private void feedback(SimTrustGraph referenceGraph) {
        if (type == FULL){
            ChatterBox.error(this, "feedback()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        for (Agent src : alg.getReputationGraph().vertexSet()){
            for (Agent sink : alg.getReputationGraph().vertexSet()){
                try{
                    if (!src.equals(sink) && alg.trusts(src, sink) && findEdge(src,sink) == null){
                        int id = ((MyTrustEdge)referenceGraph.findEdge(src, sink)).getID();
                        MyTrustEdge edge = createEdge(src, sink, id);
                        addEdge(edge, src, sink);
                    }
                }catch(Exception ex){
                    ChatterBox.error(this, "feedback()", ex.getMessage());
                }
            }
        }
    }

    private void unFeedback() {
        if (type == FULL){
            ChatterBox.error(this, "feedback()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        for (Agent src : getVertices()){
            for (Agent sink : getVertices()){
                try{
                    alg.trusts(src, sink);
                }catch(Exception ex){
                    this.removeEdge(findEdge(src, sink));
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

    @Override
    public void graphEvent(TrustLogEvent gev, boolean forward, SimGraph referenceGraph) {
        if (type == FULL) {
            ChatterBox.error(this, "graphEvent()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        if (forward) {
            feedback((SimTrustGraph) referenceGraph);
        } else {
            unFeedback();
        }
    }

    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type == DYNAMIC) {
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a full graph.  Illegal method call.");
            return;
        }
        addPeer(gev.getAssessor());
        addPeer(gev.getAssessee());
        for (Agent src : getVertices()) {
            for (Agent sink : getVertices()) {
                if (findEdge(src, sink) == null && !(src.equals(sink))) {
                    MyTrustEdge edge = createEdge(src, sink);
                    addEdge(edge, src, sink);
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
