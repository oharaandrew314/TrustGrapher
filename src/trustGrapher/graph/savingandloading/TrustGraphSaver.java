package trustGrapher.graph.savingandloading;

import cu.repsystestbed.entities.Agent;
import cu.repsystestbed.graphs.TestbedEdge;
import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.uci.ics.jung.graph.util.Pair;

public class TrustGraphSaver {

    private long currentTime;
    private List<TrustLogEvent> logList;
    private TrustGraph graph;
    private List<LoadingListener> progressListeners;

    //[start] Constructors
    public TrustGraphSaver(TrustGraph graph) {
        this(graph, null, 0);
    }

    public TrustGraphSaver(List<TrustLogEvent> events, long currentTime) {
        this(null, events, currentTime);
    }

    public TrustGraphSaver(TrustGraph graph, List<TrustLogEvent> events, long currentTime) {
        this.currentTime = currentTime;
        this.logList = events;
        this.graph = graph;
        progressListeners = new LinkedList<LoadingListener>();
    }
    //[end] Constructors

    //[start] Saver Method
    public void doSave() {
        Thread saverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                String path = chooseSaveFile().getAbsolutePath();
                if (path != null) {
                    Document doc = buildDoc();
                    if (outputDocumentToFile(doc, path)) {
                        JOptionPane.showMessageDialog(null, "Success: File Saved", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                    loadingComplete();
                }
            }
        });
        saverThread.start();
    }

    public static Document getGraphDocument(TrustGraph graph) {
        TrustGraphSaver saver = new TrustGraphSaver(graph);
        Document doc = saver.buildDoc();

        return doc;
    }
    //[end] Saver Method

    //[start] Document Builder Methods
    private Document buildDoc() {
        Element networkElement = new Element("network");

        if (graph != null && graph.getVertexCount() != 0) {
            //[start] Creating Graph Elements

            //[start] compile separate lists for peers, documents and peerdocuments
            LinkedList<Agent> peers = new LinkedList<Agent>();
            LinkedList<TestbedEdge> edges = new LinkedList<TestbedEdge>(graph.getEdges());
            int count = 0;
            loadingStarted(graph.getVertices().size(), "Compiling Vertex Lists");
            for (Agent vertex : graph.getVertices()) {
                if (vertex.getClass().equals(Agent.class)) {
                    peers.addLast((Agent) vertex);
                }
                loadingProgress(count);
                count++;
            }
            //[end] compile separate lists for peers, documents and peerdocuments

            Element graphElement = new Element("graph");

            graphElement.addContent(new Comment("Description of a P2PNetworkGraph")); //xml comment

            //[start] Create vertices
            Element nodemap = new Element("nodemap");
            count = 0;
            loadingChanged(peers.size(), "Peer Vertices");
            for (Agent peer : peers) { //write out all the peer information
                Element node = new Element("node");
                node.setAttribute("type", "TrustVertex");

                Element key = new Element("key");
                key.addContent(Integer.toString(peer.id));
                node.addContent(key);

                nodemap.addContent(node);
                loadingProgress(count);
                count++;
            }
            count = 0;
//            loadingChanged(documents.size(), "Document Vertices");
            graphElement.addContent(nodemap);
            //[end] Create vertices

            //[start] Create Edges
            Element edgemap = new Element("edgemap");
            count = 0;
            loadingChanged(edges.size(), "Edges");
            for (TestbedEdge e : edges) {//write out all the edge information
                Element edge = new Element("edge");

                /**Removed by me
                switch(e.getType()) {
                case TestbedEdge.P2P:
                edge.setAttribute("type", "PeerToPeer");
                break;
                case TestbedEdge.P2DOC:
                edge.setAttribute("type", "PeerToDocument");
                break;
                case TestbedEdge.DOC2DOC:
                edge.setAttribute("type", "DocumentToDocument");
                break;
                default:
                continue;
                }
                 */
                //Replaced by
                edge.setAttribute("type", "PeerToPeer");

                Pair<Agent> ends = graph.getEndpoints(e);

                String key1 = Integer.toString(ends.getFirst().id);
                String key2 = Integer.toString(ends.getSecond().id);
                Element v1 = new Element("v1");
                v1.addContent(key1);
                edge.addContent(v1);
                Element v2 = new Element("v2");
                v2.addContent(key2);

                edge.addContent(v2);

                edgemap.addContent(edge);

                loadingProgress(count);
                count++;
            }
            //[end] Create Edges

            graphElement.addContent(edgemap);
            networkElement.addContent(graphElement);
            //[end] Creating Graph Elements
        }
        if (logList != null) {
            //[start] Creating Log Event Elements
            Element logEventsElement = new Element("logevents");
            int count = 0;
            loadingChanged(logList.size(), "LogEvents");
            for (TrustLogEvent ev : logList) {
                Element event = new Element("event");
//                event.setAttribute("type", ev.getType());

                Element timeDifference = new Element("timedifference");
                timeDifference.addContent(Integer.toString((int) (ev.getTime() - currentTime)));
                event.addContent(timeDifference);

                Element paramOne = new Element("param1");
                paramOne.addContent(Integer.toString(ev.getAssessor()));
                event.addContent(paramOne);

                Element paramTwo = new Element("param2");
                paramTwo.addContent(Integer.toString(ev.getAssessee()));
                event.addContent(paramTwo);
                //End replace

                logEventsElement.addContent(event);
                loadingProgress(count);
                count++;
            }

            networkElement.addContent(logEventsElement);
            //[end] Creating Log Event Elements
        }

        return new Document(networkElement);
    }
    //[end] Document Builder Methods

    //[start] Listener Methods
    public void addLoadingListener(LoadingListener listener) {
        progressListeners.add(listener);
    }

    private void loadingStarted(int numberLines, String whatIsLoading) {
        for (LoadingListener l : progressListeners) {
            l.loadingStarted(numberLines, whatIsLoading);
        }
    }

    private void loadingProgress(int progress) {
        for (LoadingListener l : progressListeners) {
            l.loadingProgress(progress);
        }
    }

    private void loadingChanged(int numberLines, String whatIsLoading) {
        for (LoadingListener l : progressListeners) {
            l.loadingChanged(numberLines, whatIsLoading);
        }
    }

    private void loadingComplete() {
        for (LoadingListener l : progressListeners) {
            l.loadingComplete();
        }
    }
    //[end] Listener Methods

    //[start] XML outputter
    /**
     * This method shows how to use XMLOutputter to output a JDOM document to
     * a file located at xml/myFile.xml.
     * @param myDocument the JDOM document built from Listing 2.
     */
    private boolean outputDocumentToFile(Document myDocument, String path) {
        //setup this like outputDocument
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

            //output to a file
            FileWriter writer = new FileWriter(path);
            outputter.output(myDocument, writer);
            writer.close();

        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    //[end] XML outputter

    //[start] Static Methods
    public static File chooseSaveFile() {
        JFileChooser fileNamer = new JFileChooser();
        fileNamer.setFileFilter(new ExtensionFileFilter(".xml Files", "xml"));
        int returnVal = fileNamer.showSaveDialog(null);


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fileNamer.getSelectedFile().getAbsolutePath().endsWith(".xml")) {

                return fileNamer.getSelectedFile();
            }
            return new File(fileNamer.getSelectedFile().getAbsolutePath() + ".xml");
        } else if (returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
            return null;
        }
        return null;
    }
    //[end] Static Methods
}
