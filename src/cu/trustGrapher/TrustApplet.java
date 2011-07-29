/////////////////////////////////////TrustApplet////////////////////////////////
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import utilities.AreWeThereYet;

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
public class TrustApplet extends JApplet implements EventPlayerListener {

    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    //Each of the viewers in this pane is a component which displays a graph
    private ArrayList<VisualizationViewer<Agent, TestbedEdge>> viewers;
    private AbstractLayout<Agent, TestbedEdge> layout = null;
    private LinkedList<TrustLogEvent> events;
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
    protected JTable logList;
    protected JButton fastForwardButton, forwardButton, pauseButton, reverseButton, fastReverseButton;
    protected JSlider fastSpeedSlider, playbackSlider;
    protected JPopupMenu rightClickMenu;
    protected TrustEventPlayer eventThread;
    //This container holds all of the viewers
    private Container graphsPanel;
    //When a right-click is done, the viewer that it was done in will be held here.  Useful to know which graph to change the layout for
    private VisualizationViewer currentViewer;
    //This JFrame is used to configure which algorithms to load, what graphs are using what algorithms, and what graphs to display.
    //It saves all of the configurations to TrustPropertyManager config
    public final AlgorithmLoader options;
    JCheckBoxMenuItem toggleLogTable; //A check box which is used to show the log table
    AreWeThereYet loadingBar; //A JDialog which displays a loading bar

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates the TrustApplet frame and menu.  The viewers are not yet loaded
     */
    public TrustApplet() {
        config = new TrustPropertyManager("TrustApplet.properties");
        options = new AlgorithmLoader(this, config);

        //Initializes basic frame components.  The rest are started when a graph is loaded
        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
        setJMenuBar(createFileMenu());

        JFrame frame = new JFrame("Trust Grapher - Written by Andrew O'Hara");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);

        loadingBar = new AreWeThereYet(frame);
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

    /**
     * Sets the shape transformers for the vertices and edges
     * @param viewer
     * @param peerShape
     */
    private void initSpecialTransformers(VisualizationViewer<Agent, TestbedEdge> viewer,
            VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
            EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {

        viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape));
        viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
        viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width

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
                    options.run();
                }
            }
        });

        //Create the exit button
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                try {
                    pauseButton.doClick();
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
            viewType = TrustApplet.DEFAULT_VIEW;
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
                    if (button.isSelected()) {
                        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                        p.setResizeWeight(1);
                        p.add(getContentPane().getComponent(0));
                        p.add(initializeLogList(events));
                        p.setDividerSize(3);

                        getContentPane().add(p);
                        validate();
                    } else {
                        JSplitPane p = (JSplitPane) ((JSplitPane) getContentPane().getComponent(0)).getLeftComponent();
                        getContentPane().removeAll();
                        getContentPane().add(p);
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
     * @param logEvents
     * @return
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
     * Helper Method for initializing the Buttons and slider for the South Panel.
     * @return The South Panel, laid out properly, to be displayed.
     */
    private JPanel initializeSouthPanel() {

        fastSpeedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
        fastSpeedSlider.addChangeListener(new SpeedSliderListener());
        fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum() - fastSpeedSlider.getMinimum()) / 4);
        fastSpeedSlider.setFont(new Font("Arial", Font.PLAIN, 8));
        fastSpeedSlider.setPaintTicks(false);
        fastSpeedSlider.setPaintLabels(true);
        fastSpeedSlider.setForeground(Color.BLACK);
        fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Quick Playback Speed"));
        fastSpeedSlider.setEnabled(false);

        fastReverseButton = new JButton("<|<|");
        fastReverseButton.addActionListener(new FastReverseButtonListener());
        fastReverseButton.setEnabled(false);

        reverseButton = new JButton("<|");
        reverseButton.addActionListener(new ReverseButtonListener());
        reverseButton.setEnabled(false);

        pauseButton = new JButton("||");
        pauseButton.addActionListener(new PauseButtonListener());
        pauseButton.setEnabled(false);

        forwardButton = new JButton("|>");
        forwardButton.addActionListener(new ForwardButtonListener());
        forwardButton.setEnabled(false);
        //forwardButton.setIcon(new ImageIcon(getClass().getResource("/trustGrapher/resources/forward.png")));
        //forwardButton.setSize(48,25);

        fastForwardButton = new JButton("|>|>");
        fastForwardButton.addActionListener(new FastForwardButtonListener());
        fastForwardButton.setEnabled(false);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setEnabled(false);

        GridBagLayout southLayout = new GridBagLayout();
        GridBagConstraints southConstraints = new GridBagConstraints();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(southLayout);

        buttonPanel.add(fastReverseButton);
        buttonPanel.add(reverseButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(fastForwardButton);
        southConstraints.gridwidth = GridBagConstraints.REMAINDER;//make each item take up a whole line
        southLayout.setConstraints(fastSpeedSlider, southConstraints);
        buttonPanel.add(fastSpeedSlider);


        JPanel south = new JPanel();
        south.setLayout(new GridLayout(2, 1));
        south.setBorder(BorderFactory.createTitledBorder("Playback Panel"));
        south.add(buttonPanel);
        south.add(playbackSlider);

        return south;
    }

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
     * This is called when algorithms have been loaded.
     *
     * It creates the south window, all the viewers, and organizes them according to the user-selected view mode
     */
    private void initGraphComponents() {
        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        graphsPanel = (viewType == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        graphsPanel.setBackground(Color.LIGHT_GRAY);
        mainPane.setResizeWeight(1);
        mainPane.add(graphsPanel);
        mainPane.add(initializeSouthPanel());
        mainPane.setDividerSize(3);
        getContentPane().removeAll();
        getContentPane().add(mainPane);
        validate();
    }

    /**
     * Once algorithms have been has been loaded, build the viewing panes and start the graph
     */
    public void startGraph() {

        //Build all the viewing panes
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        GraphMouseListener graphListener = new GraphMouseListener();
        initializeMouseContext(gm);
        graphsPanel.removeAll();
        viewers = new ArrayList<VisualizationViewer<Agent, TestbedEdge>>();

        //Create the Visualization Viewers
        int i = 0;
        for (SimGraph[] graph : graphs) {
            if (graph[FULL].isDisplayed()) {
                layout = new FRLayout2<Agent, TestbedEdge>(graph[FULL]);
                layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH / 3, DEFHEIGHT / 2)));
                viewers.add((VisualizationViewer) visualizationViewerBuilder(layout, DEFWIDTH / 3, DEFHEIGHT / 2, gm));
                VisualizationViewer<Agent, TestbedEdge> viewer = viewers.get(i);
                viewer.addMouseListener(graphListener);
                initSpecialTransformers(viewer, VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.RECTANGLE, EdgeShapeType.QUAD_CURVE, EdgeShapeType.CUBIC_CURVE, EdgeShapeType.LINE, EdgeShapeType.LINE);
                viewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graph[DYNAMIC]));
                viewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graph[DYNAMIC]));

                //Create the view mode components
                if (viewType == GRID) {  //This assumes that the graphsPanel is already in GridFormat
                    viewer.setBorder(BorderFactory.createTitledBorder(graph[FULL].toString()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Tabbed view by default.  This assumes that the graphsPanel is already a JSplitPane
                    ((JTabbedPane) graphsPanel).addTab(graph[FULL].toString(), viewer);
                }
                i++;
            }
        }
        graphsPanel.validate();

        if (events.isEmpty()) {
            SliderListener s = new SliderListener();

            playbackSlider.setMaximum(0);
            playbackSlider.addChangeListener(s);
            playbackSlider.addMouseListener(s);

            /// create the event player
            eventThread = new TrustEventPlayer(graphs);
            eventThread.addEventPlayerListener(this);
        } else {
            SliderListener s = new SliderListener();
            playbackSlider.setMinimum(0);
            playbackSlider.setMaximum((int) events.getLast().getTime());
            playbackSlider.addChangeListener(s);
            playbackSlider.addMouseListener(s);

            /// create the event player
            eventThread = new TrustEventPlayer(graphs, events, playbackSlider);
            eventThread.addEventPlayerListener(this);
        }

        fastReverseButton.setEnabled(true);
        reverseButton.setEnabled(true);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(false);
        fastForwardButton.setEnabled(true);
        playbackSlider.setEnabled(true);
        fastSpeedSlider.setEnabled(true);

        layout.lock(true);

        doRepaint();
        eventThread.run();
    }

    public List<JMenuItem> getLayoutItems() {
        List<JMenuItem> menuItems = new LinkedList<JMenuItem>();
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

    @Override
    public void playbackFastReverse() {
        fastReverseButton.setEnabled(false);
        reverseButton.setEnabled(true);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(true);
        fastForwardButton.setEnabled(true);
    }

    @Override
    public void playbackReverse() {
        fastReverseButton.setEnabled(true);
        reverseButton.setEnabled(false);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(true);
        fastForwardButton.setEnabled(true);
    }

    @Override
    public void playbackPause() {
        if (eventThread.atFront()) {
            fastReverseButton.setEnabled(false);
            reverseButton.setEnabled(false);
        } else {
            fastReverseButton.setEnabled(true);
            reverseButton.setEnabled(true);
        }
        pauseButton.setEnabled(false);
        if (eventThread.atBack()) {
            forwardButton.setEnabled(false);
            fastForwardButton.setEnabled(false);
        } else {
            forwardButton.setEnabled(true);
            fastForwardButton.setEnabled(true);
        }
    }

    @Override
    public void playbackForward() {
        fastReverseButton.setEnabled(true);
        reverseButton.setEnabled(true);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(false);
        fastForwardButton.setEnabled(true);
    }

    @Override
    public void playbackFastForward() {
        fastReverseButton.setEnabled(true);
        reverseButton.setEnabled(true);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(true);
        fastForwardButton.setEnabled(false);
    }

    @Override
    public void doRepaint() {
        for (VisualizationViewer<Agent, TestbedEdge> viewer : viewers) {
            viewer.repaint();
        }
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
            if (rightClickMenu.isVisible()){
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

    class FastReverseButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            eventThread.fastReverse();
        }
    }

    /**
     * An ActionListener that defines the action of the reverse button for the applet
     * @author Matthew
     */
    class ReverseButtonListener implements ActionListener {

        /**
         * Method called when the reverse button has an action performed(clicked)
         * Tells eventThread to traverse the graph placement in reverse.
         * @param ae	The ActionEvent that triggered the listener
         */
        public void actionPerformed(ActionEvent ae) {
            eventThread.reverse();
        }
    }

    class PauseButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            eventThread.pause();
        }
    }

    class ForwardButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            eventThread.forward();
        }
    }

    class FastForwardButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            eventThread.fastForward();
        }
    }

    class SpeedSliderListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent arg0) {
            eventThread.setFastSpeed(((JSlider) arg0.getSource()).getValue());
        }
    }

    class SliderListener extends MouseAdapter implements ChangeListener {

        PlayState prevState = PlayState.PAUSE;

        @Override
        public void stateChanged(ChangeEvent ce) {
            JSlider source = (JSlider) ce.getSource();
            eventThread.goToTime(source.getValue());
            if (logList != null) { //if log list is initialized and showing
                if (logList.isVisible()) {
                    logList.clearSelection();
                    logList.addRowSelectionInterval(0, eventThread.getCurrentIndex() - 1);
                    logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentIndex() - 1, 0, true));
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (((JSlider) (e.getSource())).isEnabled()) {
                prevState = eventThread.getPlayState();
                eventThread.pause();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (((JSlider) (e.getSource())).isEnabled()) {
                if (prevState == PlayState.FASTREVERSE) {
                    eventThread.fastReverse();
                } else if (prevState == PlayState.REVERSE) {
                    eventThread.reverse();
                } else if (prevState == PlayState.FORWARD) {
                    eventThread.forward();
                } else if (prevState == PlayState.FASTFORWARD) {
                    eventThread.fastForward();
                } else if (prevState == PlayState.PAUSE) {
                    eventThread.pause();
                }
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

    /**
     * Called by the AlgorithmLoader when the user clicks ok.
     * If a log is already being simulated, remove the events, and pause the simulator
     * Then, based on the selected algorithms and the view, mode the south panel and graph viewers are created
     */
    public void loadAlgorithms() {
        if (config.containsKey(AlgorithmLoader.LOG_PATH)) {
            if (events != null) {
                events.clear();
                eventThread.stopPlayback();
                fastReverseButton.setEnabled(false);
                reverseButton.setEnabled(false);
                pauseButton.setEnabled(false);
                forwardButton.setEnabled(false);
                fastForwardButton.setEnabled(false);
                playbackSlider.setEnabled(false);
                playbackSlider.setValue(0);
                fastSpeedSlider.setEnabled(false);
            }

            initGraphComponents();
            TrustGraphLoader graphLoader = new TrustGraphLoader(config, loadingBar);
            graphs = graphLoader.getGraphs();
            events = graphLoader.createList(new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH)));
            startGraph();
            if (toggleLogTable.isSelected()) {
                toggleLogTable.setSelected(false);
                toggleLogTable.doClick();
            }
        }
    }

    /**
     * to run this applet as a java application
     */
    public static void main(String[] args) {
        TrustApplet myapp = new TrustApplet();
    }
}
