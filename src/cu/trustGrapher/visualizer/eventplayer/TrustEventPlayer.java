/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.visualizer.eventplayer;

import cu.trustGrapher.PlaybackPanel;
import cu.trustGrapher.TrustGrapher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import utilities.ChatterBox;

/**
 * Plays through the list of leg events and sends graphEvents to the graphs
 * @author alan
 * @author Andrew O'Hara
 *
 */
public final class TrustEventPlayer implements ActionListener {

    public static final int FASTREVERSE = -2, REVERSE = -1, PAUSE = 0, FORWARD = 1, FASTFORWARD = 2;
    public static final int DELAY = 250; // This many milliseconds between events while playing regularly
    private int state, currentEventIndex, fastMultiplier;
    private Timer schedule;
    private TimeCounter timeCounter;
    private List<TrustLogEvent> logEvents;
    private PlaybackPanel playbackPanel;
    private TrustGrapher applet;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventPlayer(TrustGrapher applet, List<TrustLogEvent> eventlist, PlaybackPanel playbackPanel) {
        this.applet = applet;
        this.playbackPanel = playbackPanel;
        logEvents = eventlist;
        currentEventIndex = 0;
        fastMultiplier = 10;
        state = PAUSE;
        timeCounter = new TimeCounter(DELAY, 0, 0, eventlist.size() * DELAY);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public int getPlayState() {
        return state;
    }

    public int getCurrentEventIndex() {
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
        return currentEventIndex * DELAY;
    }
    
///////////////////////////////////Methods//////////////////////////////////////
    
    public void setFastSpeed(int value) {
        if (value != fastMultiplier) {
            fastMultiplier = value;
            if (state == FASTFORWARD) {
                timeCounter.setIncrement(DELAY * fastMultiplier);
            } else if (state == FASTREVERSE) {
                timeCounter.setIncrement(-DELAY * fastMultiplier);
            }
        }
    }

    public void fastReverse() {
        playbackPanel.playbackFastReverse();
        if (state != FASTREVERSE) {
            int prevState = state;
            state = FASTREVERSE;
            timeCounter.setIncrement(-DELAY * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void reverse() {
        playbackPanel.playbackReverse();
        if (state != REVERSE) {
            int prevState = state;
            state = REVERSE;
            timeCounter.setIncrement(-DELAY);
            wakeup(prevState);
        }
    }

    public void fastForward() {
        playbackPanel.playbackFastForward();
        if (state != FASTFORWARD) {
            int prevState = state;
            state = FASTFORWARD;
            timeCounter.setIncrement(DELAY * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void forward() {
        playbackPanel.playbackForward();
        if (state != FORWARD) {
            int prevState = state;
            state = FORWARD;
            timeCounter.setIncrement(DELAY);
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

    public void run() {
        schedule = new Timer(DELAY, this);
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
            boolean isForward = timeCounter.getTime() > getCurrentTime();
            goToEvent( currentEventIndex + ((isForward) ? 1 : -1));
        }
    }
    
    public void goToEvent(int eventIndex) {
        LinkedList<TrustLogEvent> events = new LinkedList<TrustLogEvent>();
        boolean isForward = currentEventIndex < eventIndex;
        if (isForward) { //Forward
            while (currentEventIndex < eventIndex) {
                currentEventIndex++;
                events.add(logEvents.get(currentEventIndex));
            }
        } else { //Backward
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
        playbackPanel.getSlider().setValue(getCurrentTime());
        playbackPanel.doRepaint();
        if (state == PAUSE || atAnEnd()){
            pause();
        }
    }
    
    public void insertEvent(TrustLogEvent event){
        ArrayList<TrustLogEvent> newEvents = new ArrayList<TrustLogEvent>(logEvents.subList(0, currentEventIndex));
        newEvents.add(event);
        newEvents.addAll(logEvents.subList(currentEventIndex, logEvents.size()));
        goToEvent(0);
        applet.startGraph(newEvents);
    }
    
    public void removeEvent(){
        if (currentEventIndex != 0){ //Make sure you don't remove the start event
            int index = currentEventIndex;
            goToEvent(0);
            logEvents.remove(index);
            applet.startGraph(logEvents);
        }else{
            ChatterBox.alert("You cannot remove the start event.");
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
