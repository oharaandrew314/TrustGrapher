/////////////////////////////////////TrustApplet////////////////////////////////
package trustGrapher;

import trustGrapher.graph.*;
import trustGrapher.visualizer.*;
import trustGrapher.graph.savingandloading.*;
import trustGrapher.visualizer.eventplayer.*;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import utilities.ChatterBox;

/**
 *
 * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public class TrustApplet extends JApplet implements EventPlayerListener{
    // for the length of the edges in the graph layout

    //default size for the swing graphic components
    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768;
    private ArrayList<VisualizationViewer<Agent, TestbedEdge>> viewers;
    private AbstractLayout<Agent, TestbedEdge> layout = null;
    private LinkedList<TrustLogEvent> events;
    private ArrayList<String[]> algs;
    
    private ArrayList<MyGraph[]> graphs;//addition = 0, eigen rep = 1, rankbased rep = 2, eigen trust = 3, rank based trust = 4;
    public static final int DYNAMIC = 0, FULL = 1;
    
    private List<LoadingListener> loadingListeners;
    protected JTable logList;
    protected JButton fastForwardButton, forwardButton, pauseButton, reverseButton, fastReverseButton;
    protected JSlider fastSpeedSlider, playbackSlider;
    protected JPopupMenu mouseContext;
    protected TrustEventPlayer eventThread;
    private JPanel graphsPanel;
    private VisualizationViewer currentViewer;
    public final Configure optionsDialog;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustApplet() {

        loadingListeners = new LinkedList<LoadingListener>();
        initComponents();
        start();
        JFrame frame = new JFrame();

        //The default frame state is now maximized
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);

        frame.pack();
        frame.setTitle("Trust Grapher - Written by Andrew O'Hara");
        frame.setVisible(true);
        optionsDialog = new Configure(this);
    }
    
    /**
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
                //viewer.getGraphLayout().setSize(arg0.getComponent().getSize());
            }
        });
        if (viewer == null){
            ChatterBox.criticalError(null, this, "visualizationViewerBuilder()", "The viewer is null.");
        }

        return viewer;
    }

    private void initSpecialTransformers(VisualizationViewer<Agent, TestbedEdge> viewer,
            VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
            EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {

        //add my own vertex shape & color fill transformers
        viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape));
        // note :the color depends on being picked.

        //make the p2p edges different from the peer to doc edges
        viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
        viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width

    }

    //[start] Create Components
    private JMenuBar createFileMenu() {
        //[start] File Menu
        JMenu file = new JMenu("File");

        JMenuItem options = new JMenuItem("Load Algorithms");
        options.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!optionsDialog.isVisible()){
                    optionsDialog.run();
                }
            }
        });

        //[start] Exit Entry
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                pauseButton.doClick();
                if (ChatterBox.yesNoDialog("Are you sure you want to exit?")){
                    System.exit(0);
                }
            }
        });
        //[end] Exit Entry
    
        file.add(options);
        file.addSeparator();
        file.add(exit);

        //[start] Window Menu
        JMenu window = new JMenu("Window");
        JMenuItem logTable = new JMenuItem("Show Log Table");
        logTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                if (events != null) { //graph has been initialized
                    JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                    p.setResizeWeight(1);
                    p.add(getContentPane().getComponent(0));
                    p.add(initializeLogList(events));

                    getContentPane().add(p);
                    validate();
                }
            }
        });
        window.add(logTable);
        //[end] Window Menu

        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(window);
        bar.setVisible(true);
        return bar;
    }

    private JPanel initializeLogList(List<TrustLogEvent> logEvents) {
        for (LoadingListener l : loadingListeners) {
            l.loadingStarted(logEvents.size(), "Log List");
        }

        Object[][] table = new Object[logEvents.size()][4];
        int i = 0;
        for (TrustLogEvent evt : logEvents) {
            table[i] = evt.toArray();
            i++;
            for (LoadingListener l : loadingListeners) {
                l.loadingProgress(i);
            }
        }
        Object[] titles = {"Time (ms)", "Assessor", "Assessee", "Feedback"};


        logList = new JTable(table, titles);

        logList.setBackground(Color.LIGHT_GRAY);
        logList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        logList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        logList.setEnabled(false);
        logList.setColumnSelectionAllowed(false);
        logList.setVisible(true);

        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(logEvents.size() + 1);
        }

        JScrollPane listScroller = new JScrollPane(logList);
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
        listScroller.setSize(logList.getWidth(), logList.getHeight());

        JPanel tablePanel = new JPanel(new GridLayout(1, 1));
        tablePanel.add(listScroller);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Log Events"));

        for (LoadingListener l : loadingListeners) {
            l.loadingComplete();
        }

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
        //fastSpeedSlider.setBackground(Color.DARK_GRAY);
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

        //playbackSlider.setBackground(Color.LIGHT_GRAY);
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
        //south.setBackground(Color.LIGHT_GRAY);
        south.setLayout(new GridLayout(2, 1));
        south.setBorder(BorderFactory.createTitledBorder("Playback Options"));
        south.add(buttonPanel);
        south.add(playbackSlider);

        return south;
    }

    private void initializeMouseContext(final DefaultModalGraphMouse<Agent, TestbedEdge> gm) {
        mouseContext = new JPopupMenu("Mouse Mode");
        JMenuItem picking = new JMenuItem("Picking");
        picking.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gm.setMode(Mode.PICKING);
                int size = mouseContext.getComponentCount();
                for (int i = size - 1; i > 4; i--) {
                    mouseContext.remove(i);
                }
                mouseContext.setVisible(false);
                mouseContext.setEnabled(false);
            }
        });

        JMenuItem transforming = new JMenuItem("Transforming");
        transforming.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                gm.setMode(Mode.TRANSFORMING);
                int size = mouseContext.getComponentCount();
                for (int i = size - 1; i > 4; i--) {
                    mouseContext.remove(i);
                }
                mouseContext.setVisible(false);
                mouseContext.setEnabled(false);
            }
        });
        mouseContext.add("Mouse Mode:").setEnabled(false);
        mouseContext.add(picking);
        mouseContext.add(transforming);
        mouseContext.addSeparator();
        mouseContext.add("Set Layout:").setEnabled(false);
    }
    //[end] Create Components

    /**
     * Builds the split pane and adds the empty graphs panel
     */
    private void initComponents() {

        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        graphsPanel = new JPanel(new GridLayout(2, 3));
        graphsPanel.setBackground(Color.LIGHT_GRAY);
        mainPane.setResizeWeight(1);
        mainPane.add(graphsPanel);
        mainPane.add(initializeSouthPanel());

        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        //try set the size
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setJMenuBar(createFileMenu());
        getContentPane().add(mainPane);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));

        loadingListeners.add(new LoadingBar());
    }

    /**
     * Once a graph has been loaded, build the viewing panes and start the graph
     */
    public void startGraph() {
        for (LoadingListener l : loadingListeners) {
            l.loadingStarted(7, "Building Visualizer");
        }

        //Build all the viewing panes
        buildViewers();

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
            //playbackSlider.setMinimum((int)events.getFirst().getTime());
            playbackSlider.setMinimum(0);
            playbackSlider.setMaximum((int) events.getLast().getTime());
            playbackSlider.addChangeListener(s);
            playbackSlider.addMouseListener(s);

            /// create the event player
            eventThread = new TrustEventPlayer(graphs, events, playbackSlider);
            eventThread.addEventPlayerListener(this);
        }
        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(2);
        }

        for (LoadingListener l : loadingListeners) {
            l.loadingComplete();
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

    private void buildViewers(){
        for (LoadingListener l : loadingListeners) {
            l.loadingChanged(5, "Building Visualizer");
        }

        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        GraphMouseListener graphListener = new GraphMouseListener();
        graphsPanel.removeAll();
        viewers = new ArrayList<VisualizationViewer<Agent, TestbedEdge>>();

        //Find the names of the graphs to displays
        ArrayList<String> names = new ArrayList<String>();
        for (String[] entry : algs){
            if (entry[Configure.DISPLAY].equals(Configure.TRUE)){
                names.add(entry[Configure.NAME]);
            }
        }

        //Create the Visualization Viewers
        for (int i=0 ; i < names.size() ; i++){
            layout = new FRLayout2<Agent, TestbedEdge>(graphs.get(i)[FULL]);
            layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH/3, DEFHEIGHT/2)));
            viewers.add((VisualizationViewer) visualizationViewerBuilder(layout, DEFWIDTH/3, DEFHEIGHT/2, gm));
            viewers.get(i).addMouseListener(graphListener);
            viewers.get(i).setName(names.get(i));
            initSpecialTransformers(viewers.get(i), VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.RECTANGLE, EdgeShapeType.QUAD_CURVE, EdgeShapeType.CUBIC_CURVE, EdgeShapeType.LINE, EdgeShapeType.LINE);
            viewers.get(i).getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(graphs.get(i)[DYNAMIC]));
            viewers.get(i).getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(graphs.get(i)[DYNAMIC]));
            viewers.get(i).setBorder(BorderFactory.createTitledBorder(names.get(i)));

            this.graphsPanel.add(viewers.get(i), i);
            for (LoadingListener l : loadingListeners) {
                l.loadingProgress(i +1);
            }
        }

        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(1);
        }

        initializeMouseContext(gm);
        graphsPanel.revalidate();
    }
    //[end] Initialization
    
    /**
     * to run this applet as a java application
     */
    public static void main(String[] args) {
        TrustApplet myapp = new TrustApplet();
    }

    public List<JMenuItem> getLayoutItems() {
        List<JMenuItem> menuItems = new LinkedList<JMenuItem>();
        JMenuItem kkLayout = new JMenuItem("KK Layout");
        JMenuItem frLayout = new JMenuItem("FR Layout");
        JMenuItem isomLayout = new JMenuItem("ISOM Layout");
        JMenuItem circleLayout = new JMenuItem("Circle Layout");
        JMenuItem springLayout = new JMenuItem("Spring Layout");

        frLayout.addActionListener(new FRLayoutListener(currentViewer));
        kkLayout.addActionListener(new KKLayoutListener(currentViewer));
        isomLayout.addActionListener(new ISOMLayoutListener(currentViewer));
        circleLayout.addActionListener(new CircleLayoutListener(currentViewer));
        springLayout.addActionListener(new SpringLayoutListener(currentViewer));

        menuItems.add(circleLayout);
        menuItems.add(frLayout);
        menuItems.add(isomLayout);
        menuItems.add(kkLayout);
        menuItems.add(springLayout);
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
        for (int i=0 ; i < viewers.size() ; i++){
            viewers.get(i).repaint();
        }
    }

    /**
     * an actionlistener that defines the use of a button to stop the spring-layout processing
     * @author adavoust
     *
     */
    private class GraphMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            doPop(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (System.getProperty("os.name").toLowerCase().equals("windows")) {
                doPop(e);
            }
        }

        private void doPop(MouseEvent e) {
            currentViewer = (VisualizationViewer) e.getComponent();
            if (e.isPopupTrigger()) {
                for (JMenuItem item : getLayoutItems()) {
                    mouseContext.add(item);
                }
                mouseContext.setEnabled(true);
                mouseContext.show(null, e.getXOnScreen(), e.getYOnScreen());
            } else if (mouseContext.isVisible()) {
                int size = mouseContext.getComponentCount();
                for (int i = size - 1; i > 4; i--) {
                    mouseContext.remove(i);
                }
                mouseContext.setVisible(false);
                mouseContext.setEnabled(false);
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
     * @version May 2011
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
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
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
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
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
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
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
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
            }
            graphLayout.lock(true);
        }
    }

    class SpringLayoutListener implements ActionListener {

        VisualizationViewer<Agent, TestbedEdge> vv;

        public SpringLayoutListener(VisualizationViewer<Agent, TestbedEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<Agent, TestbedEdge> graphLayout = new SpringLayout<Agent, TestbedEdge>(graphs.get(viewers.indexOf(currentViewer))[FULL]);
            vv.getModel().setGraphLayout(graphLayout);
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
            }
            graphLayout.lock(true);
        }
    }

    public void loadOptions(){
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
        algs = optionsDialog.getAlgs();
        TrustEventLoader logBuilder = new TrustEventLoader(algs);        
        graphs = logBuilder.getGraphs();
        events = logBuilder.createList(optionsDialog.getLogFile());

        startGraph();
    }
}
