////////////////////////////////FeedbackHistoryGraphEdge//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.data.Feedback;
import utilities.ChatterBox;

/**
 * A wrapper for the FeedbackHistoryGraphEdge class for use in the TrustGrapher trust simulator
 * This is an edge that represents a list of transactions going from one peer to the other.
 * @author Andrew O'Hara
 */
public class FeedbackEdge extends FeedbackHistoryGraphEdge {
    private int key;

//////////////////////////////////Constructor///////////////////////////////////

    public FeedbackEdge(Integer key, AgentWrapper assessor, AgentWrapper assessee) throws Exception {
        super(assessor, assessee);
        this.key = key;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getKey() {
        return key;
    }

    public boolean hasMultipleFeedback() {
        return super.feedbacks.size() > 1;
    }

    @Override
    public String toString(){
        String s = "" + feedbacks.get(0).value;
        for (int i=1 ; i<feedbacks.size() ; i++){
            s = s + ", " + feedbacks.get(i).value;
        }
        return s;
    }

    public AgentWrapper getAssessor(){
        return (AgentWrapper) src;
    }

    public AgentWrapper getAssessee(){
        return (AgentWrapper) sink;
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void addFeedback(AgentWrapper assessor, AgentWrapper assessee, double feedback) {
        try {
            super.addFeedback(new Feedback(assessor, assessee, feedback));
        } catch (Exception ex) {
            ChatterBox.error(this, "addFeedback()", ex.getMessage());
        }
    }

    public void removeFeedback(double feedback) {
        for (int i = 0; i < feedbacks.size(); i++) {
            if (feedbacks.get(i).value == feedback) {
                feedbacks.remove(i);
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof FeedbackHistoryGraphEdge == false){
            return false;
        }
        FeedbackEdge other = (FeedbackEdge) o;
        return this.key == other.key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.key;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////
