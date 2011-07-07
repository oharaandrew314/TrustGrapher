////////////////////////////////////TrustGraph//////////////////////////////////
package trustGrapher.graph;

import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import java.util.Collection;

import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import utilities.ChatterBox;

public class TrustGraph extends DirectedSparseMultigraph<TrustVertex, TrustConnection> implements Graph<TrustVertex, TrustConnection> {

    private static final long serialVersionUID = 1L;
    public static final int FEEDBACK_HISTORY = 0, REPUTATION = 1, TRUST = 2;
    private int type;
    int edgecounter = 0;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustGraph(int type) {
        super();
        this.type = type;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public TrustConnection findConnection(int from, int to) {
        return findEdge(getVertexInGraph(from), getVertexInGraph(to));
    }

    public TrustVertex getVertexInGraph(int peerNum){
        return getVertexInGraph(new TrustVertex(peerNum));
    }
    
     /**
     * this methods gets a vertex already in the graph that is equal to the input vertex
     * to be used when adding edges; the edge should relate two vertices actually in the graph, not copies of these vertices.
     * @param input a TrustVertex object
     * @return a TrustVertex v such that v.equals(input) and v is in the graph
     */
    public TrustVertex getVertexInGraph(TrustVertex input) {
        for (TrustVertex v : vertices.keySet()) {
            if (v.equals(input)) {
                return v;
            }
        }
        return null;
    }

    public TrustVertex getPeer(int peerNumber) {
        return (TrustVertex) getVertexInGraph(new TrustVertex(peerNumber));
    }

    //override these methods so the underlying collection is not unmodifiable
    @Override
    public Collection<TrustConnection> getEdges() {
        return edges.keySet();
    }

    @Override
    public Collection<TrustVertex> getVertices() {
        return vertices.keySet();
    }

    public int getType(){
        return type;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /** adding a peer in the network*/
    public void addPeer(int peernumber) {
        TrustVertex v1 = new TrustVertex(peernumber);
        super.addVertex(v1);
    }

    public void removePeer(int peerNum){
        TrustVertex peer = new TrustVertex(peerNum);
    	Collection<TrustConnection> edgeset = getIncidentEdges(peer);
    	for (TrustConnection e: edgeset){
            removeEdge(e);
    	}
    	removeVertex(peer);
    }

    public void feedback(int from, int to, double feedback) {
        Integer key = new Integer(++edgecounter);
        feedback(from, to, feedback, key);
    }

    public void feedback(int from, int to, double feedback, Integer key) {
        //ChatterBox.debug(this, "feedback()", "from " + from + " to " + to + " rating " + feedback + " key " + key.toString());
        if (getVertexInGraph(from) == null){
            addPeer(from);
        }
        if (getVertexInGraph(to) == null){
            addPeer(to);             
        }
        TrustConnection edge = findConnection(from, to);
        if (edge != null){//If the edge already exists, add the feedback to it
            edge.addFeedback(feedback);
        }else{//Otherwise, create a new edge and add it to the graph
            edge = new TrustConnection(key, TrustConnection.FEEDBACK, feedback);
            TrustVertex assessor = this.getVertexInGraph(new TrustVertex(from));
            TrustVertex assessee = getVertexInGraph(new TrustVertex(to));
            //ChatterBox.alert("Vertexes to be added" + assessor.toString() + " " + assessee.toString());
            addEdge(edge, assessor, assessee);
        }      
    }

    public void unFeedback(int from, int to, double feedback, int key) {
        TrustConnection edge = findConnection(from, to);
        if (edge != null){
            if (edge.hasMultipleFeedback()){
                edge.removefeedback(feedback);
            }else{
                Collection<TrustVertex> verts = super.getIncidentVertices(edge);
                super.removeEdge(edge);
                for (TrustVertex v : verts){
                    if (super.getIncidentEdges(v).isEmpty()){
                        super.removeVertex(v);
                    }
                }
                
            }
        }else{
            ChatterBox.alert("omg, couldn't find an edge to remove!");
        }
    }

    /**
     * Returns a tree graph of documents and the peers which host them.
     * @param graph	The source which the tree Graph will be made from
     * @return	The Document Tree Graph
     */
    public static Forest<TrustVertex, TrustConnection> makeTreeGraph(TrustGraph graph) {
        Forest<TrustVertex, TrustConnection> tree = new DelegateForest<TrustVertex, TrustConnection>();
        for (TrustVertex documentVertex : graph.getVertices()) { //iterate over all vertices in the graph
            if (documentVertex.getClass().equals(DocumentVertex.class)) { //a document represents the root of a tree in the graph
                //DocumentVertex docVertex = new DocumentVertex(p2pV);
                tree.addVertex(documentVertex);
                for (TrustConnection edge : graph.getOutEdges(documentVertex)) { //get all the document's edges to find all the peers connected to it
                    TrustVertex opposite = graph.getOpposite(documentVertex, edge);
                    if (opposite.getClass().equals(DocumentVertex.class)) {
                        tree.addEdge(edge, documentVertex, opposite);
                    }
                }
                for (TrustConnection edge : graph.getInEdges(documentVertex)) {
                    TrustVertex opposite = graph.getOpposite(documentVertex, edge);
                    if (opposite.getClass().equals(PeerDocumentVertex.class)) {
                        tree.addEdge(edge, documentVertex, opposite);
                    }
                }
            }
        }
        return tree;
    }

    /**
     * Handles the Log Events which affect the structure of the graph.
     * @param gev				The Log event which needs to be handled.
     * @param forward			<code>true</code> if play-back is playing forward.
     * @param referenceGraph	The Graph to get edge numbers from.
     */
    public void graphEvent(TrustLogEvent gev, boolean forward, TrustGraph referenceGraph) {
        int assessor = gev.getAssessor();
        int assessee = gev.getAssessee();
        double feedback = gev.getFeedback();
        int key = 0;
        try{
            key = referenceGraph.findConnection(assessor, assessee).getKey();
        }catch (NullPointerException ex){
            ChatterBox.error(this, "graphEvent", "Error finding key from event.  assessor: " + assessor + " assessee: " + assessee + " feedback: " + feedback);
        }
        if (forward) {
            if (type == FEEDBACK_HISTORY) {
                feedback(assessor, assessee, feedback, key);
            } else {
                ChatterBox.error(this, "graphEvent()", "An event was called but I have mot implemented a handler for this graph type yet.");
            }
        } else {
            if (type == FEEDBACK_HISTORY) {
                unFeedback(assessor, assessee, feedback, key);
            } else {
                ChatterBox.error(this, "graphEvent()", "An event was called but I have mot implemented a handler for this graph type yet.");
            }
        }
    }

    /**
     * Limited version of graphEvent for construction a graph for layout purposes
     * @param gev	The Log event which needs to be handled.
     */
    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type == FEEDBACK_HISTORY) {
            this.feedback(gev.getAssessor(), gev.getAssessee(), gev.getFeedback());
        } else {
            ChatterBox.error(this, "graphEvent()", "An event was called but I have mot implemented a handler for this graph type yet.");
        }
    }
}