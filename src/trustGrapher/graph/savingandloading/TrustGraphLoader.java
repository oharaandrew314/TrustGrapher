package trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import trustGrapher.graph.*;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;


public class TrustGraphLoader {

    public static final File STARTING_DIRECTORY = new File("/home/zalpha314/Documents/Programming/Work/TrustGrapher2/test");
    private LinkedList<TrustLogEvent> logList;
    private List<LoadingListener> loadingListeners;
    private ArrayList<MyGraph[]> graphs;

    //[start] Constructor
    public TrustGraphLoader() {
        logList = new LinkedList<TrustLogEvent>();
        loadingListeners = new LinkedList<LoadingListener>();

    }
    //[end] Constructor6

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
                    graphs = logBuilder.getGraphs();
                    //hiddenGraph = logBuilder.getHiddenGraph(); //load hidden graph but keep visible graph empty
                    
                    loadingComplete();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Failure in doLoad()", JOptionPane.ERROR_MESSAGE);
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

    //[start] Getters
    public LinkedList<TrustLogEvent> getLogList() {
        return logList;
    }

    public ArrayList<MyGraph[]> getGraphs(){
        return graphs;
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
}
