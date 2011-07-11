package trustGrapher.visualizer;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import trustGrapher.graph.AgentWrapper;;

public class P2PVertexStrokeTransformer implements
		Transformer<AgentWrapper, Stroke> {

	@Override
	public Stroke transform(AgentWrapper v) {
            return new BasicStroke(1.0f); // if it's to a document, make it narrow
	}

}