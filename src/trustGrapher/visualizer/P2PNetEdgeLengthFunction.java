package trustGrapher.visualizer;

//import edu.uci.ics.jung.algorithms.layout.SpringLayout.LengthFunction;
import org.apache.commons.collections15.Transformer;

import trustGrapher.graph.TrustConnection;

public class P2PNetEdgeLengthFunction implements Transformer<TrustConnection,Integer> {
	
    @Override
    public Integer transform(TrustConnection edge) {
        /** Removed by me
        if( edge.isP2P())
                return new Integer(75); //distance between two peers
        else
                return new Integer(20); //distance between a peer and a document
         */
        //Replaced by
        return new Integer(75); //distance between two peers
    }

}
