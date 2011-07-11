////////////////////////////////////FeedbackHistoryGraph//////////////////////////////////
package trustGrapher.graph;

import org.jgrapht.graph.SimpleDirectedGraph;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.graphs.JungAdapterGraph;
import cu.repsystestbed.graphs.FeedbackHistoryEdgeFactory;
import cu.repsystestbed.entities.Agent;

import java.util.Collection;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;

import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import utilities.ChatterBox;

public class FeedbackHistoryGraph extends JungAdapterGraph<Agent, FeedbackHistoryGraphEdge>{
    int edgecounter = 0;

//////////////////////////////////Constructor///////////////////////////////////
    public FeedbackHistoryGraph() { 
        super(new SimpleDirectedGraph(new FeedbackHistoryEdgeFactory()));
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public FeedbackEdge fineEdge(int from, int to) {
        return (FeedbackEdge) super.findEdge(getVertexInGraph(from), getVertexInGraph(to));
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
    /** adding a peer in the network*/
    public void addPeer(int peernumber) {
        if (getVertexInGraph(peernumber) == null){
            addVertex(new Agent(peernumber));
        }else{
            ChatterBox.error(this, "addPeer()", "Tried to add a peer that already exists");
        }
    }

    public void removePeer(int peerNum) {
        Agent peer = new Agent(peerNum);
        Collection<FeedbackHistoryGraphEdge> edgeset = getIncidentEdges(peer);
        for (FeedbackHistoryGraphEdge e : edgeset) {
            removeEdge(e);
        }
        removeVertex(peer);
    }

    public void feedback(int from, int to, double feedback) {
        Integer key = new Integer(++edgecounter);
        feedback(from, to, feedback, key);
    }

    public void feedback(int from, int to, double feedback, Integer key) {
        if (getVertexInGraph(from) == null) {
            addPeer(from);
        }
        if (getVertexInGraph(to) == null) {
            addPeer(to);
        }
        Agent assessor = getVertexInGraph(from);
        Agent assessee = getVertexInGraph(to);
        FeedbackEdge edge = fineEdge(from, to);
        if (edge == null) {//If the edge doesn't  exist, add it
            try {
                edge = new FeedbackEdge(key, assessor, assessee);
                addEdge(edge, edge.getAssessor(), edge.getAssessee());
            } catch (Exception ex) {
                ChatterBox.error(this, "feedback()", "Error creating edge: " + ex.getMessage());
            }
        }
        edge.addFeedback(assessor, assessee, feedback);
    }

    public void unFeedback(int from, int to, double feedback, int key) {
        FeedbackEdge edge = fineEdge(from, to);
        if (edge != null) {
            if (edge.hasMultipleFeedback()) {
                edge.removeFeedback(feedback);
            } else {
                Collection<Agent> verts = super.getIncidentVertices(edge);
                super.removeEdge(edge);
                for (Agent v : verts) {
                    if (super.getIncidentEdges(v).isEmpty()) {
                        super.removeVertex(v);
                    }
                }
            }
        } else {
            ChatterBox.error(this, "unFeedback()", "Couldn't find an edge to remove!");
        }
    }

    /**
     * Returns a tree graph of documents and the peers which host them.
     * @param graph	The source which the tree Graph will be made from
     * @return	The Document Tree Graph
     */
    public static Forest<Agent, FeedbackHistoryGraphEdge> makeTreeGraph(FeedbackHistoryGraph graph) {
        Forest<Agent, FeedbackHistoryGraphEdge> tree = new DelegateForest<Agent, FeedbackHistoryGraphEdge>();
        for (Agent documentVertex : graph.getVertices()) { //iterate over all vertices in the graph
        }
        return tree;
    }

    /**
     * Handles the Log Events which affect the structure of the graph.
     * @param gev				The Log event which needs to be handled.
     * @param forward			<code>true</code> if play-back is playing forward.
     * @param referenceGraph	The Graph to get edge numbers from.
     */
    public void graphEvent(TrustLogEvent gev, boolean forward, FeedbackHistoryGraph referenceGraph) {
        int from = gev.getAssessor();
        int to = gev.getAssessee();
        double feedback = gev.getFeedback();
        int key = referenceGraph.fineEdge(from, to).getKey();
        if (forward) {
            feedback(from, to, feedback, key);
        } else {
            unFeedback(from, to, feedback, key);
        }
    }

    /**
     * Limited version of graphEvent for construction a graph for layout purposes
     * @param gev	The Log event which needs to be handled.
     */
    public void graphConstructionEvent(TrustLogEvent gev) {
        feedback(gev.getAssessor(), gev.getAssessee(), gev.getFeedback());
    }
}
