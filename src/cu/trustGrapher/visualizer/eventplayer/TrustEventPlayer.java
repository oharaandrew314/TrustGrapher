/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.visualizer.eventplayer;

import cu.trustGrapher.PlaybackPanel;

import cu.trustGrapher.TrustGrapher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.Timer;

/**
 * Plays through the list of leg events and sends graphEvents to the graphs
 * @author alan
 * @author Andrew O'Hara
 *
 */
public final class TrustEventPlayer implements ActionListener {

    public static final int FASTREVERSE = -2, REVERSE = -1, PAUSE = 0, FORWARD = 1, FASTFORWARD = 2;
    private int state;
    private Timer schedule;
    private TimeCounter timeCounter;
    public static final int speed = 250; // This many milliseconds between events while playing regularly
    private int fastMultiplier = 10;
    private ArrayList<TrustLogEvent> logEvents;
    private PlaybackPanel playbackPanel;
    private int currentEventIndex;
    private TrustGrapher applet;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventPlayer(TrustGrapher applet, ArrayList<TrustLogEvent> eventlist, PlaybackPanel playbackPanel) {
        this.applet = applet;
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
        return currentEventIndex == 0;
    }

    public boolean atBack() {
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

    public void goToTime(int newTime) {
        int prevState = state;
        state = (newTime < timeCounter.getTime()) ? REVERSE : FORWARD;
        timeCounter.setTime(newTime);
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
     * Called when the slider is changed
     * @param arg0 
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (state != PAUSE) {
            timeCounter.doIncrement();
            long nextTime = timeCounter.getTime();
            boolean isForward = nextTime > getCurrentTime();
            TrustLogEvent event = getNextEvent(isForward);
            if (event != null) {
                applet.getGraphManager().handleGraphEvent(event, isForward);
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
                applet.getGraphManager().handleGraphEvent(event, isForward);
            }
        }
        playbackPanel.getSlider().setValue(currentEventIndex * speed);
        playbackPanel.doRepaint();
    }
    
    public void insertEvent(TrustLogEvent event){
        ArrayList<TrustLogEvent> newEvents = new ArrayList<TrustLogEvent>(logEvents.subList(0, currentEventIndex));
        newEvents.add(event);
        newEvents.addAll(logEvents.subList(currentEventIndex + 1, logEvents.size() - 1));
        applet.startGraph(newEvents);
    }
    
    public void removeEvent(){
        logEvents.remove(currentEventIndex);
        applet.startGraph(logEvents);
    }
}
////////////////////////////////////////////////////////////////////////////////
