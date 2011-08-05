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
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
     * When the event list table is shown, this is called to pass the event list to this
     * The logList is needed so the PlaybackPanel can highlight the events that have passed
     * @param logList The list of Trust Log Events
     */
    public void setLogList(JTable logList){
        this.logList = logList;
    }

    /**
     * Resets the panel buttons and EventPlayer when a new log is loaded
     * @param events The List of log events
     * @param graphs The graphs to be displayed in the viewers
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
        if (applet.getViewType() == TrustGrapher.TABBED){
            applet.getCurrentViewer().repaint();
        }else{
            for (VisualizationViewer<Agent, TestbedEdge> viewer : applet.getViewers()) {
                viewer.repaint();
            }
        }
    }

////////////////////////////////////Listeners///////////////////////////////////

    /**
     * Handles PlaybackPanel button clicks
     */
    class PlaybackButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton)e.getSource()).getText();
            if (buttonText.equals("<|<|")){
                eventThread.fastReverse();
            }else if (buttonText.equals("<|")){
                eventThread.reverse();
            }else if (buttonText.equals("||")){
                eventThread.pause();
            }else if (buttonText.equals("|>")){
                eventThread.forward();
            }else if (buttonText.equals("|>|>")){
                eventThread.fastForward();
            }else{
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
                switch (prevState) {
                    case TrustEventPlayer.FASTREVERSE:
                        eventThread.fastReverse();
                    case TrustEventPlayer.REVERSE:
                        eventThread.reverse();
                    case TrustEventPlayer.PAUSE:
                        eventThread.pause();
                    case TrustEventPlayer.FORWARD:
                        eventThread.forward();
                    case TrustEventPlayer.FASTFORWARD:
                        eventThread.fastForward();
                }
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
