//////////////////////////////////TrustEventLoader//////////////////////////////
package trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
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
    private List<LoadingListener> loadingListeners;
    private LinkedList<TrustLogEvent> logEvents;
    private ArrayList<TrustGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventLoader() {
        loadingListeners = new ArrayList<LoadingListener>();
        graphs = new ArrayList<TrustGraph[]>();
        TrustGraph[] graphSet = new TrustGraph[2];
        try{
            //Feedback History Graphs
            graphSet[0] = new FeedbackHistoryGraph(); //visible
            graphSet[1] = new FeedbackHistoryGraph(); //hidden
            graphs.add(graphSet.clone());
            //Eigen Rep graphs
            graphSet[0] = null; //visible
            graphSet[1] = null; //hidden
            graphs.add(graphSet.clone());
            //RankBased Rep graphs
            graphSet[0] = null; //visible
            graphSet[1] = null; //hidden
            graphs.add(graphSet.clone());
            //Eigen Trust graphs
            graphSet[0] = null; //visible
            graphSet[1] = null; //hidden
            graphs.add(graphSet.clone());
            //Rank Based Trust graphs
            graphSet[0] = null; //visible
            graphSet[1] = null; //hidden
            graphs.add(graphSet.clone());
        }catch(Exception ex){
            ChatterBox.error(this, "TrustEventLoader()", ex.getMessage());
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public void addLoadingListener(LoadingListener loadingListener) {
        loadingListeners.add(loadingListener);
    }

    public ArrayList<TrustGraph[]> getGraphs(){
        return graphs;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public LinkedList<TrustLogEvent> createList(File logFile){
        logEvents = new LinkedList<TrustLogEvent>();
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
                for (int i=0 ; i<4 ; i++){
                    if (graphs.get(i)[1] != null){
                        graphs.get(i)[1].graphConstructionEvent(gev);
                    }
                }
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