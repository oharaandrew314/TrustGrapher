////////////////////////////////PlaybackPanel//////////////////////////////////
package cu.trustGrapher.eventplayer;

import cu.trustGrapher.OptionsWindow;
import java.awt.GridBagLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import aohara.utilities.ChatterBox;

/**
 * The PlaybackPanel always exists when graphs are loaded, but it is only displayed when the check box option for it is checked.
 * It contains a timeline slider which can be scrubbed or dragged and dropped depending on the option that is selected.
 * It also has buttons which control the direction of event playback.
 * There is also a slider which changes the number of events processed on each tick (essentially the speed of playback)
 * 
 * This class is just the JPanel and handlers.  It must be placed by TrustGrapher, and is only updated to display the current
 * play state if the EventPlayer adds it as a listener.
 * @author Andrew O'Hara
 */
public final class PlaybackPanel extends JPanel implements EventPlayerListener {

    private static final String EVENTS_PER_TICK = "eventsPerTick";
    private boolean scrubMode = true;  //The scrub mode for the timelineSlider is true by default
    private JButton forwardButton, reverseButton, pauseButton;
    private JSlider speedSlider, timelineSlider;
    private EventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a JPanel which contains controls for the event player
     */
    public PlaybackPanel(EventPlayer eventThread) {
        this.eventThread = eventThread;
        initComponents();
        //Load saved properties
        aohara.utilities.PropertyManager config = eventThread.getTrustGrapher().getPropertyManager();
        if (config.containsKey(EVENTS_PER_TICK)) { //Try loading value for speedSlider
            try {
                speedSlider.setValue(Integer.parseInt(config.getProperty(EVENTS_PER_TICK)));
            } catch (NumberFormatException ex) {
                aohara.utilities.ChatterBox.alert("Invalid eventsPerTick property.  Can continue.  Will set to defualt.");
            }
        }

        if (config.containsKey(OptionsWindow.SCRUB_MODE)) { //Try loading the timelineSlider mode
            try{
                scrubMode = Boolean.parseBoolean(config.getProperty(OptionsWindow.SCRUB_MODE));
            }catch (NullPointerException ex){
                ChatterBox.error("PlaybackPanel", "<init>", "NullPointer Exception: most likely because the " + 
                        OptionsWindow.SCRUB_MODE + " property was invalid.");                
            }
        }
        setVisible(eventThread.getTrustGrapher().getTrustMenuBar().togglePlaybackPanelButton.isSelected());
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public JButton getPauseButton() {
        return pauseButton;
    }

    public EventPlayer getEventPlayer() {
        return eventThread;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Sets whether the timelineSlider mode is set to scrub.  Drag & Drop otherwise.
     * @param scrub true to set to the timelineSlider to scrub mode, false to set to Drag & Drop mode
     */
    public void setScrubMode(boolean scrub) {
        scrubMode = scrub;
    }

    /**
     * Creates all the components of the playbackPanel
     */
    private void initComponents() {
        ActionListener buttonListener = new PlaybackButtonListener();
        TimelineSliderListener playbackSliderListener = new TimelineSliderListener();

        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 1);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintLabels(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.addChangeListener(new SpeedSliderListener());
        speedSlider.setBorder(BorderFactory.createTitledBorder("Events per Tick"));

        reverseButton = new JButton(" <| ");
        reverseButton.addActionListener(buttonListener);
        reverseButton.setEnabled(false);

        pauseButton = new JButton(" || ");
        pauseButton.addActionListener(buttonListener);
        pauseButton.setEnabled(false);

        forwardButton = new JButton(" |> ");
        forwardButton.addActionListener(buttonListener);
        //forwardButton.setIcon(new ImageIcon(getClass().getResource("/trustGrapher/resources/forward.png")));
        //forwardButton.setSize(48,25);

        timelineSlider = new JSlider(JSlider.HORIZONTAL, 0, eventThread.getEvents().size() - 1, 0);
        timelineSlider.setEnabled(false);
        timelineSlider.setSnapToTicks(true);

        timelineSlider.addChangeListener(playbackSliderListener);
        timelineSlider.addMouseListener(playbackSliderListener);
        timelineSlider.setEnabled(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(reverseButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(speedSlider);

        setLayout(new java.awt.GridLayout(2, 1));
        setBorder(BorderFactory.createTitledBorder("Playback Panel"));
        add(buttonPanel);
        add(timelineSlider);
    }

    /**
     * Moves the timeline slider to the given index and updates the playback buttons to
     * reflect the position in the timeline and the EventPlayer playState.
     * This should only be called by the EventPlayer to notify it that the currentEventIndex has changed.
     * @param eventIndex 
     */
    @Override
    public void goToIndex(int eventIndex) {
        if (eventIndex > timelineSlider.getValue()) {
            reverseButton.setEnabled(true);
            pauseButton.setEnabled(true);
            forwardButton.setEnabled(eventThread.getPlayState() != EventPlayer.FORWARD);
        } else if (eventIndex < timelineSlider.getValue()) {
            reverseButton.setEnabled(eventThread.getPlayState() != EventPlayer.REVERSE);
            pauseButton.setEnabled(true);
            forwardButton.setEnabled(true);
        }
        timelineSlider.setValue(eventIndex); //Gets the viewers that are visible from the main class and repaints them
        if (timelineSlider.getValue() == 0 || timelineSlider.getValue() == eventThread.getEvents().size() - 1) {
            playbackPause();
        }
    }

    /**
     * Updates the playback buttons to reflect a paused playState depending on 
     * the position in the timeline.
     */
    public void playbackPause() {
        reverseButton.setEnabled(eventThread.atFront() ? false : true);
        pauseButton.setEnabled(false);
        forwardButton.setEnabled(eventThread.atBack() ? false : true);
    }
////////////////////////////////////Listeners///////////////////////////////////

    /**
     * Handles PlaybackPanel button clicks
     */
    class PlaybackButtonListener implements ActionListener {

        /**
         * Handles the playbackButton clicks.  Notifies the EventPlayer what direction to play in.
         * @param actionEvent 
         */
        public void actionPerformed(ActionEvent actionEvent) {
            String buttonText = ((AbstractButton) actionEvent.getSource()).getText();
            if (buttonText.equals(" || ")) {
                playbackPause();
                eventThread.pause();
            } else if (buttonText.equals(" |> ")) {
                eventThread.forward();
            } else if (buttonText.equals(" <| ")) {
                eventThread.reverse();
            } else {
                aohara.utilities.ChatterBox.error(this, "actionPerformed()", "An unhandled PlayBackPanel button press occured");
            }
        }
    }

    /**
     * Handles changes to the Speed Slider
     */
    class SpeedSliderListener implements ChangeListener {

        /**
         * Notifies the EventPlayer of how many events to process per tick, and then saves the proeprty.
         * @param changeEvent 
         */
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            eventThread.setEventsPerTick(((JSlider) changeEvent.getSource()).getValue());
            eventThread.getTrustGrapher().getPropertyManager().setProperty(EVENTS_PER_TICK, "" + speedSlider.getValue());
            eventThread.getTrustGrapher().getPropertyManager().save();
        }
    }

    /**
     * Handles mouse clicks on the timeline slider on the playbackPanel
     */
    class TimelineSliderListener extends MouseAdapter implements ChangeListener {

        int prevState;
        boolean scrubbing;

        /**
         * If the timelineSlider mode is set to scrub, and the mouse is scrubbing,
         * then tell the EventPlayer to move to the event index selected by the timelineSlider
         * @param changeEvent 
         */
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if (scrubbing && scrubMode) {
                eventThread.goToEvent(timelineSlider.getValue());
            }
        }

        /**
         * If the mouse is pressed on the timelineSlider, pause playback
         * and let the listener know that the mouse is scrubbing
         * @param mouseEvent 
         */
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            if (timelineSlider.isEnabled()) {
                scrubbing = true;
                prevState = eventThread.getPlayState();
                eventThread.pause();
            }
        }

        /**
         * If the timelineSlider mode is not set to scrub, tell the EventPlayer to go to
         * the event selected by the timelineSlider, and let the listener know that the
         * mouse is no longer scrubbing the timelineSlider.
         * Then tell the EventPlayer to resume the previous playstate
         * @param e 
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!scrubMode) {
                eventThread.goToEvent(timelineSlider.getValue());
            }
            if (timelineSlider.isEnabled()) {
                switch (prevState) {
                    case EventPlayer.REVERSE:
                        eventThread.reverse();
                        break;
                    case EventPlayer.PAUSE:
                        eventThread.pause();
                        playbackPause();
                        break;
                    case EventPlayer.FORWARD:
                        eventThread.forward();
                        break;
                }
                scrubbing = false;
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
