package cu.trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import cu.trustGrapher.graph.SimGraph;
import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * this vertex placer is used in the spring layout to initialize the position of a new node in the graph when calculating the layout.
 * Peers should start in random positions, but docs should start next to (here at the exact same location as) the peer that publishes the doc 
 * @author alan
 *
 */
public class P2PVertexPlacer implements Transformer<Agent, Point2D> {

    private Layout<Agent, TestbedEdge> existinglayout;
    private RandomLocationTransformer<Agent> rt;

    public P2PVertexPlacer(Layout<Agent, TestbedEdge> l, Dimension d) {
        existinglayout = l;
        rt = new RandomLocationTransformer<Agent>(d);
    }

//    public P2PVertexPlacer(AbstractLayout<Agent, TestbedEdge> l, Dimension d) {
//        existinglayout = l;
//        rt = new RandomLocationTransformer<Agent>(d);
//    }

    @Override
    public Point2D transform(Agent v) {
        return rt.transform(v); //placing a peer
    }
}
