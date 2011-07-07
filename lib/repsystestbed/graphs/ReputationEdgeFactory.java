package cu.repsystestbed.graphs;

import org.jgrapht.EdgeFactory;

import cu.repsystestbed.entities.Agent;

public class ReputationEdgeFactory implements EdgeFactory<Agent, ReputationEdge>
{

	@Override
	public ReputationEdge createEdge(Agent src, Agent sink)
	{
		
		return new ReputationEdge(src, sink);
	}

}
