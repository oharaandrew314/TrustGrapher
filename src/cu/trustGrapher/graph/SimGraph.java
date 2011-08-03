//////////////////////////////////SimGraph////////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.TrustGrapher;
import cu.trustGrapher.graph.edges.MyFeedbackEdge;
import cu.trustGrapher.graph.edges.MyReputationEdge;
import cu.trustGrapher.graph.edges.MyTrustEdge;

import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;

import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * @author Andrew O'Hara
 */
public abstract class SimGraph extends JungAdapterGraph<Agent, TestbedEdge> {

    public static final int DYNAMIC = TrustGrapher.DYNAMIC, FULL = TrustGrapher.FULL;
    protected int type;
    protected int edgecounter = 0;
    protected int graphID;
    protected boolean display;

//////////////////////////////////Constructor///////////////////////////////////

    /**
     * Calls superclass and initializes some fields
     * @param graph This graph is given to the superclass.  It is the graph this graph will be built on
     * @param type The graph type (full or dynamic)
     * @param graphID The graphID number of this graph
     * @param display Whether or not this graph will have a viewer built for it
     */
    public SimGraph(SimpleDirectedGraph<Agent, TestbedEdge> graph, int type, int id, boolean display) {
        super(graph);
        this.type = type;
        this.graphID = id;
        this.display = display;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    /**
     * This String returned by this is the String displayed on the viewer border
     */
    @Override
    public String toString(){
        return this.getClass().getSimpleName() + " " + graphID;
    }

    public int getType() {
        return type;
    }

    public int getID(){
        return graphID;
    }

    /**
     * Whether or not this graph will have a viewer built for it
     * @return the displayed boolean
     */
    public boolean isDisplayed(){
        return display;
    }
    
    /**
     * Gets an Agent already in the graph that has the given peerID
     * to be used when adding edges; the edge should relate two Agents actually in the graph, not copies of these vertices
     *
     * Returns null if the agent doesn't exist
     * @param graphID The ID of the Agent to find
     * @return The Agent with the given ID
     */
    protected Agent findAgent(Agent agent) {
        for (Agent v : super.getVertices()) {
            if (v.equals(agent)) {
                return (Agent) v;
            }
        }
        return null;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Ensures that an Agent with the given ID exists in the graph
     * If it doesn't, then it is added
     * @param graphID the ID of the agent that must exist
     * @return An instance of the Agent that was to exist in the graph
     */
    protected Agent ensureAgentExists(int id) {
        Agent agent = new Agent(id);
        if (!containsVertex(agent)) {
            addVertex(agent);
        }
        return agent;
    }

    protected TestbedEdge ensureEdgeExists(Agent src, Agent sink, int id, SimGraph caller){
        TestbedEdge edge = newEdge(src, sink, id, caller);
        addEdge(edge, src, sink);
        return edge;
    }

    /**
     * Create an edge, and add all connected Agents to the graph if they have not yet been added
     * @param src
     * @param sink
     * @param graphID
     * @param caller
     * @return
     */
    private TestbedEdge newEdge(Agent src, Agent sink, int id, SimGraph caller){
        if (caller instanceof SimFeedbackGraph){
            try{
                return new MyFeedbackEdge(src, sink, id);
            }catch(Exception ex){
                ChatterBox.debug("MyFeedbackEdge", "MyFeedbackEdge()", ex.getMessage());
                return null;
            }
        }else if (caller instanceof SimReputationGraph){
            return new MyReputationEdge(src, sink, id);
        }else if (caller instanceof SimTrustGraph){
            return new MyTrustEdge(src, sink, id);
        }else{
            ChatterBox.debug(this, "newEdge()", "Unsupported caller");
            return null;
        }
    }

    /**
     * Removes the given edge from the graph and and the Agents connected to it if they
     * would no longer have any edges after removing this one
     * @param edge The edge to be removed
     * @return Whether or not the edge was succesfully removed from the graph
     */
    public void removeEdgeAndVertices(TestbedEdge edge){
        removeEdge(edge); //I actually need the super here
        for (Agent v : getIncidentVertices(edge)) {
            if (getIncidentEdges(v).isEmpty()) {
                removeVertex(v);
            }
        }
    }

    /**
     * Determines how to handle a normal TrustLogEvent
     * @param gev
     * @param forward
     * @param fullGraph
     */
    public void graphEvent(TrustLogEvent gev, boolean forward, SimGraph fullGraph) {
        if (type != DYNAMIC){
            ChatterBox.error(this, "graphEvent()", "This graph is not a dynamic graph.  Illegal method call");
            return;
        }
        if (forward){
            forwardEvent(gev, fullGraph);
        }else{
            backwardEvent(gev, fullGraph);
        }
    }

    public void graphConstructionEvent(TrustLogEvent gev) {
        if (type != FULL) {
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a full graph.  Illegal method call.");
            return;
        }
        ensureAgentExists(gev.getAssessor());
        ensureAgentExists(gev.getAssessee());
        for (Agent src : getVertices()) {
            for (Agent sink : getVertices()) {
                if (!src.equals(sink)){
                    ensureEdgeExists(src, sink, edgecounter++, this);
                }
            }
        }
    }

    protected abstract void forwardEvent(TrustLogEvent gev, SimGraph fullGraph);
    protected abstract void backwardEvent(TrustLogEvent gev, SimGraph fullGraph);
}
////////////////////////////////////////////////////////////////////////////////