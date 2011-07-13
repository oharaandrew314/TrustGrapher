package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends Agent> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<Agent, TestbedEdge> g, Class<? extends Agent> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<Agent, TestbedEdge>, Agent> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
