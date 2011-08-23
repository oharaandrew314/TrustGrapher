///////////////////////////////SimFeedbackGraph/////////////////////////////
package cu.trustGrapher.graphs;

import cu.trustGrapher.eventplayer.TrustLogEvent;
import cu.trustGrapher.graphs.edges.SimFeedbackEdge;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryEdgeFactory;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.trustGrapher.loading.GraphConfig;

import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * A graph that displays individual feedbacks grouped together into edges.
 * There is only ever one SimfeedbackGraph in a simulation.
 * @author Andrew O'Hara
 */
public class SimFeedbackGraph extends SimAbstractGraph {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a feedback graph
     * @param graphConfig This object contains all of the configurations for this graph
     */
    public SimFeedbackGraph(GraphConfig graphConfig) {
        super(graphConfig, (SimpleDirectedGraph) new FeedbackHistoryGraph(new FeedbackHistoryEdgeFactory()));
    }
    
///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  It is assumed that this graph is a dynamic graph.
     * This method handles the addition or subtraction of edges and agents from the dynamic graph, and edge labels for both
     * based on the event that is currently being processed.
     * @param event The TrustLogEvent that is being processed
     * @param forward Whether or not the graph is being played forward
     */
    @Override
    public void graphEvent(TrustLogEvent event, boolean forward) {
        Agent src = ensureAgentExists(event.getAssessor(), this), sink = ensureAgentExists(event.getAssessee(), this);
        SimFeedbackEdge dynEdge = (SimFeedbackEdge) ensureEdgeExists(src, sink, this);
        SimFeedbackEdge refEdge = (SimFeedbackEdge) referenceGraph.findEdge(src, sink);
        double feedback = event.getFeedback();
        if (forward) {
            //Add the feedback to the full edge so it can be seen in the viewer            
            dynEdge.addFeedback(src, sink, feedback);
            refEdge.addFeedback(src, sink, feedback);
        } else {
            dynEdge.removeFeedback(feedback);
            refEdge.removeFeedback(feedback);
            if (dynEdge.feedbacks.isEmpty()){
                removeEdgeAndVertices(dynEdge); //Remove the dynamic edge
            }
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param event The current event being processed that contains the ids of the assessor and assesse and feedback given
     */
    @Override
    public void graphConstructionEvent(TrustLogEvent event) {
        if (event != null) {
            Agent src = ensureAgentExists(event.getAssessor(), referenceGraph);
            Agent sink = ensureAgentExists(event.getAssessee(), referenceGraph);
            ensureEdgeExists(src, sink, referenceGraph);
        }
       
    }
}
////////////////////////////////////////////////////////////////////////////////

