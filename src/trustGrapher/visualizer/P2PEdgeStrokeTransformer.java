package trustGrapher.visualizer;

import trustGrapher.graph.TrustConnection;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

public class P2PEdgeStrokeTransformer implements Transformer<TrustConnection, Stroke> {

    @Override
    public Stroke transform(TrustConnection edge) {
        /** Removed by me
        if(edge.isP2P())
                if (edge.isQuerying())
                        return new BasicStroke(2.5f); //make the stroke twice as wide if there is a query going through this edge
                else
                        return new BasicStroke(1.5f); // if it's a regular connection between peers, make it wide
        else
                return new BasicStroke(0.5f); // if it's to a document, make it narrow
         */
        //Replaced by
        return new BasicStroke(1.5f); // if it's a regular connection between peers, make it wide

    }

}
