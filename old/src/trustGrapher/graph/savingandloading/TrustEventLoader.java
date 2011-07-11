//////////////////////////////////TrustEventLoader//////////////////////////////
package trustGrapher.graph.savingandloading;

import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import utilities.ChatterBox;

/**
 * Reads a .txt or .arff and parses it into a collection TrustLogEvents
 * It also adds every event to a hidden graph so that the event player knows where to add each object
 * @author Matthew Smith (I think)
 * @author Andrew O'Hara
 */
public class TrustEventLoader {
    private FeedbackHistoryGraph hiddenGraph;
    private List<LoadingListener> loadingListeners;
    private LinkedList<TrustLogEvent> logEvents;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventLoader(FeedbackHistoryGraph hiddenGraph) {
        loadingListeners = new ArrayList<LoadingListener>();
        this.hiddenGraph = hiddenGraph;
    }

    public TrustEventLoader() {
        this(new FeedbackHistoryGraph());
        ChatterBox.debug(this, "LogEventListBuilder", "A new graph was instanciated.  I have set it to feedback history by default.");
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public void addLoadingListener(LoadingListener loadingListener) {
        loadingListeners.add(loadingListener);
    }

    public FeedbackHistoryGraph getHiddenGraph() {
        return hiddenGraph;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public LinkedList<TrustLogEvent> createList(File logFile){
        logEvents = new LinkedList<TrustLogEvent>();
        FeedbackHistoryGraph tempGraph = new FeedbackHistoryGraph();
        ChatterBox.debug(this, "createList()", "A new graph was instanciated.  I have set it to feedback history by default.");
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        int totalLines;

        try{
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));

            if (logFile.getAbsolutePath().endsWith(".arff")){
                totalLines = findTotalLines(logFile);
                skipToData(logReader);
            }else{
                totalLines = Integer.parseInt(logReader.readLine()); //the total number of lines so the loading bar can size itself properly
            }

            for (LoadingListener l : loadingListeners) { //notify the listeners that the log events have begun loading
                    l.loadingChanged(totalLines, "LogEvents");
                }

            logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing graph

            while ((line = logReader.readLine()) != null) { //reading lines log logFile
                lineCount++;
                for (LoadingListener l : loadingListeners) { //Notify loading bar that another line has been read
                    l.loadingProgress(lineCount);
                }
                if (logFile.getAbsolutePath().endsWith(".arff")){
                    line = (lineCount * 100) + "," + line;
                }
                TrustLogEvent gev = new TrustLogEvent(line);//create the log event
                logEvents.add(gev); //add this read log event to the list

                // Update the temporary graph afterwards so it can be used as a reference
                hiddenGraph.graphConstructionEvent(gev);
                //tempGraph.graphEvent(gev, true, hiddenGraph);
            }
            logEvents.add(TrustLogEvent.getEndEvent(logEvents.get(logEvents.size() - 1))); //add an end log to know to stop the playback of the graph 100 ms after

        }catch (IOException ex){
            ChatterBox.error(this, "TrustEventLoader", "Read a null line when loading events");
        }
            return (LinkedList<TrustLogEvent>) logEvents;
    }

    private int findTotalLines(File logFile){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            int totalLines = 0;
            while (true){
                if (reader.readLine() != null){
                    totalLines++;
                }else{
                    return totalLines;
                }
            }
        }catch (IOException ex){
            ChatterBox.error(this, "findTotalLines", "Problem reading log");
        }
        return 0;
    }

    private void skipToData(BufferedReader log){
        boolean dataReached = false;
        String line;
        while (true) { //reading lines log file
            try{
                line = log.readLine();
                if (line.equals("@data")  || line.equals("@data\n")){ //Wait until the data filed has started
                    dataReached = true;
                    return;
                }
            }catch (IOException ex){
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////