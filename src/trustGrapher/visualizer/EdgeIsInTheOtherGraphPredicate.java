package trustGrapher.visualizer;

import org.apache.commons.collections15.Predicate;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import trustGrapher.graph.FeedbackHistoryGraph;
import trustGrapher.graph.MyAgent;

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
public class EdgeIsInTheOtherGraphPredicate implements Predicate<Context<Graph<MyAgent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge>> {

    private Graph<MyAgent, FeedbackHistoryGraphEdge> othergraph;

    public EdgeIsInTheOtherGraphPredicate(Graph<MyAgent, FeedbackHistoryGraphEdge> g) {
        othergraph = g;

    }

    public EdgeIsInTheOtherGraphPredicate(FeedbackHistoryGraph visibleGraph) {

        othergraph = visibleGraph;
    }

    @Override
    public boolean evaluate(Context<Graph<MyAgent, FeedbackHistoryGraphEdge>, FeedbackHistoryGraphEdge> context) {
        return (othergraph.containsEdge(context.element));
    }
}
