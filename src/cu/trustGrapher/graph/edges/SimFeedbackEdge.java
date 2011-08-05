////////////////////////////////FeedbackHistoryGraphEdge//////////////////////////////////
package cu.trustGrapher.graph.edges;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.entities.Agent;
import utilities.ChatterBox;

/**
 * An extension of FeedbackHistoryGraphEdge for use in the TrustGrapher trust simulator
 * This is an edge that represents a list of transactions going from one peer to the other.
 * @author Andrew O'Hara
 */
public class SimFeedbackEdge extends FeedbackHistoryGraphEdge {

//////////////////////////////////Constructor///////////////////////////////////

    /**
     * Creates a SimFeedbackEdge
     * @param src The Agent that this edge originates from
     * @param sink The Agent that this edge ends at
     * @throws Exception If the superclass constructor cries, because it's stupid since I didn't write it
     */
    public SimFeedbackEdge(Agent src, Agent sink) throws Exception{
        super(src, sink);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Returns a string representation of this edge.  This string is displayed by the edge in the TrustGraphViewer.
     * This String contains all of the feedback values that this edge has
     * @return A string representation of this edge
     */
    @Override
    public String toString(){
        String s = "";
        if (!feedbacks.isEmpty()){
            s = s + "" + feedbacks.get(0).value;
            for (int i=1 ; i<feedbacks.size() ; i++){
                s = s + ", " + feedbacks.get(i).value;
            }
        }
        return s;
    }
///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds feedback to this edge's list of feedbacks
     * @param assessor The agent that gave the feedback
     * @param assessee The agent that is receiving the feedback
     * @param feedback The value of the feedback
     */
    public void addFeedback(Agent assessor, Agent assessee, double feedback) {
        try {
            super.addFeedback(new Feedback(assessor, assessee, feedback));
        } catch (Exception ex) {
            ChatterBox.error(this, "addFeedback()", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Removes a feedback with the specified parameters form the edge
     * @param feedback The value of the feedback to remove
     */
    public void removeFeedback(double feedback) {
        for (int i = 0; i < feedbacks.size(); i++) {
            if (feedbacks.get(i).value == feedback) {
                feedbacks.remove(i);
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof SimFeedbackEdge){
            SimFeedbackEdge other = (SimFeedbackEdge) o;
            return (src.equals(other.src)) && (sink.equals(other.sink));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////