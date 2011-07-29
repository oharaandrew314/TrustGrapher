//////////////////////////////////SimGraph////////////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.graph.JungAdapterGraph;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.Collection;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * @author Andrew O'Hara
 */
public abstract class SimGraph extends JungAdapterGraph<Agent, TestbedEdge> {

    public static final int DYNAMIC = 0, FULL = 1;
    protected int type;
    protected int edgecounter = 0;
    protected int id;
    protected boolean display;

//////////////////////////////////Constructor///////////////////////////////////

    public SimGraph(SimpleDirectedGraph<Agent, TestbedEdge> graph, int type, int id, boolean display) {
        super(graph);
        this.type = type;
        this.id = id;
        this.display = display;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " " + id;
    }

    public int getType() {
        return type;
    }

    public int getID(){
        return id;
    }

    public boolean isDisplayed(){
        return display;
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
     * this methods gets a vertex already in the graph that is equal to the agent vertex
     * to be used when adding edges; the edge should relate two vertices actually in the graph, not copies of these vertices.
     * @param agent a Agent object
     * @return a Agent v such that v.equals(agent) and v is in the graph
     */
    public Agent getVertexInGraph(Agent agent) {
        for (Agent v : super.getVertices()) {
            if (v.equals(agent)) {
                return (Agent) v;
            }
        }
        return null;
    }

    public abstract void graphEvent(TrustLogEvent gev, boolean forward, SimGraph referenceGraph);

    public abstract void graphConstructionEvent(TrustLogEvent gev);

///////////////////////////////////Methods//////////////////////////////////////
    public void addPeer(int peerNum) {
        if (getVertexInGraph(peerNum) == null) {
            addVertex(new Agent(peerNum));
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

