package cu.repsystestbed.graphs;

import cu.repsystestbed.entities.Agent;

public class ReputationEdge extends TestbedEdge
{

	private static final long serialVersionUID = -6027745106941999388L;
	private double reputation;
	
	public ReputationEdge(Agent src, Agent sink, double reputation)
	{
		super.src = src;
		super.sink = sink;
		this.setReputation(reputation);
	}
	
	public ReputationEdge(Agent src, Agent sink)
	{
		super.src = src;
		super.sink = sink;
		this.setReputation(Double.MIN_VALUE);
	}

	public void setReputation(double reputation)
	{
		this.reputation = reputation;
	}

	public double getReputation()
	{
		return reputation;
	}
	
	

}