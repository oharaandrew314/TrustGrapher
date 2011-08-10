/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.visualizer.eventplayer;

import cu.trustGrapher.PlaybackPanel;
import cu.trustGrapher.TrustGrapher;
import cu.trustGrapher.graph.SimGraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;
import utilities.ChatterBox;

/**
 * Plays through the list of leg events and sends graphEvents to the graphs
 * @author alan
 * @author Andrew O'Hara
 *
 */
public class TrustEventPlayer implements ActionListener {

    public static final int FASTREVERSE = -2, REVERSE = -1, PAUSE = 0, FORWARD = 1, FASTFORWARD = 2;
    private Timer schedule;
    private TimeCounter timeCounter;
    public static final int speed = 250; // This many milliseconds between events while playing regularly
    private int fastMultiplier = 10;
    private int state;
    private ArrayList<TrustLogEvent> logEvents;
    private PlaybackPanel playbackPanel;
    private int currentEventIndex;
    private ArrayList<SimGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventPlayer(ArrayList<SimGraph[]> graphs, ArrayList<TrustLogEvent> eventlist, PlaybackPanel playbackPanel) {
        this.graphs = graphs;
        this.playbackPanel = playbackPanel;
        logEvents = eventlist;
        currentEventIndex = 0;
        state = FORWARD;
        timeCounter = new TimeCounter(speed, 0, 0, eventlist.size() * speed);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getPlayState() {
        return state;
    }

    public int getCurrentIndex() {
        return currentEventIndex;
    }

    /**
     * Returns whether or not the graph is playing forward or backwards.
     * @return <code>true</code> if the Play State is forward or fast forward.
     */
    public boolean isForward() {

        return ((state == FASTFORWARD) || (state == FORWARD));
    }

    /**
     * Returns whether or not the graph is playing fast
     * @return <code>true</code> if the Play State is fast in forward or reverse.
     */
    public boolean isFast() {
        return ((state == FASTFORWARD) || (state == FASTREVERSE));
    }

    public boolean atFront() {
//        return timeCounter.getTime() == timeCounter.getLowerBound();
        return currentEventIndex == 0;
    }

    public boolean atBack() {
//        return timeCounter.getTime() == timeCounter.getUpperBound();
        return currentEventIndex == logEvents.size() - 1;

    }

    public boolean atAnEnd() {
        return atFront() || atBack();
    }

    public int getCurrentTime() {
        return currentEventIndex * speed;
    }

    private TrustLogEvent getNextEvent(boolean isForward) {
        if (isForward && !atBack()) {
            currentEventIndex++;
            return logEvents.get(currentEventIndex);
        } else if (!isForward && !atFront()) {
            TrustLogEvent event = logEvents.get(currentEventIndex);
            currentEventIndex--;
            return event;
        }
        return null;
    }
///////////////////////////////////Methods//////////////////////////////////////
    public void setFastSpeed(int value) {
        if (value != fastMultiplier) {
            fastMultiplier = value;
            if (state == FASTFORWARD) {
                timeCounter.setIncrement(speed * fastMultiplier);
            } else if (state == FASTREVERSE) {
                timeCounter.setIncrement(-speed * fastMultiplier);
            }
        }
    }

    public void fastReverse() {
        playbackPanel.playbackFastReverse();
        if (state != FASTREVERSE) {
            int prevState = state;
            state = FASTREVERSE;
            timeCounter.setIncrement(-speed * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void reverse() {
        playbackPanel.playbackReverse();
        if (state != REVERSE) {
            int prevState = state;
            state = REVERSE;
            timeCounter.setIncrement(-speed);
            wakeup(prevState);
        }
    }

    public void fastForward() {
        playbackPanel.playbackFastForward();
        if (state != FASTFORWARD) {
            int prevState = state;
            state = FASTFORWARD;
            timeCounter.setIncrement(speed * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void forward() {
        playbackPanel.playbackForward();
        if (state != FORWARD) {
            int prevState = state;
            state = FORWARD;
            timeCounter.setIncrement(speed);
            wakeup(prevState);
        }
    }

    private synchronized void wakeup(int previousState) {
        if (previousState == PAUSE) {
            if (atAnEnd()) {
                timeCounter.doIncrement();
            }
            schedule.start();
            notify();
        }
    }

    public synchronized void pause() {
        playbackPanel.playbackPause();
        if (state != PAUSE) {
            state = PAUSE;
            notify();
        }
    }

    public void goToTime(int value) {
        int prevState = state;
        state = (value < timeCounter.getTime()) ? REVERSE : FORWARD;
        timeCounter.setTime(value);
        state = prevState;
    }

    public void stopPlayback() {
        wakeup(state);
        schedule.stop();
    }

    public void run() {
        schedule = new Timer(speed, this);
        schedule.start();

    }

    /**
     * Handles the passed TrustLogEvent be it structural or visual.
     * @param evt The Log event to handle.
     */
    private void handleLogEvent(TrustLogEvent evt, boolean forward) {
        for (SimGraph[] graph : graphs) {
            graph[TrustGrapher.DYNAMIC].graphEvent(evt, forward, graph[TrustGrapher.FULL]);
        }
    }

    /**
     * Called when the slider is changed
     * @param arg0 
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (state != PAUSE) {
            timeCounter.doIncrement();
            long nextTime = timeCounter.getTime();
            boolean isforward = nextTime > getCurrentTime();
            TrustLogEvent event = getNextEvent(isforward);
            if (event != null) {
                handleLogEvent(event, isforward);
                playbackPanel.getSlider().setValue(currentEventIndex * speed);
                playbackPanel.doRepaint();
            } else {
                pause();
            }
        }
    }
    
    public void goToEvent(int eventIndex) {
        boolean isForward = true;
        LinkedList<TrustLogEvent> events = new LinkedList<TrustLogEvent>();
        if (currentEventIndex < eventIndex) { //Forward
            while (currentEventIndex < eventIndex) {
                currentEventIndex++;
                events.add(logEvents.get(currentEventIndex));
            }
        } else if (currentEventIndex > eventIndex) { //Backward
            isForward = false;
            while (currentEventIndex > eventIndex) {
                events.add(logEvents.get(currentEventIndex));
                currentEventIndex--;
            }
        }
        if (!events.isEmpty()){
            for (TrustLogEvent event : events){
                handleLogEvent(event, isForward);
            }
        }
        playbackPanel.getSlider().setValue(currentEventIndex * speed);
        playbackPanel.doRepaint();
    }
}
////////////////////////////////////////////////////////////////////////////////
