package cu.repsystestbed.graphs;

import org.jgrapht.graph.SimpleDirectedGraph;

import cu.repsystestbed.entities.Agent;

public class TrustGraph extends SimpleDirectedGraph<Agent, TrustEdge>
{

	private static final long serialVersionUID = -327490271972222723L;

	public TrustGraph(TrustEdgeFactory trustEdgeFactory)
	{
		super(trustEdgeFactory);
	}

}
