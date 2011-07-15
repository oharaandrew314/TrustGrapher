////////////////////////////////MyReputationEdge//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdge;

/**
 * A ReputationEdge extension that adds a id field and other useful methods
 * @author Andrew O'Hara
 */
public class MyReputationEdge extends ReputationEdge {

    private int id;

//////////////////////////////////Constructor///////////////////////////////////
    public MyReputationEdge(Agent from, Agent to, int id) {
        super(from, to);
        this.id = id;
    }

    public MyReputationEdge(Agent from, Agent to, double feedback, int id) {
        super(from, to, feedback);
        this.id = id;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getID() {
        return id;
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public boolean equals(Object o){
        if (o instanceof MyReputationEdge == false){
            return false;
        }
        MyReputationEdge other = (MyReputationEdge) o;
//        return (this.src == other.src) && (this.sink == other.sink);
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.id;
        return hash;
    }

    @Override
    public String toString(){
        String s = "" + super.getReputation();
        return "" + s.charAt(0) + s.charAt(1) + s.charAt(2) + s.charAt(3) + s.charAt(4);
    }

}
////////////////////////////////////////////////////////////////////////////////
