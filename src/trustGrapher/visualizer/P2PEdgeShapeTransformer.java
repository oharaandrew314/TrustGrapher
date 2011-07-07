package trustGrapher.visualizer;

import trustGrapher.graph.TrustConnection;
import trustGrapher.graph.TrustVertex;

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
		Transformer<Context<Graph<TrustVertex, TrustConnection>, TrustConnection>, Shape> {

	private AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> P2PEdgeShape;
	private AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> P2DocEdgeShape;
	private AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> Doc2PDocEdgeShape;
	private AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> P2PDocEdgeShape;
	private final AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> defaultEdgeShape;
	
	/**
	 * 
	 * @param P2PEdgeShape		Peer to Peer Edge Shape.
	 * @param P2DocEdgeShape	Peer to Document Edge Shape.
	 * @param Doc2PDocEdgeShape	Document to PeerDocument Edge Shape.
	 * @param P2PDocEdgeShape	Peer to PeerDocument Edge Shape.
	 */
	public P2PEdgeShapeTransformer(EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape){
		this.P2PEdgeShape = shapeChooser(P2PEdgeShape);
		this.P2DocEdgeShape = shapeChooser(P2DocEdgeShape);
		this.Doc2PDocEdgeShape = shapeChooser(Doc2PDocEdgeShape);
		this.P2PDocEdgeShape = shapeChooser(P2PDocEdgeShape);
		
		defaultEdgeShape = new EdgeShape.Box<TrustVertex, TrustConnection>();
	}
	@Override
	public Shape transform(Context<Graph<TrustVertex, TrustConnection>, TrustConnection> context) {

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
            return P2PEdgeShape.transform(context); //a curve if this is between peers
	}
	
	private AbstractEdgeShapeTransformer<TrustVertex, TrustConnection> shapeChooser(EdgeShapeType chosenShape) {
		switch(chosenShape) {
		
		case BENT_LINE:
			return new EdgeShape.BentLine<TrustVertex, TrustConnection>();
		case BOX:
			return new EdgeShape.Box<TrustVertex, TrustConnection>();
		case CUBIC_CURVE:
			return new EdgeShape.CubicCurve<TrustVertex, TrustConnection>();
		case LINE:
			return new EdgeShape.Line<TrustVertex, TrustConnection>();
		case LOOP:
			return new EdgeShape.Loop<TrustVertex, TrustConnection>();
		case ORTHOGONAL:
			return new EdgeShape.Orthogonal<TrustVertex, TrustConnection>();
		case QUAD_CURVE:
			return new EdgeShape.QuadCurve<TrustVertex, TrustConnection>();
		case SIMPLE_LOOP:
			return new EdgeShape.SimpleLoop<TrustVertex, TrustConnection>();
		case WEDGE:
			return new EdgeShape.Wedge<TrustVertex, TrustConnection>(3);
    		
		}
		return new EdgeShape.Line<TrustVertex, TrustConnection>();
	}

}
