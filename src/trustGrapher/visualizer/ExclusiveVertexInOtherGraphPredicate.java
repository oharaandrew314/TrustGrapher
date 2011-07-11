package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends Agent> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<Agent, FeedbackHistoryGraphEdge> g, Class<? extends Agent> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<Agent, FeedbackHistoryGraphEdge>, Agent> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
