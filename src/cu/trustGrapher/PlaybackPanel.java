////////////////////////////////PlaybackPanel//////////////////////////////////
package cu.trustGrapher;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.visualizer.eventplayer.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import utilities.ChatterBox;

/**
 * The Playback Panel components, listeners, and handlers
 * This panel controls the playback direction and speed of the log events
 * @author Andrew O'Hara
 */
public class PlaybackPanel extends javax.swing.JPanel implements EventPlayerListener {

    public JButton fastForwardButton, forwardButton, pauseButton, reverseButton, fastReverseButton;
    private JPanel tablePanel;
    public JSlider fastSpeedSlider, playbackSlider;
    private TrustGrapher applet;
    public TrustEventPlayer eventThread;
    private JTable logList;
    private JPopupMenu menu;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a JPanel which contains the controls for the event player
     * @param applet The main TrustGrapher object
     * @param logList The east log panel if it exists
     */
    public PlaybackPanel(TrustGrapher applet) {
        this.applet = applet;
        initComponents();
    }
    
//////////////////////////////////Accessors/////////////////////////////////////
    public JSlider getSlider(){
        return playbackSlider;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Creates all the components of the playback panel
     */
    private void initComponents() {
        fastSpeedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
        fastSpeedSlider.addChangeListener(new SpeedSliderListener());
        fastSpeedSlider.setMajorTickSpacing((fastSpeedSlider.getMaximum() - fastSpeedSlider.getMinimum()) / 4);
        fastSpeedSlider.setFont(new Font("Arial", Font.PLAIN, 8));
        fastSpeedSlider.setPaintTicks(false);
        fastSpeedSlider.setPaintLabels(true);
        fastSpeedSlider.setForeground(java.awt.Color.BLACK);
        fastSpeedSlider.setBorder(BorderFactory.createTitledBorder("Quick Playback Speed"));
        fastSpeedSlider.setEnabled(false);

        ActionListener listener = new PlaybackButtonListener();

        fastReverseButton = new JButton("<|<|");
        fastReverseButton.addActionListener(listener);
        fastReverseButton.setEnabled(false);

        reverseButton = new JButton("<|");
        reverseButton.addActionListener(listener);
        reverseButton.setEnabled(false);

        pauseButton = new JButton("||");
        pauseButton.addActionListener(listener);
        pauseButton.setEnabled(false);

        forwardButton = new JButton("|>");
        forwardButton.addActionListener(listener);
        forwardButton.setEnabled(false);
        //forwardButton.setIcon(new ImageIcon(getClass().getResource("/trustGrapher/resources/forward.png")));
        //forwardButton.setSize(48,25);

        fastForwardButton = new JButton("|>|>");
        fastForwardButton.addActionListener(listener);
        fastForwardButton.setEnabled(false);

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        buttonPanel.add(fastReverseButton);
        buttonPanel.add(reverseButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(fastForwardButton);
        buttonPanel.add(fastSpeedSlider);

        setLayout(new java.awt.GridLayout(2, 1));
        setBorder(BorderFactory.createTitledBorder("Playback Panel"));
        add(buttonPanel);
        add(playbackSlider);
    }

    /**
     * Creates the log list panel which shows the events in the current log
     * @param logEvents The list of log events
     * @return The JPanel containing the log list
     */
    public JPanel initializeLogList(List<TrustLogEvent> logEvents) {
        Object[][] table = new Object[logEvents.size()][3];
        table[0] = new Object[] {"","empty",""};
        int i = 1;
        for (TrustLogEvent evt : logEvents) {
            if (evt != null){
                table[i] = evt.toArray();
                i++;
            }
        }
        
        Object[] titles = {"Assessor", "Assessee", "Feedback"};
        logList = new JTable(table, titles);
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setBackground(Color.LIGHT_GRAY);
        logList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        logList.setColumnSelectionAllowed(false);
        logList.setRowSelectionAllowed(true);
        ListListener listener = new ListListener();
        logList.getSelectionModel().addListSelectionListener(listener);
        logList.addMouseListener(listener);
        logList.setVisible(true);

        JScrollPane listScroller = new JScrollPane(logList);
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
        listScroller.setSize(logList.getWidth(), logList.getHeight());

        tablePanel = new JPanel(new GridLayout(1, 1));
        tablePanel.add(listScroller);
        tablePanel.setName("Log Events");
        tablePanel.setBorder(BorderFactory.createTitledBorder(tablePanel.getName()));
        createPopupMenu(listener);

        return tablePanel;
    }

    private void createPopupMenu(ActionListener listener) {
        JMenuItem insert = new JMenuItem("Insert Event After");
        insert.addActionListener(listener);
        JMenuItem remove = new JMenuItem("Remove Event");
        remove.addActionListener(listener);

        menu = new JPopupMenu("Edit Events");
        menu.add(insert);
        menu.add(remove);
    }

    /**
     * Resets the panel buttons and EventPlayer when a new log is loaded
     * @param events The List of log events
     * @param graphs The graphs to be displayed in the viewers
     */
    public void resetPanel(java.util.ArrayList<TrustLogEvent> events, java.util.ArrayList<cu.trustGrapher.graph.SimGraph[]> graphs) {
        playbackSlider.setMinimum(0);
        playbackSlider.setMaximum((events.size()-1) * TrustEventPlayer.speed);
        eventThread = new TrustEventPlayer(graphs, events, this); // create the event player
        
        SliderListener s = new SliderListener();
        playbackSlider.addChangeListener(s);
        playbackSlider.addMouseListener(s);

        fastReverseButton.setEnabled(true);
        reverseButton.setEnabled(true);
        pauseButton.setEnabled(true);
        forwardButton.setEnabled(false);
        fastForwardButton.setEnabled(true);
        playbackSlider.setEnabled(true);
        fastSpeedSlider.setEnabled(true);
    }

    public void disableButtons() {
        fastReverseButton.setEnabled(false);
        reverseButton.setEnabled(false);
        pauseButton.setEnabled(false);
        forwardButton.setEnabled(false);
        fastForwardButton.setEnabled(false);
        playbackSlider.setEnabled(false);
        playbackSlider.setValue(0);
        fastSpeedSlider.setEnabled(false);
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
        fastReverseButton.setEnabled(eventThread.atFront() ? false : true);
        reverseButton.setEnabled(eventThread.atFront() ? false : true);
        pauseButton.setEnabled(false);
        forwardButton.setEnabled(eventThread.atBack() ? false : true);
        fastForwardButton.setEnabled(eventThread.atBack() ? false : true);
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

    /**
     * Repaints the viewers to update them
     * If the view type is tabbed, then it is only necessary to update the current viewer
     */
    @Override
    public void doRepaint() {
        if (applet.getViewType() == TrustGrapher.TABBED) {
            applet.getCurrentViewer().repaint();
        } else {
            for (VisualizationViewer<Agent, TestbedEdge> viewer : applet.getViewers()) {
                viewer.repaint();
            }
        }
    }
////////////////////////////////////Listeners///////////////////////////////////

    private class ListListener extends MouseAdapter implements ListSelectionListener, ActionListener {
        private int currentRow;
                
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel model = logList.getSelectionModel();
            if (eventThread.getPlayState() == TrustEventPlayer.PAUSE) {
                for (int i = 0; i <= logList.getRowCount(); i++) {
                    if (model.isSelectedIndex(i)) {
                        currentRow = i;
                        eventThread.goToEvent(i);
//                        playbackSlider.setValue(i * TrustEventPlayer.speed);
//                        eventThread.actionPerformed(null);
//                      eventThread.goToTime(i * TrustEventPlayer.speed);
//                        doRepaint();
                        break;
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            currentRow = logList.rowAtPoint(p);
            logList.getSelectionModel().setSelectionInterval(currentRow, currentRow);
            if (SwingUtilities.isRightMouseButton(e)) {
                menu.show(tablePanel, tablePanel.getMousePosition().x, tablePanel.getMousePosition().y);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("Insert Event After")) {
                EventInjector.getNewEvent();
            } else if (buttonText.equals("Remove Event")) {
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press");
            }
        }
    }

    /**
     * Handles PlaybackPanel button clicks
     */
    class PlaybackButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals("<|<|")) {
                eventThread.fastReverse();
            } else if (buttonText.equals("<|")) {
                eventThread.reverse();
            } else if (buttonText.equals("||")) {
                eventThread.pause();
            } else if (buttonText.equals("|>")) {
                eventThread.forward();
            } else if (buttonText.equals("|>|>")) {
                eventThread.fastForward();
            } else {
                utilities.ChatterBox.error(this, "actionPerformed()", "An unhandled PlayBackPanel button press occured");
            }
        }
    }

    /**
     * Handles changes to the Speed Slider
     */
    class SpeedSliderListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent arg0) {
            eventThread.setFastSpeed(((JSlider) arg0.getSource()).getValue());
        }
    }

    /**
     * Handles mouse clicks on the timeline slider on the playbackPanel
     */
    class SliderListener extends MouseAdapter implements ChangeListener {

        int prevState;
        
        @Override
        public void stateChanged(ChangeEvent ce) {
            eventThread.goToTime(playbackSlider.getValue());
            if (logList != null) { //if log list is initialized and showing
                if (logList.isVisible()) {
                    logList.clearSelection();
                    ChatterBox.print("" + playbackSlider.getValue() / TrustEventPlayer.speed);
                    logList.addRowSelectionInterval(0, playbackSlider.getValue() / TrustEventPlayer.speed);
                    logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentIndex() - 1, 0, true));
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (playbackSlider.isEnabled()) {
                prevState = eventThread.getPlayState();
                eventThread.pause();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (playbackSlider.isEnabled()) {
                switch (prevState) {
                    case TrustEventPlayer.FASTREVERSE:
                        eventThread.fastReverse();
                        break;
                    case TrustEventPlayer.REVERSE:
                        eventThread.reverse();
                        break;
                    case TrustEventPlayer.PAUSE:
                        eventThread.pause();
                        break;
                    case TrustEventPlayer.FORWARD:
                        eventThread.forward();
                        break;
                    case TrustEventPlayer.FASTFORWARD:
                        eventThread.fastForward();
                        break;
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
