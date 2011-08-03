/////////////////////////////////////TrustGrapher////////////////////////////////
package cu.trustGrapher;

import cu.trustGrapher.graph.*;
import cu.trustGrapher.visualizer.*;
import cu.trustGrapher.graph.savingandloading.*;
import cu.trustGrapher.visualizer.eventplayer.*;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.util.List;
import java.util.ArrayList;

import utilities.ChatterBox;

/**
 *
 * An applet that will display Trust Graphs generated from a series of feedback events.
 * Multiple graphs can be viewed in different layouts and viewing modes
 * The events can be "played" forward and backward, and the graph will change to display its state at every event
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public class TrustGrapher extends JFrame {

    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    //Each of the viewers in this pane is a component which displays a graph
    private ArrayList<VisualizationViewer<Agent, TestbedEdge>> viewers;
    private AbstractLayout<Agent, TestbedEdge> layout = null;
    private ArrayList<TrustLogEvent> events;
    private ArrayList<SimGraph[]> graphs;  //Each element is an array containing a hidden and dynamic graph
    //The dynamic graph is not shown, but components in the full graph will only be displayed if they exist in the dynamic graph.
    //As events occur, they are added to the dynamic graphs through the graphEvent() method.
    public static final int DYNAMIC = 0;
    //The full graph is the one that is shown, but all vertices and edges that are ever shown must be on the graph before the events start playing
    //The graphConstructionEvent() method is used as the events are parsed to add all components to the graph
    public static final int FULL = 1;
    private Integer viewType; //Keeps track of which graph view to use
    public static final Integer TABBED = 0, GRID = 1, DEFAULT_VIEW = TABBED;
    //This PropertyManager keeps track of all class paths, all algorithms, what viewType to use, etc.
    private TrustPropertyManager config;
    private JTable logList;
    private JPopupMenu rightClickMenu;
    //This container holds all of the viewers
    private Container graphsPanel;
    //When a right-click is done, the viewer that it was done in will be held here.  Useful to know which graph to change the layout for
    private VisualizationViewer currentViewer;
    //This JFrame is used to configure which algorithms to load, what graphs are using what algorithms, and what graphs to display.
    //It saves all of the configurations to TrustPropertyManager config
    public final AlgorithmLoader options;
    JCheckBoxMenuItem toggleLogTable; //A check box which is used to show the log table
    PlaybackPanel playbackPanel; //The panel beneath the graphs which controls playback
    JSplitPane mainPane;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates the TrustGrapher frame and menu bar.  The viewers are not yet loaded
     */
    public TrustGrapher() {
        config = new TrustPropertyManager("TrustApplet.properties");
        options = new AlgorithmLoader(this, config);
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public ArrayList<VisualizationViewer<Agent, TestbedEdge>> getViewers() {
        return viewers;
    }

    /**
     * Creates a List of menu items specific to the current viewer which correspond the the layouts that the 
     * graphs can be swtiched to.  These menu items will be added to a right-click menu
     * @return A list of Menu Items
     */
    private List<JMenuItem> getLayoutItems() {
        List<JMenuItem> menuItems = new java.util.LinkedList<JMenuItem>();
        JMenuItem kkLayout = new JMenuItem("KK Layout");
        JMenuItem frLayout = new JMenuItem("FR Layout");
        JMenuItem isomLayout = new JMenuItem("ISOM Layout");
        JMenuItem circleLayout = new JMenuItem("Circle Layout");

        frLayout.addActionListener(new FRLayoutListener(currentViewer));
        kkLayout.addActionListener(new KKLayoutListener(currentViewer));
        isomLayout.addActionListener(new ISOMLayoutListener(currentViewer));
        circleLayout.addActionListener(new CircleLayoutListener(currentViewer));

        menuItems.add(circleLayout);
        menuItems.add(frLayout);
        menuItems.add(isomLayout);
        menuItems.add(kkLayout);
        return menuItems;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the AlgorithmLoader when the user clicks ok.
     * If a log is already being simulated, remove the events, and pause the simulator
     * Then, based on the selected algorithms and the view mode, the playback panel and graph viewers are created
     */
    public void loadAlgorithms() {
        if (config.containsKey(AlgorithmLoader.LOG_PATH)) {
            if (events != null) {
                events.clear();
                playbackPanel.eventThread.stopPlayback();
                playbackPanel.disableButtons();
            }

            GraphLoader graphLoader = new GraphLoader(config);
            graphs = graphLoader.getGraphs();
            java.io.File logFile = new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH));
            LogReader logReader = new LogReader(this, playbackPanel.getLoadingBar(), graphs, logFile);
            logReader.execute(); //After the reader thread is complete, startGraph() will be called
        }
    }

    /**
     * Called by the log reader thread upon completion.  Builds the graph viewers and starts the graph
     * @param events The event list returned by the evetn reader thread
     */
    public void startGraph(ArrayList<TrustLogEvent> events) {
        this.events = events;
        buildViewers();
        playbackPanel.eventThread.run();
        if (toggleLogTable.isSelected()) {
            toggleLogTable.setSelected(false);
            toggleLogTable.doClick();
        }
    }

///////////////////////////////////Frame Builders///////////////////////////////
    /**
     * Initialize the base frame components.  The viewers and playback panel are not created yet
     */
    private void initComponents() {
        setTitle("Trust Grapher - Written by Andrew O'Hara");
        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
        setJMenuBar(createFileMenu());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        playbackPanel = new PlaybackPanel(this, logList);
        mainPane.add(new JPanel());
        mainPane.add(playbackPanel);
        mainPane.setResizeWeight(1);
        mainPane.setDividerSize(3);
        add(mainPane);
        pack();
        validate();
    }

    /**
     * Creates the file menu components
     * @return The JMenuBar to be added to the applet
     */
    private JMenuBar createFileMenu() {

        //Create the algorithm loader button
        JMenuItem optionsButton = new JMenuItem("Load Algorithms");
        optionsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!options.isVisible()) {
                    playbackPanel.pauseButton.doClick();
                    options.run();
                }
            }
        });

        //Create the exit button
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    playbackPanel.pauseButton.doClick();
                } catch (NullPointerException ex) {
                }
                if (ChatterBox.yesNoDialog("Are you sure you want to exit?")) {
                    System.exit(0);
                }
            }
        });

        //Create the tabbed view radio button
        JRadioButtonMenuItem tabbedView = new JRadioButtonMenuItem("Tabbed View");
        tabbedView.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                config.setProperty("viewType", "" + TABBED);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != TABBED) {
                    viewType = TABBED;
                    loadAlgorithms();
                } else {
                    viewType = TABBED;
                }
            }
        });

        //Create the grid view radio button
        JRadioButtonMenuItem gridView = new JRadioButtonMenuItem("Grid View");
        gridView.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                config.setProperty("viewType", "" + GRID);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != GRID) {
                    viewType = GRID;
                    loadAlgorithms();
                } else {
                    viewType = GRID;
                }
            }
        });

        //Set view type from value in config
        try {
            viewType = Integer.parseInt(config.getProperty("viewType"));
        } catch (NumberFormatException ex) {
            viewType = TrustGrapher.DEFAULT_VIEW;
        }

        //Update the radio buttons to show the view type
        if (viewType == GRID) {
            gridView.setSelected(true);
        } else {
            tabbedView.setSelected(true);
        }

        //Create the log table button
        toggleLogTable = new JCheckBoxMenuItem("Toggle Log Table");
        toggleLogTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (events != null) { //graph has been initialized
                    JCheckBoxMenuItem button = (JCheckBoxMenuItem) arg0.getSource();
                    if (button.isSelected()) { //Add the log table
                        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                        p.setResizeWeight(1);
                        p.add(mainPane);
                        p.add(initializeLogList(events));
                        p.setDividerSize(3);

                        getContentPane().add(p);
                        validate();
                    } else { //Remove the log table
                        getContentPane().removeAll();
                        getContentPane().add(mainPane);
                        validate();
                    }
                }
            }
        });

        //Create the file menu
        JMenu file = new JMenu("File");
        file.add(optionsButton);
        file.addSeparator();
        file.add(exit);

        //Create the view menu
        JMenu view = new JMenu("View");
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(tabbedView);
        viewGroup.add(gridView);
        view.add(tabbedView);
        view.add(gridView);
        view.addSeparator();
        view.add(toggleLogTable);

        //Add the menus to the menu bar
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(view);
        bar.setVisible(true);
        return bar;
    }

    /**
     * Creates the log list panel which shows the events in the current log
     * @param logEvents The list of log events
     * @return The JPanel containing the log list
     */
    private JPanel initializeLogList(List<TrustLogEvent> logEvents) {
        Object[][] table = new Object[logEvents.size() - 1][3];
        int i = 0;
        for (TrustLogEvent evt : logEvents) {
            if (evt.getAssessor() != -1) { //If the event isn't a start or end event
                table[i] = evt.toArray();
                i++;
            }
        }
        Object[] titles = {"Assessor", "Assessee", "Feedback"};
        logList = new JTable(table, titles);
        logList.setBackground(Color.LIGHT_GRAY);
        logList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        logList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        logList.setEnabled(false);
        logList.setColumnSelectionAllowed(false);
        logList.setVisible(true);

        JScrollPane listScroller = new JScrollPane(logList);
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
        listScroller.setSize(logList.getWidth(), logList.getHeight());

        JPanel tablePanel = new JPanel(new GridLayout(1, 1));
        tablePanel.add(listScroller);
        tablePanel.setName("Log Events");
        tablePanel.setBorder(BorderFactory.createTitledBorder(tablePanel.getName()));

        return tablePanel;
    }

    /**
     * Called when the algorithms have been loaded and the graph is to be started
     * This method builds the viewing panes and adds them to the main pane
     */
    public void buildViewers() {
        graphsPanel = (viewType == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        graphsPanel.setBackground(Color.LIGHT_GRAY);
        mainPane.removeAll();
        mainPane.add(graphsPanel);
        mainPane.add(playbackPanel);
        playbackPanel.resetPanel(events, graphs);

        //Build all the viewing panes
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        GraphMouseListener graphListener = new GraphMouseListener();
        initializeMouseContext(gm);
        graphsPanel.removeAll();
        viewers = new ArrayList<VisualizationViewer<Agent, TestbedEdge>>();

        //Create the Visualization Viewers
        for (SimGraph[] graph : graphs) {
            if (graph[FULL].isDisplayed()) {
                layout = new FRLayout2<Agent, TestbedEdge>(graph[FULL]);
                layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH / 3, DEFHEIGHT / 2)));
                viewers.add((VisualizationViewer) visualizationViewerBuilder(layout, DEFWIDTH / 3, DEFHEIGHT / 2, gm));
                VisualizationViewer<Agent, TestbedEdge> viewer = viewers.get(viewers.size() - 1);
                viewer.addMouseListener(graphListener);

                //Set the graph component transformers
                viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(VertexShapeType.ELLIPSE)); //vertex shape type
                viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
                viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width
                viewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graph[DYNAMIC]));
                viewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph[DYNAMIC]));

                //Create the view mode components
                if (viewType == GRID) {  //This assumes that the graphsPanel is already in GridFormat
                    viewer.setBorder(BorderFactory.createTitledBorder(graph[FULL].toString()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Tabbed view by default.  This assumes that the graphsPanel is already a JSplitPane
                    ((JTabbedPane) graphsPanel).addTab(graph[FULL].toString(), viewer);
                }
            }
        }
        
        layout.lock(true);
        validate();
    }

    /**
     * This creates a viewer component
     * The graph passed in by the layout paramter is displayed inside this viewer
     * @return The Initialized Visualization Viewer
     */
    private VisualizationViewer<Agent, TestbedEdge> visualizationViewerBuilder(final Layout<Agent, TestbedEdge> layout, int width, int height, GraphMouse gm) {
        VisualizationViewer<Agent, TestbedEdge> viewer = new VisualizationViewer<Agent, TestbedEdge>(layout, new Dimension(width, height));
        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        viewer.setGraphMouse(gm);

        //set graph rendering parameters & functions
        viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //the vertex labeler will use the tostring method which is fine, the Agent class has an appropriate toString() method implementation
        viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Agent>());
        viewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<TestbedEdge>());
        viewer.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer(viewer.getPickedVertexState(), Color.red, Color.yellow));
        // Agent objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.

        viewer.getRenderContext().setVertexStrokeTransformer(new P2PVertexStrokeTransformer());
        viewer.setForeground(Color.white);
        viewer.setBackground(Color.GRAY);
        viewer.setBounds(0, 0, width, height);

        viewer.addComponentListener(new ComponentAdapter() {

            /**
             * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
             */
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
            }
        });
        if (viewer == null) {
            ChatterBox.criticalError(null, this, "visualizationViewerBuilder()", "The viewer is null.");
        }

        return viewer;
    }

////////////////////////////////////Listeners///////////////////////////////////
    /**
     * Initializes the right-click menu components and listeners
     * @param gm
     */
    private void initializeMouseContext(final DefaultModalGraphMouse<Agent, TestbedEdge> gm) {
        rightClickMenu = new JPopupMenu("Mouse Mode");
        JMenuItem picking = new JMenuItem("Picking");
        picking.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gm.setMode(Mode.PICKING);
                int size = rightClickMenu.getComponentCount();
                for (int i = size - 1; i > 4; i--) {
                    rightClickMenu.remove(i);
                }
                rightClickMenu.setVisible(false);
                rightClickMenu.setEnabled(false);
            }
        });

        JMenuItem transforming = new JMenuItem("Transforming");
        transforming.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gm.setMode(Mode.TRANSFORMING);
                int size = rightClickMenu.getComponentCount();
                for (int i = size - 1; i > 4; i--) {
                    rightClickMenu.remove(i);
                }
                rightClickMenu.setVisible(false);
                rightClickMenu.setEnabled(false);
            }
        });
        rightClickMenu.add("Mouse Mode:").setEnabled(false);
        rightClickMenu.add(picking);
        rightClickMenu.add(transforming);
        rightClickMenu.addSeparator();
        rightClickMenu.add("Set Layout:").setEnabled(false);
    }

    /**
     * an actionlistener that defines the use of a button to stop the spring-layout processing
     * @author adavoust
     *@author Andrew O'Hara
     */
    private class GraphMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            //Remove any menus that are visible
            if (rightClickMenu.isVisible()) {
                for (int i = rightClickMenu.getComponentCount() - 1; i > 4; i--) {
                    rightClickMenu.remove(i);
                }
                rightClickMenu.setVisible(false);
                rightClickMenu.setEnabled(false);
            }
            doPop(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (System.getProperty("os.name").toLowerCase().equals("windows")) {
                doPop(e);  //Windows only shows the popup menu on right-click release.  Silly Windows.
            }
        }

        /**
         * Called when the right-click menu is to be displayed
         * @param e The mouse event which triggered the menu to be displayed
         */
        private void doPop(MouseEvent e) {
            currentViewer = (VisualizationViewer) e.getComponent();
            //If this is a right-click, add the menu
            if (e.isPopupTrigger()) {
                for (JMenuItem item : getLayoutItems()) {
                    rightClickMenu.add(item);
                }
                rightClickMenu.setEnabled(true);
                rightClickMenu.show(currentViewer, currentViewer.getMousePosition().x, currentViewer.getMousePosition().y);
            }
        }
    }

    class FRLayoutListener implements ActionListener {

        VisualizationViewer<Agent, TestbedEdge> vv;

        public FRLayoutListener(VisualizationViewer<Agent, TestbedEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = new FRLayout<Agent, TestbedEdge>(graphs.get(viewers.indexOf(currentViewer))[FULL], vv.getSize());
            vv.getModel().setGraphLayout(graphLayout);
            rightClickMenu.setVisible(false);
            rightClickMenu.setEnabled(false);
            int size = rightClickMenu.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                rightClickMenu.remove(i);
            }
            graphLayout.lock(true);
        }
    }

    class ISOMLayoutListener implements ActionListener {

        VisualizationViewer<Agent, TestbedEdge> vv;

        public ISOMLayoutListener(VisualizationViewer<Agent, TestbedEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = new ISOMLayout<Agent, TestbedEdge>(graphs.get(viewers.indexOf(currentViewer))[FULL]);
            vv.getModel().setGraphLayout(graphLayout);
            rightClickMenu.setVisible(false);
            rightClickMenu.setEnabled(false);
            int size = rightClickMenu.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                rightClickMenu.remove(i);
            }
            graphLayout.lock(true);
        }
    }

    class KKLayoutListener implements ActionListener {

        VisualizationViewer<Agent, TestbedEdge> vv;

        public KKLayoutListener(VisualizationViewer<Agent, TestbedEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = new KKLayout<Agent, TestbedEdge>(graphs.get(viewers.indexOf(currentViewer))[FULL]);
            vv.getModel().setGraphLayout(graphLayout);
            rightClickMenu.setVisible(false);
            rightClickMenu.setEnabled(false);
            int size = rightClickMenu.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                rightClickMenu.remove(i);
            }
            graphLayout.lock(true);
        }
    }

    class CircleLayoutListener implements ActionListener {

        VisualizationViewer<Agent, TestbedEdge> vv;

        public CircleLayoutListener(VisualizationViewer<Agent, TestbedEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = new CircleLayout<Agent, TestbedEdge>(graphs.get(viewers.indexOf(currentViewer))[FULL]);
            vv.getModel().setGraphLayout(graphLayout);
            rightClickMenu.setVisible(false);
            rightClickMenu.setEnabled(false);
            int size = rightClickMenu.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                rightClickMenu.remove(i);
            }
            graphLayout.lock(true);
        }
    }

////////////////////////////////Static Methods//////////////////////////////////
    /**
     * to run this applet as a java application
     */
    public static void main(String[] args) {
        TrustGrapher myApp = new TrustGrapher();
        myApp.setVisible(true);
    }
}
