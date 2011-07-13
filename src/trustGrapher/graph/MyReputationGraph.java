////////////////////////////////MyReputationGraph///////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;
import java.util.Collection;
import org.jgrapht.graph.SimpleDirectedGraph;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;
import utilities.ChatterBox;

/**
 * A  graph that displays each agent's trust towards other agents that it's had transactions with.
 * @author Andrew O'Hara
 */
public class MyReputationGraph extends TrustGraph {
    private ReputationAlgorithm alg = null;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a hidden graph.  The components of this graph are actually the ones beign displayed.
     */
    public MyReputationGraph() {
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
        type = HIDDEN;
    }

    /**
     * Creates a visible graph.  This graph is only used by the hidden graph to see which of it's own components should be displayed.
     * @param hiddenGraph A reference to the hiddenGraph so that the reputation can be changed
     * @param alg The Reputation algorithm that will calculate the reputation towards each agent
     */
    public MyReputationGraph(ReputationAlgorithm alg) {
        super((SimpleDirectedGraph) new ReputationGraph(new ReputationEdgeFactory()));
        this.alg = alg;
        type = VISIBLE;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public ReputationAlgorithm getAlg() {
        return alg;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public void addition(int from, int to, double feedback, int key) {
        if (getVertexInGraph(from) == null) {
            addPeer(from);
        }
        if (getVertexInGraph(to) == null) {
            addPeer(to);
        }
        Agent assessor = getVertexInGraph(from);
        Agent assessee = getVertexInGraph(to);
        MyReputationEdge edge = (MyReputationEdge) findEdge(from, to);
        if (edge == null) {
            edge = new MyReputationEdge(assessor, assessee, key);
            this.addEdge(edge, assessor, assessee);
        }
        edge.setReputation(feedback);
    }

    public void subtraction(int from, int to, double feedback, int key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void graphEvent(TrustLogEvent gev, boolean forward, TrustGraph referenceGraph) throws Exception {
        if (type == HIDDEN){
            ChatterBox.error(this, "graphEvent()", "This graph is not a visible graph.");
            return;
        }
        try {
            Collection<Agent> agents = referenceGraph.getVertices();
            for (Agent src : agents) {
                for (Agent sink : agents) {
                    if (!src.equals(sink)) {
                        //MyReputationEdge hiddenEdge = (MyReputationEdge) hiddenGraph.findEdge(src, sink);
                        double trustScore = getAlg().calculateTrustScore(src, sink);
                        if (trustScore < 0.0 || trustScore > 1.0) {
                            throw new Exception("The trustScore was " + trustScore + ".  It needs to be [0,1]");
                        }
                        int key = ((MyFeedbackEdge) referenceGraph.findEdge(src, sink)).getKey();
                        addition(src.id, sink.id, trustScore, key);
                    }
                }
            }
        } catch (Exception ex) {
            ChatterBox.debug(this, "graphEvent()", ex.getMessage());
        }
    }

    /**
     * Creates an edge but does not yet add the feedback to it.  As the visible edges, are added, the feedbacks will be added to the hidden edges
     * @param gev	The Log event which needs to be handled.
     */
    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type == VISIBLE){
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a hidden graph.");
            return;
        }
        int from = gev.getAssessor();
        int to = gev.getAssessee();
        if (getVertexInGraph(from) == null) {
            addPeer(from);
        }
        if (getVertexInGraph(to) == null) {
            addPeer(to);
        }
        Agent assessor = getVertexInGraph(from);
        Agent assessee = getVertexInGraph(to);
        MyReputationEdge edge = (MyReputationEdge) findEdge(from, to);
        if (edge == null) {//If the edge doesn't  exist, add it
            try {
                edge = new MyReputationEdge(assessor, assessee, edgecounter++);
                addEdge(edge, edge.getAssessor(), edge.getAssessee());
            } catch (Exception ex) {
                ChatterBox.error(this, "feedback()", "Error creating edge: " + ex.getMessage());
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

