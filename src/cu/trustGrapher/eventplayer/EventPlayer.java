/////////////////////////////////////TrustEventPlayer////////////////////////////////
package cu.trustGrapher.eventplayer;

import cu.trustGrapher.OptionsWindow;
import cu.trustGrapher.TrustGrapher;

import cu.trustGrapher.graph.GraphPair;
import cu.trustGrapher.visualizer.GraphViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import utilities.ChatterBox;

/**
 * Plays through the list of TrustLogEvents.  After each tick, the events that occured
 * between the ticks are sent to the event handlers of all the GraphPairs.
 * @author alan
 * @author Andrew O'Hara
 *
 */
public final class EventPlayer implements ActionListener {

    public static final int REVERSE = -1, PAUSE = 0, FORWARD = 1;
    public static final int DEFAULT_DELAY = 250, DEFUALT_EVENTS_PER_TICK = 1; // This many milliseconds between events while playing regularly
    private int state, currentEventIndex, eventsPerTick;
    private TrustGrapher trustGrapher;
    private Timer timer; //Invokes the EventPlayer actionPerormed() method every time the delay expires
    private List<TrustLogEvent> events; //The list of TrustLogEvents that are played through
    private List<EventPlayerListener> listeners; //These listeners listen to EventPlayer timeline updates and handle them in some way

//////////////////////////////////Constructor///////////////////////////////////
    public EventPlayer(TrustGrapher trustGrapher, List<TrustLogEvent> events) {
        this.trustGrapher = trustGrapher;
        this.events = events;
        listeners = new LinkedList<EventPlayerListener>();
        currentEventIndex = 0;
        eventsPerTick = DEFUALT_EVENTS_PER_TICK;
        state = PAUSE;
        timer = new Timer(DEFAULT_DELAY, this);
        //Search for a  custom delay in the properties file
        if (trustGrapher.getPropertyManager().containsKey(OptionsWindow.DELAY)) {
            try {
                setDelay(Integer.parseInt(trustGrapher.getPropertyManager().getProperty(OptionsWindow.DELAY)));
            } catch (NumberFormatException ex) {
                ChatterBox.alert("Invalid event delay property.  Will continue, and set delay to defualt.");
            }
        }
        timer.start(); //Start the time counter
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public TrustGrapher getTrustGrapher() {
        return trustGrapher;
    }

    public List<TrustLogEvent> getEvents() {
        return events;
    }

    /**
     * Returns the current playstate of the EventPlayer in an integer form.  Refer to the static ints for their names.
     * @return The playstate int representation
     */
    public int getPlayState() {
        return state;
    }

    /**
     * @return The index of the event that the EventPlayer is currently at
     */
    public int getCurrentEventIndex() {
        return currentEventIndex;
    }

    /**
     * @return true or false depending on whether or not the currentEventIndex is 0
     */
    public boolean atFront() {
        return currentEventIndex == 0;
    }

    /**
     * @return true or false depending on whether or not the currentEventIndex is the last one in the event list
     */
    public boolean atBack() {
        return currentEventIndex == events.size() - 1;

    }

    /**
     * @return true or false depending on whether or atFront() or atBack() is true
     */
    public boolean atAnEnd() {
        return atFront() || atBack();
    }

    /**
     * Searches through the list of listeners and returns the first one that is an instance of a playbackPanel.
     * If the playbackPanel can not be found, an error message is shown, as this should never happen.
     * @return The playbackPanel listening to this EventPlayer
     */
    public PlaybackPanel getPlaybackPanel() {
        for (EventPlayerListener listener : listeners) {
            if (listener instanceof PlaybackPanel) {
                return (PlaybackPanel) listener;
            }
        }
        ChatterBox.error(this, "getPlaybackPanel()", "No playbackPanel was found.  This should never occur.");
        return null;
    }

    /**
     * Searches through the list of listeners and returns the first one that is an instance of a LogPanel.
     * If the logPanel can not be found, an error message is shown, as this should never happen.
     * @return The LogPanel listening to this EventPlayer
     */
    public LogPanel getLogPanel() {
        for (EventPlayerListener listener : listeners) {
            if (listener instanceof LogPanel) {
                return (LogPanel) listener;
            }
        }
        ChatterBox.error(this, "getLogPanel()", "No logPanel was found.  This should never occur.");
        return null;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Adds an EventPlayerListener to the EventPlayer.  This will have the EventPlayer 
     * notify the listeners of timeline changes.
     * @param listener 
     */
    public void addEventPlayerListener(EventPlayerListener listener) {
        listeners.add(listener);
    }

    /**
     * Sets the delay of the timer.  The Event Player will now tick after the new number of milliseconds.
     * @param value the new millisecond value to set the timer delay to
     */
    public void setDelay(int value) {
        timer.setDelay(value);
    }

    /**
     * Sets the number of events for the EventPlayer to process after each tick.
     * The viewers only update after all the events during each tick are processed.
     * @param value The number of events to process during each tick
     */
    public void setEventsPerTick(int value) {
        eventsPerTick = value;
    }

    /**
     * Plays the EventPlayer to play forward.
     * Never modify the playState directly.  Always use this.
     */
    public void reverse() {
        if (state != REVERSE) {
            int prevState = state;
            state = REVERSE;
            wakeup(prevState);
        }
    }

    /**
     * Sets the EventPlayer to play backward.
     * Never modify the playState directly.  Always use this.
     */
    public void forward() {
        if (state != FORWARD) {
            int prevState = state;
            state = FORWARD;
            wakeup(prevState);
        }
    }

    /**
     * Restarts the timer after it has been stopped.
     * @param previousState
     */
    private synchronized void wakeup(int previousState) {
        if (previousState == PAUSE) {
            timer.start();
            notify();
        }
    }
   
    /**
     * Sets the EventPlayer to pause.
     * Never modify the playState directly.  Always use this.
     */
    public synchronized void pause() {
        if (state != PAUSE) {
            state = PAUSE;
            notify();
        }
    }

    /**
     * Called by the timer after every tick.
     * Calls goToEvent with the new eventIndex to go to depending on the 
     * playState and the eventsPerTick.
     * @param event The ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (state != PAUSE) {
            goToEvent(currentEventIndex + (state == FORWARD ? eventsPerTick : -eventsPerTick));
        }
    }

    /**
     * The EventPlayer processes the events between the currentEventIndex and the new one, then updates the GraphViewers.  
     * If the newEventIndex is out of the bounds of the event list, it is brought into the bounds.
     * If the newEventIndex is an end of the event list, playback is paused.
     * @param newEventIndex The index of the event to go to
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
                    for (GraphPair graphPair : trustGrapher.getGraphs()) {
                        graphPair.handleGraphEvent(event, isForward);
                    }
                }
                for (EventPlayerListener listener : listeners) {
                    listener.goToIndex(currentEventIndex);
                }
                for (GraphViewer viewer : trustGrapher.getVisibleViewers()) {
                    viewer.repaint();
                }
            }
            if (state == PAUSE || atAnEnd()) {
                pause();
            }
        }
    }

    /**
     * Rewinds to the start of the simulation, then calls startGraph() with the 
     * new event list that has had the new event added to restart the simulation.
     * @param event The new event to add
     */
    public void insertEvent(TrustLogEvent event) {
        ArrayList<TrustLogEvent> newEvents = new ArrayList<TrustLogEvent>(events.subList(0, currentEventIndex + 1));
        newEvents.add(event);
        newEvents.addAll(events.subList(currentEventIndex + 1, events.size()));
        goToEvent(0);
        trustGrapher.startGraph(newEvents);
    }

    /**
     * Rewinds to the start of the simulation, then calls startGraph() with the 
     * new event list that has had the event at the previous index removed to
     * restart the simulation.
     */
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

    /**
     * Rewinds to the start of the simulation, then calls startGraph() with the 
     * new event list that has had the event at the previous index modified to
     * restart the simulation.
     * @param event 
     */
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
