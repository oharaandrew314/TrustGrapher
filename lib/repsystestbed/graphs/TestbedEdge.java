package cu.repsystestbed.graphs;

import java.io.Serializable;

import org.jgrapht.graph.DefaultEdge;

public abstract class TestbedEdge extends DefaultEdge implements Cloneable, Serializable
{
	private static final long serialVersionUID = 3258408452177932855L;
	public Object src, sink;
	
    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        return super.clone();
    }
}
