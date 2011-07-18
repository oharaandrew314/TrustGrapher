////////////////////////////////MyTrustGraph//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.algorithms.MyRankbasedTrust;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * Description
 * @author Andrew O'Hara
 */
public class MyTrustGraph extends MyGraph{
    private MyTrustGraph fullGraph = null;
    private MyRankbasedTrust alg;

//////////////////////////////////Constructor///////////////////////////////////
    public MyTrustGraph(int type){
        super((SimpleDirectedGraph)new TrustGraph(new TrustEdgeFactory()), type);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public MyTrustGraph getFullGraph(){
        if (type != DYNAMIC){
            ChatterBox.error(this, "getFullGraph()", "This is not a dynamic graph.  This method cannot be called.");
        }
        return fullGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public void graphEvent(TrustLogEvent gev, boolean forward, MyGraph referenceGraph) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void graphConstructionEvent(TrustLogEvent gev) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
////////////////////////////////////////////////////////////////////////////////