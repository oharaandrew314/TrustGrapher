////////////////////////////////MyReputationEdge//////////////////////////////////
package trustGrapher.graph;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.ReputationEdge;
import utilities.ChatterBox;

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
        if (o instanceof MyReputationEdge){
            return this.id == ((MyReputationEdge)o).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.id;
        return hash;
    }

    @Override
    public String toString(){
        if (getReputation() == 0.0){
            return "" + id;
        }
        String s = "" + super.getReputation();
        String s2 = "";
        int length;
        if (s.length() > 4){
            length = 5;
        }else{
            length = s.length();
        }
        for (int i=0 ; i<length ; i++){
            s2 = s2 + s.charAt(i);
        }
        return s2;
    }

}
////////////////////////////////////////////////////////////////////////////////
