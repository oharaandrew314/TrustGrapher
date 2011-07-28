////////////////////////////////AlgorithmLoader//////////////////////////////////
package trustGrapher.graph.savingandloading;

import java.io.File;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import trustGrapher.TrustApplet;

import utilities.BitStylus;
import utilities.ChatterBox;

/**
 * An options frame which allows the user to choose which algorithms to load, and which ones to display
 * @author Andrew O'Hara
 */
public class AlgorithmLoader extends javax.swing.JFrame {

    public static final int NAME = 0, TYPE = 1, DISPLAY = 2, BASE = 3, PATH = 4, CONFIG = 5, MAX_ALGS = 12, MAX_GRAPHS = 6;
    public static final String FB = "FeedbackHistory", REP = "ReputationAlgorithm", TRUST = "TrustAlgorithm", TRUE = "true";
    public static final String FALSE = "false", LOG_PATH = "logPath", CLASS_PATH = "classPath", PROPERTY_PATH = "propertyPath";
    public static String NO_BASE = "noBase", NO_CLASS = "noClass", NO_CONFIG = "noConfig";
    private TrustPropertyManager config;
    private int visibleGraphs;
    private TrustApplet applet;
    private File logFile;

//////////////////////////////////Constructor///////////////////////////////////
    public AlgorithmLoader(TrustApplet applet, TrustPropertyManager config) {
        this.config = config;
        visibleGraphs = 0;
        this.applet = applet;
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    private String getNewKey(String type) {
        for (int i = 0; i < config.keySet().size() + 1; i++) {
            if (!config.containsKey(type + i)) {
                return type + i;
            }
        }
        return null;
    }

    public File getLogFile() {
        return logFile;
    }

///////////////////////////////////Methods//////////////////////////////////////
    private void updateFields() {
        int index = algList.getSelectedIndex();

        if (index != -1) { //If an algorithm is selected
            String[] entry = config.getAlg(index);

            nameField.setText("alg" + index + "-" + entry[NAME]); //Set the graph name field
            displayField.setSelected(entry[DISPLAY].equals(TRUE) ? true : false); //Set the Display JCheckBox

            propertiesField.setText(entry[CONFIG].equals(NO_CONFIG) ? "" : new File(entry[CONFIG]).getName()); //Set the properties file
            //If the feedbackHistory is selected, disable the properties buttons
            choosePropertiesButton.setEnabled(entry[TYPE].equals(FB) ? false : true);
            removePropertyButton.setEnabled(entry[TYPE].equals(FB) ? false : true);

            //Set the Base List
            baseField.setEnabled(false);
            baseField.removeAllItems();
            if (index == 0) { //If the alg is the feedbackHistory
                baseField.addItem(NO_BASE);
                baseField.setSelectedIndex(0);
            } else if (entry[TYPE].equals(REP)) { //if the alg is a reputation alg
                baseField.addItem("0-" + config.getAlg(0)[NAME]);
                baseField.setSelectedIndex(0);
                config.modifyAlg(index, BASE, "alg0");
            } else { //Otherwise, it must be a trust alg
                int baseIndex = 1;
                for (int i = 0; i <= MAX_ALGS; i++) { //Add All reputation algs to the base list
                    if (!config.containsKey("alg" + i)) {
                        continue;
                    }
                    String[] e = config.getAlg(i);
                    if (e[TYPE].equals(REP)) {
                        baseField.addItem(i + "-" + e[NAME]);
                        if (entry[BASE].equals("alg" + i)) { //If this alg is the alg's base, select it
                            baseField.setSelectedItem(e[NAME]);
                            baseIndex = i;
                        }
                    }
                }
                config.modifyAlg(index, BASE, "alg" + baseIndex);
            }
            //If the selected algorithm is a trust algrithm, enable the base field
            baseField.setEnabled(entry[TYPE].equals(TRUST) ? true : false);

            //Set the class list

            classList.removeAllItems();
            for (int i = 0; i < config.keySet().size(); i++) {
                if (config.containsKey("class" + i)) {
                    classList.addItem(TrustClassLoader.formatClassName(config.getClassPath(i)));
                }
            }
        }
    }

    public void run() {
        //If the feedbackHistory graph does not exist, add it
        if (!config.hasAlg(0)) {//     name       type   display     base         class path        config
            config.setProperty("alg0", FB + "," + FB + ",true," + NO_BASE + "," + NO_CLASS + "," + NO_CONFIG);
        }
        //Count the number of displayed algorithms
        visibleGraphs = 0;
        for (String[] alg : config.getAlgs()) {
            if (alg[DISPLAY].equals(TRUE)) {
                visibleGraphs++;
            }
        }
        //Set the path field to the last log
        String logPath = config.getProperty(LOG_PATH);
        if (logPath != null) {
            pathField.setText(new File(logPath).getPath());
            logFile = new File(logPath);
        }
        algList.setListData(config.getAlgDisplayNames()); //Set the algList
        algList.setSelectedIndex(0);
        updateFields();
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
        //Generate a key, and check if you're not trying to have more than the maximum number of algorithms
        String newKey = getNewKey("alg");
        if (Integer.parseInt(newKey.replace("alg", "")) > MAX_ALGS) {
            ChatterBox.alert("You cannot have more than " + MAX_ALGS + " algorithms.");
            return;
        }

        //Create the list of simple class names
        int highestClassIndex = -1;
        for (int i = 0 ; i < config.keySet().size() ; i++){
            if (config.containsKey("class" + i)){
                highestClassIndex = i;
            }
        }

        ChatterBox.print("" + highestClassIndex);
        String[] options = new String[highestClassIndex + 1];
        for (int i = 0; i <= highestClassIndex; i++) {
            options[i] = TrustClassLoader.formatClassName(config.getClassPath(i));
        }
        //Choose which algorithm to load
        String path = null;
        String temp = ChatterBox.selectionPane("Question", "Which class would you like to load?", options);
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(temp)) {
                path = config.getClassPath(i);
                break;
            }
        }
        if (path == null) {
            return;
        }

        //Load the algorithm to find out what type of algorithm it is, and if it can be added
        Object alg = TrustClassLoader.newAlgorithm(path);
        String type = (alg instanceof ReputationAlgorithm) ? REP : TRUST;
        if (type.equals(TRUST)) {
            //There must already be a reputation algorithm to use a trust algorithm.  Check if one exists
            boolean repAlgExists = false;
            for (String[] entry : config.getAlgs()) {
                if (entry[TYPE].equals(REP)) {
                    repAlgExists = true;
                    break;
                }
            }
            if (!repAlgExists) {
                ChatterBox.alert("There must be an existing Reputation Algorithm\nbefore you can add a Trust Algorithm.");
                return;
            }
        }

        //Create the property for the properties file            type         display         base         class path       configPath
        String property = alg.getClass().getSimpleName() + "," + type + "," + FALSE + "," + NO_BASE + "," + path + "," + NO_CONFIG;
        config.setProperty(newKey, property);

        //Add the info to the loader and window
        algList.setListData(config.getAlgDisplayNames());
        algList.setSelectedIndex(algList.getLastVisibleIndex());
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = algList.getSelectedIndex();
        if (index == 0) {
            ChatterBox.alert("You cannot remove the FeedbackHistory.");
        } else if (index != -1) {
            //Check if this algorithm is being used by another
            for (String[] entry : config.getAlgs()) {
                if ((entry[BASE]).equals("alg" + index)) {
                    ChatterBox.alert("You cannot remove an algorithm that is used by another.");
                    return;
                }
            }
            if (!config.removeProperty("alg" + index)) {
                ChatterBox.debug(this, "removeButtonActionPerformed()", "The property was not removed");
            }
            algList.setListData(config.getAlgDisplayNames());
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
            if (displayField.isSelected()) {
                if (visibleGraphs >= MAX_GRAPHS) {
                    displayField.setSelected(false);
                    ChatterBox.alert("You cannot have more than " + MAX_GRAPHS + " visible graphs at one time.");
                } else {
                    config.modifyAlg(algList.getSelectedIndex(), DISPLAY, TRUE);
                    visibleGraphs++;
                }
            } else {
                config.modifyAlg(algList.getSelectedIndex(), DISPLAY, FALSE);
                visibleGraphs--;
            }
        }
    }//GEN-LAST:event_displayFieldActionPerformed

    private void baseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseFieldActionPerformed
        //This is called when the selected algorithm's base is changed from the options window
        if (baseField.isEnabled() && baseField.getSelectedIndex() != -1 && isVisible()) {
            config.modifyAlg(algList.getSelectedIndex(), BASE, "alg" + baseField.getSelectedItem());
        }
    }//GEN-LAST:event_baseFieldActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        //This is called when the choose log button is pressed

        //See if there is a property containing the path of the last log file loaded
        File lastPath = config.containsKey(LOG_PATH) ? new File(config.getProperty(LOG_PATH)).getParentFile() : null;

        //Open a JFileChoose asking the user to choose a new log file
        logFile = BitStylus.chooseFile("Choose a log file to load", lastPath, new String[]{"arff"});
        if (logFile != null) {
            pathField.setText(logFile.getPath());
            config.setProperty(LOG_PATH, logFile.getPath());
        }
    }//GEN-LAST:event_loadButtonActionPerformed

    private void addClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addClassButtonActionPerformed
        //This is called when the Add Class Button is pressed

        //See if there is a property containg the path of the last class file loaded
        File directory = config.containsKey(CLASS_PATH) ? new File(config.getProperty(CLASS_PATH)) : null;

        //Open a JFileChoose asking the user to choose a class file to add
        File file = BitStylus.chooseFile("Choose an algorithm to load", directory, new String[]{"class", "jar"});
        if (file == null) {
            return;
        }
        Object o = TrustClassLoader.newAlgorithm(file.getPath()); //Load the object
        if (o == null) {
            return;
        }

        config.setProperty(CLASS_PATH, file.getParent()); //Save the path of the class file that was loaded

        String path = file.getPath().endsWith(".jar") ? file.getPath() + "!" + o.getClass().getName() : file.getPath();
        config.setProperty(getNewKey("class"), path);
        updateFields();
        classList.setSelectedIndex(classList.getItemCount() - 1);
    }//GEN-LAST:event_addClassButtonActionPerformed

    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        //This is called when the remove class button is called
        int index = classList.getSelectedIndex();

        //see if any algorithms depend on this class.  If so, the class cannot be deleted
        for (String[] entry : config.getAlgs()) {
            if (entry[PATH].equals(config.getClassPath(index))) {
                ChatterBox.alert("This class cannot be removed since it has algorithms that depend on it.");
                return;
            }
        }
        if (ChatterBox.yesNoDialog("Are you sure you want to remove " + classList.getItemAt(index) + "?")) {
            config.removeProperty("class" + index);
            classList.removeItemAt(index);
            updateFields();
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed

    private void choosePropertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choosePropertiesButtonActionPerformed
        //This is called when the Choose Properties button is pressed

        //See if there is a property containing the path of the last properties file loaded
        File lastPath = config.containsKey(PROPERTY_PATH) ? new File(config.getProperty(PROPERTY_PATH)) : null;
        File configFile = BitStylus.chooseFile("Choose a properties file to load", lastPath, new String[]{"properties"});
        if (configFile != null) {
            propertiesField.setText(configFile.getName());
            config.setProperty(PROPERTY_PATH, configFile.getPath());
            config.modifyAlg(algList.getSelectedIndex(), CONFIG, configFile.getPath());
        }
    }//GEN-LAST:event_choosePropertiesButtonActionPerformed

    private void removePropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePropertyButtonActionPerformed
        propertiesField.setText("");
        config.modifyAlg(algList.getSelectedIndex(), CONFIG, NO_CONFIG);
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

