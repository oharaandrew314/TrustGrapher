////////////////////////////////MyEigenTrust//////////////////////////////////
package trustGrapher.algorithms;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationGraph;
import java.util.Set;
import trustGrapher.graph.MyReputationEdge;
import trustGrapher.graph.MyReputationGraph;

/**
 * An extension of EigenTrust to make it easier to do what I want it to!
 * @author Andrew O'Hara
 */
public class MyEigenTrust extends EigenTrust {

    private MyReputationGraph myRepGraph;
    public static final int VISIBLE = 0, HIDDEN = 1;

//////////////////////////////////Constructor///////////////////////////////////
    public MyEigenTrust(int iterations, double threshold2Satisfy) {
        super(iterations, threshold2Satisfy);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    @Override
    public ReputationGraph getReputationGraph() {
        return this.reputationGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void setMyReputationGraph(MyReputationGraph graph) {
        this.myRepGraph = graph;
    }

    public void setMatrixFilled(boolean filled){
        this.matrixFilled = filled;
    }

    @Override
    public void update() {
        if (myRepGraph.getType() == VISIBLE){
            System.out.println("Updating reputation graph...");
        }
        matrixFilled = false;
        setIterations(getIterations() + 1);

        Set<Agent> agents = feedbackHistoryGraph.vertexSet();
        for (Agent src : agents) {
            for (Agent sink : agents) {
                if (!src.equals(sink)) {
                    double trustScore = calculateTrustScore(src, sink);
                    if (myRepGraph.getType() == VISIBLE){
                        System.out.println("Setting the edge from " + src + " to " + sink + " to " + trustScore);
                    }
                    //ensure the trust score is in [0,1].
                    if (trustScore < 0.0 || trustScore > 1.0) {
                        System.out.println("Algorithm returned a trust score ("
                                + trustScore + ")that is not within [0,1]. ");
                    }

                    if (myRepGraph.getType() == VISIBLE){
                        removeEntities();
                    }
                    
                    MyReputationEdge repEdge = (MyReputationEdge) myRepGraph.findEdge(src, sink);
                    if (repEdge == null) {
                        if (myRepGraph.getType() == HIDDEN) {
                            repEdge = myRepGraph.createEdge(src, sink);
                            myRepGraph.addEdge(repEdge, src, sink);
                        } else {
                            int key = ((MyReputationEdge) myRepGraph.getHiddenGraph().findEdge(src, sink)).getID();
                            repEdge = myRepGraph.createEdge(src, sink, key);
                            myRepGraph.addEdge(repEdge, src, sink);
                        }
                    }
                    repEdge.setReputation(trustScore);
                }
            }
        }
        if (myRepGraph.getType() == VISIBLE){
            System.out.println("Done.");
        }
    }

    public void removeEntities(){
//        Collection<TestbedEdge> repEdges = myRepGraph.getEdges();
//        Set<FeedbackHistoryGraphEdge> feedbackEdges = feedbackHistoryGraph.edgeSet();
//        for (TestbedEdge e : repEdges){
//            for (FeedbackHistoryGraphEdge e2: feedbackEdges){
//                if ((e.src == e2.src) && (e.sink == e2.sink)){
//                    myRepGraph.removeEdge(e);
//                    break;
//                }
//            }
//        }

//        Collection<Agent> repAgents = myRepGraph.getVertices();
//        Set<Agent> feedbackAgents = feedbackHistoryGraph.vertexSet();
//        for (Agent a: repAgents){
//            if (!feedbackAgents.contains(a)){
//                System.out.println("Removing " + a);
//                myRepGraph.removeVertex(a);
//            }
//        }
        
    }
}
////////////////////////////////////////////////////////////////////////////////
