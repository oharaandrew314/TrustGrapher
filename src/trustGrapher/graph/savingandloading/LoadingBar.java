package trustGrapher.graph.savingandloading;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class LoadingBar extends JDialog {

    JProgressBar progressBar;

    public LoadingBar() {
        setTitle("Loading...");

        setBounds(100, 100, 250, 100);
        setResizable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setBackground(Color.BLACK);
        setLayout(new GridLayout(1, 1));

        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        add(progressBar);
    }

    public void loadingStarted(int numberLines, String whatIsLoading) {
        setTitle("Loading " + whatIsLoading + "...");
        progressBar.setMinimum(0);
        progressBar.setMaximum(numberLines);
        progressBar.setName(whatIsLoading);
        progressBar.setValue(0);
        progressBar.setString(whatIsLoading + ": 0%");
        progressBar.setStringPainted(true);
        setVisible(true);

    }

    public void loadingProgress(int lineNumber) {
        progressBar.setValue(lineNumber);
        progressBar.setString((String.format(progressBar.getName() + ": %.3g%n", progressBar.getPercentComplete() * 100)) + "%");
    }

    public void loadingProgress() {
        loadingProgress(progressBar.getValue() + 1);
    }

    public void loadingComplete() {
        setVisible(false);

    }

    public static void main(String[] args) {
        LoadingBar test = new LoadingBar();
        int size = 100000;

        test.loadingStarted(size, "Test 1");
        for (int i = 0; i < size; i++) {
            test.loadingProgress(i);
        }
        test.loadingStarted(size * 5, "Test 2");
        for (int i = 0; i < size * 5; i++) {
            test.loadingProgress(i);
        }
        test.loadingComplete();
        System.exit(0);
    }
}
