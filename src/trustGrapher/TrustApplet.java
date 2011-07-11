/////////////////////////////////////TrustApplet////////////////////////////////
package trustGrapher;

import cu.repsystestbed.graphs.FeedbackHistoryGraphEdge;
import trustGrapher.graph.*;
import trustGrapher.visualizer.*;
import trustGrapher.graph.savingandloading.*;
import trustGrapher.visualizer.eventplayer.*;
import trustGrapher.networking.*;

import utilities.ChatterBox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.jdom.JDOMException;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;

/**
 * //////////////NOTE/////////////////
 * I modified the mouseReleasedEvent() method to skip the doPop() method
 * On Linux, this caused the popup menu to dissapear as soon as the mouse button was released
 * I am not sure what effect this would have on a windows machine.
 *
 * an applet that will display a graph using a spring layout, and as the graph changes the layout is updated.
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public class TrustApplet extends JApplet implements EventPlayerListener, NetworkListener {
    // for the length of the edges in the graph layout

    public static final Transformer<FeedbackHistoryGraphEdge, Integer> UNITLENGTHFUNCTION = new ConstantTransformer(100);
    //default size for the swing graphic components
    public static final int DEFWIDTH = 1360;
    public static final int DEFHEIGHT = 768;
    private static final long serialVersionUID = 2L;
    private VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> feedbackViewer = null;
    private AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> layout = null;
    private LinkedList<TrustLogEvent> events;
    //a hidden graph that contains all the nodes that will ever be added in order to calculate the positions of all the nodes
    private FeedbackHistoryGraph hiddenGraph, visibleGraph = null;
    private List<LoadingListener> loadingListeners;
    private HTTPClient networkClient;
    protected JTable logList;
    protected JTabbedPane tabsPane;
    protected JButton fastForwardButton, forwardButton, pauseButton, reverseButton, fastReverseButton;
    protected JSlider fastSpeedSlider, playbackSlider;
    protected JPopupMenu mouseContext;
    protected TrustEventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustApplet() {

        networkClient = new HTTPClient(this);
        loadingListeners = new LinkedList<LoadingListener>();
        init();
        start();
        JFrame frame = new JFrame();

        //The default frame state is now maximized
        frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @return The Initialized Visualization Viewer
     */
    private VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> visualizationViewerBuilder(final Layout<MyAgent, FeedbackHistoryGraphEdge> layout, int width, int height, GraphMouse gm) {
        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> viewer = new VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge>(layout, new Dimension(width, height));
        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        // the default mouse makes the mouse usable as a picking tool (pick, drag vertices & edges) or as a transforming tool (pan, zoom)
        viewer.setGraphMouse(gm);

        //set graph rendering parameters & functions
        viewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        //the vertex labeler will use the tostring method which is fine, the MyAgent class has an appropriate toString() method implementation
        viewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyAgent>());
        viewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<FeedbackHistoryGraphEdge>());
        viewer.getRenderContext().setVertexFillPaintTransformer(new P2PVertexFillPaintTransformer(viewer.getPickedVertexState()));
        // MyAgent objects also now have multiple states : we can represent which nodes are documents, picked, querying, queried, etc.

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

        return viewer;
    }

    private void initSpecialTransformers(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> viewer,
            VertexShapeType peerShape, VertexShapeType documentShape, VertexShapeType peerDocumentShape,
            EdgeShapeType P2PEdgeShape, EdgeShapeType P2DocEdgeShape, EdgeShapeType Doc2PDocEdgeShape, EdgeShapeType P2PDocEdgeShape) {

        //add my own vertex shape & color fill transformers
        viewer.getRenderContext().setVertexShapeTransformer(new P2PVertexShapeTransformer(peerShape));
        // note :the color depends on being picked.

        //make the p2p edges different from the peer to doc edges
        viewer.getRenderContext().setEdgeStrokeTransformer(new P2PEdgeStrokeTransformer()); //stroke width
        viewer.getRenderContext().setEdgeShapeTransformer(new P2PEdgeShapeTransformer()); //stroke width
        BasicEdgeLabelRenderer labeller = new BasicEdgeLabelRenderer();

    }
    //[end] Create the visualization viewer

    //[start] Create Components
    private JMenuBar createFileMenu() {
        //[start] File Menu
        JMenu file = new JMenu("File");
        //[start] Connect Entry
//        JMenuItem connect = new JMenuItem("Connect to..");
//        connect.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent arg0) {
//                pauseButton.doClick();
//                String option = JOptionPane.showInputDialog(null, "Enter a URL:", "Connect", JOptionPane.PLAIN_MESSAGE);
//                if (option != null) {
//                    if (option.startsWith("http://")) {
//                        //networkClient.closeNetwork();
//                        networkClient.startNetwork(option);
//                        //client.addNetworkListener();
//                    } else {
//                        JOptionPane.showMessageDialog(null, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//                //else cancel option, don't do anything
//            }
//        });
        //[end] Connect Entry

        //[start] Save Entry
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                pauseButton.doClick();
                int option = JOptionPane.showConfirmDialog(null, "Would you like to save the first 500 log events after this graph snapshot",
                        "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION) {
                    TrustGraphSaver saver = new TrustGraphSaver(visibleGraph, eventThread.getSaveEvents(), eventThread.getCurrentTime());
                    saver.addLoadingListener(new LoadingBar());
                    saver.doSave();
                } else if (option == JOptionPane.NO_OPTION) {
                    TrustGraphSaver saver = new TrustGraphSaver(visibleGraph);
                    saver.addLoadingListener(new LoadingBar());
                    saver.doSave();
                }
                //else cancel option, don't do anything
            }
        });
        //[end] Save Entry

        //[start] Load Entry
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(new LoadListener());
        //[end] Load Entry

        //[start] Exit Entry
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                pauseButton.doClick();
                int option = JOptionPane.showConfirmDialog(null, "Would you like to save before quitting?", "Save",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    //GraphSaverAndLoader.save(visibleGraph);
                    System.exit(0);
                } else if (option == JOptionPane.NO_OPTION) {
                    System.exit(0);
                }
                //else if(option == JOptionPane.CANCEL_OPTION) {
                //do nothing
                //}
            }
        });
        //[end] Exit Entry

//        file.add(connect);
        file.addSeparator();
        file.add(save);
        file.add(load);
        file.addSeparator();
        file.add(exit);
        //[end] File Menu

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
        //tablePanel.setBackground(Color.GRAY);
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

    private void initializeMouseContext(final DefaultModalGraphMouse<MyAgent, FeedbackHistoryGraphEdge> gm) {
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

    public void init() {

        //[start] Tabs Pane
        tabsPane = new JTabbedPane(JTabbedPane.TOP) {

            private static final long serialVersionUID = -4075340829665484983L;
            //private int i=0;

            @Override
            public void paint(Graphics g) {
                //i++;
                //System.out.println("tabsPane paint "+i);
                try {
                    super.paint(g);
                } catch (Exception e) {
                }
            }
        };
        JPanel initialTab = new JPanel();
        tabsPane.addTab("Welcome", initialTab);

        tabsPane.setEnabled(false);
        //[end] Tabs Pane

        JPanel graphsPanel = new JPanel(new GridLayout(1, 1));
        graphsPanel.add(tabsPane);

        JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        p.setResizeWeight(1);
        p.add(graphsPanel);
        p.add(initializeSouthPanel());


        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        //try set the size
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setJMenuBar(createFileMenu());
        getContentPane().add(p);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));

        //startGraph();
        loadingListeners.add(new LoadingBar());
    }

    public void startGraph() {
        for (LoadingListener l : loadingListeners) {
            l.loadingStarted(7, "Building Visualizer");
        }
        tabsPane.removeAll();

        //layout = springLayoutBuilder(DEFWIDTH,DEFHEIGHT,hiddenGraph);
        layout = new FRLayout2<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
        layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH, DEFHEIGHT)));

        for (LoadingListener l : loadingListeners) {
            l.loadingChanged(5, "Building Visualizer");
        }

        DefaultModalGraphMouse<MyAgent, FeedbackHistoryGraphEdge> gm = new DefaultModalGraphMouse<MyAgent, FeedbackHistoryGraphEdge>();
        GraphMouseListener graphListener = new GraphMouseListener();

        //[start] Feedback Visualization Viewer Init
        feedbackViewer = visualizationViewerBuilder(layout, DEFWIDTH, DEFHEIGHT, gm);
        feedbackViewer.addMouseListener(graphListener);
        feedbackViewer.setName("Feedback History");
        //add my own vertex shape & color fill transformers
        initSpecialTransformers(feedbackViewer, VertexShapeType.ELLIPSE, VertexShapeType.PENTAGON, VertexShapeType.RECTANGLE,
                EdgeShapeType.QUAD_CURVE,
                EdgeShapeType.CUBIC_CURVE,
                EdgeShapeType.LINE,
                EdgeShapeType.LINE);

        feedbackViewer.getRenderContext().setVertexIncludePredicate(new VertexIsInTheOtherGraphPredicate(visibleGraph));
        feedbackViewer.getRenderContext().setEdgeIncludePredicate(new EdgeIsInTheOtherGraphPredicate(visibleGraph));
        //[end] Feedback Visualization Viewer Init
        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(1);
        }

        initializeMouseContext(gm);

        if (events.isEmpty()) {
            SliderListener s = new SliderListener();

            playbackSlider.setMaximum(0);
            playbackSlider.addChangeListener(s);
            playbackSlider.addMouseListener(s);

            /// create the event player
            eventThread = new TrustEventPlayer(hiddenGraph, visibleGraph);
            eventThread.addEventPlayerListener(this);
        } else {
            SliderListener s = new SliderListener();
            //playbackSlider.setMinimum((int)events.getFirst().getTime());
            playbackSlider.setMinimum(0);
            playbackSlider.setMaximum((int) events.getLast().getTime());
            playbackSlider.addChangeListener(s);
            playbackSlider.addMouseListener(s);


            /// create the event player
            eventThread = new TrustEventPlayer(hiddenGraph, visibleGraph, events, playbackSlider);
            eventThread.addEventPlayerListener(this);
        }
        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(2);
        }


        //Add the viewers to the tabs pane
        tabsPane.addTab(feedbackViewer.getName(), feedbackViewer);
        tabsPane.setEnabled(true);
        tabsPane.setIgnoreRepaint(false);

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

    //[end] Initialization
    /**
     * to run this applet as a java application
     * @param args optional argument : the log file to process
     */
    public static void main(String[] args) {
        @SuppressWarnings("unused")
        TrustApplet myapp = new TrustApplet();
    }


    public List<JMenuItem> getLayoutItems() {
        List<JMenuItem> menuItems = new LinkedList<JMenuItem>();


        if (tabsPane.getSelectedComponent().getName().equals("Feedback History")) {
            JMenuItem frLayout = new JMenuItem("FR Layout");
            frLayout.addActionListener(new FRLayoutListener(feedbackViewer));

            JMenuItem kkLayout = new JMenuItem("KK Layout");
            kkLayout.addActionListener(new KKLayoutListener(feedbackViewer));

            JMenuItem isomLayout = new JMenuItem("ISOM Layout");
            isomLayout.addActionListener(new ISOMLayoutListener(feedbackViewer));

            JMenuItem circleLayout = new JMenuItem("Circle Layout");
            circleLayout.addActionListener(new CircleLayoutListener(feedbackViewer));

            JMenuItem springLayout = new JMenuItem("Spring Layout");
            springLayout.addActionListener(new SpringLayoutListener(feedbackViewer));

            menuItems.add(circleLayout);
            menuItems.add(frLayout);
            menuItems.add(isomLayout);
            menuItems.add(kkLayout);
            menuItems.add(springLayout);
        } else {
            ChatterBox.error(this, "getLayoutItems()", "Tried to create popup menu for a view other than feedback view.  You will have to implement this.  See original method for reference");
        }

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
        feedbackViewer.repaint();
        //collapsedDocumentViewViewer.repaint();
        //collapsedPeerViewViewer.repaint();
        //collapsedPeerAndDocumentViewViewer.repaint();
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
            if (System.getProperty("os.name").toLowerCase().equals("linux") == false){
                doPop(e);
            }
        }

        private void doPop(MouseEvent e) {
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

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public FRLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new FRLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph, vv.getSize());
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

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public ISOMLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new ISOMLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
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

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public KKLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new KKLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
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

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public CircleLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new CircleLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
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

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public SpringLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new SpringLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
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

    class TreeLayoutListener implements ActionListener {

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public TreeLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreeLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout =
                    new TreeLayout<MyAgent, FeedbackHistoryGraphEdge>(FeedbackHistoryGraph.makeTreeGraph(hiddenGraph)) {

                        @Override
                        public void setSize(Dimension size) {
                            // The set size method was being called, and it raised an exception every time
                        }
                    };
            vv.getModel().setGraphLayout(graphLayout);
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
            }

        }
    }

    class BalloonLayoutListener implements ActionListener {

        VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv;

        public BalloonLayoutListener(VisualizationViewer<MyAgent, FeedbackHistoryGraphEdge> vv) {
            this.vv = vv;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BalloonLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new BalloonLayout<MyAgent, FeedbackHistoryGraphEdge>(FeedbackHistoryGraph.makeTreeGraph(hiddenGraph));
            graphLayout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH, DEFHEIGHT)));

            vv.getModel().setGraphLayout(graphLayout);
            mouseContext.setVisible(false);
            mouseContext.setEnabled(false);
            int size = mouseContext.getComponentCount();
            for (int i = size - 1; i > 4; i--) {
                mouseContext.remove(i);
            }
        }
    }

    class LoadListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {

            pauseButton.doClick();
            Thread loadingThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    TrustGraphLoader loader = new TrustGraphLoader();
                    loader.addLoadingListener(new LoadingBar());
                    if (loader.doLoad()) {
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
                            tabsPane.setEnabled(false);
                            fastSpeedSlider.setEnabled(false);

                            //When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
                            AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new CircleLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
//                            collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
                        }
                        events = loader.getLogList();
                        hiddenGraph = loader.getHiddenP2PNetworkGraph();
                        visibleGraph = loader.getVisibleP2PNetworkGraph();
                        startGraph();
                    }
                }
            });
            loadingThread.start();
        }
    }
    //[end] Load Listener

    //[end] Swing Event Listeners
    //[start] Network Listeners
    @Override
    public synchronized void incomingLogEvents(InputStream inStream) {
        try {
            eventThread.pause();
            LinkedList<TrustLogEvent> events;
            synchronized (hiddenGraph) {
                events = TrustGraphLoader.buildLogs(inStream, hiddenGraph);
            }


            events.addLast(TrustLogEvent.getEndEvent(events.getLast()));
            eventThread.addEvents(events);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void incomingGraph(InputStream inStream) {

        try {
            TrustGraphLoader loader = TrustGraphLoader.buildGraph(inStream);

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
                tabsPane.setEnabled(false);
                fastSpeedSlider.setEnabled(false);

                //When loading a new Graph, if the collapsed document view has a tree layout it crashes because of setsize()
                AbstractLayout<MyAgent, FeedbackHistoryGraphEdge> graphLayout = new CircleLayout<MyAgent, FeedbackHistoryGraphEdge>(hiddenGraph);
//                collapsedDocumentViewViewer.getModel().setGraphLayout(graphLayout);
            }

            events = loader.getLogList();
            hiddenGraph = loader.getHiddenP2PNetworkGraph();
            visibleGraph = loader.getVisibleP2PNetworkGraph();
            startGraph();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //[end] Network Listeners
}
