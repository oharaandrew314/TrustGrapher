////////////////////////////////MyReputationGraph///////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TestbedEdge;
import java.util.Collection;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.algorithms.MyEigenTrust;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * A  graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class MyReputationGraph extends MyGraph {
    private MyReputationGraph hiddenGraph = null;
    public EigenTrust alg;

//////////////////////////////////Constructor///////////////////////////////////
//    /**
//     * Creates a hidden graph.  The components of this graph are actually the ones beign displayed.
//     */
//    public MyReputationGraph() {
//        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
//        type = HIDDEN;
//    }

    /**
     * Creates a hidden graph.  The components of this graph are actually the ones beign displayed.
     */
    public MyReputationGraph(SimpleDirectedGraph baseGraph){
        super((SimpleDirectedGraph)baseGraph);
        type = HIDDEN;
    }

    /**
     * Creates a visible graph.  This graph is only used by the hidden graph to see which of it's own components should be displayed.
     * @param baseGraph The graph that this graph will be based on
     * @param hiddenGraph A reference to the hiddenGraph so that the reputation can be changed
     */
    public MyReputationGraph(SimpleDirectedGraph baseGraph, MyReputationGraph hiddenGraph, EigenTrust alg) {
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
        this.hiddenGraph = hiddenGraph;
        this.alg = alg;
        type = VISIBLE;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public MyReputationGraph getHiddenGraph(){
        if (type != VISIBLE){
            ChatterBox.error(this, "getHiddenGraph()", "This is not a visible graph.  This method cannot be called.");
        }
        return hiddenGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public void feedback(MyReputationGraph hiddenGraph, MyReputationEdge refEdge) {
        Agent src = (Agent)refEdge.src;
        Agent sink= (Agent)refEdge.sink;
        int key = refEdge.getID();
        MyReputationEdge edge = (MyReputationEdge) findEdge(src.id, sink.id);
        if (edge == null){
            edge = createEdge((Agent)refEdge.src, (Agent)refEdge.sink, key);
            addEdge(edge, src, sink);
        }
        ((MyEigenTrust)alg).setMatrixFilled(false);
        edge.setReputation(alg.calculateTrustScore(src, sink));

    }

    public MyReputationEdge createEdge(Agent src, Agent sink){
        return createEdge(src, sink, edgecounter++);
    }

    public MyReputationEdge createEdge(Agent src, Agent sink, int key){
        if (getVertexInGraph(src.id) == null) {
            addPeer(src.id);
            ChatterBox.print("Creating " + src);
        }
        if (getVertexInGraph(sink.id) == null) {
            addPeer(sink.id);
            ChatterBox.print("Creating " + sink);
        }
        src = getVertexInGraph(src.id);
        sink = getVertexInGraph(sink.id);
        return new MyReputationEdge(src, sink, key);
    }

    public void unFeedback(MyReputationGraph hiddenGraph, MyReputationEdge repEdge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void graphEvent(TrustLogEvent gev, boolean forward, MyGraph referenceGraph) {
        if (type == HIDDEN){
            ChatterBox.error(this, "graphEvent()", "This graph is not a visible graph.");
            return;
        }
        MyReputationEdge refEdge = (MyReputationEdge) referenceGraph.findEdge(gev.getAssessor(), gev.getAssessee());
        if (refEdge == null){
            ChatterBox.error(this, "graphEvent()", "Could not find the edge in the hidden graph.");
        }
        if (forward){
            feedback((MyReputationGraph)referenceGraph, refEdge);
        }else{
            unFeedback((MyReputationGraph) referenceGraph,refEdge);
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param gev	The Log event which needs to be handled.
     */
    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type == VISIBLE){
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a hidden graph.");
            return;
        }
        int from = gev.getAssessor();
        int to = gev.getAssessee();
        if (getVertexInGraph(from) == null) {
            addPeer(from);
        }
        if (getVertexInGraph(to) == null) {
            addPeer(to);
        }
        Agent assessor = getVertexInGraph(from);
        Agent assessee = getVertexInGraph(to);
        MyReputationEdge edge = (MyReputationEdge) findEdge(from, to);
        if (edge == null) {//If the edge doesn't  exist, add it
            try {
                edge = new MyReputationEdge(assessor, assessee, edgecounter++);
                addEdge(edge, (Agent)edge.src, (Agent)edge.sink);
            } catch (Exception ex) {
                ChatterBox.error(this, "feedback()", "Error creating edge: " + ex.getMessage());
            }
        }
    }

    public void printGraph() {
        if (type == HIDDEN) {
            ChatterBox.print("Printing hidden " + this.getClass().getSimpleName() + "...");
        } else {
            ChatterBox.print("Printing visible " + this.getClass().getSimpleName() + "...");
        }
        Collection<Agent> agents = this.getVertices();
        for (Agent a : agents) {
            ChatterBox.print(a.toString());
        }
        Collection<TestbedEdge> edges = getEdges();
        for (TestbedEdge e : edges) {
            ChatterBox.print("Edge: " + e.src + " to " + e.sink + " " + ((MyReputationEdge)e).getReputation());

        }
        ChatterBox.print("Done.");
    }

}
////////////////////////////////////////////////////////////////////////////////

