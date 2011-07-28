////////////////////////////////AlgorithmLoader//////////////////////////////////
package trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import trustGrapher.TrustApplet;

import utilities.BitStylus;
import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 * An options frame which allows the user to choose which algorithms to load, and which ones to display
 * @author Andrew O'Hara
 */
public class AlgorithmLoader extends javax.swing.JFrame {

    public static final int NAME = 0, ID = 1, TYPE = 2, DISPLAY = 3, BASE = 4, PATH = 5, CONFIG = 6, MAX_ALGS = 12, MAX_GRAPHS = 6;
    public static final String FB = "FeedbackHistory", REP = "ReputationAlgorithm", TRUST = "TrustAlgorithm", TRUE = "true";
    public static final String FALSE = "false", NONE = "none", LOG_PATH = "logPath", CLASS_PATH = "classPath";
    private PropertyManager config;
    private ArrayList<String[]> algs;
    private ArrayList<String> classes;
    private int visibleGraphs;
    private TrustApplet applet;
    private File logFile;

//////////////////////////////////Constructor///////////////////////////////////
    public AlgorithmLoader(TrustApplet applet, PropertyManager config) {
        this.config = config;
        algs = new ArrayList<String[]>();
        classes = new ArrayList<String>();
        visibleGraphs = 0;
        this.applet = applet;

        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    private String[] algNames() {
        String[] names = new String[algs.size()];
        for (int i = 0; i < algs.size(); i++) {
            names[i] = algs.get(i)[NAME].replace("alg", "");
        }
        return names;
    }

    private ArrayList<String> getBaseAlgs(String[] entry) {
        ArrayList<String> algNames = new ArrayList<String>();
        if (entry[TYPE].equals(FB)) {
            algNames.add(NONE);
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

    public String getNewKey(String type) {
        for (int i = 0; i < config.getKeys().length + 1; i++) {
            if (!config.hasKey(type + i)) {
                return type + i;
            }
        }
        return null;
    }

    public File getLogFile() {
        return logFile;
    }

    public ArrayList<String[]> getAlgs() {
        return (ArrayList<String[]>) algs.clone();
    }

///////////////////////////////////Methods//////////////////////////////////////
    private void saveEntry(String[] entry) {
        String s = "";
        for (int i = 0; i < entry.length - 1; i++) {
            s = s + entry[i] + ",";
        }
        s = s + entry[entry.length - 1]; //Add the last element without putting a comma at the end
        //Split to get rid of the key in the name.  That has no use in the properties file
        config.setProperty(entry[ID], s.split("-")[1]);
    }

    private void updateFields() {
        int index = algList.getSelectedIndex();

        if (index != -1) { //If an algorithm is selected
            String[] entry = algs.get(index);

            //Set the graph name field
            nameField.setText(entry[NAME]);

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
            for (String name : bases) {
                if (name.split("-")[0].equals(entry[BASE])) {
                    baseField.setSelectedItem(name);
                }
            }
            saveBaseField();
            //If the selected algorithm is the feecback history or a reputation graph, disable the base field.
            baseField.setEnabled(entry[TYPE].equals(FB) || entry[TYPE].equals(REP) ? false : true);

            //Set the class list
            classList.removeAllItems();
            for (String path : classes) {
                classList.addItem(TrustClassLoader.formatClassName(path));
            }

            //Set the properties file
            if (entry[CONFIG].equals(NONE)) {
                propertiesField.setText("");
            } else {
                propertiesField.setText(new File(entry[CONFIG]).getName());
            }
            choosePropertiesButton.setEnabled(entry[TYPE].equals(FB) ? false : true);
            removePropertyButton.setEnabled(entry[TYPE].equals(FB) ? false : true);
        }
    }

    public void run() {
        if (config.getProperty("alg0") == null) { //If the feedbackHistory graph does not exist, add it
            config.setProperty("alg0", FB + ",alg0," + FB + ",true," + NONE + "," + NONE + "," + NONE);
        }

        //Load the algorithm properties from the properties file
        algs.clear();
        for (int i = 0; i < MAX_ALGS + 1; i++) { //MAX_ALGS is increeased by 1 since the feedbackgraph doesn't count
            if (config.getProperty("alg" + i) != null) { //Algorithms have keys of integers
                String[] entry = config.getProperty("alg" + i).split(","); //Split the property into an entry array
                entry[NAME] = i + "-" + entry[NAME]; //Add the key to the name for easy identification
                algs.add(entry);
                if (entry[DISPLAY].equals(TRUE)) {
                    visibleGraphs++;
                }
            }
        }
        algList.setListData(algNames());

        //Load the class properties
        classes.clear();
        for (int i=0 ; i < config.getKeys().length ; i++){
            if (config.hasKey("class" + i)){
                classes.add(config.getProperty("class" + i));
            }
        }

        //Set the path field to the last log
        String logPath = config.getProperty(LOG_PATH);
        if (logPath != null) {
            logFile = new File(logPath);
            if (logFile != null) {
                pathField.setText(logFile.getPath());
            }
        }

        algList.setSelectedIndex(0);
        updateFields();
        setVisible(true);
    }

    private void saveBaseField() {
        String[] entry = algs.get(algList.getSelectedIndex());
        String baseName = (String) baseField.getSelectedItem();
        entry[BASE] = (baseName.equals(NONE)) ? NONE : baseName.split("-")[0];  //Save the algorithm number
        saveEntry(entry);
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
        jSeparator3 = new javax.swing.JSeparator();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        pathField = new javax.swing.JTextField();
        loadButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        classList = new javax.swing.JComboBox();
        addClassButton = new javax.swing.JButton();
        removeClassButton = new javax.swing.JButton();
        graphLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        propertiesField = new javax.swing.JTextField();
        choosePropertiesButton = new javax.swing.JButton();
        removePropertyButton = new javax.swing.JButton();

        setTitle("Algorithm Configuration");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

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

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
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

        jLabel2.setText("Base Algorithm");

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

        jLabel7.setText("Classes:");

        classList.setMaximumSize(new java.awt.Dimension(41, 28));

        addClassButton.setText("Add");
        addClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addClassButtonActionPerformed(evt);
            }
        });

        removeClassButton.setText("Remove");
        removeClassButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeClassButtonActionPerformed(evt);
            }
        });

        graphLabel.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        graphLabel.setText("Algorithm:");

        nameField.setEditable(false);
        nameField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        jLabel4.setText("Algorithm Properties");

        propertiesField.setEditable(false);

        choosePropertiesButton.setText("Choose");
        choosePropertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                choosePropertiesButtonActionPerformed(evt);
            }
        });

        removePropertyButton.setText("Remove");
        removePropertyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePropertyButtonActionPerformed(evt);
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
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(addButton)
                                        .addGap(18, 18, 18)
                                        .addComponent(removeButton))
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(126, 126, 126)
                                        .addComponent(graphLabel))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(displayField)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addGap(18, 18, 18)
                                                .addComponent(baseField, 0, 217, Short.MAX_VALUE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(choosePropertiesButton)
                                                    .addComponent(jLabel4))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(removePropertyButton)
                                                    .addComponent(propertiesField, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)))
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel5)
                                            .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)))))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(191, 191, 191)
                        .addComponent(applyButton)
                        .addGap(18, 18, 18)
                        .addComponent(okButton)
                        .addGap(18, 18, 18)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(classList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addClassButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeClassButton))
                            .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(loadButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(classList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addClassButton)
                            .addComponent(removeClassButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(addButton)
                                    .addComponent(removeButton)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(displayField)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2)
                                    .addComponent(baseField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(propertiesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(choosePropertiesButton)
                                    .addComponent(removePropertyButton))
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
                            .addComponent(applyButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(102, 102, 102)
                        .addComponent(graphLabel)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void algListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_algListValueChanged
        if (algList.getSelectedIndex() != -1) {
            updateFields();
        }
    }//GEN-LAST:event_algListValueChanged

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String newKey = getNewKey("alg");
        if (Integer.parseInt(newKey.replace("alg", "")) > MAX_ALGS) {
            ChatterBox.alert("You cannot have more than " + MAX_ALGS + " algorithms.");
            return;
        }

        String[] options = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            options[i] = TrustClassLoader.formatClassName(classes.get(i));
        }
        //Choose which algorithm to load
        String path = null;
        String temp = ChatterBox.selectionPane("Question", "Which class would you like to load?", options);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(temp)) {
                path = classes.get(i);
                break;
            }
        }
        if (path == null) {
            return;
        }

        //Load the algorithm to find out what type of algorithm it is, and if it can be added
        Object alg = TrustClassLoader.newAlgorithm(path);
        String type = null;
        if (alg instanceof ReputationAlgorithm) {
            type = REP;
        } else { //It is guaranteed to either be a reputation or trust algorithm by newAlgorithm()
            type = TRUST;
            //There must already be a reputation algorithm to use a trust algorithm.  Check if one exists
            boolean repAlgExists = false;
            for (String[] entry : algs) {
                if (entry[TYPE].equals(REP)) {
                    repAlgExists = true;
                }
            }
            if (!repAlgExists) {
                ChatterBox.alert("There must be an existing Reputation Algorithm\nbefore you can add a Trust Algorithm.");
                return;
            }
        }

        //Create the property for the properties file           key            type       display       base      class path   configPath
        String string = alg.getClass().getSimpleName() + "," + newKey + "," + type + "," + FALSE + "," + NONE + "," + path + "," + NONE;
        String[] entry = string.split(",");
        entry[NAME] = newKey + "-" + entry[NAME];
        config.setProperty(newKey, string);

        //Add the info to the loader and window
        algs.add(entry);
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
            if (!config.removeProperty(algs.get(index)[ID])) {
                ChatterBox.debug(this, "removeButtonActionPerformed()", "The property was not removed");
            }
            algs.remove(index);
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
        applet.loadAlgorithms();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void displayFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFieldActionPerformed
        if (algList.getSelectedIndex() != -1 && isVisible()) {
            String[] entry = algs.get(algList.getSelectedIndex());
            if (displayField.isSelected()) {
                if (visibleGraphs == MAX_GRAPHS) {
                    displayField.setSelected(false);
                    ChatterBox.alert("You cannot have more than " + MAX_GRAPHS + " visible graphs at one time.");
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
            saveBaseField();
        }
        saveEntry(algs.get(algList.getSelectedIndex()));
    }//GEN-LAST:event_baseFieldActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        File lastPath = null;
        if (config.getProperty(LOG_PATH) != null) {
            lastPath = new File(config.getProperty(LOG_PATH)).getParentFile();
        }

        logFile = BitStylus.chooseFile("Choose a log file to load", lastPath, ".arff files only", new String[]{"arff"});
        if (logFile != null) {
            pathField.setText(logFile.getPath());
            config.setProperty(LOG_PATH, logFile.getPath());
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    private void addClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassButtonActionPerformed
        //Find the class or jar file
        String path = config.getProperty(CLASS_PATH);
        File directory = null;
        if (path != null) {
            directory = new File(path);
        }
        File file = BitStylus.chooseFile("Choose an algorithm to load", directory, ".class and .jar files only", new String[]{"class", "jar"});
        if (file == null) return;
        config.setProperty(CLASS_PATH, file.getParent());

        //Load the object
        Object o = TrustClassLoader.newAlgorithm(file.getPath());
        if (o == null) return;
        if (file.getPath().endsWith(".class")) { //if it is a class file
            config.setProperty(getNewKey("class"), file.getPath());
            classes.add(file.getPath());
        } else { //Otherwise, it's a jar file.  This is ensured by the file filter
            config.setProperty(getNewKey("class"), file.getPath() + "!" + o.getClass().getName());
            classes.add(file.getPath() + "!" + o.getClass().getName());
        }

        updateFields();
        classList.setSelectedIndex(classList.getItemCount() - 1);
    }//GEN-LAST:event_addClassButtonActionPerformed

    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        int index = classList.getSelectedIndex();

        //see if any algorithms depend on this class
        for (String[] entry : algs) {
            if (entry[PATH].equals(classes.get(index))) {//Remove the algorithm if it uses class
                ChatterBox.alert("This class cannot be removed since it has algorithms that depend on it.");
                return;
            }
        }
        ChatterBox.alert(index + " " + classes.get(index));
        ChatterBox.alert("Property is\n" + config.getProperty("class" + index));
        if (ChatterBox.yesNoDialog("Are you sure you want to remove " + classList.getItemAt(index) + "?")) {
            config.removeProperty("class" + index);
            classes.remove(index);
            classList.removeItemAt(index);

            updateFields();
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed

    private void choosePropertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choosePropertiesButtonActionPerformed
        File lastPath = null;
        if (config.getProperty("propertyPath") != null) {
            lastPath = new File(config.getProperty("propertyPath"));
        }
        File configFile = BitStylus.chooseFile("Choose a properties file to load", lastPath, ".properties only", new String[]{"properties"});
        if (configFile != null) {
            propertiesField.setText(configFile.getName());
            config.setProperty("propertyPath", configFile.getPath());
            algs.get(algList.getSelectedIndex())[CONFIG] = configFile.getPath();
            saveEntry(algs.get(algList.getSelectedIndex()));
        }
    }//GEN-LAST:event_choosePropertiesButtonActionPerformed

    private void removePropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePropertyButtonActionPerformed
        propertiesField.setText("");
        algs.get(algList.getSelectedIndex())[CONFIG] = NONE;
        saveEntry(algs.get(algList.getSelectedIndex()));
    }//GEN-LAST:event_removePropertyButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addClassButton;
    private javax.swing.JList algList;
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox baseField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton choosePropertiesButton;
    private javax.swing.JComboBox classList;
    private javax.swing.JCheckBox displayField;
    private javax.swing.JLabel graphLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loadButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField pathField;
    private javax.swing.JTextField propertiesField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeClassButton;
    private javax.swing.JButton removePropertyButton;
    // End of variables declaration//GEN-END:variables
}
////////////////////////////////////////////////////////////////////////////////

