/////////////////////////////////////TrustGrapher///////////////////////////////
package cu.trustGrapher;

import cu.trustGrapher.loading.*;
import cu.trustGrapher.eventplayer.*;
import cu.trustGrapher.visualizer.*;
import cu.trustGrapher.graphs.SimAbstractGraph;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.List;
import java.util.LinkedList;

import aohara.utilities.*;

/**
 * An application that will display Trust Graphs generated from a series of feedback events.
 * Multiple graphs can be viewed in different layouts and viewing modes
 * The events can be "played" forward and backward, and the graphs will change to display their states after every tick
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public final class TrustGrapher extends JFrame {

    public static final double VERSION = 1.00;
    public static final int TABBED = 0, GRID = 1; //View types
    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    protected TrustMenuBar menuBar;
    protected List<GraphViewer> viewers; //Each of the viewers is a component which displays a graph
    protected List<SimAbstractGraph> graphs;  //A list of the the graph pairs attached to the viewers
    protected ViewerPopupMenu popupMenu;  //The popup menu that is shown when a viewer is right-clicked
    protected PropertyManager config; //The Property Manager that contains all of the saved algorithmLoader for the applet and algorithm loader
    protected Container graphsPanel; //This container contains all of the TrustGraphViewers as components
    protected EventPlayer eventThread; //Plays through the list of events and updates the graphs and its' listeners

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates and initializes TrustGrapher and its' frame and menu bar.  The viewers are not yet created
     */
    public TrustGrapher() {
        String configPath = getClass().getResource("").getPath();
        configPath = configPath.replace("file:", "");
        if (configPath.contains(".jar")) { //if this class is in a jar, save the properties file next to it
            configPath = configPath.substring(0, configPath.indexOf("!")) + "TrustGrapher.properties";
        } else { //Otherwise, save it in the project root
            configPath = "TrustGrapher.properties";
        }
        config = new PropertyManager(configPath);
        initComponents();
        enableMenu(false);
        setVisible(true);
        startAlgorithmLoader();
        enableMenu(true);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public boolean graphsLoaded() {
        return eventThread != null && viewers.get(0) != null && graphsPanel != null;
    }

    /**
     * @return Returns the list of graphs
     */
    public List<SimAbstractGraph> getGraphs() {
        return graphs;
    }

    /**
     * Returns the int representation of the graphsPanel view type.
     * There can be a tabbed or grid view.
     * @return the view type of the graphsPanel
     */
    public Integer getViewType() {
        String s = config.getProperty("viewType");
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return TABBED; //The default view if the property can't be read or doesn't exist
        }
    }

    /**
     * @return Returns the propertyManager
     */
    public PropertyManager getPropertyManager() {
        return config;
    }

    /**
     * Returns a list of TrustGraphViewers that are visible
     * If the view is a tabbed view, then returns the viewer that is in the selected tab
     * If the view is in grid, then returns all of the viewers
     * @return The visible TrustGraphViewers
     */
    public List<GraphViewer> getVisibleViewers() {
        if (graphsPanel instanceof JTabbedPane) {
            List<GraphViewer> visibleViewers = new java.util.LinkedList<GraphViewer>();
            visibleViewers.add((GraphViewer) ((JTabbedPane) graphsPanel).getSelectedComponent());
            return visibleViewers;
        } else {
            return viewers;
        }
    }

    public EventPlayer getEventPlayer() {
        return eventThread;
    }

    /**
     * Returns the TrustMenuBar for the simulation window.  This is necessary
     * despite the getJMenuBar method since there are important fields that must
     * be accessed from the full class, not just the JMenuBar itself.
     * @return the TrustMenuBar class for the TrustGrapher simulation window
     */
    public TrustMenuBar getTrustMenuBar() {
        return menuBar;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the AlgorithmLoader when the user clicks ok.
     * If a log is already being simulated, pause the simulator.
     * Then, build the graphs accoring to the graphConfigs,
     * and then tell the logReader to begin loading the events.
     */
    public void algorithmsLoaded(List<GraphConfig> graphConfigs) {
        //If a log path has been selected by the Algortihm Loader
        if (config.containsKey(AlgorithmLoader.LOG_PATH)) {
            //If there are any events currently loaded, pause the simulator 
            if (graphsLoaded()) {
                eventThread.pause();
            }

            //Build graphs based on graphConfigs
            graphs = GraphLoader.loadGraphs(graphConfigs);

            //Begin reading the log and performing graphConstructionEvents
            LogReader.startReader(this, new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH)), new AreWeThereYet(this));
        } else {
            ChatterBox.alert("No log was loaded, so no action will be taken.");
        }
    }

    /**
     * Creates a new AlgorithmLoader and runs it.  After the user clicks ok, startGraph() will be called.
     * In the meantime, the sumulator will be idle.
     */
    public void startAlgorithmLoader() {
        AlgorithmLoader.run(this, config);
    }

    /**
     * Enables or disables the menus in the menu bar
     * @param enabled true for enabled, false for disabled
     */
    public void enableMenu(boolean enabled) {
        for (Component menu : getJMenuBar().getComponents()) {
            menu.setEnabled(enabled);
        }
    }

///////////////////////////////////Frame Builders///////////////////////////////
    /**
     * Initialize the base frame components.  The viewers and panels are not created yet
     */
    private void initComponents() {
        //Initialize frame
        setTitle("TrustGrapher v" + VERSION + " - Written by Andrew O'Hara");
        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.menuBar = new TrustMenuBar(this);
        setJMenuBar(menuBar.getJMenuBar());
    }

    /**
     * Called by the log reader thread upon completion, or by the EventPlayer upon an event modification,
     * or the view type buttons in the menu bar when the view type is changed.
     * This method resets the mainPane of the simulator window, and adds new TrustGraphViewers to the graphsPanel,
     * then creates and starts a new EventPlayer.
     * @param events The event list returned by the log reader thread or EventPlayer
     */
    public void startGraph(List<TrustLogEvent> events) {
        //Creates a new graphsPanel depending on the view type
        graphsPanel = (getViewType() == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        graphsPanel.setBackground(Color.LIGHT_GRAY);

        //Create viewer listeners and popup menu
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        ViewerListener listener = new ViewerListener();
        popupMenu = new ViewerPopupMenu(gm, listener);

        //Create the Visualization Viewers
        viewers = new LinkedList<GraphViewer>();
        for (SimAbstractGraph graph : graphs) {
            if (graph.isDisplayed()) {
                //Sets the initial layout of the graph.  The graphs must have already had their construction events processed, or the graphs will have a random layout
                AbstractLayout<Agent, TestbedEdge> layout = new FRLayout<Agent, TestbedEdge>(graph.getReferenceGraph());
                layout.setInitializer(new VertexPlacer(new Dimension (DEFWIDTH / 3, DEFHEIGHT /2)));
                //Creates the new GraphViewer
                GraphViewer viewer = new GraphViewer(layout, gm, listener, graph);
                if (graphsPanel instanceof JPanel) {  //If the graphsPanel is set for grid view
                    viewer.setBorder(BorderFactory.createTitledBorder(graph.getDisplayName()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Othwerwise, the graphsPanel is set for Tabbed view, which is the default.
                    ((JTabbedPane) graphsPanel).addTab(graph.getDisplayName(), viewer);
                }
                layout.lock(true); //Locking the layout will prevent the graph entities from moving around
                viewers.add(viewer);
            }
        }

        //Create the eventThread and its listener panels for the simulator
        eventThread = new EventPlayer(this, events);
        eventThread.addEventPlayerListener(new PlaybackPanel(eventThread));
        eventThread.addEventPlayerListener(new LogPanel(eventThread));

        //Create the mainPane, add the graphsPanel and playbackPanel, and add the mainPane to the content pane
        getContentPane().removeAll();  //Necessary to removeAll since there might already be existing viewers present
        JSplitPane secondaryPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphsPanel, eventThread.getPlaybackPanel());
        JSplitPane primaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, secondaryPane, eventThread.getLogPanel());
        primaryPane.setResizeWeight(1);
        secondaryPane.setResizeWeight(1); //Means the size of the playbackPanel will set the position of the divider
        primaryPane.setDividerSize(3);
        secondaryPane.setDividerSize(3);
        getContentPane().add(primaryPane); //The main pane includes the graphsPanel and the playbackPanel
        validate();
    }

////////////////////////////////////Listeners///////////////////////////////////
    /**
     * Listens for events related to the GraphViewer right-click popup menu
     * If a right-click is detected, tells the ViewerPopupMenu to show itself
     * If a button on that menu is clicked, then it notifies the popup menu
     */
    private class ViewerListener extends MouseAdapter implements ActionListener {

        @Override
        /**
         * If a right-click is done on a GraphViewer, display the ViewerPopupMenu
         */
        public void mousePressed(MouseEvent mouseClick) {
            if (SwingUtilities.isRightMouseButton(mouseClick)) {
                popupMenu.showPopupMenu((GraphViewer) mouseClick.getComponent());
            }
        }

        /**
         * If a ViewerPopupMenuEvent is detected, passes the event on to the ViewerPopupMenu button handler
         * @param buttonEvent The Button Press event
         */
        public void actionPerformed(ActionEvent buttonEvent) {
            popupMenu.popupMenuEvent(((AbstractButton) buttonEvent.getSource()).getText());
        }
    }

////////////////////////////////Static Methods//////////////////////////////////
    /**
     * to start this program as a java application
     */
    public static void main(String[] args) {
        TrustGrapher myApp = new TrustGrapher();
    }
}
////////////////////////////////////////////////////////////////////////////////
