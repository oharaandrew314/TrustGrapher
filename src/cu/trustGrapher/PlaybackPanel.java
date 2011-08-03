////////////////////////////////PlaybackPanel//////////////////////////////////
package cu.trustGrapher;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import cu.trustGrapher.visualizer.eventplayer.*;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import utilities.AreWeThereYet;

/**
 * The Playback Panel components, listeners, and handlers
 * This panel controls the playback direction and speed of the log events
 * @author Andrew O'Hara
 */
public class PlaybackPanel extends javax.swing.JPanel implements EventPlayerListener {

    public JButton fastForwardButton, forwardButton, pauseButton, reverseButton, fastReverseButton;
    public JSlider fastSpeedSlider, playbackSlider;
    private TrustGrapher applet;
    public TrustEventPlayer eventThread;
    private JTable logList;
    private AreWeThereYet loadingBar;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a JPanel which contains the controls for the event player
     * @param applet The main TrustGrapher object
     * @param logList The east log panel if it exists
     */
    public PlaybackPanel(TrustGrapher applet, JTable logList) {
        this.applet = applet;
        this.logList = logList;
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Gets the loading bar object which is embedded into the playbackPanel
     * @return The loading bar
     */
    public AreWeThereYet getLoadingBar() {
        return loadingBar;
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

        loadingBar = new AreWeThereYet(this, true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        buttonPanel.add(fastReverseButton);
        buttonPanel.add(reverseButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(fastForwardButton);
        buttonPanel.add(fastSpeedSlider);
        buttonPanel.add(loadingBar.embed());

        setLayout(new java.awt.GridLayout(2, 1));
        setBorder(BorderFactory.createTitledBorder("Playback Panel"));
        add(buttonPanel);
        add(playbackSlider);
    }

    /**
     * Resets the panel buttons and EventPlayer when a new log is loaded
     * @param events The List of log events
     * @param graphs The graphs to be displayed
     */
    public void resetPanel(java.util.ArrayList<TrustLogEvent> events, java.util.ArrayList<cu.trustGrapher.graph.SimGraph[]> graphs) {
        if (events.isEmpty()) {
            playbackSlider.setMaximum(0);
            eventThread = new TrustEventPlayer(graphs); // create the event player
        } else {
            playbackSlider.setMinimum(0);
            playbackSlider.setMaximum((int) events.get(events.size() - 1).getTime());
            eventThread = new TrustEventPlayer(graphs, events, playbackSlider); // create the event player
        }
        SliderListener s = new SliderListener();
        playbackSlider.addChangeListener(s);
        playbackSlider.addMouseListener(s);
        eventThread.addEventPlayerListener(this);

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
        fastReverseButton.setEnabled(eventThread.atFront() ? false: true);
        reverseButton.setEnabled(eventThread.atFront() ? false: true);
        pauseButton.setEnabled(false);
        forwardButton.setEnabled(eventThread.atBack() ? false: true);
        fastForwardButton.setEnabled(eventThread.atBack() ? false: true);
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
     * Repaints all the viewers to update them
     */
    @Override
    public void doRepaint() {
        for (VisualizationViewer<Agent, TestbedEdge> viewer : applet.getViewers()) {
            viewer.repaint();
        }
    }

////////////////////////////////////Listeners///////////////////////////////////

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

        int prevState = TrustEventPlayer.PAUSE;

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
                switch (prevState){
                    case TrustEventPlayer.FASTREVERSE: eventThread.fastReverse();
                    case TrustEventPlayer.REVERSE: eventThread.reverse();
                    case TrustEventPlayer.PAUSE: eventThread.pause();
                    case TrustEventPlayer.FORWARD: eventThread.forward();
                    case TrustEventPlayer.FASTFORWARD: eventThread.fastForward();
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////