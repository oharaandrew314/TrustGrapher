////////////////////////////////////TrustVertex/////////////////////////////////
/**
 * Contains the class definition for a TrustVertex in the Trust Graph project.
 * @author Matthew Smith
 * @author Alan Davoust
 * @author Andrew O'Hara
 */
package trustGrapher.graph;

/**
 * <p>A class for a vertex in the P2P network visualization.</p>
 * <ul>
 * <li>A vertex has a label which is drawn over the vertex on the graph.</li>
 * <li>A vertex has a key which is its identifier, <code>equals(Object other)</code> compares the key value.</li>
 * </ul>
 * @author Matthew Smith
 * @author Alan Davoust
 * @author Andrew O'Hara
 * @version May 12, 2011
 */
public class TrustVertex implements Comparable<TrustVertex> {

    protected Integer label; //the label will be drawn over the vertex on the graph
    protected Integer key; //the identifier which defines this vertex
    protected int rep; //Keeps track of how much reputation this peer has

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a vertex in the network Graph
     * @param key The identifier which defines this vertex.
     */
    public TrustVertex(Integer key) {
        this(key, 0);
    }

    /**
     * Creates a vertex in the network Graph
     * @param key The identifier which defines this vertex.
     * @param rep Sets an inital amount of reputation for this peer
     */
    public TrustVertex(Integer key, int rep) {
        this.key = key;
        this.label = key;
        this.rep = rep;
    }

    /**
     * Copy Constructor
     * @param vertex the TrustVertex to copy
     */
    protected TrustVertex(TrustVertex vertex) {
        this.key = vertex.key;
        this.label = vertex.label;
        this.rep = vertex.rep;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Gets the label for the vertex.
     * @return Returns the label of this vertex.
     */
    public Integer getLabel() {
        return label;
    }

    /**
     * Gets the key for the vertex.
     * @return Returns the key(identifier) of this vertex.
     */
    public Integer getKey() {
        return key;
    }
    //[end] Getters and Setters

    //[start] Overridden Methods
    @Override
    public String toString() {
        return label.toString();
    }

///////////////////////////////////Methods//////////////////////////////////////

    //Important : most Graph classes seem to rely on equals() to find vertices in their collection.
    @Override
    public boolean equals(Object other) {
        if (other instanceof TrustVertex) {
            return (key.equals(((TrustVertex) other).getKey()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public int compareTo(TrustVertex other) {
        if (other instanceof TrustVertex) {
            return (key.compareTo(((TrustVertex) other).getKey()));
        } else {
            return 0; // there's a problem anyway : can only compare two P2PVertices
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
