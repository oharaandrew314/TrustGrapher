////////////////////////////////FeedbackHistoryGraphEdge//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.entities.Agent;
import utilities.ChatterBox;

/**
 * A wrapper for the FeedbackHistoryGraphEdge class for use in the TrustGrapher trust simulator
 * This is an edge that represents a list of transactions going from one peer to the other.
 * @author Andrew O'Hara
 */
public class MyFeedbackEdge extends FeedbackHistoryGraphEdge {
    private int id;

//////////////////////////////////Constructor///////////////////////////////////

    public MyFeedbackEdge(int key, Agent assessor, Agent assessee) throws Exception {
        super(assessor, assessee);
        this.id = key;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getID() {
        return id;
    }

    public boolean hasMultipleFeedback() {
        return super.feedbacks.size() > 1;
    }

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
    public void addFeedback(Agent assessor, Agent assessee, double feedback) {
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
                break;
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof FeedbackHistoryGraphEdge == false){
            return false;
        }
        MyFeedbackEdge other = (MyFeedbackEdge) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.id;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////