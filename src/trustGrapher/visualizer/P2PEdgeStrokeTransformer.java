package trustGrapher.visualizer;



import cu.repsystestbed.graphs.TestbedEdge;
import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

public class P2PEdgeStrokeTransformer implements Transformer<TestbedEdge, Stroke> {

    @Override
    public Stroke transform(TestbedEdge edge) {
        return new BasicStroke(1.5f); // if it's a regular connection between peers, make it wide

    }

}
