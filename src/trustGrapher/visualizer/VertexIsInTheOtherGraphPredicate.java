package trustGrapher.visualizer;

import org.apache.commons.collections15.Predicate;

import trustGrapher.graph.TrustConnection;
import trustGrapher.graph.TrustVertex;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class VertexIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<TrustVertex, TrustConnection>, TrustVertex>> {

	protected Graph<TrustVertex, TrustConnection> othergraph;
	
	public VertexIsInTheOtherGraphPredicate(Graph<TrustVertex, TrustConnection> g){
		othergraph = g;
		
	}
	@Override
	public boolean evaluate(Context<Graph<TrustVertex, TrustConnection>, TrustVertex> context) {
		return (othergraph.containsVertex(context.element));
		
	}
	
}
