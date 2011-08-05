////////////////////////////////MyReputationEdge//////////////////////////////////
package cu.trustGrapher.graph.edges;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdge;

/**
 * A ReputationEdge extension that adds a id field and other useful methods
 * @author Andrew O'Hara
 */
public class SImReputationEdge extends ReputationEdge {

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new SImReputationEdge
     * @param src The Agent that this edge originates from
     * @param sink The Agent that this edge ends at
     */
    public SImReputationEdge(Agent src, Agent sink) {
        super(src, sink);
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Returns a string representation of this edge.  This string is displayed by the edge in the TrustGraphViewer.
     * This String contains the reputation of this edge
     * @return A string representation of this edge
     */
    @Override
    public String toString(){
        String rawRep = "" + super.getReputation();
        String finalRep = "";
        int length;
        if (rawRep.length() > 4){
            length = 5;
        }else{
            length = rawRep.length();
        }
        for (int i=0 ; i<length ; i++){
            finalRep = finalRep + rawRep.charAt(i);
        }
        return finalRep;
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof SImReputationEdge){
            SImReputationEdge other = (SImReputationEdge) o;
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
