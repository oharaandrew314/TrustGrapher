////////////////////////////////ReputationGraph//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

/**
 * Description
 * @author Andrew O'Hara
 */
public class ReputationGraph extends TrustGraph{
    ReputationAlgorithm algorithm;

//////////////////////////////////Constructor///////////////////////////////////
    public ReputationGraph(ReputationAlgorithm algorithm){
        this.algorithm = algorithm;
    }

//////////////////////////////////Accessors/////////////////////////////////////

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public void graphConstructionEvent(TrustLogEvent gev) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void graphEvent(TrustLogEvent gev, boolean forward, TrustGraph referenceGraph) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


////////////////////////////////Static Methods//////////////////////////////////

}
////////////////////////////////////////////////////////////////////////////////