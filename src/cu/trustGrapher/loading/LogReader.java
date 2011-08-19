////////////////////////////////LogReader//////////////////////////////////
package cu.trustGrapher.loading;

import cu.trustGrapher.TrustGrapher;
import cu.trustGrapher.eventplayer.TrustLogEvent;
import java.io.BufferedReader;

import javax.swing.SwingWorker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aohara.utilities.ChatterBox;
import aohara.utilities.AreWeThereYet;
import cu.trustGrapher.graphs.SimAbstractGraph;

/**
 * Description
 * @author Andrew O'Hara
 */
public class LogReader extends SwingWorker<ArrayList<TrustLogEvent>, String> {

    private TrustGrapher trustGrapher;
    private AreWeThereYet loadingBar;
    private File logFile;

//////////////////////////////////Constructor///////////////////////////////////
    public LogReader(TrustGrapher trustGrapher, File logFile, AreWeThereYet loadingBar) {
        this.loadingBar = loadingBar;
        this.trustGrapher = trustGrapher;
        this.logFile = logFile;
        //Disable the menu bars to stop user from messing up background thread
        trustGrapher.enableMenu(false);
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
            skipToData(reader);
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
                trustGrapher.startGraph(get());
            } else {
                ChatterBox.error(this, "done()", "Attempted to load the trust log events when they were not done loading");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        trustGrapher.enableMenu(true);
    }

    /**
     * Reads the logFile and parses it into a list of TrustLogEvents
     * @return The list of TrustLogEvents
     * @throws Exception The reader may throw an exception if an I/O error occurs
     */
    @Override
    protected ArrayList<TrustLogEvent> doInBackground() throws Exception {
        TrustLogEvent event = null;
        int totalLines = findTotalLines(logFile);
        ArrayList<TrustLogEvent> logEvents = new ArrayList<TrustLogEvent>(totalLines);
        loadingBar.loadingStarted(totalLines, "Log Events");
        BufferedReader logReader = new BufferedReader(new FileReader(logFile));
        skipToData(logReader);

        logEvents.add(null); //Adding an empty event the start.  This is to signify that no feedback has been given yet
        //reading logFile
        for (int i = 0; i < totalLines; i++) {
            event = new TrustLogEvent(logReader.readLine()); //Read the next line in the 
            logEvents.add(event); //add this log event to the list
            for (SimAbstractGraph graph : trustGrapher.getGraphs()) {
                graph.graphConstructionEvent(event); //Build any necessary entities referenced by the event
            }
            publish("progress");
        }

        //An empty event is not added to the event list, this is just to get the non-feedback graphs to do a construction event
        //since they will only construct their edges when passed a null event.  During regular events, they only add vertices if needed
        //If they were to constuct edges after every event, it would severely slow the loading of long logs
        for (SimAbstractGraph graph : trustGrapher.getGraphs()) {
            graph.graphConstructionEvent(null);
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

