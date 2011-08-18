///////////////////////////////SimFeedbackGraph/////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.trustGrapher.graph.edges.SimFeedbackEdge;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryEdgeFactory;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;

import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * A graph that displays individual feedbacks grouped together into edges
 * @author Andrew O'Hara
 */
public class SimFeedbackGraph extends SimGraph {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a feedback graph
     * @param graphPair The graphPair that is to hold this graph
     */
    public SimFeedbackGraph(GraphPair graphPair) {
        super(graphPair, (SimpleDirectedGraph) new FeedbackHistoryGraph(new FeedbackHistoryEdgeFactory()));
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * This String returned by this is the String displayed on the viewer border
     */
    @Override
    public String getDisplayName() {
        return "0-FeedbackHistory";
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * First, ensures that the Agents and edge referenced in the graphEvent exist.
     * Then adds the given feedback value to the edge and the equivalent full edge
     * Even though the dynamic edge is not visible, feedback must be added to it so the algorithms can find it
     * @param graphEvent The current event being processed thaat contains the ids of the assessor and assesse and feedback given
     * @param fullGraph The fullGraph in this GraphPair to add the feedback to
     */
    @Override
    protected void forwardEvent(TrustLogEvent graphEvent, SimGraph fullGraph) {
        Agent src = ensureAgentExists(graphEvent.getAssessor());
        Agent sink = ensureAgentExists(graphEvent.getAssessee());
        ((SimFeedbackEdge) ensureEdgeExists(src, sink, this)).addFeedback(src, sink, graphEvent.getFeedback()); //Ensures that the proper edge exists in the dynamic graph
        //Add the feedback to the full edge so it can be seen in the viewer
        ((SimFeedbackEdge) fullGraph.findEdge(src, sink)).addFeedback(src, sink, graphEvent.getFeedback());
    }

    /**
     * Removes the given feedback from the edge between the given src and sink Agents
     * If the edge no longer has any feedback, it removes the edge from this dynamic graph
     * @param graphEvent The current event being processed that contains the ids of the assessor and assesse and feedback given
     * @param fullGraph The fullGraph in this GraphPair to remove the feedbacks from
     */
    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        Agent src = ensureAgentExists(gev.getAssessor()), sink = ensureAgentExists(gev.getAssessee());
        double feedback = gev.getFeedback();

        SimFeedbackEdge fullEdge = ((SimFeedbackEdge) fullGraph.findEdge(src, sink));
        SimFeedbackEdge dynEdge = ((SimFeedbackEdge) findEdge(src, sink));
        if (fullEdge == null) {
            ChatterBox.error(this, "backwardEvent()", "fullEdge " + src + " " + sink + " wasn't found when it should have existed!");
            return;
        } else if (dynEdge == null) {
            ChatterBox.error(this, "backwardEvent()", "dynEdge " + src + " " + sink + " wasn't found when it should have existed!");
            return;
        }
        fullEdge.removeFeedback(feedback);
        if (fullEdge.feedbacks.isEmpty()) {
            removeEdgeAndVertices(dynEdge); //Remove that edges dynamic edge partner
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param event The current event being processed that contains the ids of the assessor and assesse and feedback given
     */
    @Override
    public void graphConstructionEvent(TrustLogEvent event) {
        if (event != null) {
            Agent src = ensureAgentExists(event.getAssessor());
            Agent sink = ensureAgentExists(event.getAssessee());
            ensureEdgeExists(src, sink, this);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
