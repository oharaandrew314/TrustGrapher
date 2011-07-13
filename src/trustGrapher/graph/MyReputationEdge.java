////////////////////////////////MyReputationEdge//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdge;

/**
 * A ReputationEdge extension that adds a key field and other useful methods
 * @author Andrew O'Hara
 */
public class MyReputationEdge extends ReputationEdge {

    int key;

//////////////////////////////////Constructor///////////////////////////////////
    public MyReputationEdge(Agent from, Agent to, int key) {
        super(from, to);
        this.key = key;
    }

    public MyReputationEdge(Agent from, Agent to, double feedback, int key) {
        super(from, to, feedback);
        this.key = key;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getKey() {
        return key;
    }

    public Agent getAssessor(){
        return (Agent) src;
    }

    public Agent getAssessee(){
        return (Agent) sink;
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public boolean equals(Object o){
        if (o instanceof MyReputationEdge == false){
            return false;
        }
        MyReputationEdge other = (MyReputationEdge) o;
        return this.key == other.key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.key;
        return hash;
    }

    @Override
    public String toString(){
        return "" + super.getReputation();
    }

}
////////////////////////////////////////////////////////////////////////////////
