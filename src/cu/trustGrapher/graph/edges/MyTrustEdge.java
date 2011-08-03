//////////////////////////////////MyTrustEdge///////////////////////////////////
package cu.trustGrapher.graph.edges;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TrustEdge;

/**
 * An edge between two Agents that signifies that one trusts the other
 * @author Andrew O'Hara
 */
public class MyTrustEdge extends TrustEdge{
    private int id;

//////////////////////////////////Constructor///////////////////////////////////
    public MyTrustEdge(Agent from, Agent to, int id){
        super(from, to);
        this.id = id;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getID(){
        return id;
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public String toString(){
        return "";
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof MyTrustEdge){
            return this.id == ((MyTrustEdge)o).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
        return hash;
    }
}
////////////////////////////////////////////////////////////////////////////////