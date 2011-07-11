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
public class P2PEdgeShapeTransformer
		implements
		Transformer<Context<Graph<Agent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge>, Shape> {

//	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> P2PEdgeShape;
//	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> P2DocEdgeShape;
//	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> Doc2PDocEdgeShape;
//	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> P2PDocEdgeShape;
	private final AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> defaultEdgeShape;
	
	/**
	 * 
	 * @param P2PEdgeShape		Peer to Peer Edge Shape.
	 * @param P2DocEdgeShape	Peer to Document Edge Shape.
	 * @param Doc2PDocEdgeShape	Document to PeerDocument Edge Shape.
	 * @param P2PDocEdgeShape	Peer to PeerDocument Edge Shape.
	 */
	public P2PEdgeShapeTransformer(){
//		this.P2PEdgeShape = shapeChooser(P2PEdgeShape);
//		this.P2DocEdgeShape = shapeChooser(P2DocEdgeShape);
//		this.Doc2PDocEdgeShape = shapeChooser(Doc2PDocEdgeShape);
//		this.P2PDocEdgeShape = shapeChooser(P2PDocEdgeShape);		
		defaultEdgeShape = new EdgeShape.QuadCurve<Agent, FeedbackHistoryGraphEdge>();
	}
	@Override
	public Shape transform(Context<Graph<Agent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge> context) {

            /** Removed by me
            if (context.element.isP2P()) {
                    return P2PEdgeShape.transform(context); //a curve if this is between peers
            }
            else if (context.element.isP2DOC()){
                    return P2DocEdgeShape.transform(context); // between peer and doc, a straight line
            }
            else if (context.element.isDOC2PDOC()){

                    return Doc2PDocEdgeShape.transform(context); // between peer and doc, a straight line
            }
            else if (context.element.isP2PDOC()){

                    return P2PDocEdgeShape.transform(context); // between peer and doc, a straight line
            }
            return defaultEdgeShape.transform(context);
             */
            //Replaced by

            return defaultEdgeShape.transform(context);
	}
	
	private AbstractEdgeShapeTransformer<Agent, FeedbackHistoryGraphEdge> shapeChooser(EdgeShapeType chosenShape) {
            return new EdgeShape.QuadCurve<Agent, FeedbackHistoryGraphEdge>();
//		switch(chosenShape) {
//
//		case BENT_LINE:
//			return new EdgeShape.BentLine<Agent, FeedbackHistoryGraphEdge>();
//		case BOX:
//			return new EdgeShape.Box<Agent, FeedbackHistoryGraphEdge>();
//		case CUBIC_CURVE:
//			return new EdgeShape.CubicCurve<Agent, FeedbackHistoryGraphEdge>();
//		case LINE:
//			return new EdgeShape.Line<Agent, FeedbackHistoryGraphEdge>();
//		case LOOP:
//			return new EdgeShape.Loop<Agent, FeedbackHistoryGraphEdge>();
//		case ORTHOGONAL:
//			return new EdgeShape.Orthogonal<Agent, FeedbackHistoryGraphEdge>();
//		case QUAD_CURVE:
//			return new EdgeShape.QuadCurve<Agent, FeedbackHistoryGraphEdge>();
//		case SIMPLE_LOOP:
//			return new EdgeShape.SimpleLoop<Agent, FeedbackHistoryGraphEdge>();
//		case WEDGE:
//			return new EdgeShape.Wedge<Agent, FeedbackHistoryGraphEdge>(3);
//
//		}
//		return new EdgeShape.Line<Agent, FeedbackHistoryGraphEdge>();
	}

}
