///////////////////////////////SimFeedbackGraph/////////////////////////////
package cu.trustGrapher.graph;

import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;

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
     * Creates a generic feedback graph of either dynamic or full type
     * @param type The graph type (full or dynamic)
     * @param display Whether or not this graph will have a viewer built for it.  This is only necessary for full graphs
     */
    public SimFeedbackGraph(int type, boolean display) {
        super((SimpleDirectedGraph) new FeedbackHistoryGraph(new FeedbackHistoryEdgeFactory()), type, 0);
        this.display = display;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * This String returned by this is the String displayed on the viewer border
     */
    @Override
    public String getDisplayName(){
        return "0-FeedbackHistory";
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Creates a new dynamic edge if the one with the given graphID doesn't exist
     * Then adds the given feedback value to it and the equivalent full edge
     *
     * Warning: For efficiency, this assumes that the agents area already ensured to exist in the graph by the graphEvent method
     * @param fullGraph This graph's fullGraph partner.  Needed to add the feedback since the equivalent edge in that graph is the one actually being displayed in the viewer
     * @param src The graphID of the Agent that gave the feedback
     * @param sink The graphID of the Agent that is recieving the feedback
     * @param feedback The double value of the feedback being given
     * @param graphID The graphID of the edge to add feedback to
     */
    @Override
    protected void forwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        Agent src = ensureAgentExists(gev.getAssessor());
        Agent sink = ensureAgentExists(gev.getAssessee());            
        ensureEdgeExists(src, sink, this); //Ensures that the proper edge exists in the dynamic graph
        //Add the feedback to the full edge so it can be seen in the viewer
        ((SimFeedbackEdge) fullGraph.findEdge(src, sink)).addFeedback(src, sink, gev.getFeedback());
    }

    /**
     * Removes the given feedback from the edge between the given src and sink Agents
     * If the edge no longer has any feedback, it removes the edge from the dynamic graph
     * 
     * Note: The edge must stay in the full graph since it is needed if the user chooses to play forward again
     * Warning: For efficiency, this assumes that the agents area already ensured to exist in the graph by the graphEvent method
     * @param fullGraph
     * @param src
     * @param sink
     * @param feedback
     */
    @Override
    protected void backwardEvent(TrustLogEvent gev, SimGraph fullGraph) {
        Agent src = ensureAgentExists(gev.getAssessor()), sink = ensureAgentExists(gev.getAssessee());
        double feedback = gev.getFeedback();
        
        SimFeedbackEdge fullEdge = ((SimFeedbackEdge) fullGraph.findEdge(src, sink));
        if (fullEdge == null) {
            ChatterBox.error(this, "backwardEvent()", "fullEdge " + src + " " + sink + " wasn't found when it should have existed!");
            return;
        }
        fullEdge.removeFeedback(feedback);
        if (fullEdge.feedbacks.isEmpty()) {
            removeEdgeAndVertices(findEdge(src, sink));
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param event	The Log event which needs to be handled.
     */
    @Override
    public void graphConstructionEvent(TrustLogEvent event) {
        if (type != FULL) {
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a full graph.  Illegal method call");
            return;
        }
        if (event.getAssessor() != -1){
            Agent src = ensureAgentExists(event.getAssessor());
            Agent sink = ensureAgentExists(event.getAssessee());
            ensureEdgeExists(src, sink, this);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
