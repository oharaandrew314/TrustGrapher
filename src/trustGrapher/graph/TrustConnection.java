//////////////////////////////////TrustConnection///////////////////////////////
package trustGrapher.graph;

import java.util.ArrayList;
import utilities.ChatterBox;

/**
 *
 * @author Andrew O'Hara
 */
public class TrustConnection implements Comparable<TrustConnection> {

    public static final int FEEDBACK = 0, REPUTATION = 1, TRUST = 2;
    private int type;
    private ArrayList<Double> feedback;
    private Integer key;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * @param type either P2P (edge between two peers) or P2DOC (edge between a peer and a document)
     * @param key
     */
    public TrustConnection(Integer key) {
        ChatterBox.debug("trustGrapher.graph.TrustConnection", "TrustConnection()", "Got a call without a rating.  Making it +1");
    }

    public TrustConnection(Integer key, int type, double feedback) {
        this.type = type;
        this.key = key;
        this.feedback = new ArrayList<Double>();
        this.feedback.add(feedback);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public Integer getKey() {
        return key;
    }

    public ArrayList<Double> getRating() {
        return feedback;
    }

    public double getRating(int i){
        return feedback.get(i);
    }

    public boolean hasMultipleFeedback(){
        return feedback.size() > 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TrustConnection) {
            return (key.equals(((TrustConnection) other).getKey()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public int compareTo(TrustConnection other) {
        if (other instanceof TrustConnection) {
            return (key.compareTo(((TrustConnection) other).getKey()));
        }
        return 0; // there's a problem anyway : can only compare two TrustConnection objects
    }

    @Override
    public String toString() {
        String string = "";
        for (int i=0 ; i<feedback.size() ; i++){
            string = string + "+" + feedback.get(i) + "\n";
        }
        return "edge " + key.toString() + "\n" + string;
    }
///////////////////////////////////Methods//////////////////////////////////////

    public void addFeedback(double feedback){
        this.feedback.add(feedback);
    }

    public void removefeedback(double feedback){
        this.feedback.remove(feedback);
    }

}
////////////////////////////////////////////////////////////////////////////////

