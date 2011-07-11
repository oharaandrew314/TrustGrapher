package trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

public class P2PVertexStrokeTransformer implements
		Transformer<Agent, Stroke> {

	@Override
	public Stroke transform(Agent v) {
            return new BasicStroke(1.0f); // if it's to a document, make it narrow
	}

}
