package trustGrapher.visualizer;

import trustGrapher.graph.TrustConnection;
import trustGrapher.graph.TrustVertex;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;

public class ExclusiveVertexInOtherGraphPredicate extends VertexIsInTheOtherGraphPredicate {

	Class<? extends TrustVertex> exclude;
	
	public ExclusiveVertexInOtherGraphPredicate(Graph<TrustVertex, TrustConnection> g, Class<? extends TrustVertex> exclude) {
		super(g);
		this.exclude = exclude;
	}
	
	@Override
	public boolean evaluate(Context<Graph<TrustVertex, TrustConnection>, TrustVertex> context) {
		
		return (super.evaluate(context) && !(context.element.getClass().equals(exclude)));
		
	}

}
