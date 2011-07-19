////////////////////////////////MyTrustGraph//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.algorithms.MyRankbasedTrust;
import trustGrapher.graph.edges.MyTrustEdge;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * Description
 * @author Andrew O'Hara
 */
public class MyTrustGraph extends MyGraph {
    private MyRankbasedTrust alg;

//////////////////////////////////Constructor///////////////////////////////////
    public MyTrustGraph() {
        super((SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()), FULL);
    }

    public MyTrustGraph(MyRankbasedTrust alg) {
        super((SimpleDirectedGraph) new TrustGraph(new TrustEdgeFactory()), DYNAMIC);
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

    private void feedback(MyTrustGraph referenceGraph, int from, int to) {
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
                    ChatterBox.debug(this, "feedback()", ex.getMessage());
                }
            }
        }
    }

    private void unFeedback(MyTrustGraph referenceGraph, int assessor, int assessee) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void graphEvent(TrustLogEvent gev, boolean forward, MyGraph referenceGraph) {
        if (type == FULL) {
            ChatterBox.error(this, "graphEvent()", "This graph is not a dynamic graph.  Illegal method call.");
            return;
        }
        if (forward) {
            feedback((MyTrustGraph) referenceGraph, gev.getAssessor(), gev.getAssessee());
        } else {
            unFeedback((MyTrustGraph) referenceGraph, gev.getAssessor(), gev.getAssessee());
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
