//////////////////////////////////TrustEventLoader//////////////////////////////
package trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.EigenTrust;
import cu.repsystestbed.algorithms.RankbasedTrustAlg;
import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import trustGrapher.graph.edges.MyFeedbackEdge;
import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.FeedbackHistoryGraph;
import cu.repsystestbed.graphs.ReputationGraph;
import cu.repsystestbed.graphs.TestbedEdge;
import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import org.jgrapht.graph.SimpleDirectedGraph;
import utilities.ChatterBox;

/**
 * Reads a .txt or .arff and parses it into a collection of TrustLogEvents
 * It also adds every event to a hidden feedbackGraph so that the event player knows where to add each object
 * @author Matthew Smith (I think)
 * @author Andrew O'Hara
 */
public class TrustEventLoader {

    public static final int DYNAMIC = 0, FULL = 1;
    private List<LoadingListener> loadingListeners;
    private LinkedList<TrustLogEvent> logEvents;
    private ArrayList<MyGraph[]> graphs;

//////////////////////////////////Constructor///////////////////////////////////
    public TrustEventLoader() {
        loadingListeners = new ArrayList<LoadingListener>();
        graphs = new ArrayList<MyGraph[]>();
        MyGraph[] graphSet = new MyGraph[2];
        
        //Feedback History Graphs
        graphSet[DYNAMIC] = new MyFeedbackGraph(DYNAMIC);
        graphSet[FULL] = new MyFeedbackGraph(FULL);
        graphs.add(graphSet.clone());

        //Eigen Reputation graphs
        ReputationAlgorithm dynEigenAlg = new EigenTrust();
        ReputationAlgorithm fulEigenAlg = new EigenTrust();

        SimpleDirectedGraph dynFeedbackGraph = graphs.get(0)[DYNAMIC].getInnerGraph();
        SimpleDirectedGraph fulFeedbackGraph = graphs.get(0)[FULL].getInnerGraph();

        ((FeedbackHistoryGraph) dynFeedbackGraph).addObserver(dynEigenAlg); //The algorithm will then add the graphs
        ((FeedbackHistoryGraph) fulFeedbackGraph).addObserver(fulEigenAlg);

        graphSet[FULL] = new MyReputationGraph((MyFeedbackGraph)graphs.get(0)[FULL]); //This automatically turns the full feedbackGraph into the full reputationGraph
        graphSet[DYNAMIC] = new MyReputationGraph((MyFeedbackGraph)graphs.get(0)[DYNAMIC], dynEigenAlg);

        graphs.add(graphSet.clone());

        //RankBased Trust graphs
        TrustAlgorithm dynRankAlg = new RankbasedTrustAlg();

        TrustAlgorithm fulRankAlg = new RankbasedTrustAlg();
        try{
            ((RankbasedTrustAlg)dynRankAlg).setRatio(0.7);
            ((RankbasedTrustAlg)dynRankAlg).setRatio(0.7);
        }catch(Exception ex){
            ChatterBox.error(this, "TrustEventLoader()", ex.getMessage());
        }

        SimpleDirectedGraph dynRepGraph = graphs.get(1)[DYNAMIC].getInnerGraph();
        SimpleDirectedGraph fulRepGraph = graphs.get(1)[FULL].getInnerGraph();

        ((ReputationGraph) dynRepGraph).addObserver(dynRankAlg); //The algorithm will then add the graphs
        ((ReputationGraph) fulRepGraph).addObserver(fulRankAlg);

        graphSet[FULL] = new MyTrustGraph();
        graphSet[DYNAMIC] = new MyTrustGraph(dynRankAlg);
        graphs.add(graphSet.clone());
    }

//////////////////////////////////Accessors/////////////////////////////////////
    public void addLoadingListener(LoadingListener loadingListener) {
        loadingListeners.add(loadingListener);
    }

    public ArrayList<MyGraph[]> getGraphs() {
        return graphs;
    }

///////////////////////////////////Methods//////////////////////////////////////
    public LinkedList<TrustLogEvent> createList(File logFile) {
        logEvents = new LinkedList<TrustLogEvent>();
        String line = ""; //will contain each log event as it is read.
        int lineCount = 0;
        int totalLines;

        try {
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));

            if (logFile.getAbsolutePath().endsWith(".arff")) {
                totalLines = findTotalLines(logFile);
                skipToData(logReader);
            } else {
                totalLines = Integer.parseInt(logReader.readLine()); //the total number of lines so the loading bar can size itself properly
            }

            for (LoadingListener l : loadingListeners) { //notify the listeners that the log events have begun loading
                l.loadingChanged(totalLines, "LogEvents");
            }

            logEvents.add(TrustLogEvent.getStartEvent()); //a start event to know when to stop playback of a reversing feedbackGraph

            while ((line = logReader.readLine()) != null) { //reading lines log logFile
                lineCount++;
                for (LoadingListener l : loadingListeners) { //Notify loading bar that another line has been read
                    l.loadingProgress(lineCount);
                }
                if (logFile.getAbsolutePath().endsWith(".arff")) {
                    line = (lineCount * 100) + "," + line;
                }
                TrustLogEvent gev = new TrustLogEvent(line);//create the log event
                logEvents.add(gev); //add this read log event to the list

                for (MyGraph[] graph: graphs){
                    graph[FULL].graphConstructionEvent(gev); //Add the construction event to the hidden graph
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
        boolean dataReached = false;
        String line;
        while (true) { //reading lines log file
            try {
                line = log.readLine();
                if (line.equals("@data") || line.equals("@data\n")) { //Wait until the data filed has started
                    dataReached = true;
                    return;
                }
            } catch (IOException ex) {
                ChatterBox.error(this, "skipToData()", "Read a null line while trying to skip to data.");
            }
        }
    }

    private void debugEntities(){
        ChatterBox.print("Debugging entities...");
        String found;
        MyFeedbackGraph graph = (MyFeedbackGraph) graphs.get(0)[FULL];
        Collection<Agent> agents = graph.getVertices();
        for (Agent a : agents){
            ChatterBox.print(a.toString());
        }
        Collection<TestbedEdge> edges = graph.getEdges();
        for (TestbedEdge e : edges){
            MyFeedbackEdge e2 = (MyFeedbackEdge) e;
            if (graph.findEdge((Agent) e.src, (Agent) e.sink) == null){
                found = "not found";
            }else{
                found = "found";
            }
            ChatterBox.print("Edge " + e.src + " " + e.sink + " " + found);
        }
        ChatterBox.print("Done.");
    }

    private static void printLog(LinkedList<TrustLogEvent> logEvents){
        ChatterBox.print("Printing log...");
        for (TrustLogEvent event : logEvents){
            ChatterBox.print("time: " + event.getTime() + " assessor: " + event.getAssessor() + " assessee: " + event.getAssessee() + " feedback: " + event.getFeedback());

        }
        ChatterBox.print("Done.");
    }

}
////////////////////////////////////////////////////////////////////////////////
