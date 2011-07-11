package trustGrapher.visualizer;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import trustGrapher.graph.AgentWrapper;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends AgentWrapper> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<AgentWrapper, FeedbackHistoryGraphEdge> g, Class<? extends AgentWrapper> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<AgentWrapper, FeedbackHistoryGraphEdge>, AgentWrapper> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
