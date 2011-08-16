////////////////////////////////PlaybackPanel//////////////////////////////////
package cu.trustGrapher.eventplayer;

import cu.trustGrapher.OptionsWindow;
import java.awt.GridBagLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The Playback Panel components, listeners, and handlers
 * This panel controls the playback direction and speed of the log events
 * @author Andrew O'Hara
 */
public final class PlaybackPanel extends JPanel implements EventPlayerListener {

    private static final String EVENTS_PER_TICK = "eventsPerTick";
    private boolean scrubMode = true;
    private JButton forwardButton, reverseButton, pauseButton;
    private JSlider speedSlider, playbackSlider;
    private EventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a JPanel which contains the controls for the event player
     */
    public PlaybackPanel(EventPlayer eventThread) {
        this.eventThread = eventThread;
        initComponents();
        utilities.PropertyManager config = eventThread.getTrustGrapher().getPropertyManager();
        if (config.containsKey(EVENTS_PER_TICK)) {
            try {
                speedSlider.setValue(Integer.parseInt(config.getProperty(EVENTS_PER_TICK)));
            } catch (NumberFormatException ex) {
                utilities.ChatterBox.alert("Invalid eventsPerTick property.  Can continue.  Will set to defualt.");
            }
        }

        if (config.containsKey(OptionsWindow.SCRUB_MODE)) {
            scrubMode = Boolean.parseBoolean(config.getProperty(OptionsWindow.SCRUB_MODE));
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public JButton getPauseButton() {
        return pauseButton;
    }

    public EventPlayer getEventPlayer() {
        return eventThread;
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void setScrubMode(boolean scrub) {
        scrubMode = scrub;
    }

    /**
     * Creates all the components of the playback panel
     */
    private void initComponents() {
        ActionListener buttonListener = new PlaybackButtonListener();
        PlaybackSliderListener playbackSliderListener = new PlaybackSliderListener();

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

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, eventThread.getEvents().size() - 1, 0);
        playbackSlider.setEnabled(false);
        playbackSlider.setSnapToTicks(true);

        playbackSlider.addChangeListener(playbackSliderListener);
        playbackSlider.addMouseListener(playbackSliderListener);
        playbackSlider.setEnabled(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(reverseButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(speedSlider);

        setLayout(new java.awt.GridLayout(2, 1));
        setBorder(BorderFactory.createTitledBorder("Playback Panel"));
        add(buttonPanel);
        add(playbackSlider);
    }

    @Override
    public void goToIndex(int eventIndex) {
        if (eventIndex > playbackSlider.getValue()) {
            reverseButton.setEnabled(true);
            pauseButton.setEnabled(true);
            forwardButton.setEnabled(eventThread.getPlayState() != EventPlayer.FORWARD);
        } else if (eventIndex < playbackSlider.getValue()) {
            reverseButton.setEnabled(eventThread.getPlayState() != EventPlayer.REVERSE);
            pauseButton.setEnabled(true);
            forwardButton.setEnabled(true);
        }
        playbackSlider.setValue(eventIndex); //Gets the viewers that are visible from the main class and repaints them
        if (playbackSlider.getValue() == 0 || playbackSlider.getValue() == eventThread.getEvents().size() - 1) {
            playbackPause();
        }
    }

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

        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
            if (buttonText.equals(" || ")) {
                playbackPause();
                eventThread.pause();
            } else if (buttonText.equals(" |> ")) {
                eventThread.forward();
            } else if (buttonText.equals(" <| ")) {
                eventThread.reverse();
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
            eventThread.setEventsPerTick(((JSlider) arg0.getSource()).getValue());
            eventThread.getTrustGrapher().getPropertyManager().setProperty(EVENTS_PER_TICK, "" + speedSlider.getValue());
            eventThread.getTrustGrapher().getPropertyManager().save();
        }
    }

    /**
     * Handles mouse clicks on the timeline slider on the playbackPanel
     */
    class PlaybackSliderListener extends MouseAdapter implements ChangeListener {

        int prevState;
        boolean scrubbing;

        @Override
        public void stateChanged(ChangeEvent ce) {
            if (scrubbing && scrubMode) {
                eventThread.goToEvent(playbackSlider.getValue());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (playbackSlider.isEnabled()) {
                scrubbing = true;
                prevState = eventThread.getPlayState();
                eventThread.pause();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!scrubMode) {
                eventThread.goToEvent(playbackSlider.getValue());
            }
            if (playbackSlider.isEnabled()) {
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
