////////////////////////////////Configure//////////////////////////////////
package trustGrapher;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;

import trustGrapher.graph.MyGraph;
import trustGrapher.graph.savingandloading.LoadingBar;
import trustGrapher.graph.savingandloading.TrustGraphLoader;
import trustGrapher.visualizer.eventplayer.TrustLogEvent;

import utilities.BitStylus;
import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 * An options frame which allows the user to choose which algorithms to load, and which ones to display
 * @author Andrew O'Hara
 */
public class Configure extends javax.swing.JFrame {

    private PropertyManager config;
    private ArrayList<String[]> algs;
    public static final int NAME = 0, KEY = 1, TYPE = 2, DISPLAY = 3, BASE = 4, PATH = 5, MAX_ALGS = 12, MAX_GRAPHS = 6;
    public static final int MAX_VISIBLE_GRAPHS = 6;
    public static final String FB = "FeedbackHistory", REP = "ReputationAlgorithm", TRUST = "TrustAlgorithm", TRUE = "true";
    public static final String FALSE = "false", NO_BASE = "none", LOG_PATH = "logPath", ALG_PATH = "algPath";
    private int visibleGraphs;
    private TrustApplet applet;
    TrustGraphLoader loader;

//////////////////////////////////Constructor///////////////////////////////////
    public Configure(TrustApplet applet) {
        algs = new ArrayList<String[]>();
        visibleGraphs = 0;
        this.applet = applet;
        loader = new TrustGraphLoader();
        loader.addLoadingListener(new LoadingBar());

        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    private String[] algNames() {
        String[] names = new String[algs.size()];
        for (int i = 0; i < algs.size(); i++) {
            names[i] = algs.get(i)[NAME];
        }
        return names;
    }

    private ArrayList<String> getBaseAlgs(String[] entry) {
        ArrayList<String> algNames = new ArrayList<String>();
        if (entry[TYPE].equals(FB)) {
            algNames.add(NO_BASE);
        } else if (entry[TYPE].equals(REP)) {
            algNames.add(algs.get(0)[NAME]);
        } else if (entry[TYPE].equals(TRUST)) {
            for (String[] e : algs) {
                if (e[TYPE].equals(REP)) {
                    algNames.add(e[NAME]);
                }
            }
        } else {
            ChatterBox.debug(this, "getBaseAlgs()", "This entry had an unknown type");
        }
        return algNames;
    }

    public ArrayList<MyGraph[]> getGraphs() {
        return loader.getGraphs();
    }

    public LinkedList<TrustLogEvent> getEvents() {
        return loader.getLogList();
    }

    public ArrayList<String[]> getAlgs() {
        return algs;
    }

///////////////////////////////////Methods//////////////////////////////////////

    private void saveEntry(String[] entry) {
        String s = "";
        for (int i = 0; i < entry.length - 1; i++) {
            s = s + entry[i] + ",";
        }
        s = s + entry[entry.length - 1]; //Add the last element without putting a comma at the end
        //Split to get rid of the key in the name.  That has no use in the properties file
        config.setProperty(entry[KEY], s.split("-")[1]);
    }

    private void updateFields(int index) {
        if (index != -1) { //If an algorithm is selected
            String[] entry = algs.get(index);

            //Set the type label
            typeField.setText("Graph Type: " + entry[TYPE]);

            //Set the Display JCheckBox
            if (entry[DISPLAY].equals(FALSE)) {
                displayField.setSelected(false);
            } else {
                displayField.setSelected(true);
            }

            //Set the Base List
            baseField.setEnabled(false);
            baseField.removeAllItems();
            ArrayList<String> bases = getBaseAlgs(entry);
            for (String name : bases) {
                baseField.addItem(name);
            }
            baseField.setEnabled(true);
            for (String name : bases){
                if (name.split("-")[0].equals(entry[BASE])){
                    baseField.setSelectedItem(name);
                }
            }
        }
    }

    public void run() {
        config = new PropertyManager("TrustApplet.properties"); //Open the property manager
        if (config.getProperty("0") == null) { //If the feedbackHistory graph does not exist, add it
            config.setProperty("0", FB + ",0," + FB + ",true," + NO_BASE);
        }

        //Load the algorithm properties from the properties file
        algs.clear();
        for (int i = 0; i < MAX_ALGS; i++) {
            if (config.getProperty("" + i) != null) { //Algorithms have keys of integers
                String[] entry = config.getProperty("" + i).split(","); //Split the property into an entry array
                entry[NAME] = i + "-" + entry[NAME]; //Add the key to the name for easy identification
                algs.add(entry);
                if (entry[DISPLAY].equals(TRUE)) {
                    visibleGraphs++;
                }
            }
        }
        algList.setListData(algNames());
        algList.setSelectedIndex(0);
        updateFields(algList.getSelectedIndex());

        //Set the last log to the path field
        if (config.getProperty(LOG_PATH) != null){
            if (loader.loadFile(new File(config.getProperty(LOG_PATH)))){
                pathField.setText(loader.file.getPath());
            }
        }
        setVisible(true);
    }

/////////////////////////////////GUI Components/////////////////////////////////
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        algList = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        displayField = new javax.swing.JCheckBox();
        baseField = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        typeField = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        userField = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pathField = new javax.swing.JTextField();
        loadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Algorithm Configuration");

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        algList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        algList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                algListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(algList);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Algorithms");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        displayField.setText("Display Graph");
        displayField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayFieldActionPerformed(evt);
            }
        });

        baseField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Base graph");

        typeField.setText("Graph Type: Empty");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Used by:");

        userField.setEditable(false);
        jScrollPane2.setViewportView(userField);

        jLabel3.setText("Maximum 12 algorithms at any time.");

        jLabel5.setText("Maximum 6 graphs displayed at any time.");

        jLabel6.setText("Feedback Log:");

        pathField.setEditable(false);

        loadButton.setText("Choose Log");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(addButton)
                                        .addGap(18, 18, 18)
                                        .addComponent(removeButton)))
                                .addGap(12, 12, 12)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(displayField)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addGap(18, 18, 18)
                                                .addComponent(baseField, 0, 238, Short.MAX_VALUE))
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel5)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addGap(38, 38, 38)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                                .addGap(11, 11, 11))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(61, 61, 61)
                                        .addComponent(typeField))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(191, 191, 191)
                        .addComponent(applyButton)
                        .addGap(18, 18, 18)
                        .addComponent(okButton)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(loadButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(loadButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addButton)
                            .addComponent(removeButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(typeField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(displayField)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(baseField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(applyButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void algListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_algListValueChanged
        if (algList.getSelectedIndex() != -1) {
            updateFields(algList.getSelectedIndex());
        }
    }//GEN-LAST:event_algListValueChanged

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        //Find a new key for the algorithm
        int newKey = -1;
        for (int i = 1; i < MAX_ALGS + 1; i++) {
            if (!config.hasKey("" + i)) {
                newKey = i;
                break;
            }
        }
        if (newKey == -1) {
            ChatterBox.alert("You cannot have more than " + MAX_ALGS + " algorithms at one time.");
            return;
        }

        //Load the object
        String path = config.getProperty(ALG_PATH);
        File directory = null;
        if (path != null){
            directory = new File(path);
        }
        File classFile = BitStylus.chooseFile("Choose an algorithm to load", directory);
        if (classFile == null){
            return;
        }
        config.setProperty(ALG_PATH, classFile.getParent());
        Object o = BitStylus.classInstance(BitStylus.loadClass(classFile));
        ChatterBox.print(o.getClass().getSimpleName());
        if (o != null){
            config.setProperty(ALG_PATH, classFile.getParentFile().getPath());
        }else{
            return;
        }
        String type = null;
        if (o instanceof ReputationAlgorithm) {
            type = REP;
        } else if (o instanceof TrustAlgorithm) {            
            //Find out if there are any existing Reputation Algorithms
            //If there aren't any, then you can't add this Trust Algorithm
            boolean none = true;
            for (String[] entry : algs) {
                if (entry[TYPE].equals(REP)) {
                    none = false;
                    break;
                }
            }
            if (none) {
                ChatterBox.alert("You must have an existing Reputation\nAlgorithm to add a Trust Algorithm.");
                return;
            }
            type = TRUST;
        } else {
            ChatterBox.debug(this, "addButtonActionperformed()", "The file was not a recognized algorithm.");
            return;
        }

        String string = o.getClass().getSimpleName() + "," + newKey + "," + type + "," + FALSE + "," + NO_BASE + "," + classFile.getPath();
        String[] entry = string.split(",");
        entry[NAME] = newKey + "-" + entry[NAME];
        algs.add(entry);
        config.setProperty("" + newKey, string);
        algList.setListData(algNames());
        algList.setSelectedIndex(algList.getLastVisibleIndex());

    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = algList.getSelectedIndex();
        if (index == 0) {
            ChatterBox.alert("You cannot remove the FeedbackHistory.");
        } else if (index != -1) {
            //Check if this algorithm is being used by another
            for (String[] entry : algs) {
                if ((entry[BASE]).equals("" + index)) {
                    ChatterBox.alert("You cannot remove an algorithm that is used by another.");
                    return;
                }
            }
            algs.remove(index);
            if (!config.removeProperty("" + index)){
                ChatterBox.debug(this, "removeButtonActionPerformed()", "The property was not removed");
            }
            algList.setListData(algNames());
            algList.setSelectedIndex(algList.getLastVisibleIndex());
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        config.save();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        config.save();
        setVisible(false);
        applet.loadOptions();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void displayFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFieldActionPerformed
        if (algList.getSelectedIndex() != -1 && isVisible()) {
            String[] entry = algs.get(algList.getSelectedIndex());
            if (displayField.isSelected()) {
                if (visibleGraphs == MAX_VISIBLE_GRAPHS) {
                    displayField.setSelected(false);
                    ChatterBox.alert("You cannot have more than " + MAX_VISIBLE_GRAPHS + " visible graphs at one time.");
                } else {
                    entry[DISPLAY] = TRUE;
                    visibleGraphs++;
                }
            } else {
                entry[DISPLAY] = FALSE;
                visibleGraphs--;
            }
            saveEntry(entry);
        }
    }//GEN-LAST:event_displayFieldActionPerformed

    private void baseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseFieldActionPerformed
        if (baseField.isEnabled() && algList.getSelectedIndex() != -1 && baseField.getSelectedIndex() != -1 && isVisible()) {
            String[] entry = algs.get(algList.getSelectedIndex());
            String baseName = (String) baseField.getSelectedItem();
            if (baseName.equals(NO_BASE)) {
                entry[BASE] = NO_BASE;
            } else { //Set the entry's base to the key of the selected algorithm
                entry[BASE] = baseName.split("-")[0];
            }
            saveEntry(entry);
        }
    }//GEN-LAST:event_baseFieldActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        File lastPath = null;
        if (config.getProperty(LOG_PATH) != null){
            lastPath = new File (config.getProperty(LOG_PATH)).getParentFile();
        }
        if (loader.doLoad(lastPath)) {
            pathField.setText(loader.file.getPath());
            config.setProperty(LOG_PATH, loader.file.getPath());
            ChatterBox.debug(this, "loadButtonActionPerformed()", loader.file.getPath());
        }
    }//GEN-LAST:event_loadButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList algList;
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox baseField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox displayField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField pathField;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel typeField;
    private javax.swing.JTextPane userField;
    // End of variables declaration//GEN-END:variables
}
////////////////////////////////////////////////////////////////////////////////

