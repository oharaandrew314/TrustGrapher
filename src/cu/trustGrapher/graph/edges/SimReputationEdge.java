////////////////////////////////MyReputationEdge//////////////////////////////////
package cu.trustGrapher.graph.edges;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdge;

/**
 * A ReputationEdge extension that shows the degree of trust an agent has for another.
 * @author Andrew O'Hara
 */
public class SimReputationEdge extends ReputationEdge {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new SimReputationEdge.
     * @param src The Agent that this edge originates from
     * @param sink The Agent that this edge ends at
     */
    public SimReputationEdge(Agent src, Agent sink) {
        super(src, sink);
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Returns a string representation of this edge.  This string is displayed by the edge in the TrustGraphViewer.
     * This String contains the reputation of this edge.
     * @return A string representation of this edge
     */
    @Override
    public String toString(){
        String rawRep = "" + getReputation();
        int length = (rawRep.length() >= 4) ? 4 : rawRep.length();
        return rawRep.substring(0, length);
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof SimReputationEdge){
            SimReputationEdge other = (SimReputationEdge) o;
        return (src.equals(other.src)) && (sink.equals(other.sink));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////
