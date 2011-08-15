////////////////////////////////PlaybackPanel//////////////////////////////////
package cu.trustGrapher.eventplayer;

import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The Playback Panel components, listeners, and handlers
 * This panel controls the playback direction and speed of the log events
 * @author Andrew O'Hara
 */
public final class PlaybackPanel extends JPanel implements EventPlayerListener {

    public JButton pauseButton;
    private JButton forwardButton, reverseButton;
    private JSlider speedSlider, playbackSlider;
    private EventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Creates a JPanel which contains the controls for the event player
     * @param applet The main TrustGrapher object
     * @param logList The east log panel if it exists
     */
    public PlaybackPanel(List<TrustLogEvent> events) {
        initComponents(events);
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Creates all the components of the playback panel
     */
    private void initComponents(List<TrustLogEvent> events) {
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

        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, events.size() - 1, 0);
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
            forwardButton.setEnabled (eventThread.getPlayState() != EventPlayer.FORWARD);
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

    @Override
    public void addEventPlayer(EventPlayer eventThread) {
        this.eventThread = eventThread;
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
            if (scrubbing) {
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
            if (playbackSlider.isEnabled()) {
                switch (prevState) {
                    case EventPlayer.REVERSE:
                        eventThread.reverse();
                        break;
                    case EventPlayer.PAUSE:
                        eventThread.pause();
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
