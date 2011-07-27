//////////////////////////////////TrustEventLoader//////////////////////////////
package trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.BitStylus;
import utilities.ChatterBox;

/**
 * Reads an .arff file and parses it into a collection of TrustLogEvents
 * It also adds every event to a full graph so that the event player knows where to add each object
 * @author Matthew Smith (I think)
 * @author Andrew O'Hara
 */
public class TrustEventLoader {

    public static final int DYNAMIC = 0, FULL = 1;
    private List<LoadingListener> loadingListeners;
    private LinkedList<TrustLogEvent> logEvents;
    private ArrayList<MyGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventLoader(ArrayList<String[]> algs) {
        graphs = new ArrayList<MyGraph[]>();
        loadingListeners = new ArrayList<LoadingListener>();

        //Build the graphs
        ArrayList<String[]> trustAlgs = new ArrayList<String[]>();
        int i;
        for (i=0 ; i<algs.size() ; i++){
            String[] entry = algs.get(i);
            boolean display = false;
            if (entry[Configure.DISPLAY].equals(Configure.TRUE)){
                display = true;
            }
            if (entry[Configure.TYPE].equals(Configure.FB)){
                addFeedbackGraph(display);
            }else if (entry[Configure.TYPE].equals(Configure.REP)){
                addReputationGraph(entry, display);
            }else if (entry[Configure.TYPE].equals(Configure.TRUST)){
                trustAlgs.add(entry);
            }else{
                ChatterBox.error("TrustEventLoader", "TrustEventLoader()", "Uncaught graph type.");
            }
        }
        for (String[] entry : trustAlgs){ //Trust graphs are made last because their base graph might not be made yet
            boolean display = false;
            if (entry[Configure.DISPLAY].equals(Configure.TRUE)){
                display = true;
            }
            addTrustGraph(entry, display);
            i++;
        }
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public ArrayList<MyGraph[]> getGraphs() {
        return graphs;
    }

///////////////////////////////////Methods//////////////////////////////////////

    public void addLoadingListener(LoadingListener loadingListener) {
        loadingListeners.add(loadingListener);
    }

    private void addFeedbackGraph(boolean display){
        MyGraph[] graphSet = new MyGraph[2];
        graphSet[DYNAMIC] = new MyFeedbackGraph(DYNAMIC, display);
        graphSet[FULL] = new MyFeedbackGraph(FULL, display);
        graphs.add(graphSet.clone());
    }

    private void addReputationGraph(String[] entry, boolean display){
        MyGraph[] graphSet = new MyGraph[2];
        int id = Integer.parseInt(entry[Configure.ID].replace("alg", ""));
        
        ReputationAlgorithm dynEigenAlg = (ReputationAlgorithm) newAlgorithm(entry[Configure.PATH]);
        ReputationAlgorithm fulEigenAlg = (ReputationAlgorithm) newAlgorithm(entry[Configure.PATH]);

        ((FeedbackHistoryGraph)getBase(DYNAMIC, entry[Configure.BASE])).addObserver(dynEigenAlg); //The algorithm will then add the graphs
        ((FeedbackHistoryGraph)getBase(FULL, entry[Configure.BASE])).addObserver(fulEigenAlg);

        graphSet[FULL] = new MyReputationGraph((MyFeedbackGraph)graphs.get(0)[FULL], id, display); //This automatically turns the full feedbackGraph into the full reputationGraph
        graphSet[DYNAMIC] = new MyReputationGraph((MyFeedbackGraph)graphs.get(0)[DYNAMIC], dynEigenAlg, id, display);

        graphs.add(graphSet.clone());
    }

    private void addTrustGraph(String[] entry, boolean display){
        MyGraph[] graphSet = new MyGraph[2];
        int id = Integer.parseInt(entry[Configure.ID].replace("alg", ""));

        TrustAlgorithm dynRankAlg = (TrustAlgorithm) newAlgorithm(entry[Configure.PATH]);
        TrustAlgorithm fulRankAlg = (TrustAlgorithm) newAlgorithm(entry[Configure.PATH]);

        ((ReputationGraph) getBase(DYNAMIC, entry[Configure.BASE])).addObserver(dynRankAlg); //The algorithm will then add the graphs
        ((ReputationGraph) getBase(FULL, entry[Configure.BASE])).addObserver(fulRankAlg);

        graphSet[FULL] = new MyTrustGraph(id, display);
        graphSet[DYNAMIC] = new MyTrustGraph(dynRankAlg, id, display);
        graphs.add(graphSet.clone());
    }

    private SimpleDirectedGraph getBase(int type, String baseID){
        try{
            for (MyGraph[] graph : graphs){
                if (graph != null){
                    if (graph[type].getID() == Integer.parseInt(baseID)){
                        return graph[type].getInnerGraph();
                    }
                }
            }
        }catch(NullPointerException ex){
            ChatterBox.error(this, "getBase()", "Tried to pass a base id that is not an integer.");
            ex.printStackTrace();
        }
        ChatterBox.debug(this, "getBase()", "Could not find a graph with id " + baseID);
        return null;
    }

    private Object newAlgorithm(String path){
        if (path.contains("!")){ //If it is a jar
            File file = new File(path.split("!")[0]);
            String name = path.split("!")[1];
            return BitStylus.classInstance( (Class) BitStylus.loadJarClass(file, name)[0]);
        }else{//Otherwise, it must be a class
            return BitStylus.classInstance(BitStylus.loadClass(new File(path)));
        }      
    }
    
    public LinkedList<TrustLogEvent> createList(File logFile) {
        logEvents = new LinkedList<TrustLogEvent>();
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        int totalLines;

        try {
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));

            totalLines = findTotalLines(logFile);
            skipToData(logReader);

            for (LoadingListener l : loadingListeners) { //notify the listeners that the log events have begun loading
                l.loadingChanged(totalLines, "LogEvents");
            }

            logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing feedbackGraph

            while ((line = logReader.readLine()) != null) { //reading lines log logFile
                lineCount++;
                for (LoadingListener l : loadingListeners) { //Notify loading bar that another line has been read
                    l.loadingProgress(lineCount);
                }
                line = (lineCount * 100) + "," + line; //Add the timestamp to the line
                TrustLogEvent event = new TrustLogEvent(line);//create the log event
                logEvents.add(event); //add this read log event to the list

                for (MyGraph[] graph: graphs){
                    graph[FULL].graphConstructionEvent(event); //Add the construction event to the hidden graph
                }
            }
            logEvents.add(TrustLogEvent.getEndEvent(logEvents.get(logEvents.size() - 1))); //add an end log to know to stop the playback of the feedbackGraph 100 ms after

        } catch (IOException ex) {
            ChatterBox.error(this, "TrustEventLoader()", "Read a null line when loading events");
        }

        ((MyReputationGraph)graphs.get(1)[FULL]).removeRep();

        for (LoadingListener l : loadingListeners) {
            l.loadingComplete();
        }

        return (LinkedList<TrustLogEvent>) logEvents;
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

    private void skipToData(BufferedReader log) {
        String line;
        while (true) { //reading lines log file
            try {
                line = log.readLine();
                if (line.equals("@data") || line.equals("@data\n")) { //Wait until the data filed has started
                    return;
                }
            } catch (IOException ex) {
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
