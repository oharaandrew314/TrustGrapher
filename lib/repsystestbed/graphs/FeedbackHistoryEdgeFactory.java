package cu.repsystestbed.graphs;

import org.apache.log4j.Logger;
import org.jgrapht.EdgeFactory;

import cu.repsystestbed.entities.Agent;

public class FeedbackHistoryEdgeFactory implements EdgeFactory<Agent, FeedbackHistoryGraphEdge>
{
	static Logger logger = Logger.getLogger(FeedbackHistoryEdgeFactory.class.getName());
	public FeedbackHistoryEdgeFactory(){}

	public FeedbackHistoryGraphEdge createEdge(Agent src, Agent sink)
	{
		try 
		{
			return new FeedbackHistoryGraphEdge(src, sink);
		} catch (Exception e) {
			
			logger.error(e.getStackTrace());
			System.exit(1);
		}
		return null; //should not come here
	}
	
}






