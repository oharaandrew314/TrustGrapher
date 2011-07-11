///////////////////////////////FeedbackHistoryGraph/////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import java.util.Collection;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

public class FeedbackHistoryGraph extends TrustGraph{
    int edgecounter = 0;

///////////////////////////////////Methods//////////////////////////////////////

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
////////////////////////////////////////////////////////////////////////////////