////////////////////////////////Configure//////////////////////////////////
package trustGrapher.graph.savingandloading;

import java.io.File;
import java.util.ArrayList;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import trustGrapher.TrustApplet;

import utilities.BitStylus;
import utilities.ChatterBox;
import utilities.PropertyManager;

/**
 * An options frame which allows the user to choose which algorithms to load, and which ones to display
 * @author Andrew O'Hara
 */
public class Configure extends javax.swing.JFrame {

    public static final int NAME = 0, ID = 1, TYPE = 2, DISPLAY = 3, BASE = 4, PATH = 5, MAX_ALGS = 12, MAX_GRAPHS = 6;
    public static final String FB = "FeedbackHistory", REP = "ReputationAlgorithm", TRUST = "TrustAlgorithm", TRUE = "true";
    public static final String FALSE = "false", NO_BASE = "none", LOG_PATH = "logPath", CLASS_PATH = "classPath";
    private PropertyManager config;
    private ArrayList<String[]> algs;
    private ArrayList<String> classes;
    private int visibleGraphs;
    private TrustApplet applet;
    private File logFile;

//////////////////////////////////Constructor///////////////////////////////////
    public Configure(TrustApplet applet, PropertyManager config) {
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

    public String getNewKey(String type){
        for (int i = 0; i < config.getKeys().length + 1; i++) {
            if (!config.hasKey(type + i)) {
                return type + i;
            }
        }
        return null;
    }

    public String formatClassName(String path){
        if (path.contains("!")){ //Replace the long jar path with "<algName> from <jarName>"
            path = path.substring(path.lastIndexOf('.') +1, path.length()) + " from " + path.substring(path.lastIndexOf('/') + 1, path.indexOf('!'));
        }else{
            path = path.substring(path.lastIndexOf('/') + 1).replace(".class", "");
        }
        return path;
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
            for (String name : bases) {
                if (name.split("-")[0].equals(entry[BASE])) {
                    baseField.setSelectedItem(name);
                }
            }
            saveBaseField();

            //Set the class list
            classList.removeAllItems();
            for (String path : classes){
                classList.addItem(formatClassName(path));
            }
        }
    }

    public void run() {
        if (config.getProperty("alg0") == null) { //If the feedbackHistory graph does not exist, add it
            config.setProperty("alg0", FB + ",alg0," + FB + ",true," + NO_BASE);
        }

        //Load the algorithm properties from the properties file
        algs.clear();
        for (int i = 0; i < MAX_ALGS; i++) {
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
        for (String key : config.getKeys()){
            if (key.startsWith("class") && !key.contains("Path")){
                classes.add(config.getProperty(key));
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
        if (baseName.equals(NO_BASE)) {
            entry[BASE] = NO_BASE;
        } else { //Set the entry's base to the key of the selected algorithm
            entry[BASE] = baseName.split("-")[0];
        }
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
        jLabel7 = new javax.swing.JLabel();
        classList = new javax.swing.JComboBox();
        addClassButton = new javax.swing.JButton();
        removeClassButton = new javax.swing.JButton();

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(addButton)
                                        .addGap(18, 18, 18)
                                        .addComponent(removeButton)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(displayField)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addGap(18, 18, 18)
                                                .addComponent(baseField, 0, 230, Short.MAX_VALUE))
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel5)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addGap(38, 38, 38)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                                                .addGap(11, 11, 11))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(88, 88, 88)
                                        .addComponent(typeField))))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(typeField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addComponent(jLabel5))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addButton)
                            .addComponent(removeButton))))
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
            updateFields();
        }
    }//GEN-LAST:event_algListValueChanged

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String[] options = new String[classes.size()];
        for (int i=0 ; i<classes.size() ; i++){
            options[i] = formatClassName(classes.get(i));
        }
        String path = null;
        String temp = ChatterBox.selectionPane("Question", "Which class would you like to load?", options);
        for (int i=0 ; i<options.length ; i++){
            if (options[i].equals(temp)){
                path = classes.get(i);
                break;
            }
        }
        if (path == null){
            return;
        }
        Object o = null;
        if (path.contains("!")){ //If it is a jar
            File file = new File(path.split("!")[0]);
            String name = path.split("!")[1];
            o = BitStylus.classInstance( (Class) BitStylus.loadJarClass(file, name)[0]);
        }else{//Otherwise, it must be a class
            o = BitStylus.classInstance(BitStylus.loadClass(new File(path)));
        }

        String type = null;
        if (o instanceof ReputationAlgorithm){
            type = REP;
        }else if (o instanceof TrustAlgorithm){
            type = TRUST;
        }else{
            ChatterBox.debug(this, "addButtonActionperformed()", "The class was not a supported algorithm");
            return;
        }

        String newKey = getNewKey("alg");
        String string = o.getClass().getSimpleName() + "," + newKey + "," + type + "," + FALSE + "," + NO_BASE + "," + path;
        String[] entry = string.split(",");
        entry[NAME] = newKey + "-" + entry[NAME];
        algs.add(entry);
        config.setProperty(newKey, string);
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
            if (!config.removeProperty("" + index)) {
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
        if (file == null) {
            return;
        }
        config.setProperty(CLASS_PATH, file.getParent());

        //Load the object
        Object o = null;
        try{
            if (file.getPath().endsWith(".class")){  //If it's a class file
                o = BitStylus.classInstance(BitStylus.loadClass(file));
                config.setProperty(getNewKey("class"), file.getPath());
                classes.add(file.getPath());
            }
            else{ //Otherwise, it's a jar file.  This is ensured by the file filter
                Object[] temp = BitStylus.loadJarClass(file);
                o = BitStylus.classInstance((Class) temp[0]);
                String name = (String) temp[1];
                config.setProperty(getNewKey("class"), file.getPath() + "!" + name);
                classes.add(file.getPath() + "!" + name);
            }
        }catch(NullPointerException ex){
            ex.printStackTrace();
        }
        
        if (!(o instanceof ReputationAlgorithm) && !(o instanceof TrustAlgorithm)){
            ChatterBox.debug(this, "addClassButtonActionperformed()", "The file was not a recognized algorithm.");
        }

        updateFields();
        classList.setSelectedIndex(classList.getItemCount() - 1);
    }//GEN-LAST:event_addClassButtonActionPerformed

    private void removeClassButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeClassButtonActionPerformed
        if (ChatterBox.yesNoDialog("Are you sure you want to remove this class?")){
            int index = classList.getSelectedIndex();
            classes.remove(index);
            config.removeProperty("class" + index);
            classList.removeItemAt(index);
            
            //TODO remove algorithms that depend on this
        }
    }//GEN-LAST:event_removeClassButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addClassButton;
    private javax.swing.JList algList;
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox baseField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox classList;
    private javax.swing.JCheckBox displayField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField pathField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeClassButton;
    private javax.swing.JLabel typeField;
    private javax.swing.JTextPane userField;
    // End of variables declaration//GEN-END:variables
}
////////////////////////////////////////////////////////////////////////////////

