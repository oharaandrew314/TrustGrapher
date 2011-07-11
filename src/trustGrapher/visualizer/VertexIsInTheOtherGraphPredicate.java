package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<Agent, FeedbackHistoryGraphEdge>, Agent>> {

	protected Graph<Agent, FeedbackHistoryGraphEdge> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<Agent, FeedbackHistoryGraphEdge> g){
		othergraph = g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<Agent, FeedbackHistoryGraphEdge>, Agent> context) {
		return (othergraph.containsVertex(context.element));
		
	}
	
}
