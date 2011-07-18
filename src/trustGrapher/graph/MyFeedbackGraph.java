///////////////////////////////MyFeedbackGraph/////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryEdgeFactory;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.TestbedEdge;
import java.util.Collection;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * A trust graph that displays individual feedbacks grouped together into edges
 * @author Andrew O'Hara
 */
public class MyFeedbackGraph extends MyGraph {

//////////////////////////////////Constructor///////////////////////////////////
    public MyFeedbackGraph(int type) {
        super((SimpleDirectedGraph) new FeedbackHistoryGraph(new FeedbackHistoryEdgeFactory()), type);
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void feedback(MyFeedbackGraph hiddenGraph, int from, int to, double feedback, int key) {
        if (type == FULL) {
            ChatterBox.error(this, "feedback()", "This graph is not a visible graph");
            return;
        }
        if (getVertexInGraph(from) == null) {
            addPeer(from);
        }
        if (getVertexInGraph(to) == null) {
            addPeer(to);
        }
        Agent assessor = getVertexInGraph(from);
        Agent assessee = getVertexInGraph(to);
        MyFeedbackEdge edge = (MyFeedbackEdge) findEdge(from, to);
        if (edge == null) {//If the edge doesn't  exist, add it
            try {
                edge = new MyFeedbackEdge(key, assessor, assessee);
                addEdge(edge, (Agent) edge.src, (Agent) edge.sink);
            } catch (Exception ex) {
                ChatterBox.error(this, "feedback()", "Error creating edge: " + ex.getMessage());
            }
        }

        MyFeedbackEdge fullEdge = ((MyFeedbackEdge) hiddenGraph.findEdge(assessor, assessee));
        edge.addFeedback(assessor, assessee, feedback);
        fullEdge.addFeedback(assessor, assessee, feedback);
    }

    public void unfeedback(MyFeedbackGraph fullGraph, int from, int to, double feedback, int key) {
        if (type == FULL) {
            ChatterBox.error(this, "unfeedback()", "This graph is not a visible graph");
            return;
        }
        MyFeedbackEdge edge = (MyFeedbackEdge) findEdge(from, to);
        if (edge == null) {
            ChatterBox.alert("Edge " + from + " " + to + " feedback: " + feedback);
            ChatterBox.error(this, "unFeedback()", "Couldn't find an edge to remove!");
            return;
        }
        MyFeedbackEdge fullEdge = ((MyFeedbackEdge) fullGraph.findEdge(from, to));
        fullEdge.removeFeedback(feedback);
        if (fullEdge.feedbacks.isEmpty()) {
            Collection<Agent> verts = super.getIncidentVertices(edge);
            super.removeEdge(edge);
            for (Agent v : verts) {
                if (super.getIncidentEdges(v).isEmpty()) {
                    super.removeVertex(v);
                }
            }
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param gev	The Log event which needs to be handled.
     */
    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type == DYNAMIC) {
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
        MyFeedbackEdge edge = (MyFeedbackEdge) findEdge(from, to);
        if (edge == null) {//If the edge doesn't  exist, add it
            try {
                edge = new MyFeedbackEdge(edgecounter++, assessor, assessee);
                addEdge(edge, (Agent)edge.src, (Agent)edge.sink);
            } catch (Exception ex) {
                ChatterBox.error(this, "feedback()", "Error creating edge: " + ex.getMessage());
            }
        }
    }

    public void graphEvent(TrustLogEvent gev, boolean forward, MyGraph referenceGraph) {
        int from = gev.getAssessor();
        int to = gev.getAssessee();
        double feedback = gev.getFeedback();
        int key = ((MyFeedbackEdge) referenceGraph.findEdge(from, to)).getID();
        if (forward) {
            feedback((MyFeedbackGraph) referenceGraph, from, to, feedback, key);
        } else {
            unfeedback((MyFeedbackGraph) referenceGraph, from, to, feedback, key);
        }
    }

    public void printGraph() {
        if (type == FULL) {
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
            ChatterBox.print("Edge: " + e.src + " to " + e.sink);

        }
        ChatterBox.print("Done.");
    }
}
////////////////////////////////////////////////////////////////////////////////
