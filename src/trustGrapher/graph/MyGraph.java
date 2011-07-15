//////////////////////////////////MyGraph////////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.repsystestbed.graphs.JungAdapterGraph;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Collection;
import utilities.ChatterBox;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * @author Andrew O'Hara
 */
public class MyGraph extends JungAdapterGraph<Agent, TestbedEdge> {
    public static final int VISIBLE = 0, HIDDEN = 1;
    protected int type;
    int edgecounter = 0;
    protected boolean forward = true;

//////////////////////////////////Constructor///////////////////////////////////
    public MyGraph(SimpleDirectedGraph<Agent, TestbedEdge> graph) {
        super(graph);
    }

    public MyGraph(SimpleDirectedGraph<Agent, TestbedEdge> graph, int type){
        super(graph);
        this.type = type;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getType(){
        return type;
    }

    public boolean getForward(){
        return forward;
    }

    /**
     * Finds an edge that already exists in the graph
     * @param from
     * @param to
     * @return
     */
    public TestbedEdge findEdge(int from, int to) {
        return super.findEdge(getVertexInGraph(from), getVertexInGraph(to));
    }

    public Agent getVertexInGraph(int peerNum) {
        return getVertexInGraph(new Agent(peerNum));
    }

    /**
     * this methods gets a vertex already in the graph that is equal to the input vertex
     * to be used when adding edges; the edge should relate two vertices actually in the graph, not copies of these vertices.
     * @param input a Agent object
     * @return a Agent v such that v.equals(input) and v is in the graph
     */
    public Agent getVertexInGraph(Agent input) {
        for (Agent v : super.getVertices()) {
            if (v.equals(input)) {
                return (Agent) v;
            }
        }
        return null;
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void addPeer(int peernumber) {
        if (getVertexInGraph(peernumber) == null) {
            addVertex(new Agent(peernumber));
        } else {
            ChatterBox.error(this, "addPeer()", "Tried to add a peer that already exists");
        }
    }

    public void removePeer(int peerNum) {
        Agent peer = new Agent(peerNum);
        Collection<TestbedEdge> edgeset = getIncidentEdges(peer);
        for (TestbedEdge e : edgeset) {
            removeEdge(e);
        }
        removeVertex(peer);
    }
}
////////////////////////////////////////////////////////////////////////////////
