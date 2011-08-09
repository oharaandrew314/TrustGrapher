/////////////////////////////////////TrustGrapher////////////////////////////////
package cu.trustGrapher;

import cu.trustGrapher.graph.*;
import cu.trustGrapher.visualizer.*;
import cu.trustGrapher.graph.savingandloading.*;
import cu.trustGrapher.visualizer.eventplayer.*;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.util.List;
import java.util.ArrayList;

import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 *
 * An application that will display Trust Graphs generated from a series of feedback events.
 * Multiple graphs can be viewed in different layouts and viewing modes
 * The events can be "played" forward and backward, and the graph will change to display its state at every event
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public class TrustGrapher extends JFrame {

    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    //Each of the viewers in this pane is a component which displays a graph
    private ArrayList<TrustGraphViewer> viewers;
    //When a mouse click is done, the viewer that it was done in will be held here.  Useful to know which graph to change the layout for
    private TrustGraphViewer currentViewer;
    private ArrayList<SimGraph[]> graphs;  //Each element is an array containing a hidden and dynamic graph
    private ArrayList<SimGraph> displayedGraphs;  //The list of graphs that are currently being displayed by the viewers
    //The dynamic graph is not shown, but components in the full graph will only be displayed if they exist in the dynamic graph.
    //As events occur, they are added to the dynamic graphs through the graphEvent() method.
    public static final int DYNAMIC = SimGraph.DYNAMIC;
    //The full graph is the one that is shown, but all vertices and edges that are ever shown must be on the graph before the events start playing
    //The graphConstructionEvent() method is used as the events are parsed to add all components to the graph
    public static final int FULL = SimGraph.FULL;
    private Integer viewType; //Keeps track of which graph view to use
    public static final Integer TABBED = 0, GRID = 1, DEFAULT_VIEW = TABBED;
    private TrustPopupMenu popupMenu;  //The popup menu that is shown when a viewer is right-clicked
    //This window is used to configure which algorithms to load, what graphs are using what algorithms, and what graphs to display.
    //It saves all of the configurations to TrustPropertyManager config
    public final AlgorithmLoader options;
    private PropertyManager config; //The Property Manager that contains all of the saved options for the applet and algorithm loader
    private ArrayList<TrustLogEvent> events;  // The List of Trust Log Events
    private JCheckBoxMenuItem toggleLogTable; //A check box which is used to show the log table
    private JSplitPane mainPane; //The JSplitPane which is to contain the graphsPanel and playbackPanel
    private Container graphsPanel; //This container contains all of the TrustGrapgViewers as components
    private PlaybackPanel playbackPanel; //The panel beneath the graphs which controls playback

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates the TrustGrapher frame and menu bar.  The viewers are not yet created
     */
    public TrustGrapher() {
        config = new PropertyManager("TrustApplet.properties");
        options = new AlgorithmLoader(this, config);
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public ArrayList<TrustGraphViewer> getViewers() {
        return viewers;
    }

    public int getViewType() {
        return viewType;
    }

    /**
     * Returns the TrustGraphViewer that is currently selected based on the view type
     * If the view type is tabbed, then the current viewer is the one contained in the selected tab
     * If the view type is grid, then the current viewer is the one that the mouse clicked last
     * @return The currently selected TrustGraphViewer
     */
    public TrustGraphViewer getCurrentViewer() {
        return currentViewer;
    }

    /**
     * Returns the graph that is currently selected based on the view type
     * If the view type is tabbed, then the current graph is the one shown by the currently selected tab
     * If the view type is grid, then the current graph is the one contained in the viewer that the mouse clicked last
     * @return The currently selected graph
     */
    public SimGraph getCurrentGraph(){
        for (int i = 0 ; i < viewers.size() ; i++){
            if (viewers.get(i) == currentViewer){
                return displayedGraphs.get(i);
            }
        }
        ChatterBox.error(this, "getCurrentGraph()", "No currently selected graph was detected!");
        return null;
    }

    public ArrayList<SimGraph[]> getGraphs(){
        return graphs;
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

            graphs = GraphLoader.loadGraphs(options.getAlgorithms());
            java.io.File logFile = new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH));
            LogReader logReader = new LogReader(this, logFile);
            logReader.execute(); //After the log reader thread is complete, startGraph() will be called
        }else{
            ChatterBox.alert("No log was loaded, so no action will be taken.");
        }
    }

///////////////////////////////////Frame Builders///////////////////////////////
    /**
     * Initialize the base frame components.  The viewers are not created yet
     */
    private void initComponents() {
        //Initialize frame
        setTitle("Trust Grapher - Written by Andrew O'Hara");
        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create the Menu Bar
        MenuBarListener listener = new MenuBarListener();        
        JMenuItem optionsButton = new JMenuItem("Load Algorithms");
        optionsButton.addActionListener(listener);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(listener);
        JRadioButtonMenuItem tabbedView = new JRadioButtonMenuItem("Tabbed View");
        tabbedView.addActionListener(listener);
        JRadioButtonMenuItem gridView = new JRadioButtonMenuItem("Grid View");
        gridView.addActionListener(listener);
        toggleLogTable = new JCheckBoxMenuItem("Toggle Log Table");
        toggleLogTable.addActionListener(listener);

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
        setJMenuBar(bar);

        //Create the frame panels
        playbackPanel = new PlaybackPanel(this);
        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainPane.add(new JPanel());
        mainPane.add(playbackPanel);
        mainPane.setResizeWeight(1);
        mainPane.setDividerSize(3);
        add(mainPane);
        pack();
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
        JTable logList = new JTable(table, titles);
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
        playbackPanel.setLogList(logList);

        return tablePanel;
    }

    /**
     * Called by the log reader thread upon completion.
     * This method rebuilds the main pane, and adds new TrustGraphViewers to the graphsPanel, then starts the graph
     * @param events The event list returned by the log reader thread
     */
    public void startGraph(ArrayList<TrustLogEvent> events) {
        this.events = events;

        //Rebuild the main pane
        graphsPanel = (viewType == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        if (graphsPanel instanceof JTabbedPane){
            ((JTabbedPane)graphsPanel).addChangeListener(new TabChangeListener());
        }
        graphsPanel.setBackground(Color.LIGHT_GRAY);
        playbackPanel = new PlaybackPanel(this);
        mainPane.removeAll();
        mainPane.add(graphsPanel);
        mainPane.add(playbackPanel);
        playbackPanel.resetPanel(events, graphs);        
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        MouseClickListener listener = new MouseClickListener();
        popupMenu = new TrustPopupMenu(this, gm, listener);
        
        //Create the Visualization Viewers
        viewers = new ArrayList<TrustGraphViewer>();
        displayedGraphs = new ArrayList<SimGraph>();
        for (SimGraph[] graph : graphs) {
            if (graph[FULL].isDisplayed()) {
                displayedGraphs.add(graph[FULL]);
                AbstractLayout<Agent, TestbedEdge> layout = new FRLayout2<Agent, TestbedEdge>(graph[FULL]);
                layout.setInitializer(new P2PVertexPlacer(layout, new Dimension(DEFWIDTH / 3, DEFHEIGHT / 2)));
                TrustGraphViewer viewer = new TrustGraphViewer(layout, DEFWIDTH / 3, DEFHEIGHT / 2, gm, listener, graph);

                if (viewType == GRID) {  //This assumes that the graphsPanel is already in GridFormat
                    viewer.setBorder(BorderFactory.createTitledBorder(graph[DYNAMIC].getDisplayName()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Tabbed view by default.  This as5sumes that the graphsPanel is already a JSplitPane
                    ((JTabbedPane) graphsPanel).addTab(graph[DYNAMIC].getDisplayName(), viewer);
                }
                layout.lock(true);
                viewers.add(viewer);
            }
        }
        
        validate();
        playbackPanel.eventThread.run();
        if (toggleLogTable.isSelected()) {
            toggleLogTable.setSelected(false);
            toggleLogTable.doClick();
        }
    }

////////////////////////////////////Listeners///////////////////////////////////
    /**
     * If the graphsPanel is a JTabbedPane, updates the current viewer when it is changed
     * so that the playbackPanel knows which viewer to repaint
     */
    public class TabChangeListener implements ChangeListener{

        public void stateChanged(ChangeEvent e) {
            currentViewer = (TrustGraphViewer) ((JTabbedPane) graphsPanel).getSelectedComponent();
        }

    }

    /**
     * Listens for events related to the TrustGraphViewer right-click popup menu
     * If a right-click (e.isPopupTrigeer()) is detected, tells the TrustPopupMenu to show itself
     * If a button on that menu is clicked, then it notifies the popup menu
     */
    public class MouseClickListener extends MouseAdapter implements ActionListener{

        @Override
        public void mousePressed(MouseEvent e) {
            currentViewer = (TrustGraphViewer) e.getComponent();
            if (e.isPopupTrigger()) {
                popupMenu.showPopupMenu();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //Windows only shows the popup menu on right-click release.  Silly Windows
            if (System.getProperty("os.name").toLowerCase().startsWith("windows") & e.isPopupTrigger()){
                popupMenu.showPopupMenu();
            }
        }

        public void actionPerformed(ActionEvent e) {
            popupMenu.popupMenuEvent(((AbstractButton)e.getSource()).getText());
        }
    }

    /**
     * Listens for menu bar button events and deals with them accordingly
     */
    private class MenuBarListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton)e.getSource()).getText();
            if (buttonText.equals("Load Algorithms")){
                if (!options.isVisible()) {
                    playbackPanel.pauseButton.doClick();
                    options.run();
                }
            }else if(buttonText.equals("Exit")){
                try {
                    playbackPanel.pauseButton.doClick();
                } catch (NullPointerException ex) {
                }
                if (ChatterBox.yesNoDialog("Are you sure you want to exit?")) {
                    System.exit(0);
                }
            }else if (buttonText.equals("Tabbed View")){
                config.setProperty("viewType", "" + TABBED);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != TABBED) {
                    viewType = TABBED;
                    loadAlgorithms();
                } else {
                    viewType = TABBED;
                }
            }else if (buttonText.equals("Grid View")){
                config.setProperty("viewType", "" + GRID);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != GRID) {
                    viewType = GRID;
                    loadAlgorithms();
                } else {
                    viewType = GRID;
                }
            }else if (buttonText.equals("Toggle Log Table")){
                if (events != null) { //graph has been initialized
                    JCheckBoxMenuItem button = (JCheckBoxMenuItem) e.getSource();
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
        }

    }

////////////////////////////////Static Methods//////////////////////////////////
    /**
     * to run this program as a java application
     */
    public static void main(String[] args) {
        TrustGrapher myApp = new TrustGrapher();
        myApp.setVisible(true);
    }
}
