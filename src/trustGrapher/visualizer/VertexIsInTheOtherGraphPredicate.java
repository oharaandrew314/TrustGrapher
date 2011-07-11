package trustGrapher.visualizer;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import org.apache.commons.collections15.Predicate;

import trustGrapher.graph.MyAgent;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<MyAgent, FeedbackHistoryGraphEdge>, MyAgent>> {

	protected Graph<MyAgent, FeedbackHistoryGraphEdge> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<MyAgent, FeedbackHistoryGraphEdge> g){
		othergraph = g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<MyAgent, FeedbackHistoryGraphEdge>, MyAgent> context) {
		return (othergraph.containsVertex(context.element));
		
	}
	
}
