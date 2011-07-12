package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<Agent, TestbedEdge>, Agent>> {

	protected Graph<Agent, TestbedEdge> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<Agent, TestbedEdge> g){
		othergraph = g;
	}
        
	@Override
	public boolean evaluate(Context<Graph<Agent,TestbedEdge>, Agent> context) {
		return (othergraph.containsVertex(context.element));
		
	}
	
}
