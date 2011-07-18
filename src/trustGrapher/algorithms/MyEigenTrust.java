////////////////////////////////MyEigenTrust//////////////////////////////////
package trustGrapher.algorithms;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;

/**
 * An extension of EigenTrust to make it easier to do what I want it to!
 * @author Andrew O'Hara
 */
public class MyEigenTrust extends EigenTrust {

//////////////////////////////////Constructor///////////////////////////////////
    public MyEigenTrust(int iterations, double threshold2Satisfy) {
        super(iterations, threshold2Satisfy);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    @Override
    public ReputationGraph getReputationGraph() {
        return this.reputationGraph;
    }

    public FeedbackHistoryGraph getFeedbackGraph(){
        return this.feedbackHistoryGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public void setMatrixFilled(boolean filled){
        this.matrixFilled = filled;
    }
}
////////////////////////////////////////////////////////////////////////////////
