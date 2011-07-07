/////////////////////////////////////EventPlayer////////////////////////////////
package trustGrapher.visualizer.eventplayer;

import trustGrapher.graph.TrustGraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JSlider;
import javax.swing.Timer;
import utilities.ChatterBox;

/**
 * an internal class extending thread, that can play the sequence of events from the log file in real time
 * or fast forward.
 * @author alan
 * @author Andrew O'Hara
 *
 */
public class EventPlayer implements ActionListener {

    private Timer schedule;
    private TimeCounter timeCounter;
    private static final int speed = 33; // 33 millisec between events while playing regularly
    private int fastMultiplier = 10;
    private PlayState state;
    private LinkedList<TrustLogEvent> myEventList;
    private List<EventPlayerListener> my_listeners;
    private int current_index;
    private TrustGraph hiddenGraph;
    private TrustGraph visibleGraph;
    private long myTimeNow;
    JSlider playbackSlider;
    private boolean playable; //for when a graph is loaded without any events

//////////////////////////////////Constructor///////////////////////////////////
    public EventPlayer(TrustGraph hiddenGraph, TrustGraph visibleGraph, LinkedList<TrustLogEvent> eventlist, JSlider playbackSlider) {
        this.hiddenGraph = hiddenGraph;
        this.visibleGraph = visibleGraph;
        this.playbackSlider = playbackSlider;
        myEventList = eventlist;
        current_index = 0;
        state = PlayState.FORWARD;
        //timeCounter = new TimeCounter(speed,eventlist.getFirst().getTime(),eventlist.getFirst().getTime(),eventlist.getLast().getTime());
        timeCounter = new TimeCounter(speed, 0, 0, eventlist.getLast().getTime());
        my_listeners = new LinkedList<EventPlayerListener>();
        myTimeNow = timeCounter.getLowerBound();
        playable = true;
    }

    public EventPlayer(TrustGraph hiddenGraph, TrustGraph visibleGraph) {
        this.hiddenGraph = hiddenGraph;
        this.visibleGraph = visibleGraph;
        this.playbackSlider = null;
        myEventList = new LinkedList<TrustLogEvent>();
        current_index = 0;
        state = PlayState.PAUSE;
        timeCounter = new TimeCounter(0, 0, 0, 0);
        my_listeners = new LinkedList<EventPlayerListener>();
        playable = false;
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public PlayState getPlayState() {
        return state;
    }

    public int getCurrentIndex() {
        return current_index;
    }

    /**
     * Returns whether or not the graph is playing forward or backwards.
     * @return <code>true</code> if the Play State is forward or fast forward.
     */
    public boolean isForward() {

        return ((state == PlayState.FASTFORWARD) || (state == PlayState.FORWARD));
    }

    /**
     * Returns whether or not the graph is playing fast
     * @return <code>true</code> if the Play State is fast in forward or reverse.
     */
    public boolean isFast() {
        return ((state == PlayState.FASTFORWARD) || (state == PlayState.FASTREVERSE));
    }

    public boolean atFront() {
        return timeCounter.getTime() == timeCounter.getLowerBound();
    }

    public boolean atBack() {
        return timeCounter.getTime() == timeCounter.getUpperBound();

    }

    public boolean atAnEnd() {
        return atFront() || atBack();
    }

///////////////////////////////////Methods//////////////////////////////////////
    public void addEventPlayerListener(EventPlayerListener epl) {
        my_listeners.add(epl);
    }

    public void setFastSpeed(int value) {
        if (value != fastMultiplier) {
            fastMultiplier = value;
            if (state == PlayState.FASTFORWARD) {
                timeCounter.setIncrement(speed * fastMultiplier);
            } else if (state == PlayState.FASTREVERSE) {
                timeCounter.setIncrement(-speed * fastMultiplier);
            }
        }
    }

    public void fastReverse() {
        for (EventPlayerListener epl : my_listeners) {
            epl.playbackFastReverse();
        }
        if (state != PlayState.FASTREVERSE) {
            PlayState prevState = state;
            state = PlayState.FASTREVERSE;
            timeCounter.setIncrement(-speed * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void reverse() {
        for (EventPlayerListener epl : my_listeners) {
            epl.playbackReverse();
        }
        if (state != PlayState.REVERSE) {
            PlayState prevState = state;
            state = PlayState.REVERSE;
            timeCounter.setIncrement(-speed);
            wakeup(prevState);
        }
    }

    public void fastForward() {
        for (EventPlayerListener epl : my_listeners) {
            epl.playbackFastForward();
        }
        if (state != PlayState.FASTFORWARD) {
            PlayState prevState = state;
            state = PlayState.FASTFORWARD;
            timeCounter.setIncrement(speed * fastMultiplier);
            wakeup(prevState);
        }
    }

    public void forward() {
        for (EventPlayerListener epl : my_listeners) {
            epl.playbackForward();
        }
        if (state != PlayState.FORWARD) {
            PlayState prevState = state;
            state = PlayState.FORWARD;
            timeCounter.setIncrement(speed);
            wakeup(prevState);
        }
    }

    private synchronized void wakeup(PlayState previousState) {
        if (previousState == PlayState.PAUSE) {
            if (atAnEnd()) {
                timeCounter.doIncrement();
            }
            schedule.start();
            notify();
        }
    }

    public synchronized void pause() {
        for (EventPlayerListener epl : my_listeners) {
            epl.playbackPause();
        }
        if (state != PlayState.PAUSE) {
            state = PlayState.PAUSE;
            notify();
            //schedule.stop();
                    /*for(EventPlayerListener epl : my_listeners) {
            epl.doRepaint();
            }*/
        }
    }

    public void goToTime(int value) {
        PlayState prevState = state;

        if (value < timeCounter.getTime()) {
            state = PlayState.REVERSE;
        } else {
            state = PlayState.FORWARD;
        }

        /*for( TrustLogEvent evt : getLogEventsUntil(value) ) {
        handleLogEvent(evt);
        }*/
        /*for(EventPlayerListener epl : my_listeners) {
        epl.doRepaint();
        }*/

        timeCounter.setTime(value);
        state = prevState;
    }

    public void stopPlayback() {
        wakeup(state);
        schedule.stop();
    }

    public void run() {
        //ChatterBox.debug(this, "run()", "Starting log event sequence.");
        schedule = new Timer(speed, this);
        schedule.start();

    }

    //[start] Graph Event Getting & Handling
    /**
     * current_index is always the next event with time greater than the simulation time.
     *
     * if current index is 3, simulation time (represented by '|') will be less than the index.
     * [0]-[1]-[2]-[3]-[4]-[5]-[6]
     *            |
     *
     * @param timeGoingTo The simulation time (in milliseconds) to play events up to.
     * @return	The list of log events which need to be taken care of for this time span.
     */
    private List<TrustLogEvent> getLogEventsUntil(long timeGoingTo) {
        List<TrustLogEvent> events = new LinkedList<TrustLogEvent>();
        TrustLogEvent evt;
        if (myTimeNow < timeGoingTo) {
            evt = myEventList.get(current_index);
            while (evt.getTime() < timeGoingTo) {
                current_index++;
                if (current_index >= myEventList.size()) {
                    current_index = myEventList.size() - 1;
                    break;
                }
                events.add(evt);
                evt = myEventList.get(current_index);

            }
        } else {
            evt = myEventList.get(current_index - 1);
            while (evt.getTime() > timeGoingTo) {

                current_index--;
                if (current_index < 1) {
                    break;
                }
                events.add(evt);
                evt = myEventList.get(current_index - 1);
            }
        }
        return events;
    }

    /**
     * Handles the passed TrustLogEvent be it structural or visual.
     * @param evt The Log event to handle.
     */
    private void handleLogEvent(TrustLogEvent evt, boolean forward) {
        //All events in this case modify the structure of the graph, so they are sent to the visible graph
        if (!evt.equals(TrustLogEvent.getStartEvent()) && !evt.equals(TrustLogEvent.getEndEvent(evt))){
            //ChatterBox.debug(this, "handleLogEvent()", "TrustLogEvent: " + evt.toString());
            visibleGraph.graphEvent(evt, forward, hiddenGraph);
        }
    }
    //[end] Graph Event Handling

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (playable) {
            if (state != PlayState.PAUSE) {
                timeCounter.doIncrement();
            }
            long nextTime = timeCounter.getTime();

            boolean isforward = nextTime > myTimeNow;


            if (atAnEnd()) {
                pause();
            }

            List<TrustLogEvent> events = getLogEventsUntil(nextTime);

            for (TrustLogEvent evt : events) {
                handleLogEvent(evt, isforward);
            }
            myTimeNow = nextTime; //advance time
            playbackSlider.setValue((int) myTimeNow);
            if (!events.isEmpty()) {
                for (EventPlayerListener epl : my_listeners) {
                    epl.doRepaint();
                }// if anything happened, update visual
            }
        } else {
            for (EventPlayerListener epl : my_listeners) {
                epl.doRepaint();
            }// since it isn't playable, re-draw the graph every scheduled time.
        }
    }

    public List<TrustLogEvent> getSaveEvents() {
        ListIterator<TrustLogEvent> i = myEventList.listIterator(current_index);
        List<TrustLogEvent> events = new LinkedList<TrustLogEvent>();
        while (i.hasNext()) {
            events.add(i.next());
        }
        return events;
    }

    public synchronized void addEvents(LinkedList<TrustLogEvent> events) {
        //current_index--;
        myEventList.removeLast();
        myEventList.addAll(events);
        playbackSlider.setMaximum((int) myEventList.getLast().getTime());
    }

    public long getCurrentTime() {
        return myTimeNow;
    }
}
////////////////////////////////////////////////////////////////////////////////
