package trustGrapher.visualizer;

import org.apache.commons.collections15.Transformer;

import trustGrapher.graph.MyAgent;

/**
 * Size function for the vertices in a graph representing a P2P network.
 * The size of the shapes representing the nodes are given in the constructor.
 * This class has three attributes, that indicate the size of vertices representing peers, 
 * the size of vertices representing documents stored by the peers, and a parameter that indicates 
 * the limit between integers (vertex labels) representing documents and integers representing peers   
 * @author adavoust
 *
 * 
 */
public class P2PVertexSizeFunction implements Transformer<MyAgent, Integer> {
    int my_peer_size;

    /**
     * Constructor
     * @param dmin min value for document vertex labels (below = peers)
     * @param ds document vertex size
     * @param ps peer document size
     */
    public P2PVertexSizeFunction(int peerSize) {
        my_peer_size = peerSize;
    }

    @Override
    public Integer transform(MyAgent vertexID) {
        return new Integer(my_peer_size);
    }
}
