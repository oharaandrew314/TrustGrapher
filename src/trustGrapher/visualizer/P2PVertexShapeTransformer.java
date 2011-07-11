package trustGrapher.visualizer;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import trustGrapher.graph.MyAgent;

import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import utilities.ChatterBox;

/**
 * A "transformer" that maps vertices to shapes.
 * 
 * There will be two types of vertices, peers and documents.
 * Peers are large and round (size PEER_SIZE = 15), documents are smaller (size DOC_SIZE=6) and rectangular.
 * Peers have numbers less than DOC_MIN. Documents have numbers above DOC_MIN.
 * 
 * This class also uses P2PVertexSizeFunction to assign sizes to the vertices.
 * 
 * @author adavoust
 *
 * @param <V>
 */
public class P2PVertexShapeTransformer extends AbstractVertexShapeTransformer<MyAgent>
        implements Transformer<MyAgent, Shape> {

    public static final int PEER_SIZE = 25;
    private VertexShapeType peerShape;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public P2PVertexShapeTransformer(VertexShapeType peerShape) {
        super(new P2PVertexSizeFunction(PEER_SIZE), new ConstantTransformer(1.0f));
        this.peerShape = peerShape;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public P2PVertexShapeTransformer(VertexShapeType peerShape, int peerSize) {
        super(new P2PVertexSizeFunction(peerSize), new ConstantTransformer(1.0f));
        this.peerShape = peerShape;
    }

    public Shape transform(MyAgent v) {
        return shapeChooser(v, peerShape);
    }

    private Shape shapeChooser(MyAgent v, VertexShapeType chosenShape) {
        switch (chosenShape) {
            case ELLIPSE:
                return factory.getEllipse(v);
        }
        ChatterBox.debug(this, "shapeChooser()", "Tried to select a shape for an unsupported vertex.");
        return factory.getRectangle(v);
    }
}
