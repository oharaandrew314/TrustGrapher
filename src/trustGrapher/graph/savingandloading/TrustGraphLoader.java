//////////////////////////////////TrustGraphLoader//////////////////////////////
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
import java.io.File;
import java.io.FileReader;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.ChatterBox;

/**
 * Reads an .arff file and parses it into a collection of TrustLogEvents
 * Also creates the necessary graphs and adds every event to a full graph so
 * that the event player knows where to add each object
 * @author Matthew Smith (I think)
 * @author Andrew O'Hara
 */
public class TrustGraphLoader {

    public static final int DYNAMIC = 0, FULL = 1;
    private LoadingBar loadingBar;
    private LinkedList<TrustLogEvent> logEvents;
    private ArrayList<SimGraph[]> graphs;
    private TrustPropertyManager config;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustGraphLoader(TrustPropertyManager config, LoadingBar l) {
        graphs = new ArrayList<SimGraph[]>();
        this.config = config;
        this.loadingBar = l;
       
        loadingBar.loadingStarted(config.getAlgs().size(), "graphs");
        ArrayList<Integer> trustAlgs = new ArrayList<Integer>();

        //Finds the property indices that have algorithms
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0 ; i <= AlgorithmLoader.MAX_ALGS ; i++){
            if (config.containsKey("alg" + i)){
                indices.add(i);
            }
        }
        int i=0;
        //Build the graphs
        for (Object index : indices){
            String[] entry = config.getAlg((Integer) index);
            boolean display = (entry[AlgorithmLoader.DISPLAY].equals(AlgorithmLoader.TRUE)) ? true : false;
            if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.FB)){
                addFeedbackGraph(display);
                i++;
            }else if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.REP)){
                addReputationGraph(entry, (Integer) index, display);
                i++;
            }else if (entry[AlgorithmLoader.TYPE].equals(AlgorithmLoader.TRUST)){
                trustAlgs.add((Integer) index);
            }else{
                ChatterBox.error("TrustEventLoader", "TrustEventLoader()", "Uncaught graph type.");
            }
            loadingBar.loadingProgress(i);            
        }
        for (Integer index : trustAlgs){ //Trust graphs are made last because their base graph might not be made yet
            String[] entry = config.getAlg(index);
            boolean display = false;
            if (entry[AlgorithmLoader.DISPLAY].equals(AlgorithmLoader.TRUE)){
                display = true;
            }
            addTrustGraph(entry, (Integer) index, display);
            i++;
            loadingBar.loadingProgress(i);
        }
        loadingBar.loadingComplete();
    }

//////////////////////////////////Accessors/////////////////////////////////////

    public ArrayList<SimGraph[]> getGraphs() {
        return graphs;
    }

///////////////////////////////////Methods//////////////////////////////////////

    private void addFeedbackGraph(boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        graphSet[DYNAMIC] = new SimFeedbackGraph(DYNAMIC, display);
        graphSet[FULL] = new SimFeedbackGraph(FULL, display);
        graphs.add(graphSet.clone());
    }

    private void addReputationGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];
        
        ReputationAlgorithm dynEigenAlg = (ReputationAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        ReputationAlgorithm fulEigenAlg = (ReputationAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);

        ((FeedbackHistoryGraph)getBase(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(dynEigenAlg); //The algorithm will then add the graphs
        ((FeedbackHistoryGraph)getBase(FULL, entry[AlgorithmLoader.BASE])).addObserver(fulEigenAlg);

        graphSet[FULL] = new SimReputationGraph((SimFeedbackGraph)graphs.get(0)[FULL], index, display); //This automatically turns the full feedbackGraph into the full reputationGraph
        graphSet[DYNAMIC] = new SimReputationGraph((SimFeedbackGraph)graphs.get(0)[DYNAMIC], dynEigenAlg, index, display);
        graphs.add(graphSet.clone());
    }

    private void addTrustGraph(String[] entry, int index, boolean display){
        SimGraph[] graphSet = new SimGraph[2];

        TrustAlgorithm dynRankAlg = (TrustAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);
        TrustAlgorithm fulRankAlg = (TrustAlgorithm) TrustClassLoader.newAlgorithm(entry[AlgorithmLoader.PATH]);

        ((ReputationGraph) getBase(DYNAMIC, entry[AlgorithmLoader.BASE])).addObserver(dynRankAlg); //The algorithm will then add the graphs
        ((ReputationGraph) getBase(FULL, entry[AlgorithmLoader.BASE])).addObserver(fulRankAlg);

        graphSet[FULL] = new SimTrustGraph(index, display);
        graphSet[DYNAMIC] = new SimTrustGraph(dynRankAlg, index, display);
        graphs.add(graphSet.clone());
    }

    private SimpleDirectedGraph getBase(int type, String baseID){
        for (SimGraph[] graph : graphs){
            if (graph != null){
                if (graph[type].getID() == Integer.parseInt(baseID.replace("alg", ""))){
                    return graph[type].getInnerGraph();
                }
            }
        }
        ChatterBox.debug(this, "getBase()", "Could not find a graph with id " + baseID);
        return null;
    }
    
    public LinkedList<TrustLogEvent> createList(File logFile) {
        logEvents = new LinkedList<TrustLogEvent>();
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing feedbackGraph

        try {
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));
            int totalLines = findTotalLines(logFile) - skipToData(logReader);
            loadingBar.loadingStarted(totalLines, "LogEvents");//notify the listeners that the log events have begun loading

            while ((line = logReader.readLine()) != null) { //reading lines log logFile
                lineCount++;
                loadingBar.loadingProgress();//Notify loading bar that another line has been read
                line = (lineCount * 100) + "," + line; //Add the timestamp to the line
                TrustLogEvent event = new TrustLogEvent(line);//create the log event
                logEvents.add(event); //add this read log event to the list

                for (SimGraph[] graph: graphs){
                    graph[FULL].graphConstructionEvent(event); //Add the construction event to the hidden graph
                }
            }

        } catch (IOException ex) {
            ChatterBox.error(this, "TrustEventLoader()", "Read a null line when loading events");
        }
        logEvents.add(TrustLogEvent.getEndEvent(logEvents.get(logEvents.size() - 1))); //add an end log to know to stop the playback of the feedbackGraph 100 ms after
        loadingBar.loadingComplete();
        return (LinkedList<TrustLogEvent>) logEvents;
    }

    private int findTotalLines(File logFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            int totalLines = 0;
            while (true) {
                if (reader.readLine() != null) {
                    totalLines++;
                }
                else return totalLines;
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
                if (line.equals("@data") || line.equals("@data\n")) { //Wait until the data filed has started
                    return dataLines;
                }
                dataLines++;
            } catch (IOException ex) {
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
