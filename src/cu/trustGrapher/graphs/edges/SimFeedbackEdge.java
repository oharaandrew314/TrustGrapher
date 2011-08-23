////////////////////////////////FeedbackHistoryGraphEdge//////////////////////////////////
package cu.trustGrapher.graphs.edges;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.entities.Agent;
import aohara.utilities.ChatterBox;

/**
 * An extension of FeedbackHistoryGraphEdge for use in the TrustGrapher trust simulator
 * This is an edge that represents a list of transactions going from one peer to the other.
 * @author Andrew O'Hara
 */
public class SimFeedbackEdge extends FeedbackHistoryGraphEdge {

    private StringBuffer label; //The String that is displayed in the GraphViewer next to the edge

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a SimFeedbackEdge.
     * @param src The Agent that this edge originates from
     * @param sink The Agent that this edge ends at
     * @throws Exception If the superclass constructor whines for some reason
     */
    public SimFeedbackEdge(Agent src, Agent sink) throws Exception {
        super(src, sink);
        label = new StringBuffer();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Returns a string representation of this edge.  This string is displayed by the edge in the TrustGraphViewer.
     * This String contains all of the feedback values that this edge has.
     * If the stringBuffer has already been built, returns a string of it instead.
     * @return A string representation of this edge
     */
    @Override
    public String toString() {
        if (label.length() == 0 && !feedbacks.isEmpty()) {
            label.append(feedbacks.get(0).value);
            for (int i = 1; i < feedbacks.size(); i++) {
                label.append(", ");
                label.append(feedbacks.get(i).value);
            }
        }
        return label.toString();
    }
///////////////////////////////////Methods//////////////////////////////////////

    /**
     * Adds feedback to this edge's list of feedbacks.  Resets the label StringBuffer.
     * @param assessor The agent that gave the feedback
     * @param assessee The agent that is receiving the feedback
     * @param feedback The value of the feedback
     */
    public void addFeedback(Agent assessor, Agent assessee, double feedback) {
        try {
            super.addFeedback(new Feedback(assessor, assessee, feedback));
            label = new StringBuffer();
        } catch (Exception ex) {
            ChatterBox.error(this, "addFeedback()", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Removes a feedback with the specified parameters form the edge.
     * Resets the label StringBuffer.
     * @param feedback The value of the feedback to remove
     */
    public void removeFeedback(double feedback) {
        for (Feedback fb : feedbacks) {
            if (fb.value == feedback) {
                feedbacks.remove(fb);
                label = new StringBuffer();
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimFeedbackEdge) {
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

