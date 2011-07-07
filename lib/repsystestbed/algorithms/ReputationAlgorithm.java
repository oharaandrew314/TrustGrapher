package cu.repsystestbed.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.exceptions.GenericTestbedException;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import cu.repsystestbed.graphs.ReputationEdge;
import cu.repsystestbed.graphs.ReputationEdgeFactory;
import cu.repsystestbed.graphs.ReputationGraph;

public abstract class ReputationAlgorithm
{
	static Logger logger = Logger.getLogger(ReputationAlgorithm.class.getName());
	
	private static ReputationAlgorithm algorithm;
	private FeedbackHistoryGraph feedbackHistoryGraph;
	private ReputationGraph reputationGraph;
	
	/**
	 * Given the class name of a reputation algorithm, this method returns an instance of it.
	 * @param className
	 * @return ReputationAlgorithm
	 * @throws GenericTestbedException
	 */
	@SuppressWarnings("unchecked")
	public static ReputationAlgorithm getInstance(String className) throws GenericTestbedException
	{
		try
		{
			Class<?>cls = (Class<ReputationAlgorithm>) Class.forName(className);
			algorithm = (ReputationAlgorithm) cls.newInstance();
			
		}catch(Exception e)
		{
			String msg = "Error loading reputation algorithm with name " + className;
			logger.error(msg);
			throw new GenericTestbedException(msg, e);
		}
		return algorithm;
		
	}
	/**
	 * set the local feedback history graph variable and also initializes the
	 * reputation graph based.
	 * @param feedbackHistoryGraph 
	 */

	public void setFeedbackHistoryGraph(FeedbackHistoryGraph feedbackHistoryGraph)
	{
		this.feedbackHistoryGraph = feedbackHistoryGraph;
		
		//initialize the reputation graph
		reputationGraph = new ReputationGraph(new ReputationEdgeFactory());
		Set<FeedbackHistoryGraphEdge> edges = this.feedbackHistoryGraph.edgeSet();
		for(FeedbackHistoryGraphEdge edge : edges)
		{
			Agent src = (Agent)edge.src;
			Agent sink = (Agent)edge.sink;
			this.reputationGraph.addVertex(src);
			this.reputationGraph.addVertex(sink);
			this.reputationGraph.addEdge(src, sink);
		}
		
	}
	
	public ReputationGraph getReputationGraph() throws GenericTestbedException
	{
		if(this.reputationGraph.edgeSet().size()<1) throw new GenericTestbedException("No edges in reputation graph."); 
		return this.reputationGraph;
	}
	
	/**
	 * To be called by FeedbackHistoryGraph.notifyObservers() only
	 * Everytime a feedback is added, this method is called.
	 */
	public void update()
	{
		/*
		 * update the reputation graph
		 * for every src and sink node in the experience graph, 
		 * weight = calculateTrustGraph (src, sink)
		 * observer.setEdgeWeight(weight) 
		 */
	
		
		Set<Agent> agents = feedbackHistoryGraph.vertexSet();
		
		for(Agent src : agents)
		{
			for(Agent sink : agents)
			{
				if(!src.equals(sink))
				{
					double trustScore = calculateTrustScore(src, sink);
					ReputationEdge repEdge = this.reputationGraph.getEdge(src, sink);
					this.reputationGraph.setEdgeWeight(repEdge, trustScore);
				}
			}
		}
		
		//need to let the trust algorithms know that something has changed
		this.reputationGraph.notifyObservers();
		
	}
	
	public abstract double calculateTrustScore(Agent src, Agent sink);
	

	
	

}
