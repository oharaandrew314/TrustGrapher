////////////////////////////////TrustPopupMenu//////////////////////////////////
package cu.trustGrapher.visualizer;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import aohara.utilities.ChatterBox;

/**
 * Initializes the right-click menu components and event handlers (Does not contain the listeners)
 * @author Andrew O'Hara
 */
public class ViewerPopupMenu extends JPopupMenu {

    private DefaultModalGraphMouse<Agent, TestbedEdge> gm;
    private GraphViewer currentViewer;

    public ViewerPopupMenu(final DefaultModalGraphMouse<Agent, TestbedEdge> gm, ActionListener listener) {
        this.gm = gm;
        JMenuItem picking = new JMenuItem("Picking");
        JMenuItem transforming = new JMenuItem("Transforming");
        JMenuItem kkLayout = new JMenuItem("KK Layout");
        JMenuItem frLayout = new JMenuItem("FR Layout");
        JMenuItem isomLayout = new JMenuItem("ISOM Layout");
        JMenuItem circleLayout = new JMenuItem("Circle Layout");

        picking.addActionListener(listener);
        transforming.addActionListener(listener);
        kkLayout.addActionListener(listener);
        frLayout.addActionListener(listener);
        isomLayout.addActionListener(listener);
        circleLayout.addActionListener(listener);

        add("Mouse Mode:").setEnabled(false);
        add(picking);
        add(transforming);
        addSeparator();
        add("Set Layout:").setEnabled(false);
        add(circleLayout);
        add(frLayout);
        add(isomLayout);
        add(kkLayout);
    }

    /**
     * Shows the popup menu on the current viewer
     */
    public void showPopupMenu(GraphViewer newViewer) {
        currentViewer = newViewer;
        setEnabled(true);
        show(currentViewer, currentViewer.getMousePosition().x, currentViewer.getMousePosition().y);
    }

    /**
     * Handles popup menu button clicks
     * @param text The text of the button that was just clicked
     */
    public void popupMenuEvent(String text) {
        if (text.contains(("Layout"))) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = null;
            if (text.equals("FR Layout")) {
                graphLayout = new FRLayout<Agent, TestbedEdge>(currentViewer.getFullGraph(), currentViewer.getSize());
            } else if (text.equals("ISOM Layout")) {
                graphLayout = new ISOMLayout<Agent, TestbedEdge>(currentViewer.getFullGraph());
            } else if (text.equals("KK Layout")) {
                graphLayout = new KKLayout<Agent, TestbedEdge>(currentViewer.getFullGraph());
            } else {
                graphLayout = new CircleLayout<Agent, TestbedEdge>(currentViewer.getFullGraph());
            }
            currentViewer.getModel().setGraphLayout(graphLayout);
            graphLayout.lock(true);
        } else if (text.equals("Picking")) {
            gm.setMode(Mode.PICKING);
        } else if (text.equals("Transforming")) {
            gm.setMode(Mode.TRANSFORMING);
        } else {
            ChatterBox.error(this, "ActionPerformed()", "Uncaught menu action");
            return;
        }
        setVisible(false);
        setEnabled(false);
    }
}
////////////////////////////////////////////////////////////////////////////////
