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
import java.util.ArrayList;
import javax.swing.*;
import java.util.List;

import java.util.LinkedList;
import utilities.*;

/**
 * An application that will display Trust Graphs generated from a series of feedback events.
 * Multiple graphs can be viewed in different layouts and viewing modes
 * The events can be "played" forward and backward, and the graphs will change to display their states after every tick
 * @author Alan
 * @author Matt
 * @author Andrew O'Hara
 */
public final class TrustGrapher extends JFrame {

    public static final int CURRENT_REVISION = 48;
    public static final int DEFWIDTH = 1360, DEFHEIGHT = 768; //default size for the swing graphic components
    private List<GraphViewer> viewers; //Each of the viewers is a component which displays a graph
    private List<GraphPair> graphs;  //A list of the the graph pairs attached to the viewers
    private Integer viewType; //Keeps track of which graph view to use
    public static final int TABBED = 0, GRID = 1, DEFAULT_VIEW = TABBED; //View types
    private ViewerPopupMenu popupMenu;  //The popup menu that is shown when a viewer is right-clicked
    //This window is used to configure which algorithms to load, what graphs are using what algorithms, and what graphs to display.
    //It saves all of the configurations to TrustPropertyManager config
    private PropertyManager config; //The Property Manager that contains all of the saved algorithmLoader for the applet and algorithm loader
    private JCheckBoxMenuItem toggleEventPanel; //A check box which is used to show the log table
    private Container graphsPanel; //This container contains all of the TrustGraphViewers as components
    private EventPlayer eventThread; //Plays through the list of events and updates the graphs and its' listeners

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

    /**
     * The view type decides what format the viewers will be shown by.
     * Returns the view type.
     * @return The integer representation of the view type
     */
    public int getViewType() {
        return viewType;
    }

    /**
     * @return Returns the list of GraphPair objects
     */
    public List<GraphPair> getGraphs() {
        return graphs;
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
        if (viewType == TABBED) {
            List<GraphViewer> visibleViewers = new java.util.LinkedList<GraphViewer>();
            visibleViewers.add((GraphViewer) ((JTabbedPane) graphsPanel).getSelectedComponent());
            return visibleViewers;
        } else {
            return viewers;
        }
    }
    
    public EventPlayer getEventPlayer(){
        return eventThread;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the AlgorithmLoader when the user clicks ok.
     * If a log is already being simulated, pause the simulator
     * Then, build the graphs accoring to the graphConfigs,
     * and then tell the logReader to begin loading the events.
     */
    public void algorithmsLoaded(List<GraphConfig> graphConfigs) {
        //If a log path has been selected by the Algortihm Loader
        if (config.containsKey(AlgorithmLoader.LOG_PATH)) {
            //If there are any events currently loaded, pause the simulator 
            if (eventThread != null) {
                eventThread.pause();
            }

            //Build graphs based on graphConfigs
            graphs = new ArrayList<GraphPair>();
            ArrayList<GraphConfig> trustGraphs = new ArrayList<GraphConfig>();
            for (GraphConfig graphConfig : graphConfigs) {
                if (graphConfig.isTrustAlg()) { //Process Trust Graphs after
                    trustGraphs.add(graphConfig);
                } else { //Othwerise, create a new GraphPair, and add it to the graphs
                    graphs.add(new GraphPair(graphConfig, graphs));
                }
            }
            //Trust graphs are made last because their base graph might not have been made before
            for (GraphConfig graphConfig : trustGraphs) {
                graphs.add(new GraphPair(graphConfig, graphs));
            }

            //Begin reading the log and performing graphConstructionEvents
            java.io.File logFile = new java.io.File(config.getProperty(AlgorithmLoader.LOG_PATH));
            LogReader logReader = new LogReader(this, logFile, new AreWeThereYet(this));
            logReader.execute(); //After the log reader thread is complete, startGraph() will be called
        } else {
            ChatterBox.alert("No log was loaded, so no action will be taken.");
        }
    }

    /**
     * Creates a new AlgorithmLoader and runs it.  After the user clicks ok, startGraph() will be called
     * In the meantime, the sumulator will be idle
     */
    public void startAlgorithmLoader() {
        AlgorithmLoader.run(this, config);
    }
    
    /**
     * Enables or disables the menus in the menu bar
     * @param enabled true for enabled, false for disabled
     */
    public void enableMenu(boolean enabled){
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
        toggleEventPanel = new JCheckBoxMenuItem("Toggle Log Table");
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
        toggleEventPanel.addActionListener(viewListener);
        about.addActionListener(new HelpMenuListener());

        //Reads the property file and shows the event panel if necessary
        String showEventPanel = config.getProperty("logTable");
        if (showEventPanel == null) {
            showEventPanel = "true";
            config.setProperty("logTable", showEventPanel);
            config.save();
        }
        toggleEventPanel.setSelected(Boolean.parseBoolean(showEventPanel));

        //Set view type from value in config
        try {
            viewType = Integer.parseInt(config.getProperty("viewType"));
        } catch (NumberFormatException ex) {
            viewType = TrustGrapher.DEFAULT_VIEW;
        }
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
        view.add(toggleEventPanel);

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
     * Builds and returns a new SplitPane which contains the graphsPanel and playbackPanel.
     * @return A new mainpane containing the graphsPanel and playbackPanel
     */
    public JSplitPane buildMainPane() {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphsPanel, eventThread.getPlaybackPanel());
        pane.setResizeWeight(1); //Means the size of the playbackPanel will set the position of the divider
        pane.setDividerSize(3);
        return pane;
    }

    /**
     * Called by the log reader thread upon completion, or by the EventPlayer upon an event modification,
     * or the view type buttons in the menu bar when the view type is changed.
     * This method adds a new mainPane to the graph, and adds new TrustGraphViewers to the graphsPanel, then starts a new eventThread
     * @param events The event list returned by the log reader thread or EventPlayer
     */
    public void startGraph(List<TrustLogEvent> events) {
        //Creates a new graphsPanel depending on the view type
        graphsPanel = (viewType == GRID) ? new JPanel(new GridLayout(2, 3)) : new JTabbedPane(JTabbedPane.TOP);
        graphsPanel.setBackground(Color.LIGHT_GRAY);

        //Create viewer listeners and popup menu
        DefaultModalGraphMouse<Agent, TestbedEdge> gm = new DefaultModalGraphMouse<Agent, TestbedEdge>();
        ViewerListener listener = new ViewerListener();
        popupMenu = new ViewerPopupMenu(gm, listener);

        //Create the Visualization Viewers
        viewers = new LinkedList<GraphViewer>();
        for (GraphPair graphPair : graphs) {
            if (graphPair.isDisplayed()) {
                //Sets the initial (FRLayout) of the graph.  The graphs must have already had their construction events processed,
                //or the graphs will have a random layout
                AbstractLayout<Agent, TestbedEdge> layout = new FRLayout<Agent, TestbedEdge>(graphPair.getFullGraph());
                layout.setInitializer(new VertexPlacer(layout, new Dimension(DEFWIDTH / 3, DEFHEIGHT / 2)));
                //Creates the new GraphViewer
                GraphViewer viewer = new GraphViewer(layout, DEFWIDTH / 3, DEFHEIGHT / 2, gm, listener, graphPair);
                if (graphsPanel instanceof JPanel) {  //If the graphsPanel is set for grid view
                    viewer.setBorder(BorderFactory.createTitledBorder(graphPair.getDisplayName()));
                    ((JPanel) graphsPanel).add(viewer);
                } else { //Othwerwise, the graphsPanel is set for Tabbed view, which is the default.
                    ((JTabbedPane) graphsPanel).addTab(graphPair.getDisplayName(), viewer);
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
        getContentPane().add(buildMainPane()); //The main pane includes the graphsPanel and the playbackPanel

        if (toggleEventPanel.isSelected()) { //If the toggle log table check box is selected, reset the log table
            toggleEventPanel.setSelected(false);
            toggleEventPanel.doClick();
        }
        eventThread.run(); //Start the eventThread
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

    /**
     * Listens for file menu button click events and deals with them accordingly
     */
    private class FileMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            PlaybackPanel playbackPanel = eventThread.getPlaybackPanel();
            if (buttonText.equals("Load Algorithms")) {
                if (playbackPanel != null) {
                    playbackPanel.getPauseButton().doClick();
                }
                startAlgorithmLoader();
            } else if (buttonText.equals("Export Results")) {
                //Implement export results
                ChatterBox.alert("Export results.  To implement this, go to cu.trustGrapher.TrustGrapher,\nthen search 'Implement export results'.");
            } else if (buttonText.equals("Options")) {
                OptionsWindow.run(TrustGrapher.this);
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

    /**
     * Listens for edit menu button click events and deals with them accordingly
     */
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

    /**
     * Listens for view menu button click events and deals with them accordingly
     */
    private class ViewMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("Tabbed View")) {
                config.setProperty("viewType", "" + TABBED);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != TABBED) {
                    viewType = TABBED;
                    eventThread.goToEvent(0);
                    startGraph(eventThread.getEvents());
                } else {
                    viewType = TABBED;
                }
            } else if (buttonText.equals("Grid View")) {
                config.setProperty("viewType", "" + GRID);
                config.save();
                //If there are graphs running, and the view was changed from something else, reset the graph
                if (graphsPanel != null && viewType != GRID) {
                    viewType = GRID;
                    eventThread.goToEvent(0);
                    startGraph(eventThread.getEvents());
                } else {
                    viewType = GRID;
                }
            } else if (buttonText.equals("Toggle Log Table")) {
                config.setProperty("logTable", "" + toggleEventPanel.isSelected());
                config.save();
                if (eventThread != null) { //If events have been loaded
                    getContentPane().removeAll();
                    //If the button is now selected, build a new JSplitPane, and add the mainPane and EventPanel
                    if (toggleEventPanel.isSelected()) {
                        JSplitPane newPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMainPane(), eventThread.getLogPanel());
                        newPane.setResizeWeight(1);
                        newPane.setDividerSize(3);
                        getContentPane().add(newPane);
                    } else { //Otherwise, remove the log panel
                        getContentPane().add(buildMainPane());
                    }
                    validate();
                }
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press:\n" + buttonText);
            }
        }
    }

    /**
     * Listens for help menu button click events and deals with them accordingly
     */
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
     * to start this program as a java application
     */
    public static void main(String[] args) {
        TrustGrapher myApp = new TrustGrapher();
    }
}
////////////////////////////////////////////////////////////////////////////////
