//////////////////////////////////MyGraph////////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.repsystestbed.graphs.JungAdapterGraph;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
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

////////////////////////////////Static Methods//////////////////////////////////
    /**
     * Returns a tree graph of documents and the peers which host them.
     * @param graph	The source which the tree Graph will be made from
     * @return	The Document Tree Graph
     */
    public static Forest<Agent, TestbedEdge> makeTreeGraph(MyGraph graph) {
        Forest<Agent, TestbedEdge> tree = new DelegateForest<Agent, TestbedEdge>();
        for (Agent documentVertex : graph.getVertices()) { //iterate over all vertices in the graph
        }
        return tree;
    }
}
////////////////////////////////////////////////////////////////////////////////
