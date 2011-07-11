///////////////////////////////////////TrustLogEvent/////////////////////////////////
package trustGrapher.visualizer.eventplayer;

import utilities.ChatterBox;

/**
 * a simple class to encapsulate events to the graph as read from the
 * processed log file. 
 * @author adavoust
 * @author Andrew O'Hara
 */
public class TrustLogEvent implements Comparable<TrustLogEvent> {
    private long time;
    private int assessor;
    private int assessee;
    private double feedback;

//////////////////////////////////Constructor///////////////////////////////////
    /**constructor for an event as represented by a line in a (processed) log file*/

    public TrustLogEvent(String str) {
        // possible lines :
        //timemillisec:assessor:assessee:feedback
        str.trim();
        String [] words;
        if (str.contains(":")){
            words = str.split(":");
        }else{
            words = str.split(",");
        }


        time = Long.parseLong(words[0]);
        assessor = Integer.parseInt(words[1]);
        assessee = Integer.parseInt(words[2]);
        feedback = (double) Double.parseDouble(words[3]);
        if (feedback < 0.0 || feedback > 1.0) {
            ChatterBox.error("trustGrapher.visualizer.eventPlayer.LogEvent", "LogEvent()", "The feedback (" + feedback + ") was not in the specified range of [0,1].  I am setting the feedback to 0.5");
            feedback = 0.5;
        }
    }

    /**
     * Constructor for the static colouredLogEvent which creates an event from another TrustLogEvent which created it.
     * @param time		the time in milliseconds the event happened
     * @param assessor 	the peer who is giving the feedback (peer number)
     * @param assessee	the peerr who is recieving the feedback (peer number)
     */
    public TrustLogEvent(long time, int param1, int param2, double feedback) {
        this.time = time;
        this.assessor = param1;
        this.assessee = param2;
        this.feedback = feedback;
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public long getTime() {
        return time;
    }

    public double getFeedback() {
        return feedback;
    }

    public int getAssessor() {
        return assessor;
    }

    public int getAssessee() {
        return assessee;
    }

    public Object[] toArray() {
        Object[] array = {(new Long(time)), (new Integer(assessor)), (new Integer(assessee)), feedback};
        return array;
    }

    @Override
    public String toString() {
        return (time + ":" + assessor + ":" + assessee + ":" + feedback);
    }

///////////////////////////////////Methods//////////////////////////////////////
    @Override
    public int compareTo(TrustLogEvent other) {
        if (this.time < other.time) { //sort first by time
            return -1;
        } else if (this.time > other.time) {
            return 1;
        }
        return 0; //Otherwise they may as well be equal
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof TrustLogEvent){
            TrustLogEvent evt = (TrustLogEvent) obj;
            if (time == evt.time && assessor == evt.assessor && assessee == evt.assessee && feedback == evt.feedback){
                return true;
            }
        }
        return false;
    }

////////////////////////////////Static Methods//////////////////////////////////

    public static TrustLogEvent getStartEvent() {
        return new TrustLogEvent("0:0:0:0");
    }

    public static TrustLogEvent getEndEvent(TrustLogEvent lastEventInList) {
        return new TrustLogEvent((lastEventInList.getTime()+100)+":0:0:0");
    }
}
////////////////////////////////////////////////////////////////////////////////

