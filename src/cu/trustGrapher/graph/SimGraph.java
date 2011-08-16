////////////////////////////////////SimGraph////////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.graph.edges.SimFeedbackEdge;
import cu.trustGrapher.graph.edges.SimReputationEdge;
import cu.trustGrapher.graph.edges.SimTrustEdge;
import cu.trustGrapher.graph.savingandloading.GraphConfig;
import cu.trustGrapher.eventplayer.TrustLogEvent;

import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * This classs accepts a parameter of the jGraphT SimpleDirectedGraph that this graph is to be based on
 * @author Andrew O'Hara
 */
public abstract class SimGraph extends JungAdapterGraph<Agent, TestbedEdge> {

    public static final int DYNAMIC = GraphManager.DYNAMIC, FULL = GraphManager.FULL;
    protected int type;
    protected int graphID; //The index of the algorithm that was given to the graph
    protected boolean display;
    protected GraphManager graphManager;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Calls superclass and initializes some fields
     * @param graph This graph is given to the superclass.  It is the graph this graph will be built on
     * @param type The graph type (full or dynamic)
     * @param graphID The graphID number of this graph
     * @param display Whether or not this graph will have a viewer built for it
     */
    public SimGraph(GraphManager graphManager, SimpleDirectedGraph<Agent, TestbedEdge> graph, int type, GraphConfig algConfig) {
        super(graph);
        this.graphManager = graphManager;
        this.type = type;
        graphID = algConfig.getIndex();
        display = algConfig.isDisplayed();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Returns the graph type (full or dynamic)
     * Refer to the SimGraph static field definitions to find what graph type the int representation refers to
     * @return The int representation of the graph type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the id of this graph.  It is currently used to generate the name of the graph
     * @return The graph id
     */
    public int getID() {
        return graphID;
    }

    /**
     * Whether or not this graph will have a viewer built for it
     * @return the displayed boolean
     */
    public boolean isDisplayed() {
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
        for (Agent v : getVertices()) {
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

    /**
     * Ensures that the edge with the given agents and id exists in the graph.
     * If it doesn't exist, create it and it to the graph.
     * Then return the edge
     * @param src The Agent that the edge originates from
     * @param sink The Agent that the edge goes to
     * @param id The id of the edge
     * @param caller The graph that is calling, so that if an edge must be created, it knows what type to make
     * @return The edge that is assured to be in the graph
     */
    protected TestbedEdge ensureEdgeExists(Agent src, Agent sink, SimGraph caller) {
        TestbedEdge edge = newEdge(src, sink, caller);
        addEdge(edge, src, sink);
        return edge;
    }

    /**
     * Create an edge with the given agents and id.
     * The edge will be of the class appropriate for the caller.
     * For example, a SimFeedbackEdge will be created for a caller of class SimFeedbackGraph
     * If the caller isn't valid, then this returns null;
     * @param src The Agent that the edge is to originate from
     * @param sink The Agent that the edge is to go to
     * @param edgeID The id that the edge is to have
     * @param caller The graph that the new edge is to be returned to
     * @return The new Edge
     */
    private TestbedEdge newEdge(Agent src, Agent sink, SimGraph caller) {
        if (caller instanceof SimFeedbackGraph) {
            try {
                return new SimFeedbackEdge(src, sink);
            } catch (Exception ex) {
                ChatterBox.debug("MyFeedbackEdge", "MyFeedbackEdge()", ex.getMessage());
                return null;
            }
        } else if (caller instanceof SimReputationGraph) {
            return new SimReputationEdge(src, sink);
        } else if (caller instanceof SimTrustGraph) {
            return new SimTrustEdge(src, sink);
        } else {
            ChatterBox.debug(this, "newEdge()", "Unsupported caller");
            return null;
        }
    }

    /**
     * Removes the given edge from the graph and and the Agents connected to it if they
     * would no longer have any edges after removing the given edge
     * @param edge The edge to be removed
     */
    protected void removeEdgeAndVertices(TestbedEdge edge) {
        java.util.Collection<Agent> agents = getIncidentVertices(edge);
        removeEdge(edge);
        for (Agent v : agents) {
            if (getIncidentEdges(v).isEmpty()) {
                removeVertex(v);
            }
        }
    }

    /**
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  This graph must be a dynamic graph in order to call this method.
     * If the graph is playing forward, call the forwardEvent method, otherwise, call backwardEvent
     * @param gev The TrustLogEvent that has just occured
     * @param forward Whether or not the graph is being played forward
     * @param fullGraph The full graph paired to this dynamic graph
     */
    public void graphEvent(TrustLogEvent gev, boolean forward, SimGraph fullGraph) {
        if (type != DYNAMIC) {
            ChatterBox.error(this, "graphEvent()", "This graph is not a dynamic graph.  Illegal method call");
            return;
        }
        if (gev != null){
            if (forward) {
                forwardEvent(gev, fullGraph);
            } else {
                backwardEvent(gev, fullGraph);
            }
        }
    }

    /**
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  This graph must be a full graph in order to call this method.
     * Adds any new Agents to the full graph referred to by the TrustLogevent and all edges that might possibly exist
     * @param event The TrustLogEvent that has just occured
     */
    public void graphConstructionEvent(TrustLogEvent event) {
        if (type != FULL) {
            ChatterBox.error(this, "graphConstructionEvent()", "This graph is not a full graph.  Illegal method call.");
            return;
        }
        if (event == null) {  //A null event is passed to signal that there are no more evetns.  Add all edges to the graph
            for (Agent src : getVertices()) {
                for (Agent sink : getVertices()) {
                    if (!src.equals(sink)) {
                        ensureEdgeExists(src, sink, this);
                    }
                }
            }
        } else { //Otherwise, just add any new Agents to the graph
            ensureAgentExists(((TrustLogEvent) event).getAssessor());
            ensureAgentExists(((TrustLogEvent) event).getAssessee());
        }
    }

    public abstract String getDisplayName();

    protected abstract void forwardEvent(TrustLogEvent gev, SimGraph fullGraph);

    protected abstract void backwardEvent(TrustLogEvent gev, SimGraph fullGraph);
}
////////////////////////////////////////////////////////////////////////////////