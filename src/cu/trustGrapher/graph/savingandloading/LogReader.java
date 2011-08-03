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

    public static final int DYNAMIC = TrustGrapher.DYNAMIC, FULL = TrustGrapher.FULL;
    private AreWeThereYet loadingBar;
    private File logFile;
    private TrustGrapher applet;
    private ArrayList<TrustLogEvent> logEvents;
    private ArrayList<SimGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public LogReader(TrustGrapher applet, AreWeThereYet loadingBar, ArrayList<SimGraph[]> graphs, File logFile) {
        this.loadingBar = loadingBar;
        this.applet = applet;
        this.logFile = logFile;
        this.graphs = graphs;
        int totalLines = findTotalLines(logFile);
        logEvents = new ArrayList<TrustLogEvent>();
        logEvents.ensureCapacity(totalLines);
        loadingBar.loadingStarted(totalLines, "Log Events");
    }

//////////////////////////////////Accessors/////////////////////////////////////
///////////////////////////////////Methods//////////////////////////////////////
    @Override
    protected ArrayList<TrustLogEvent> doInBackground() throws Exception {        
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing feedbackGraph

        try {
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));
            skipToData(logReader);

            while ((line = logReader.readLine()) != null) { //reading lines log logFile
                lineCount++;
                line = (lineCount * 100) + "," + line; //Add the timestamp to the line
                TrustLogEvent event = new TrustLogEvent(line);//create the log event
                logEvents.add(event); //add this read log event to the list
                for (SimGraph[] graph : graphs) {
                    graph[FULL].graphConstructionEvent(event); //Add the construction event to the hidden graph
                }
                publish("progress");
            }

        } catch (IOException ex) {
            ChatterBox.debug(this, "TrustEventLoader()", "Read a null line when loading events");
        }
        logEvents.add(TrustLogEvent.getEndEvent(logEvents.get(logEvents.size() - 1))); //add an end log to know to stop the playback of the feedbackGraph 100 ms after
        return logEvents;
    }

    @Override
    protected void done() {
        loadingBar.loadingComplete();
        try {
            applet.startGraph(get());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void process(List<String> list) {
        for (int i = 0 ; i < list.size() ; i++) {
            loadingBar.loadingProgress();
        }
    }

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

    private int skipToData(BufferedReader log) {
        String line;
        int dataLines = 0;
        while (true) { //reading lines log file
            try {
                line = log.readLine();
                if (line.equals("@data") || line.equals("@data\n")) { //Wait until the data field has started
                    return dataLines;
                }
                dataLines++;
                loadingBar.loadingProgress();
            } catch (IOException ex) {
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
