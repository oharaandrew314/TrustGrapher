package trustGrapher.visualizer;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import org.apache.commons.collections15.Predicate;

import trustGrapher.graph.AgentWrapper;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<AgentWrapper, FeedbackHistoryGraphEdge>, AgentWrapper>> {

	protected Graph<AgentWrapper, FeedbackHistoryGraphEdge> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<AgentWrapper, FeedbackHistoryGraphEdge> g){
		othergraph = g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<AgentWrapper, FeedbackHistoryGraphEdge>, AgentWrapper> context) {
		return (othergraph.containsVertex(context.element));
		
	}
	
}
