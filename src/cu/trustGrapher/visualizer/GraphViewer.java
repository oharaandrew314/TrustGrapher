////////////////////////////////TrustGraphViewer//////////////////////////////////
package cu.trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.graphs.JungAdapterGraph;
import cu.trustGrapher.graphs.SimAbstractGraph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.Color;
import java.awt.event.MouseAdapter;

/**
 * This is a GraphViewer component.  The viewers are created in TrustGrapher after the events have been loaded
 * Each GraphViewer displays the graph passed to it.
 * @author Andrew O'Hara
 */
public class GraphViewer extends edu.uci.ics.jung.visualization.VisualizationViewer {
    private JungAdapterGraph fullGraph;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a new GraphViewer.
     * @param layout Manages how the graph entities will be organized in the viewer window
     * @param gm This mouse adapter handles the selection and dragging of the GraphViewer
     * @param mouseClickListener //Listener for GraphViewer right-click menu
     * @param graph The graph that this viewer will display
     */
    public GraphViewer(final Layout layout, DefaultModalGraphMouse<Agent, TestbedEdge> gm, MouseAdapter mouseClickListener, SimAbstractGraph graph) {
        super(layout);
        this.fullGraph = graph.getReferenceGraph();
        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        setGraphMouse(gm);
        addMouseListener(mouseClickListener); //This listener handles the mouse clicks to see if a popup event was done

        //the vertex labeler will use the tostring method which is fine, the Agent class has an appropriate toString() method implementation
        getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Agent>());
        getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //the Edge labeler will use the tostring method which is fine, each testbedEdge subclass has an appropriate toString() method implementation
        getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<TestbedEdge>());
        //Sets the predicates which decide whether the vertices and edges are displayed
        getRenderContext().setVertexIncludePredicate(graph);
        getRenderContext().setEdgeIncludePredicate(graph);

        setForeground(Color.white);
        setBackground(Color.GRAY);
    }

    /**
     * @return Returns the fullGraph of the graph that is displayed by this GraphViewer
     */
    public JungAdapterGraph getFullGraph(){
        return fullGraph;
    }
}
////////////////////////////////////////////////////////////////////////////////

