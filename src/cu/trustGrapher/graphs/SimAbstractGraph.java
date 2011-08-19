////////////////////////////////////SimAbstractGraph////////////////////////////////////
package cu.trustGrapher.graphs;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.graphs.edges.*;
import cu.trustGrapher.eventplayer.TrustLogEvent;

import org.jgrapht.graph.SimpleDirectedGraph;

import aohara.utilities.ChatterBox;
import cu.trustGrapher.loading.GraphConfig;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import org.apache.commons.collections15.Predicate;

/**
 * A graph superclass that inherits lower level Graph methods from JungAdapterGraph
 * This classs accepts a parameter of the jGraphT SimpleDirectedGraph that this graph is to be based on
 *
 * This SimAbstractGraph acts as a dynamic graph.  It is not displayed, but components in the full graph will only be
 * displayed if they exist in the dynamic graph.  As events occur, they are added to the dynamic graphs through their
 * graphEvent() method.
 *
 * The full graph is a field in this graph.  It is shown in the GraphViewer, but all vertices and edges that are ever
 * shown must be on the full graph before the events start playing.  Their graphConstructionEvent() method is used as
 * the events are parsed to add all components to the graph.
 * @author Andrew O'Hara
 */
public abstract class SimAbstractGraph extends JungAdapterGraph<Agent, TestbedEdge> implements Predicate<Context<Graph<Agent, TestbedEdge>, Object>> {

    protected JungAdapterGraph<Agent, TestbedEdge> fullGraph;
    private GraphConfig graphConfig;
    
//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Calls superclass and initializes some fields
     * @param graphPair The graphPair that is to hold this graph
     * @param innerGraph The TrustTestBed graph that is to be a base graph for this graph
     */
    public SimAbstractGraph(GraphConfig graphConfig, SimpleDirectedGraph innerGraph) {
        super(innerGraph);
        fullGraph = new JungAdapterGraph<Agent, TestbedEdge>((SimpleDirectedGraph) innerGraph.clone());
        this.graphConfig = graphConfig;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public JungAdapterGraph getFullGraph(){
        return fullGraph;
    }

    /**
     * Gets the display name for this graph.
     * The Display name consits of the graphID and the name of the algorithm attached to it.
     * @return The display name
     */
    public String getDisplayName() {
        return graphConfig.getDisplayName();
    }

    /**
     *Returns the algorithm of this Graphpair.  If this GraphPair holds a
     * SimFeedbackGraph, then it returns null.
     * @return the algorithm of this GraphPair
     */
    public Object getAlgorithm() {
        return graphConfig.getAlgorithm();
    }

    /**
     * Returns the id of this graph.  It is currently used to generate the name of the graph
     * @return The graph id
     */
    public int getID() {
        return graphConfig.getIndex();
    }

    /**
     * Whether or not this graph will have a viewer built for it
     * @return the displayed boolean
     */
    public boolean isDisplayed() {
        return graphConfig.isDisplayed();
    }

    /**
     * This is the predicate for whether to show the graph entities in the GraphViewer.
     * It is called by the GraphViewer during every repaint for every entity.
     * If the entity exists in this dynamic Graph, return true.  Otherwise false.
     * @param context The fullGraph and element that is being checked
     * @return Whether to display the element given by the context for the current repaint
     */
    public boolean evaluate(Context<Graph<Agent, TestbedEdge>, Object> context) {
        if (context.element instanceof TestbedEdge){
            return containsEdge((TestbedEdge) context.element);
        }else if (context.element instanceof Agent){
            return containsVertex((Agent) context.element);
        }else{
            ChatterBox.criticalError(this, "evaluate()", "Uncaught predicate");
            return false;
        }
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Ensures that an Agent with the given ID exists in the graph
     * If it doesn't, then it is added
     * @param id the ID of the agent that must exist
     * @return An instance of the Agent that is to exist in the graph
     */
    protected Agent ensureAgentExists(int id, JungAdapterGraph<Agent, TestbedEdge> caller) {
        Agent tempAgent = new Agent(id);
        for (Agent agent : caller.getVertices()){
            if (agent.equals(tempAgent)){
                return agent;
            }
        }
        caller.addVertex(tempAgent);
        return tempAgent;
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
    protected TestbedEdge ensureEdgeExists(Agent src, Agent sink, JungAdapterGraph<Agent, TestbedEdge> caller) {
        TestbedEdge edge = caller.findEdge(src, sink);
        if (edge == null){
            edge = newEdge(src, sink, caller);
            caller.addEdge(edge, src, sink);
        }
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
    private TestbedEdge newEdge(Agent src, Agent sink, JungAdapterGraph caller) {
        if (caller.getInnerGraph() instanceof cu.repsystestbed.graphs.FeedbackHistoryGraph) {
            try {
                return new SimFeedbackEdge(src, sink);
            } catch (Exception ex) {
                ChatterBox.debug("MyFeedbackEdge", "MyFeedbackEdge()", ex.getMessage());
                return null;
            }
        } else if (caller.getInnerGraph() instanceof cu.repsystestbed.graphs.ReputationGraph) {
            return new SimReputationEdge(src, sink);
        } else if (caller.getInnerGraph() instanceof cu.repsystestbed.graphs.TrustGraph) {
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
     * Called by the EventPlayer whenever a TrustLogEvent occurs.  It is assumed that this is a full graph.
     * Adds any new Agents to the full graph referred to by the TrustLogevent and all edges that might possibly exist.
     * @param event The TrustLogEvent that has just occured
     */
    public void graphConstructionEvent(TrustLogEvent event) {
        if (event == null) {  //A null event is passed to signal that there are no more events.  Add all edges to the graph
            for (Agent src : fullGraph.getVertices()) {
                for (Agent sink : fullGraph.getVertices()) {
                    if (!src.equals(sink)) {
                        ensureEdgeExists(src, sink, fullGraph);
                    }
                }
            }
        } else { //Otherwise, just add any new Agents to the graph
            ensureAgentExists(event.getAssessor(), fullGraph);
            ensureAgentExists(event.getAssessee(), fullGraph);
        }
    }

    /**
     * Called by this graph's GraphPair whenever a TrustLogEvent occurs.  It is assumed that this graph is a dynamic graph.
     * If the graph is playing forward, call the forwardEvent method, otherwise, calls backwardEvent
     * @param event The TrustLogEvent that is being processed
     * @param forward Whether or not the graph is being played forward
     */
    public abstract void graphEvent(TrustLogEvent event, boolean forward);
}
////////////////////////////////////////////////////////////////////////////////

