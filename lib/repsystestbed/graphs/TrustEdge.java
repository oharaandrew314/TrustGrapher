package cu.repsystestbed.graphs;

import cu.repsystestbed.entities.Agent;

public class TrustEdge extends TestbedEdge
{
	public TrustEdge(Agent src, Agent sink)
	{
		super.src = src;
		super.sink = sink;
	}

}
