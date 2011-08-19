///////////////////////////////////////TrustLogEvent/////////////////////////////////
package cu.trustGrapher.eventplayer;

import aohara.utilities.ChatterBox;

/**
 * a simple class to encapsulate events to the graph as read from the processed log file.
 * @author adavoust
 * @author Andrew O'Hara
 */
public class TrustLogEvent {
    private int assessor;
    private int assessee;
    private double feedback;
    private String string;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * constructor for an event as represented by a line in a (processed) log file
     */
    public TrustLogEvent(String str) {
        //Line format: assessor,assessee,feedback
        try{
            string = str;
            str.trim();
            String [] words = str.split(",");          
            assessor = Integer.parseInt(words[0]);
            assessee = Integer.parseInt(words[1]);
            feedback = (double) Double.parseDouble(words[2]);
            if (feedback < 0.0 || feedback > 1.0) {
                ChatterBox.error("trustGrapher.visualizer.eventPlayer.LogEvent", "LogEvent()", "The feedback (" + feedback + ") was not in the specified range of [0,1].  I am setting the feedback to 0.5");
                feedback = 0.5;
            }
        }catch (Exception ex){
            ChatterBox.error("TrustLogEvent", "TrustLogEvent()", "The log format is incorrect.");
            ex.printStackTrace();
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////

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
        Object[] array = {(new Integer(assessor)), (new Integer(assessee)), feedback};
        return array;
    }

    @Override
    public String toString() {
        return string;
    }
}
////////////////////////////////////////////////////////////////////////////////

