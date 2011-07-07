package cu.repsystestbed.tests;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.exceptions.GenericTestbedException;
import cu.repsystestbed.graphs.FeedbackHistoryEdgeFactory;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;

public class SimpleTest
{

	public static void main(String[] args) throws Exception
	{
		/*
		 * 1. create feedback history graph
		 * 2. create reputation alg and add it to feedback history graph as an observer
		 * 3. get reputation graph from reputation alg
		 * 4. create trust alg and add it to reputation graph as an observer
		 * 5. inject feedbacks 
		 */
		
		FeedbackHistoryGraph feedbackHistoryGraph = new FeedbackHistoryGraph(new FeedbackHistoryEdgeFactory());
		ReputationAlgorithm repAlg = ReputationAlgorithm.getInstance("blabla");
		repAlg.setFeedbackHistoryGraph(feedbackHistoryGraph);
		ReputationGraph repGraph = repAlg.getReputationGraph();
		TrustAlgorithm trustAlg = TrustAlgorithm.getInstance("blabla");
		repGraph.addObserver(trustAlg);
		
		
	}
}
