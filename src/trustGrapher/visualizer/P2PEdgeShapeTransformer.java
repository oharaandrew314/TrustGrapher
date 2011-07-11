package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;

/**
 * An Edge Shape Transformer class that simply encapsulates the generic QuadCurve and Line transformers.
 * This transformer is used to set the shape of an edge in a graph rendering.
 * For a P2PNetwork, connections between peers will be rendered as curves (quadcurves) and connections between 
 * peers and the docs that the peer publishes are rendered as straight lines. Note that the stroke is 
 * determined by another rendering class, and so is the length.
 * @author alan
 *
 */
public class P2PEdgeShapeTransformer implements
	Transformer<Context<Graph<Agent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge>, Shape> {
	private final AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> defaultEdgeShape;
	
	/**
	 * 
	 * @param P2PEdgeShape		Peer to Peer Edge Shape.
	 * @param P2DocEdgeShape	Peer to Document Edge Shape.
	 * @param Doc2PDocEdgeShape	Document to PeerDocument Edge Shape.
	 * @param P2PDocEdgeShape	Peer to PeerDocument Edge Shape.
	 */
	public P2PEdgeShapeTransformer(){		
		defaultEdgeShape = new EdgeShape.QuadCurve<Agent, FeedbackHistoryGraphEdge>();
	}
	@Override
	public Shape transform(Context<Graph<Agent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge> context) {
            return defaultEdgeShape.transform(context);
	}
	
	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> shapeChooser(EdgeShapeType chosenShape) {
            return new EdgeShape.QuadCurve<Agent, FeedbackHistoryGraphEdge>();
	}

}
