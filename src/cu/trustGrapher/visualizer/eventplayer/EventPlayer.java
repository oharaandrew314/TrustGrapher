/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.visualizer.eventplayer;

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
public final class EventPlayer implements ActionListener {

    public static final int FASTREVERSE = -2, REVERSE = -1, PAUSE = 0, FORWARD = 1, FASTFORWARD = 2;
    public static final int DEFAULT_DELAY = 250; // This many milliseconds between events while playing regularly
    private int state, currentEventIndex;
    private Timer schedule;
    private List<TrustLogEvent> logEvents;
    private PlaybackPanel playbackPanel;
    private TrustGrapher applet;

//////////////////////////////////Constructor///////////////////////////////////
    public EventPlayer(TrustGrapher applet, List<TrustLogEvent> eventlist, PlaybackPanel playbackPanel) {
        this.applet = applet;
        this.playbackPanel = playbackPanel;
        logEvents = eventlist;
        currentEventIndex = 0;
        state = PAUSE;
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

    public boolean atFront() {
        return currentEventIndex == 0;
    }

    public boolean atBack() {
        return currentEventIndex == logEvents.size() - 1;

    }

    public boolean atAnEnd() {
        return atFront() || atBack();
    }
    
    public int getDelay(){
        try{
            return schedule.getDelay();
        }catch(NullPointerException ex){
            return DEFAULT_DELAY;
        }
    }
    
///////////////////////////////////Methods//////////////////////////////////////
    
    public void setFastSpeed(int value) {
        schedule.setDelay(value);
    }

    public void fastReverse() {
        playbackPanel.playbackFastReverse();
        if (state != FASTREVERSE) {
            int prevState = state;
            state = FASTREVERSE;
            wakeup(prevState);
        }
    }

    public void reverse() {
        playbackPanel.playbackReverse();
        if (state != REVERSE) {
            int prevState = state;
            state = REVERSE;
            wakeup(prevState);
        }
    }

    public void fastForward() {
        playbackPanel.playbackFastForward();
        if (state != FASTFORWARD) {
            int prevState = state;
            state = FASTFORWARD;
            wakeup(prevState);
        }
    }

    public void forward() {
        playbackPanel.playbackForward();
        if (state != FORWARD) {
            int prevState = state;
            state = FORWARD;
            wakeup(prevState);
        }
    }

    private synchronized void wakeup(int previousState) {
        if (previousState == PAUSE) {
            if (atAnEnd()) {
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
        schedule = new Timer(DEFAULT_DELAY, this);
        schedule.start();

    }

    /**
     * Called by the shecule every time the current delay expires
     * @param arg0
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (state != PAUSE) {
            goToEvent( currentEventIndex + ((isForward()) ? 1 : -1));
        }
    }
    
    /**
     * If the newEventIndex is several events away, the EventPlayer will process those events, and then update the viewer
     * @param newEventIndex 
     */
    public void goToEvent(int newEventIndex) {
        if (currentEventIndex != newEventIndex){
            LinkedList<TrustLogEvent> events = new LinkedList<TrustLogEvent>();
            boolean isForward = currentEventIndex < newEventIndex;
            if (isForward) { //Forward
                while (currentEventIndex < newEventIndex) {
                    currentEventIndex++;
                    events.add(logEvents.get(currentEventIndex));
                }
            } else{ //Backward
                while (currentEventIndex > newEventIndex) {
                    events.add(logEvents.get(currentEventIndex));
                    currentEventIndex--;
                }
            }
            if (!events.isEmpty()){
                for (TrustLogEvent event : events){
                    applet.getGraphManager().handleGraphEvent(event, isForward);
                }
            }
            playbackPanel.getSlider().setValue(currentEventIndex);
            playbackPanel.doRepaint();
            if (state == PAUSE || atAnEnd()){
                pause();
            }
        }
    }
    
    public void insertEvent(TrustLogEvent event){
        ArrayList<TrustLogEvent> newEvents = new ArrayList<TrustLogEvent>(logEvents.subList(0, currentEventIndex + 1));
        newEvents.add(event);
        newEvents.addAll(logEvents.subList(currentEventIndex + 1, logEvents.size()));
        goToEvent(0);
        applet.startGraph(newEvents);
    }
    
    public void removeEvent(){
        if (currentEventIndex != 0){ //Make sure you don't remove the start event
            int indexToRemove = currentEventIndex;
            goToEvent(0); //It is necessary to go back to event 0 then remove the event so that the backwardEvent for that event is handled
            logEvents.remove(indexToRemove);
            applet.startGraph(logEvents);
        }else{
            ChatterBox.alert("You cannot remove the start event.");
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
