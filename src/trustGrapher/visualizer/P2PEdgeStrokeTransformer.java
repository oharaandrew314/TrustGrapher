package trustGrapher.visualizer;



import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

public class P2PEdgeStrokeTransformer implements Transformer<FeedbackHistoryGraphEdge, Stroke> {

    @Override
    public Stroke transform(FeedbackHistoryGraphEdge edge) {
        return new BasicStroke(1.5f); // if it's a regular connection between peers, make it wide

    }

}
