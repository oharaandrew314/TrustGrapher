////////////////////////////////LogReader//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import cu.trustGrapher.TrustGrapher;
import cu.trustGrapher.graph.SimGraph;
import cu.trustGrapher.visualizer.eventplayer.TrustLogEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import utilities.ChatterBox;
import utilities.AreWeThereYet;

/**
 * Description
 * @author Andrew O'Hara
 */
public class LogReader extends SwingWorker<ArrayList<TrustLogEvent>, String> {
    private AreWeThereYet loadingBar;
    private File logFile;
    private TrustGrapher applet;

//////////////////////////////////Constructor///////////////////////////////////
    public LogReader(TrustGrapher applet, File logFile) {
        loadingBar = new AreWeThereYet(applet);
        this.applet = applet;
        this.logFile = logFile;
        //Disable the menu bars
        for (java.awt.Component menu : applet.getJMenuBar().getComponents()) {
            menu.setEnabled(false);
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Creates a new BufferedReader and counts the number of lines in the logFile
     * @param logFile The file to count the lines of
     * @return The number of lines in the file
     */
    private int findTotalLines(File logFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            int totalLines = 0;
            while (true) {
                if (reader.readLine() != null) {
                    totalLines++;
                } else {
                    return totalLines;
                }
            }
        } catch (IOException ex) {
            ChatterBox.error(this, "findTotalLines()", "Problem reading log");
        }
        return 0;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Called by the background thread upon completion.  Passes the log events to TrustGrapher and tell it to start the graphs
     */
    @Override
    protected void done() {
        loadingBar.loadingComplete();
        try {
            if (isDone()) {
                applet.startGraph(get());
            } else {
                ChatterBox.error(this, "done()", "Attempted to load the trust log events when they were not done loading");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        for (java.awt.Component menu : applet.getJMenuBar().getComponents()) {
            menu.setEnabled(true);
        }
    }

    /**
     * Reads the logFile and parses it into a list of TrustLogEvents
     * @return The list of TrustLogEvents
     * @throws Exception The reader may throw an exception if it reads a null line
     */
    @Override
    protected ArrayList<TrustLogEvent> doInBackground() throws Exception {
        TrustLogEvent event = null;
        ArrayList<TrustLogEvent> logEvents = new ArrayList<TrustLogEvent>();
        ArrayList<SimGraph[]> graphs = applet.getGraphs();
        //Find the toal lines in the log and then ensure that the arrayList is large enough to prevent unnecessary overhead from
        int totalLines = findTotalLines(logFile);
        logEvents.ensureCapacity(totalLines);
        loadingBar.loadingStarted(totalLines, "Log Events");
        
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing feedbackGraph

        BufferedReader logReader = new BufferedReader(new FileReader(logFile));
        skipToData(logReader);

        while ((line = logReader.readLine()) != null) { //reading lines log logFile
            lineCount++;
            line = (lineCount * 100) + "," + line; //Add the timestamp to the line
            event = new TrustLogEvent(line);//create the log event
            logEvents.add(event); //add this read log event to the list
            for (SimGraph[] graph : graphs) {
                graph[SimGraph.FULL].graphConstructionEvent(event); //Add the construction event to the hidden graph
            }
            publish("progress");
        }

        event = TrustLogEvent.getEndEvent(logEvents.get(logEvents.size() - 1));
        logEvents.add(event); //add an end log to know to stop the playback of the feedbackGraph 100 ms after
        for (SimGraph[] graph : graphs) {
            graph[SimGraph.FULL].graphConstructionEvent(event); //Add the construction event to the hidden graph
        }
        return logEvents;
    }

    /**
     * Update the loadingBar for every event that was made
     * @param list 
     */
    @Override
    protected void process(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            loadingBar.loadingProgress();
        }
    }

    /**
     * Takes the BufferedReader and skips to the data lines
     * @param log The BufferedReader that contains the log file
     */
    private void skipToData(BufferedReader log) {
        String line;
        while (true) { //reading lines log file
            try {
                line = log.readLine();
                if (line.equals("@data") || line.equals("@data\n")) { //Wait until the data field has started
                    break;
                }
                publish("progress");
            } catch (IOException ex) {
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////

