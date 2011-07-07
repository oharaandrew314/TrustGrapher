package trustGrapher.visualizer;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Transformer;

import trustGrapher.graph.TrustConnection;
import trustGrapher.graph.TrustVertex;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * this vertex placer is used in the spring layout to initialize the position of a new node in the graph when calculating the layout.
 * Peers should start in random positions, but docs should start next to (here at the exact same location as) the peer that publishes the doc 
 * @author alan
 *
 */
public class P2PVertexPlacer implements Transformer<TrustVertex, Point2D> {

    private Layout<TrustVertex, TrustConnection> existinglayout;
    private RandomLocationTransformer<TrustVertex> rt;

    public P2PVertexPlacer(Layout<TrustVertex, TrustConnection> l, Dimension d) {
        existinglayout = l;
        rt = new RandomLocationTransformer<TrustVertex>(d);
    }

    @Override
    public Point2D transform(TrustVertex v) {
        return rt.transform(v); //placing a peer
    }
}
