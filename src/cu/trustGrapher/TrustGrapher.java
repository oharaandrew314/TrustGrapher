/////////////////////////////////////TrustGrapher///////////////////////////////
package cu.trustGrapher;

import cu.trustGrapher.eventplayer.*;
import cu.trustGrapher.graph.*;
import cu.trustGrapher.visualizer.*;
import cu.trustGrapher.graph.savingandloading.*;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;


import java.util.LinkedList;
import utilities.*;

/**
 *
 * An application that will display Trust Graphs generated from a series of feedback events.
 * Multiple graphs can be viewed in different layouts and viewing modes
 * The events can be "played" forward and backward, and the graph will change to display its state at every event
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public final class TrustGrapher extends JFrame {

    public static final int CURRENT_REVISION = 47;
    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    //Each of the viewers in this pane is a component which displays a graph
    private List<TrustGraphViewer> viewers;
    private GraphManager graphs;  //Each element is an array containing a full and dynamic graph
    //The dynamic graph is not shown, but components in the full graph will only be displayed if they exist in the dynamic graph.
    //As events occur, they are added to the dynamic graphs through their graphEvent() method.
    public static final int DYNAMIC = SimGraph.DYNAMIC;
    //The full graph is the one that is shown, but all vertices and edges that are ever shown must be on the graph before the events start playing
    //Their graphConstructionEvent() method is used as the events are parsed to add all components to the graph
    public static final int FULL = SimGraph.FULL;
    private Integer viewType; //Keeps track of which graph view to use
    public static final Integer TABBED = 0, GRID = 1, DEFAULT_VIEW = TABBED; //View types
    private TrustPopupMenu popupMenu;  //The popup menu that is shown when a viewer is right-clicked
    //This window is used to configure which algorithms to load, what graphs are using what algorithms, and what graphs to display.
    //It saves all of the configurations to TrustPropertyManager config
    public final AlgorithmLoader algorithmLoader;
    private PropertyManager config; //The Property Manager that contains all of the saved algorithmLoader for the applet and algorithm loader
    private JCheckBoxMenuItem toggleLogTable; //A check box which is used to show the log table
    private JSplitPane mainPane; //The JSplitPane which is to contain the graphsPanel and playbackPanel
    private Container graphsPanel; //This container contains all of the TrustGrapgViewers as components
    private EventPlayer eventThread;
    private PlaybackPanel playbackPanel;
    private LogPanel logPanel;
    private OptionsWindow optionsWindow;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates the TrustGrapher frame and menu bar.  The viewers are not yet created
     */
    public TrustGrapher() {
        config = new PropertyManager("TrustApplet.properties");
        algorithmLoader = new AlgorithmLoader(this, config);
        optionsWindow = new OptionsWindow(config, null);  //Right now, the window has no EventThread reference.
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * @return A list of the TrustGraphViewers in the simulator
     */
    public List<TrustGraphViewer> getViewers() {
        return viewers;
    }

    /**
     * The view type decides what format the viewers will be shown by.
     * Returns the view type.
     * @return The integer representation of the view type
     */
    public int getViewType() {
        return viewType;
    }

    public GraphManager getGraphManager() {
        return graphs;
    }
    
    public PropertyManager getPropertyManager(){
        return config;
    }

    /**
     * Returns a list of TrustGraphViewers that are visible
     * If the view is a tabbed view, then returns the viewer that is in the selected tab
     * If the view is in grid, then returns all of the viewers
     * @return The visible TrustGraphViewers
     */
    public List<TrustGraphViewer> getVisibleViewers() {
        if (graphsPanel instanceof JTabbedPane) {
            List<TrustGraphViewer> visibleViewers = new java.util.LinkedList<TrustGraphViewer>();
            visibleViewers.add((TrustGraphViewer) ((JTabbedPane) graphsPanel).getSelectedComponent());
            return visibleViewers;
        } else {
            return viewers;
        }
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the AlgorithmLoader when the user clicks ok.
     * If a log is already being simulated, remove the events, and pause the simulator
     * Then, tell the logReader to begin loading the events
     */
    public void loadAlgorithms() {
        if (config.containsKey(AlgorithmLoader.LOG_PATH)) {
            if (eventThread != null) {
                eventThread.getEvents().clear();
                eventThread.pause();
            }
            graphs = new GraphManager(algorithmLoader.getAlgorithms());
            java.io.File logFile = new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH));
            LogReader logReader = new LogReader(this, logFile, new AreWeThereYet(this));
            logReader.execute(); //After the log reader thread is complete, startGraph() will be called

        } else {
            ChatterBox.alert("No log was loaded, so no action will be taken.");
        }
    }

///////////////////////////////////Frame Builders///////////////////////////////
    /**
     * Initialize the base frame components.  The viewers and panels are not created yet
     */
    private void initComponents() {
        //Initialize frame
        setTitle("TrustGrapher r" + CURRENT_REVISION + " - Written by Andrew O'Hara");
        getContentPane().setFont(new Font("Arial", Font.PLAIN, 12));
        getContentPane().setBounds(0, 0, DEFWIDTH, DEFHEIGHT);
        setPreferredSize(new Dimension(DEFWIDTH, DEFHEIGHT));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create the Menu Bar

        //File buttons
        JMenuItem algLoaderButton = new JMenuItem("Load Algorithms");
        JMenuItem exportButton = new JMenuItem("Export Results");
        JMenuItem exit = new JMenuItem("Exit");
        //Edit Buttons
        JMenuItem insertEvent = new JMenuItem("Insert Event After");
        JMenuItem removeEvent = new JMenuItem("Remove Event");
        JMenuItem modifyEvent = new JMenuItem("Modify Event");
        JMenuItem optionsButton = new JMenuItem("Options");
        //View Buttons
        JRadioButtonMenuItem tabbedView = new JRadioButtonMenuItem("Tabbed View");
        JRadioButtonMenuItem gridView = new JRadioButtonMenuItem("Grid View");
        toggleLogTable = new JCheckBoxMenuItem("Toggle Log Table");
        //Help Buttons
        JMenuItem about = new JMenuItem("About");
        //Add Action Listeners
        FileMenuListener fileListener = new FileMenuListener();
        algLoaderButton.addActionListener(fileListener);
        optionsButton.addActionListener(fileListener);
        exportButton.addActionListener(fileListener);
        exit.addActionListener(fileListener);
        EditMenuListener editListener = new EditMenuListener();
        insertEvent.addActionListener(editListener);
        removeEvent.addActionListener(editListener);
        modifyEvent.addActionListener(editListener);
        ViewMenuListener viewListener = new ViewMenuListener();
        tabbedView.addActionListener(viewListener);
        gridView.addActionListener(viewListener);
        toggleLogTable.addActionListener(viewListener);
        about.addActionListener(new HelpMenuListener());

        String checked = config.getProperty("logTable");
        if (checked == null) {
            checked = "true";
            config.setProperty("logTable", checked);
            config.save();
        }
        toggleLogTable.setSelected(Boolean.parseBoolean(checked));

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
        file.add(algLoaderButton);
        file.add(exportButton);
        file.addSeparator();
        file.add(optionsButton);
        file.addSeparator();
        file.add(exit);

        //Create the Edit Menu
        JMenu edit = new JMenu("Edit");
        edit.add(insertEvent);
        edit.add(removeEvent);
        edit.add(modifyEvent);

        //Create the view menu
        JMenu view = new JMenu("View");
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(tabbedView);
        viewGroup.add(gridView);
        view.add(tabbedView);
        view.add(gridView);
        view.addSeparator();
        view.add(toggleLogTable);

        //Create the Help Menu
        JMenu help = new JMenu("Help");
        help.add(about);

        //Add the menus to the menu bar
        JMenuBar bar = new JMenuBar();
        bar.add(file);
        bar.add(edit);
        bar.add(view);
        bar.add(help);
        setJMenuBar(bar);
    }

    /**
     * Called by the log reader thread upon completion, or by the EventPlayer upon an event modification
     * This method rebuilds the main pane, and adds new TrustGraphViewers to the graphsPanel, then starts the graph
     * @param events The event list returned by the log reader thread or EventPlayer
     */
    public void startGraph(List<TrustLogEvent> events) {
        graphsPanel = (viewType == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        graphsPanel.setBackground(Color.LIGHT_GRAY);

        //Create viewer listeners and popup menu
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        ViewerListener listener = new ViewerListener();
        popupMenu = new TrustPopupMenu(gm, listener);

        //Create the Visualization Viewers
        viewers = new LinkedList<TrustGraphViewer>();
        for (SimGraph[] graph : graphs.getGraphs()) {
            if (graph[FULL].isDisplayed()) {
                AbstractLayout<Agent, TestbedEdge> layout = new FRLayout<Agent, TestbedEdge>(graph[FULL]);
                layout.setInitializer(new VertexPlacer(layout, new Dimension(DEFWIDTH / 3, DEFHEIGHT / 2)));
                TrustGraphViewer viewer = new TrustGraphViewer(layout, DEFWIDTH / 3, DEFHEIGHT / 2, gm, listener, graph);
//                    viewer.getModel().setGraphLayout(layout);

                if (viewType == GRID) {  //This assumes that the graphsPanel is already in GridFormat
                    viewer.setBorder(BorderFactory.createTitledBorder(graph[DYNAMIC].getDisplayName()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Tabbed view by default.  This assumes that the graphsPanel is already a JSplitPane
                    ((JTabbedPane) graphsPanel).addTab(graph[DYNAMIC].getDisplayName(), viewer);
                }
                layout.lock(true);
                viewers.add(viewer);
            }
        }

        //Create the eventThread and its listener panels for the simulator
        eventThread = new EventPlayer(this, events);
        eventThread.addEventPlayerListener(playbackPanel = new PlaybackPanel(events));
        eventThread.addEventPlayerListener(logPanel = new LogPanel(events));
        try {
            int delay = Integer.parseInt(config.getProperty(OptionsWindow.DELAY));
            eventThread.setDelay(delay);
        } catch (NumberFormatException ex) {
        }
        optionsWindow = new OptionsWindow(config, eventThread); //Make a new optionsWindow that the EventPlayer is attached to

        //Create the mainPane, add the graphsPanel and playbackPanel, and add the mainPane to the content pane
        getContentPane().removeAll();  //Necessary to removeAll since there might already be existing viewers present
        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphsPanel, playbackPanel);
        mainPane.setResizeWeight(1);
        mainPane.setDividerSize(3);
        getContentPane().add(mainPane);

        eventThread.run(); //Start the eventThread
        if (toggleLogTable.isSelected()) { //If the toggle log table check box is selected, reset the log table
            toggleLogTable.setSelected(false);
            toggleLogTable.doClick();
        }
        validate();
    }

////////////////////////////////////Listeners///////////////////////////////////
    /**
     * Listens for events related to the TrustGraphViewer right-click popup menu
     * If a right-click is detected, tells the TrustPopupMenu to show itself
     * If a button on that menu is clicked, then it notifies the popup menu
     */
    private class ViewerListener extends MouseAdapter implements ActionListener {

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                popupMenu.showPopupMenu((TrustGraphViewer) e.getComponent());
            }
        }

        public void actionPerformed(ActionEvent e) {
            popupMenu.popupMenuEvent(((AbstractButton) e.getSource()).getText());
        }
    }

    /**
     * Listens for menu bar button click events and deals with them accordingly
     */
    private class FileMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("Load Algorithms")) {
                if (!algorithmLoader.isVisible()) {
                    if (playbackPanel != null) {
                        playbackPanel.getPauseButton().doClick();
                    }
                    algorithmLoader.run();
                }
            } else if (buttonText.equals("Export Results")) {
                //Implement export results
                ChatterBox.alert("Export results.  To implement this, go to cu.trustGrapher.TrustGrapher,\nthen search 'Implement export results'.");
            } else if (buttonText.equals("Options")) {
                optionsWindow.showWindow();
            } else if (buttonText.equals("Exit")) {
                if (playbackPanel != null) {
                    playbackPanel.getPauseButton().doClick();
                }
                if (ChatterBox.yesNoDialog("Are you sure you want to exit?")) {
                    System.exit(0);
                }
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press:\n" + buttonText);
            }
        }
    }

    private class EditMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (eventThread != null) {
                String buttonText = ((AbstractButton) e.getSource()).getText();
                if (buttonText.equals("Insert Event After")) {
                    EventInjector.getNewEvent(eventThread);
                } else if (buttonText.equals("Remove Event")) {
                    eventThread.removeEvent();
                } else if (buttonText.equals("Modify Event")) {
                    EventInjector.modifyEvent(eventThread);
                } else {
                    ChatterBox.error(this, "actionPerformed()", "Uncaught button press");
                }
            }
        }
    }

    private class ViewMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("Tabbed View")) {
                config.setProperty("viewType", "" + TABBED);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != TABBED) {
                    viewType = TABBED;
                    loadAlgorithms();
                } else {
                    viewType = TABBED;
                }
            } else if (buttonText.equals("Grid View")) {
                config.setProperty("viewType", "" + GRID);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != GRID) {
                    viewType = GRID;
                    loadAlgorithms();
                } else {
                    viewType = GRID;
                }
            } else if (buttonText.equals("Toggle Log Table")) {
                config.setProperty("logTable", "" + toggleLogTable.isSelected());
                config.save();
                if (eventThread != null) { //If events have been loaded
                    if (toggleLogTable.isSelected()) { //If the button is now selected, add the logPanel
                        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPane, logPanel);
                        p.setResizeWeight(1);
                        p.setDividerSize(3);
                        getContentPane().add(p);
                    } else { //Otherwise, remove the log panel
                        getContentPane().removeAll();
                        getContentPane().add(mainPane);
                    }
                    validate();
                }
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press:\n" + buttonText);
            }
        }
    }

    private class HelpMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("About")) {
                ChatterBox.alert("TrustGrapher - Written by Andrew O'Hara\n\nRevision " + CURRENT_REVISION);
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press:\n" + buttonText);
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
////////////////////////////////////////////////////////////////////////////////
