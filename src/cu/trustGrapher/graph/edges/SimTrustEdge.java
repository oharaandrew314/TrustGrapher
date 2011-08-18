//////////////////////////////////MyTrustEdge///////////////////////////////////
package cu.trustGrapher.graph.edges;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TrustEdge;

/**
 * An edge between two Agents that signifies that one trusts the other
 * @author Andrew O'Hara
 */
public class SimTrustEdge extends TrustEdge{
    private static final String label = "";

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new SimTrustEdge.
     * @param src The Agent that this edge originates from
     * @param sink The Agent that this edge ends at
     */
    public SimTrustEdge(Agent src, Agent sink){
        super(src, sink);
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Returns a string representation of this edge.  This string is displayed by the edge in the TrustGraphViewer.
     * This String is empty since this edge simply shows trust between two agents
     * Warning: This method must stay here or the TrustGraphViewer will display the superclass toString method
     * @return A string representation of this edge
     */
    @Override
    public String toString(){
        return label;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof SimTrustEdge){
            SimTrustEdge other = (SimTrustEdge) o;
            return (src.equals(other.src)) && (sink.equals(other.sink));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////