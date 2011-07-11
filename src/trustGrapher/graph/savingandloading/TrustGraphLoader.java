package trustGrapher.graph.savingandloading;

import cu.repsystestbed.entities.Agent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import utilities.ChatterBox;

public class TrustGraphLoader {

    public static final File STARTING_DIRECTORY = new File("/home/zalpha314/Documents/Programming/Java/Work/TrustGrapher2/test");
    private LinkedList<TrustLogEvent> logList;
    private FeedbackHistoryGraph hiddenGraph;
    private FeedbackHistoryGraph visibleGraph;
    private List<LoadingListener> loadingListeners;

    //[start] Constructor
    public TrustGraphLoader() {
        logList = new LinkedList<TrustLogEvent>();
        ChatterBox.debug(this, "P2PNetworkGraphLoader()", "A new graph was instanciated.  I have set it to feedback history by default.");
        hiddenGraph = new FeedbackHistoryGraph();
        visibleGraph = new FeedbackHistoryGraph();
        loadingListeners = new LinkedList<LoadingListener>();
    }
    //[end] Constructor

    //[start] Loading Method
    /**
     * @return <code>true</code> if file loaded successfully
     */
    public boolean doLoad() {
        String[] acceptedExtensions = {"xml", "arff", "txt"};
        File file = chooseLoadFile(".xml , .arff and .txt only", acceptedExtensions);
        if (file != null) {
            if (file.getAbsolutePath().endsWith(".txt") || file.getAbsolutePath().endsWith(".arff")) {

                try {
                    loadingStarted(1, "Log Files");
                    TrustEventLoader logBuilder = new TrustEventLoader();
                    for (LoadingListener l : loadingListeners) {
                        logBuilder.addLoadingListener(l);
                    }

                    logList = logBuilder.createList(file);

                    hiddenGraph = logBuilder.getHiddenGraph(); //load hidden graph but keep visible graph empty
                    loadingComplete();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Failure in doLoad()", JOptionPane.ERROR_MESSAGE);
                }
            } else if (file.getAbsolutePath().endsWith(".xml")) {
                try {
                    SAXBuilder builder = new SAXBuilder();
                    final Document networkDoc = builder.build(file);
                    graphBuilder(networkDoc);
                    logList.addFirst(TrustLogEvent.getStartEvent());
                    logList.addLast(TrustLogEvent.getEndEvent(logList.getLast()));
                    return true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return false;
    }
    //[end] Loading Method

    //[start] Listener Methods
    public void addLoadingListener(LoadingListener listener) {
        loadingListeners.add(listener);
    }

    private void loadingStarted(int numberLines, String whatIsLoading) {
        for (LoadingListener l : loadingListeners) {
            l.loadingStarted(numberLines, whatIsLoading);
        }
    }

    private void loadingProgress(int progress) {
        for (LoadingListener l : loadingListeners) {
            l.loadingProgress(progress);
        }
    }

    private void loadingChanged(int numberLines, String whatIsLoading) {
        for (LoadingListener l : loadingListeners) {
            l.loadingChanged(numberLines, whatIsLoading);
        }
    }

    private void loadingComplete() {
        for (LoadingListener l : loadingListeners) {
            l.loadingComplete();
        }
    }
    //[end] Listener Methods

    //[start] Graph Builder
    private void graphBuilder(Document networkDoc) {
        ChatterBox.error(this, "graphBuilder()", "graphBuilder() method has been called.  I'm pretty sure it doesn't work.");
        if (networkDoc.getRootElement().getName().equals("network")) {
            int edgeCounter = 0;
            FeedbackHistoryGraph startGraph = new FeedbackHistoryGraph();
            ChatterBox.debug(this, "P2PNetworkGraphLoader()", "A new graph was instanciated.  I have set it to feedback history by default.");
            Element networkElem = networkDoc.getRootElement();
            int counter = 0;
            //[start] Create Graph
            Element graphElem = networkElem.getChild("graph");
            if (graphElem != null) {
                //[start] Add Vertices to graph
                Element nodeMap = graphElem.getChild("nodemap");
                loadingStarted(nodeMap.getChildren().size(), "Vertices");

                for (Object o : nodeMap.getChildren()) {
                    Element elem = (Element) o;
                    String type = elem.getAttribute("type").getValue();

                    if (type.equals("PeerVertex")) {
                        int key = Integer.parseInt(elem.getChild("key").getText());
                        hiddenGraph.addVertex(new Agent(key));
                        startGraph.addVertex(new Agent(key));
                    }
                    loadingProgress(++counter);
                }
                //[end] Add Vertices to graph

                //[start] Add Edges to graph
                Element edgeMap = graphElem.getChild("edgemap");
                loadingChanged(edgeMap.getChildren().size(), "Edges");
                counter = 0;

                for (Object o : edgeMap.getChildren()) {
                    Element elem = (Element) o;
                    String type = elem.getAttribute("type").getValue();

                    if (type.equals("PeerToPeer")) { //Peer to Peer
                        int v1Key = Integer.parseInt(elem.getChild("v1").getText());
                        int v2Key = Integer.parseInt(elem.getChild("v2").getText());
                        Agent peer1 = hiddenGraph.getVertexInGraph(new Agent(v1Key));
                        Agent peer2 = hiddenGraph.getVertexInGraph(new Agent(v2Key));
                        try{
                            startGraph.addEdge(new FeedbackEdge(edgeCounter, peer1, peer2), peer1, peer2);
                            hiddenGraph.addEdge(new FeedbackEdge(edgeCounter, peer1, peer2), peer1, peer2);
                        }catch (Exception ex){
                            ChatterBox.error(this, "graphBuilder()", "Could not create an edge");
                        }
                        edgeCounter++;
                    } else {
                        ChatterBox.debug(this, "graphBuilder()", "no diea what to do here.");
                    }
                    loadingProgress(++counter);
                }
                //[end] Add Edges to graph
            }
            //[end] Create Graph

            //[start] Create Logs
            Element logElem = networkElem.getChild("logevents");
            if (logElem != null) {
                loadingChanged(logElem.getChildren().size(), "Events");
                counter = 0;
                for (Object o : logElem.getChildren()) {

                    Element event = (Element) o;
                    String type = event.getAttribute("type").getValue();
                    if (type.equals("start") || type.equals("end")) {
                        continue;
                    }
                    long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
                    int paramOne = Integer.parseInt(event.getChildText("param1"));
                    int paramTwo = Integer.parseInt(event.getChildText("param2"));

                    ChatterBox.debug(this, "graphBuilder()", "A new LogEvent was created but I don't know how to get the feedback.  So it has a rating of +1");
                    TrustLogEvent evt = new TrustLogEvent(timeDifference, paramOne, paramTwo, 1.0);

//                    Asuuming all events are feedback events
                    Agent assessor = hiddenGraph.getVertexInGraph(new Agent(evt.getAssessee()));
                    Agent assessee = hiddenGraph.getVertexInGraph(new Agent(evt.getAssessor()));

                    //If the peers don't exist, add them
                    if (hiddenGraph.getVertexInGraph(evt.getAssessee()) == null) {
                        hiddenGraph.addPeer(evt.getAssessee());
                    }
                    if (hiddenGraph.getVertexInGraph(evt.getAssessor()) == null) {
                        hiddenGraph.addPeer(evt.getAssessor());
                    }
                    FeedbackEdge edge = null;
                    try{
                        edge = new FeedbackEdge(edgeCounter, hiddenGraph.getVertexInGraph(evt.getAssessor()), hiddenGraph.getVertexInGraph(evt.getAssessee()));
                    }catch (Exception ex){
                        ChatterBox.error(this, "graphBuilder()", ex.getMessage());
                    }
                    edgeCounter++;
                    hiddenGraph.addEdge(edge, assessor, assessee);

                    logList.add(evt);
                    loadingProgress(++counter);
                }
            }
            visibleGraph = startGraph;
            //[end] Create Logs
            loadingComplete();
        }
    }

    private void addEventsToGraph(Document networkDoc) {
        if (networkDoc.getRootElement().getName().equals("network")) {
            int edgeCounter = hiddenGraph.getEdgeCount();
            Element networkElem = networkDoc.getRootElement();
            Element logElem = networkElem.getChild("logevents");
            if (logElem != null) {
                loadingChanged(logElem.getChildren().size(), "Events");
                for (Object o : logElem.getChildren()) {

                    Element event = (Element) o;
                    String type = event.getAttribute("type").getValue();
                    if (type.equals("start") || type.equals("end")) {
                        continue;
                    }
                    long timeDifference = Integer.parseInt(event.getChildText("timedifference"));
                    int paramOne = Integer.parseInt(event.getChildText("param1"));
                    int paramTwo = Integer.parseInt(event.getChildText("param2"));
                    ChatterBox.debug(this, "addEventsToGraph()", "A new LogEvent was created but I don't know how to get the rating.  So it has a rating of +1");
                    TrustLogEvent evt = new TrustLogEvent(timeDifference, paramOne, paramTwo, 1.0);

                    //Asuuming all events are feedback events
                    Agent assessor = hiddenGraph.getVertexInGraph(new Agent(evt.getAssessee()));
                    Agent assessee = hiddenGraph.getVertexInGraph(new Agent(evt.getAssessor()));

                    //If the peers don't exist, add them
                    if (hiddenGraph.getVertexInGraph(evt.getAssessee()) == null) {
                        hiddenGraph.addPeer(evt.getAssessee());
                    }
                    if (hiddenGraph.getVertexInGraph(evt.getAssessor()) == null) {
                        hiddenGraph.addPeer(evt.getAssessor());
                    }
                    FeedbackEdge edge = null;
                    try{
                        edge = new FeedbackEdge(edgeCounter, hiddenGraph.getVertexInGraph(evt.getAssessor()), hiddenGraph.getVertexInGraph(evt.getAssessee()));
                    }catch (Exception ex){
                        ChatterBox.error(this, "graphBuilder()", ex.getMessage());
                    }
                    edgeCounter++;
                    hiddenGraph.addEdge(edge, assessor, assessee);
                    logList.add(evt);
                }
            }
        }
    }
    //[end] Graph Builder

    //[start] Getters
    public LinkedList<TrustLogEvent> getLogList() {
        return logList;
    }

    public FeedbackHistoryGraph getHiddenP2PNetworkGraph() {
        return hiddenGraph;
    }

    public FeedbackHistoryGraph getVisibleP2PNetworkGraph() {
        return visibleGraph;
    }
    //[end] Getters

    //[start] Static Methods
    public static File chooseLoadFile(String filterDescription, String[] acceptedExtensions) {
        JFileChooser fileNamer = new JFileChooser(STARTING_DIRECTORY);
        fileNamer.setFileFilter(new ExtensionFileFilter(filterDescription, acceptedExtensions));
        int returnVal = fileNamer.showOpenDialog(null);


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            for (String extension : acceptedExtensions) {
                if (fileNamer.getSelectedFile().getAbsolutePath().endsWith(extension)) {

                    return fileNamer.getSelectedFile();
                }
            }
            JOptionPane.showMessageDialog(null, "Error: Incorrect extension.", "Error", JOptionPane.ERROR_MESSAGE);

            return null;
        } else if (returnVal == JFileChooser.ERROR_OPTION) {
            JOptionPane.showMessageDialog(null, "Error: Could not load file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public static TrustGraphLoader buildGraph(InputStream inStream) throws JDOMException, IOException {

        SAXBuilder parser = new SAXBuilder();
        Document doc = parser.build(inStream);

        TrustGraphLoader loader = new TrustGraphLoader();
        loader.logList.addFirst(TrustLogEvent.getStartEvent());
        loader.logList.addLast(TrustLogEvent.getEndEvent(loader.logList.getLast()));
        loader.graphBuilder(doc);

        return loader;
    }

    public static LinkedList<TrustLogEvent> buildLogs(InputStream inStream, FeedbackHistoryGraph hiddenGraph) throws JDOMException, IOException {

        SAXBuilder parser = new SAXBuilder();
        Document doc = parser.build(inStream);
        TrustGraphLoader loader = new TrustGraphLoader();

        loader.hiddenGraph = hiddenGraph;
        loader.addEventsToGraph(doc);
        return loader.logList;
    }
    //[end] Static Methods
}
