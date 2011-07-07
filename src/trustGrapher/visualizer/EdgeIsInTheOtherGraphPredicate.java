package trustGrapher.visualizer;

import org.apache.commons.collections15.Predicate;

import trustGrapher.graph.TrustConnection;
import trustGrapher.graph.TrustGraph;
import trustGrapher.graph.TrustVertex;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

/**
 * This class is a predicate, and as such the logical predicate implemented is the following :
 * the predicate is related to a particular graph, and evaluates if the given vertex exists within the associated graph.
 * This is used because the simulation contains two graphs, one partly visible, that maintains the full layout,
 * and one invisible, which simply stores the information of the graph state as the simulation proceeds.
 * 
 * @author alan
 *
 */
public class EdgeIsInTheOtherGraphPredicate implements
		Predicate<Context<Graph<TrustVertex, TrustConnection>, TrustConnection>> {

	private Graph<TrustVertex, TrustConnection> othergraph;
	
	public EdgeIsInTheOtherGraphPredicate(Graph<TrustVertex, TrustConnection> g){
		othergraph= g;
		
	}

    public EdgeIsInTheOtherGraphPredicate(TrustGraph visibleGraph) {
        othergraph = visibleGraph;
    }
	@Override
	public boolean evaluate(Context<Graph<TrustVertex, TrustConnection>, TrustConnection> context) {
		
		return (othergraph.containsEdge(context.element));
		
	}
	

}
