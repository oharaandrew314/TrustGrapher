/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.eventplayer;

import cu.trustGrapher.TrustGrapher;

import cu.trustGrapher.visualizer.TrustGraphViewer;
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

    public static final int REVERSE = -1, PAUSE = 0, FORWARD = 1;
    public static final int DEFAULT_DELAY = 250, DEFUALT_EVENTS_PER_TICK = 1; // This many milliseconds between events while playing regularly
    private int state, currentEventIndex, eventsPerTick;
    private Timer schedule;
    private List<TrustLogEvent> events;
    private List<EventPlayerListener> listeners;
    private TrustGrapher trustGrapher;

//////////////////////////////////Constructor///////////////////////////////////
    public EventPlayer(TrustGrapher trustGrapher, List<TrustLogEvent> events) {
        this.trustGrapher = trustGrapher;
        this.events = events;
        listeners = new LinkedList<EventPlayerListener>();
        currentEventIndex = 0;
        eventsPerTick = DEFUALT_EVENTS_PER_TICK;
        state = PAUSE;
        schedule = new Timer(DEFAULT_DELAY, this);
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public List<TrustLogEvent> getEvents() {
        return events;
    }

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
        return (state == FORWARD);
    }

    public boolean atFront() {
        return currentEventIndex == 0;
    }

    public boolean atBack() {
        return currentEventIndex == events.size() - 1;

    }

    public boolean atAnEnd() {
        return atFront() || atBack();
    }

    public int getDelay() {
        try {
            return schedule.getDelay();
        } catch (NullPointerException ex) {
            return DEFAULT_DELAY;
        }
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void addEventPlayerListener(EventPlayerListener listener) {
        listeners.add(listener);
        listener.addEventPlayer(this);
    }

    public void setDelay(int value) {
        schedule.setDelay(value);
    }

    public void setEventsPerTick(int value) {
        eventsPerTick = value;
    }

    public void reverse() {
        if (state != REVERSE) {
            int prevState = state;
            state = REVERSE;
            wakeup(prevState);
        }
    }

    public void forward() {
        if (state != FORWARD) {
            int prevState = state;
            state = FORWARD;
            wakeup(prevState);
        }
    }

    private synchronized void wakeup(int previousState) {
        if (previousState == PAUSE) {
            schedule.start();
            notify();
        }
    }

    public synchronized void pause() {
        if (state != PAUSE) {
            state = PAUSE;
            notify();
        }
    }

    public void run() {
        schedule.start();
    }

    /**
     * Called by the shecule every time the current delay expires
     * @param arg0
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (state != PAUSE) {
            goToEvent(currentEventIndex + ((isForward()) ? eventsPerTick : -eventsPerTick));
        }
    }

    /**
     * If the newEventIndex is several events away, the EventPlayer will process those events, and then update the viewer
     * @param newEventIndex 
     */
    public void goToEvent(int newEventIndex) {
        if (currentEventIndex != newEventIndex) {
            if (newEventIndex < 0) {
                newEventIndex = 0;
            } else if (newEventIndex > events.size() - 1) {
                newEventIndex = events.size() - 1;
            }
            LinkedList<TrustLogEvent> eventsToProcess = new LinkedList<TrustLogEvent>();
            boolean isForward = currentEventIndex < newEventIndex;
            if (isForward) { //Forward
                while (currentEventIndex < newEventIndex) {
                    currentEventIndex++;
                    eventsToProcess.add(events.get(currentEventIndex));
                }
            } else { //Backward
                while (currentEventIndex > newEventIndex) {
                    eventsToProcess.add(events.get(currentEventIndex));
                    currentEventIndex--;
                }
            }
            if (!eventsToProcess.isEmpty()) {
                for (TrustLogEvent event : eventsToProcess) {
                    trustGrapher.getGraphManager().handleGraphEvent(event, isForward);
                }
                for (EventPlayerListener listener : listeners) {
                    listener.goToIndex(currentEventIndex);
                }
                for (TrustGraphViewer viewer : trustGrapher.getVisibleViewers()) {
                    viewer.repaint();
                }
            }
            if (state == PAUSE || atAnEnd()) {
                pause();
            }
        }
    }

    public void insertEvent(TrustLogEvent event) {
        ArrayList<TrustLogEvent> newEvents = new ArrayList<TrustLogEvent>(events.subList(0, currentEventIndex + 1));
        newEvents.add(event);
        newEvents.addAll(events.subList(currentEventIndex + 1, events.size()));
        goToEvent(0);
        trustGrapher.startGraph(newEvents);
    }

    public void removeEvent() {
        if (currentEventIndex != 0 && ChatterBox.yesNoDialog("Are you sure you want to remove this event?")) { //Make sure you don't remove the start event
            int indexToRemove = currentEventIndex;
            goToEvent(0); //It is necessary to go back to event 0 then remove the event so that the backwardEvent for that event is handled
            events.remove(indexToRemove);
            trustGrapher.startGraph(events);
        } else {
            ChatterBox.alert("You cannot remove the start event.");
        }
    }

    public void modifyEvent(TrustLogEvent event) {
        if (currentEventIndex != 0) {
            int indexToModify = currentEventIndex;
            goToEvent(0);
            events.set(indexToModify, event);
            trustGrapher.startGraph(events);
        } else {
            ChatterBox.alert("You cannot modify the start event.");
        }

    }
}
////////////////////////////////////////////////////////////////////////////////
