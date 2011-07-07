package cu.repsystestbed.algorithms;

import java.util.Set;

import org.apache.log4j.Logger;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.exceptions.GenericTestbedException;
import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.graphs.ReputationEdge;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TrustEdge;
import cu.repsystestbed.graphs.TrustEdgeFactory;
import cu.repsystestbed.graphs.TrustGraph;

public abstract class TrustAlgorithm
{
	private static TrustAlgorithm algorithm;
	static Logger logger = Logger.getLogger(TrustAlgorithm.class.getName());
	private ReputationGraph reputationGraph;
	private TrustGraph trustGraph; 
	
	@SuppressWarnings("unchecked")
	public static TrustAlgorithm getInstance(String className) throws GenericTestbedException
	{
		try
		{
			Class<?>cls = (Class<TrustAlgorithm>) Class.forName(className);
			algorithm = (TrustAlgorithm) cls.newInstance();
		}catch(Exception e)
		{
			String msg = "Error loading trust algorithm with name " + className;
			logger.error(msg);
			throw new GenericTestbedException(msg, e);

		}
		return algorithm;
		
	}

	public void update()
	{
		//go through each agent and find out the agents it trusts
		//create an edge if an agent trusts another
		Set<Agent> agents = this.trustGraph.vertexSet();
		for(Agent src : agents)
		{
			Set<Agent> agents2 = this.trustGraph.vertexSet();
			for(Agent sink : agents)
			{
				if(trusts(src, sink)) this.trustGraph.addEdge(src, sink);
			}
			
		}
		
		
	}

	public void setReputationGraph(ReputationGraph reputationGraph)
	{
		this.reputationGraph = reputationGraph;
		
		//initialize the trust graph
		trustGraph = new TrustGraph(new TrustEdgeFactory());
		Set<TrustEdge> edges = this.trustGraph.edgeSet();
		for(TrustEdge edge : edges)
		{
			Agent src = (Agent)edge.src;
			Agent sink = (Agent)edge.sink;
			this.trustGraph.addVertex(src);
			this.trustGraph.addVertex(sink);
			//an edge in the trust graph means src trusts sink. So don't copy the edges from the rep graph
			//this.trustGraph.addEdge(src, sink);
		}
	}

	public ReputationGraph getReputationGraph()
	{
		return reputationGraph;
	}


	public TrustGraph getTrustGraph()
	{
		return trustGraph;
	}
	
	public abstract boolean trusts(Agent src, Agent sink);
	
	

}


