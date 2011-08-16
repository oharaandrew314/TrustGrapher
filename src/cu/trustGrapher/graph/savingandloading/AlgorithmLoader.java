////////////////////////////////AlgorithmLoader//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import java.io.File;

import cu.trustGrapher.TrustGrapher;
import java.util.ArrayList;
import utilities.*;

/**
 * An options frame which allows the user to choose which algorithms to load, and which ones to display
 * @author Andrew O'Hara
 */
public class AlgorithmLoader extends javax.swing.JFrame {

    public static final String ALG = "alg", CLASS = "class";
    public static final String LOG_PATH = "logPath", CLASS_PATH = "classPath", PROPERTY_PATH = "propertyPath";
    private PropertyManager config;
    private TrustGrapher applet;
    private AlgorithmConfigManager algorithms;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Initializes the algorithm loader components
     * @param applet The main class.  Needed to inform it that the algorithms have been loaded
     * @param config The properties manager to load all of the class and algorithm properties
     */
    public AlgorithmLoader(TrustGrapher applet, PropertyManager config) {
        this.applet = applet;
        this.config = config;
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Gets an unused property key that can be used to store a new algorithm or class
     * This is useful to ensure that keys do not conflict.
     * If a key was deleted, this will return an old key to be reused
     * @param type A String which contains the type of key to generate.  Ex: "alg" , "class"
     * @return An unused property key that is of the specified type
     */
    private Integer getNewKey(String type) {
        for (int i = 0; i < config.keySet().size() + 1; i++) {
            if (!config.containsKey(type + i)) {
                return i;
            }
        }
        return null;
    }

    public AlgorithmConfigManager getAlgorithms() {
        return algorithms;
    }

    private int getBaseIndex() {
        return Integer.parseInt(((String) baseField.getSelectedItem()).split("-")[0]);
    }

    private GraphConfig getSelectedAlg() {
        String temp = ((String) algList.getSelectedValue()).split("-")[0];
        return algorithms.getAlg(Integer.parseInt(temp));
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Whenever an algList value is changed, this is called.
     * It updates all of the components to indicate the current selection or change that was made
     */
    private void updateFields() {
        GraphConfig alg = getSelectedAlg();
        if (alg != null) { //If an algorithm is selected
            nameField.setText(alg.getDisplayName()); //Set the graph name field
            displayField.setSelected(alg.isDisplayed()); //Set the Display JCheckBox

            propertiesField.setText(alg.getProperties() != null ? alg.getProperties().getName() : ""); //Set the properties file
            //If the feedbackHistory is selected, disable the properties buttons
            choosePropertiesButton.setEnabled(alg.isFeedbackHistory() ? false : true);
            removePropertyButton.setEnabled(alg.isFeedbackHistory() ? false : true);

            //Set the Base List
            baseField.setEnabled(false);
            baseField.removeAllItems();
            if (alg.isFeedbackHistory()) { //If the alg is the feedbackHistory
                baseField.addItem(GraphConfig.NO_BASE);
                baseField.setSelectedIndex(0);
            } else if (alg.isReputationAlg()) { //if the alg is a reputation alg
                baseField.addItem(algorithms.getAlg(0).getDisplayName());
                baseField.setSelectedIndex(0);
            } else { //Otherwise, it must be a trust alg
                for (GraphConfig a : algorithms.getAlgs()) { //Add All reputation algs to the base list
                    if (a.isReputationAlg()) {
                        baseField.addItem(a.getDisplayName());
                        if (alg.getBase() != -1) {
                            if (alg.getBase() == a.getIndex()) { //If this alg is the alg's base, select it
                                baseField.setSelectedItem(a.getDisplayName());
                            }
                        }
                    }
                }
            }
            if (((String) baseField.getSelectedItem()).equals(GraphConfig.NO_BASE)) {
                alg.setBase(-1);
            } else {
                alg.setBase(getBaseIndex());
            }
            //If the selected algorithm is a trust algrithm, enable the base field
            baseField.setEnabled(alg.isTrustAlg() ? true : false);

            //Set the class list
            classList.removeAllItems();
            for (int i = 0; i < config.keySet().size(); i++) {
                if (config.containsKey(CLASS + i)) {
                    classList.addItem(TrustClassLoader.formatClassName(i, config.getProperty(CLASS + i)));
                }
            }
            config.setProperty(alg.getKey(), alg.toString());
        }
    }

    /**
     * This is called by the TrustGrapher.  It displays the algorithm loader window,
     * loads all of the properties from the properties file, and then updates the window
     */
    public void run() {
        algorithms = new AlgorithmConfigManager(config);
        AlgorithmConfigManager.ALG_COUNT = 0;
        AlgorithmConfigManager.VISIBLE_COUNT = 0;
        config.loadPropertyFile();

        if (!config.containsKey(ALG + 0)) { //If the feedbackHistory graph does not exist in the properties, add it
            algorithms.newAlg(0, true, -1);
        }
        //Add the rest of the algorithms
        for (int i = 0; i <= config.keySet().size(); i++) {
            if (config.containsKey(ALG + i)) {
                algorithms.newAlgFromProperty(i, config.getProperty(ALG + i).split(","));
            }
        }

        //Set the path field to the last log
        String logPath = config.getProperty(LOG_PATH);
        if (logPath != null) {
            pathField.setText(logPath);
        }
        algList.setListData(algorithms.getAlgDisplayNames()); //Set the algList
        algList.setSelectedIndex(0);
        updateFields();
        setVisible(true);
        ChatterBox.print("" + AlgorithmConfigManager.VISIBLE_COUNT);
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
        helpButton = new javax.swing.JButton();

        setTitle("Algorithm Configuration");
        setBackground(new java.awt.Color(39, 31, 24));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(254, 254, 254));

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

        jSeparator1.setForeground(new java.awt.Color(254, 254, 254));
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

        jLabel2.setBackground(new java.awt.Color(52, 52, 52));
        jLabel2.setText("Depends on:");

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

        helpButton.setText("Help");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
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
                                                .addComponent(baseField, 0, 234, Short.MAX_VALUE))
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
                        .addContainerGap()
                        .addComponent(helpButton)
                        .addGap(134, 134, 134)
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
                            .addComponent(applyButton)
                            .addComponent(helpButton)))
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
        //Generate array of algorithms to load
        ArrayList<String> classPaths = new ArrayList<String>();
        for (int i = 0; i <= AlgorithmConfigManager.MAX_ALGS; i++) {
            if (config.containsKey(CLASS + i)) {
                classPaths.add(TrustClassLoader.formatClassName(i, config.getProperty(CLASS + i)));
            }
        }
        //Ask which algorithm to load
        String selectedClassName = ChatterBox.selectionPane("Question", "Which class would you like to load?", classPaths.toArray());
        if (selectedClassName != null) {
            int newKey = getNewKey(ALG);
            algorithms.newAlg(newKey, false, classPaths.indexOf((Object) selectedClassName));
            config.setProperty(ALG + newKey, algorithms.getAlg(newKey).toString());

            //Add the info to the loader and window
            algList.setListData(algorithms.getAlgDisplayNames());
            algList.setSelectedIndex(algList.getLastVisibleIndex());
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = getSelectedAlg().getIndex();
        if (index == 0) {
            ChatterBox.alert("You cannot remove the FeedbackHistory.");
        } else if (index != -1) {
            //Check if this algorithm is being used by another
            for (GraphConfig alg : algorithms.getAlgs()) {
                if (alg.getBase() != -1) {
                    if (alg.getBase() == index) {
                        ChatterBox.alert("You cannot remove an algorithm that is used by another.");
                        return;
                    }
                }
            }
            algorithms.removeAlg(index);
            config.remove(ALG + index);
            algList.setListData(algorithms.getAlgDisplayNames());
            algList.setSelectedIndex(algList.getLastVisibleIndex());
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        config.save();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        config.save();
        setVisible(false);
        if (AlgorithmConfigManager.VISIBLE_COUNT > 0) {
            applet.loadAlgorithms();
        } else {
            ChatterBox.alert("No graphs are displayed, so no action was done.");
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void displayFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFieldActionPerformed
        if (algList.getSelectedIndex() != -1 && isVisible()) {
            if (displayField.isSelected()) {
                if (AlgorithmConfigManager.VISIBLE_COUNT >= AlgorithmConfigManager.MAX_VISIBLE) {
                    displayField.setSelected(false);
                    ChatterBox.alert("You cannot have more than " + AlgorithmConfigManager.MAX_VISIBLE + " visible graphs at one time.");
                } else {
                    GraphConfig alg = getSelectedAlg();
                    alg.setDisplay(displayField.isSelected());
                    config.setProperty(alg.getKey(), alg.toString());
                }
            }
        }
    }//GEN-LAST:event_displayFieldActionPerformed

    private void baseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseFieldActionPerformed
        //This is called when the selected algorithm's base is changed from the options window
        if (baseField.isEnabled() && baseField.getSelectedIndex() != -1 && isVisible()) {
            GraphConfig alg = getSelectedAlg();
            alg.setBase(getBaseIndex());
            config.setProperty(alg.getKey(), alg.toString());
        }
    }//GEN-LAST:event_baseFieldActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        //This is called when the choose log button is pressed

        //See if there is a property containing the path of the last log file loaded
        File lastPath = config.containsKey(LOG_PATH) ? new File(config.getProperty(LOG_PATH)).getParentFile() : null;

        //Open a JFileChoose asking the user to choose a new log file
        File logFile = BitStylus.chooseFile("Choose a log file to load", lastPath, new String[]{"arff"});
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
        File file = BitStylus.chooseFile("Choose an algorithm to load", directory, new String[]{CLASS, "jar"});
        Object o = (file != null) ? TrustClassLoader.newAlgorithm(file.getPath()) : null; //Load the object
        if (o != null) {
            config.setProperty(CLASS_PATH, file.getParent()); //Save the path of the class file that was loaded
            String path = file.getPath().endsWith(".jar") ? file.getPath() + "!" + o.getClass().getName() : file.getPath();
            config.setProperty(CLASS + getNewKey(CLASS), path);
            updateFields();
            classList.setSelectedIndex(classList.getItemCount() - 1);
        }
    }//GEN-LAST:event_addClassButtonActionPerformed

    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        //This is called when the remove class button is called
        if (classList.getSelectedItem() == null) {
            return;
        }
        int index = Integer.parseInt(((String) classList.getSelectedItem()).split("-")[0].replace(CLASS, ""));

        //see if any algorithms depend on this class.  If so, the class cannot be deleted
        for (GraphConfig alg : algorithms.getAlgs()) {
            if (alg.getClassFile() != null) {
                if (alg.getClassFile().getPath().equals(config.getProperty(CLASS + index))) {
                    ChatterBox.alert("This class cannot be removed since it has algorithms that depend on it.");
                    return;
                }
            }
        }
        if (ChatterBox.yesNoDialog("Are you sure you want to remove " + classList.getSelectedItem() + "?")) {
            if (!config.remove(CLASS + index)) {
                ChatterBox.alert("The property was not removed!");
            }
            classList.removeItemAt(classList.getSelectedIndex());
            updateFields();
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed

    private void choosePropertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choosePropertiesButtonActionPerformed
        //This is called when the Choose Properties button is pressed

        //See if there is a property containing the path of the last properties file loaded
        File lastPath = config.containsKey(PROPERTY_PATH) ? new File(config.getProperty(PROPERTY_PATH)) : null;
        File propertyFile = BitStylus.chooseFile("Choose a properties file to load", lastPath, new String[]{"properties"});
        if (propertyFile != null) {
            propertiesField.setText(propertyFile.getName());
            config.setProperty(PROPERTY_PATH, propertyFile.getPath());
            GraphConfig alg = getSelectedAlg();
            alg.setProperties(propertyFile);
            config.setProperty(alg.getKey(), alg.toString());
        }
    }//GEN-LAST:event_choosePropertiesButtonActionPerformed

    private void removePropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePropertyButtonActionPerformed
        propertiesField.setText("");
        getSelectedAlg().setProperties(null);
    }//GEN-LAST:event_removePropertyButtonActionPerformed

private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
    String message = "To use this configuration window:\n\n"
            + "You must first add a class path to the classes combo box.\nClick the 'Add' Button next to it to do so.\n\n"
            + "Then you can add an algorithm to the simulator with the 'Add' Button below the 'Algorithms' list.\n"
            + "You can only add algorithms who have had their class path added already.\n\n"
            + "You can then select an individual algorithm from the list and change their properties in the panel to the right.\n"
            + "The 'Depends on' combo box means that the currently selected algorithm will listen to the graph connected to the\n"
            + "algorithm that is selected in the combo box.\n\n";
    ChatterBox.alert(message);
}//GEN-LAST:event_helpButtonActionPerformed
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
    private javax.swing.JButton helpButton;
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
