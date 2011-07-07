package cu.repsystestbed.graphs;

import org.jgrapht.EdgeFactory;

import cu.repsystestbed.entities.Agent;

public class TrustEdgeFactory implements EdgeFactory<Agent, TrustEdge>
{

	@Override
	public TrustEdge createEdge(Agent src, Agent sink)
	{
		
		return new TrustEdge(src, sink);
	}

}
