////////////////////////////////TrustGraphViewer//////////////////////////////////
package cu.trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.graph.SimGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;

/**
 * This is a TrustGraphViewer component
 * It displays the graph passed to it from inside the layout parameter
 * @author Andrew O'Hara
 */
public class TrustGraphViewer extends edu.uci.ics.jung.visualization.VisualizationViewer {

//////////////////////////////////Constructor///////////////////////////////////
    public TrustGraphViewer(final Layout layout, int width, int height, DefaultModalGraphMouse<Agent, TestbedEdge> gm, MouseAdapter mouseClickListener, SimGraph[] graph) {
        super(layout, new Dimension(width, height));
        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        setGraphMouse(gm);

        //the vertex labeler will use the tostring method which is fine, the Agent class has an appropriate toString() method implementation
        getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Agent>());
        getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //the Edge labeler will use the tostring method which is fine, each testbedEdge subclass has an appropriate toString() method implementation
        getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<TestbedEdge>());
        //Sets the predicates which decide whether the vertices and edges are displayed
        getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graph[SimGraph.DYNAMIC]));
        getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph[SimGraph.DYNAMIC]));

        setForeground(Color.white);
        setBackground(Color.GRAY);
        setBounds(0, 0, width, height);
        validate();
        addMouseListener(mouseClickListener); //This listener handles the mouse clicks to see if a popup event was done

        addComponentListener(new ComponentAdapter() {

            /**
             * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
            }
        });
    }
}
////////////////////////////////////////////////////////////////////////////////

