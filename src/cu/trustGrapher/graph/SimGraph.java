////////////////////////////////////SimGraph////////////////////////////////////
package cu.trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.graph.edges.*;
import cu.trustGrapher.eventplayer.TrustLogEvent;

import org.jgrapht.graph.SimpleDirectedGraph;

import utilities.ChatterBox;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * This classs accepts a parameter of the jGraphT SimpleDirectedGraph that this graph is to be based on
 * @author Andrew O'Hara
 */
public abstract class SimGraph extends JungAdapterGraph<Agent, TestbedEdge> {

    protected GraphPair graphPair;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Calls superclass and initializes some fields
     * @param graphPair The graphPair that is to hold this graph
     * @param innerGraph The TrustTestBed graph that is to be a base graph for this graph
     */
    public SimGraph(GraphPair graphPair, SimpleDirectedGraph innerGraph) {
        super(innerGraph);
        this.graphPair = graphPair;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    
    /**
     * @return Gets the GraphPair that contains this graph
     */
    public GraphPair getGraphPair(){
        return graphPair;
    }

    /**
     * Gets an Agent already in the graph that has the given peerID.
     * Returns null if the agent doesn't exist
     * @return An agent with the same ID to search for among the agents in the graph
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
     * @param id the ID of the agent that must exist
     * @return An instance of the Agent that is to exist in the graph
     */
    protected Agent ensureAgentExists(int id) {
        Agent agent = new Agent(id);
        if (!containsVertex(agent)) {
            addVertex(agent);
        }
        return agent;
    }

    /**
     * Ensures that the edge with the given agents exists in the graph.
     * If it doesn't exist, create it and add it to the graph.
     * Then return the edge.
     * @param src The Agent that the edge originates from
     * @param sink The Agent that the edge goes to
     * @param caller The graph that is calling, so that if an edge must be created, it knows what type to make
     * @return The edge that is assured to be in the graph
     */
    protected TestbedEdge ensureEdgeExists(Agent src, Agent sink, SimGraph caller) {
        TestbedEdge edge = newEdge(src, sink, caller);
        addEdge(edge, src, sink);
        return edge;
    }

    /**
     * Create an edge between the given agents.
     * The edge will be of the class appropriate for the caller.
     * For example, a SimFeedbackEdge will be created for a caller of class SimFeedbackGraph
     * If the caller isn't valid, then this returns null;
     * @param src The Agent that the edge is to originate from
     * @param sink The Agent that the edge is to go to
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
     * no longer have any edges after removing the given edge.
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
     * Called by this graph's GraphPair whenever a TrustLogEvent occurs.  It is assumed that this graph is a dynamic graph.
     * If the graph is playing forward, call the forwardEvent method, otherwise, calls backwardEvent
     * @param event The TrustLogEvent that is being processed
     * @param forward Whether or not the graph is being played forward
     * @param fullGraph The full graph paired to this dynamic graph
     */
    public void graphEvent(TrustLogEvent event, boolean forward, SimGraph fullGraph) {
        if (event != null){
            if (forward) {
                forwardEvent(event, fullGraph);
            } else {
                backwardEvent(event, fullGraph);
            }
        }
    }

    /**
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  It is assumed that this is a full graph.
     * Adds any new Agents to the full graph referred to by the TrustLogevent and all edges that might possibly exist.
     * @param event The TrustLogEvent that has just occured
     */
    public void graphConstructionEvent(TrustLogEvent event) {
        if (event == null) {  //A null event is passed to signal that there are no more events.  Add all edges to the graph
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