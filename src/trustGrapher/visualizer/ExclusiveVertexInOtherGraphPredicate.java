package trustGrapher.visualizer;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import trustGrapher.graph.MyAgent;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends MyAgent> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<MyAgent, FeedbackHistoryGraphEdge> g, Class<? extends MyAgent> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<MyAgent, FeedbackHistoryGraphEdge>, MyAgent> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
