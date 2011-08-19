////////////////////////////////AlgorithmLoader/////////////////////////////////
package cu.trustGrapher.loading;

import cu.trustGrapher.TrustGrapher;
import java.io.File;
import javax.swing.border.TitledBorder;

import java.util.ArrayList;
import java.util.List;
import aohara.utilities.*;

/**
 * An options window which allows the user to choose which algorithms to load, and which graphs to display.
 * The session properties of the graphs are stored in GraphConfig objects.
 * @author Andrew O'Hara
 */
public class AlgorithmLoader extends javax.swing.JFrame {

    public static final int MAX_GRAPHS = 13, MAX_VISIBLE = 6;
    public static final String GRAPH = "graph", CLASS = "class";
    public static final String LOG_PATH = "logPath", CLASS_PATH = "classPath", PROPERTY_PATH = "propertyPath";
    private PropertyManager config;
    private TrustGrapher trustGrapher;
    private List<GraphConfig> graphConfigs;

//////////////////////////////////Constructor///////////////////////////////////
    /**
     * Initializes the algorithm loader components
     * @param trustGrapher The main class.  Needed to inform it that the graphs have been loaded
     * @param config The properties manager to load all of the class and graph properties
     */
    public AlgorithmLoader(TrustGrapher trustGrapher, PropertyManager config) {
        this.trustGrapher = trustGrapher;
        this.config = config;
        initComponents();
    }

//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * Gets an unused property key that can be used to store a new graphs or class.
     * This is useful to ensure that keys do not conflict.
     * If a key was deleted, this will return an old key to be reused.
     * @param type A String which contains the type of key to generate.  Ex: "graph" , "class"
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

    /**
     * @return  Returns the index of the selected graph in the baseField
     */
    private int getBaseIndex() {
        return Integer.parseInt(((String) baseField.getSelectedItem()).split("-")[0]);
    }

    /**
     * @return Returns the GraphConfig of the graph selected in the graph list
     */
    private GraphConfig getSelectedGraph() {
        String temp = ((String) graphList.getSelectedValue()).split("-")[0];
        return getGraphConfig(Integer.parseInt(temp));
    }

    /**
     * @return The list of GraphConfigs
     */
    public List<GraphConfig> getGraphConfigs() {
        return graphConfigs;
    }

    /**
     * Returns the GraphConfig with the specified index.
     * It is necessary to find them with this method as the index of the GraphConfig
     * is not necessarily equal to its index in the list.
     * @param i the index of the GraphConfig to find
     * @return the GraphConfig with the specified index
     */
    private GraphConfig getGraphConfig(int i) {
        for (GraphConfig graphConfig : graphConfigs) {
            if (i == graphConfig.getIndex()) {
                return graphConfig;
            }
        }
        return null;
    }

    /**
     * Gets the display names of all of the graphs contained in the GraphConfigs
     * @return all of the graph display names
     */
    private Object[] getGraphDisplayNames() {
        ArrayList<String> names = new ArrayList<String>(MAX_GRAPHS);
        for (GraphConfig graphConfig : graphConfigs) {
            names.add(graphConfig.getDisplayName());
        }
        return names.toArray();
    }

    /**
     * @return true if there is a Reputation Graph added
     */
    private boolean hasRepGraph() {
        for (GraphConfig graphConfig : graphConfigs) {
            if (graphConfig != null) {
                if (graphConfig.isReputationGraph()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return Returns the number of graphs that are set to visible
     */
    public int getVisibleGraphCount(){
        int count = 0;
        for (GraphConfig graphConfig : graphConfigs){
            if (graphConfig.isDisplayed()){
                count++;
            }
        }
        return count;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * If adding another graphConfig would not go over the graph limit, add it to the list
     * @param graphConfig The GraphConfig to add
     * @return Whether or not the GraphConfig was added
     */
    private boolean addGraphConfig(GraphConfig graphConfig) {
        if (graphConfigs.size() >= MAX_GRAPHS) {
            ChatterBox.alert("Cannot have more than " + MAX_GRAPHS + " graphs at one time.");
            return false;
        }
        graphConfigs.add(graphConfig);
        return true;
    }

    private boolean addNewGraph(int index, boolean display, int classIndex) {
        String classPath = (classIndex != -1) ? config.getProperty(CLASS + classIndex) : null;
        GraphConfig graphConfig = new GraphConfig(index, display, -1, classIndex, classPath, null);
        if (graphConfig.isTrustGraph() && !hasRepGraph()) {
            ChatterBox.alert("There must be an existing Reputation Graph\nbefore you can add a Trust Graph.");
            return false;
        }
        return addGraphConfig(graphConfig);
    }

    /**
     *
     * @param index
     * @param property { display, baseIndex, classFile, configFile
     * @return
     */
    private boolean addGraphFromProperty(int index, String[] property) {
        int baseIndex = -1;
        int classIndex = -1;
        try {
            baseIndex = Integer.parseInt(property[1]);
        } catch (NumberFormatException ex) {
        }
        try {
            classIndex = Integer.parseInt(property[2]);
        } catch (NumberFormatException ex) {
        }
        String classPath = (classIndex == -1) ? null : config.getProperty(CLASS + classIndex);
        //Graph property format                                      display                    base    classIndex  classPath      properties
        GraphConfig graphConfig  = new GraphConfig(index, Boolean.parseBoolean(property[0]), baseIndex, classIndex, classPath, new File(property[3]));
        return addGraphConfig(graphConfig);
    }

    /**
     * Whenever an graphList value is changed, this is called.
     * It updates all of the components to indicate the current selection or change that was made
     */
    private void updateFields() {
        GraphConfig graph = getSelectedGraph();
        if (graph != null) { //If a graph is selected
            ((TitledBorder) graphPanel.getBorder()).setTitle(graph.getDisplayName());
            displayField.setSelected(graph.isDisplayed()); //Set the Display JCheckBox

            propertiesField.setText(graph.getProperties() != null ? graph.getProperties().getName() : ""); //Set the properties file
            //If the feedbackHistory is selected, disable the properties buttons
            choosePropertiesButton.setEnabled(graph.isFeedbackGraph() ? false : true);
            removePropertyButton.setEnabled(graph.isFeedbackGraph() ? false : true);

            //Set the Base List
            baseField.setEnabled(false);
            baseField.removeAllItems();
            if (graph.isFeedbackGraph()) { //If the graphs is the feedbackHistory
                baseField.addItem(GraphConfig.NO_BASE);
                baseField.setSelectedIndex(0);
            } else if (graph.isReputationGraph()) { //if the graphs is a reputation graph
                baseField.addItem(getGraphConfig(0).getDisplayName());
                baseField.setSelectedIndex(0);
            } else { //Otherwise, it must use be a trust graph
                for (GraphConfig a : graphConfigs) { //Add All reputation graphs to the base list
                    if (a.isReputationGraph()) {
                        baseField.addItem(a.getDisplayName());
                        if (graph.getBase() != -1) {
                            if (graph.getBase() == a.getIndex()) { //If the current graph listens to this graph, select it
                                baseField.setSelectedItem(a.getDisplayName());
                            }
                        }
                    }
                }
            }
            if (((String) baseField.getSelectedItem()).equals(GraphConfig.NO_BASE)) {
                graph.setBase(-1);
            } else {
                graph.setBase(getBaseIndex());
            }
            //If the selected graph uses a trust algorithm, enable the base field
            baseField.setEnabled(graph.isTrustGraph() ? true : false);

            //Set the class list
            classList.removeAllItems();
            for (int i = 0; i < config.keySet().size(); i++) {
                if (config.containsKey(CLASS + i)) {
                    classList.addItem(TrustClassLoader.formatClassName(i, config.getProperty(CLASS + i)));
                }
            }
            config.setProperty(graph.getKey(), graph.toString());
        }
    }

    /**
     * This is called by the TrustGrapher.  It displays the algorithm loader window,
     * loads all of the properties from the properties file, and then updates the window
     */
    public void start() {
        graphConfigs = new ArrayList<GraphConfig>();
        config.loadPropertyFile();

        if (!config.containsKey(GRAPH + 0)) { //If the feedbackHistory graph does not exist in the properties, add it
            addNewGraph(0, true, -1);
        }
        //Add the rest of the graphs
        for (int i = 0; i <= config.keySet().size(); i++) {
            if (config.containsKey(GRAPH + i)) {
                addGraphFromProperty(i, config.getProperty(GRAPH + i).split(","));
            }
        }

        //Set the path field to the last log
        String logPath = config.getProperty(LOG_PATH);
        if (logPath != null) {
            pathField.setText(logPath);
        }
        graphList.setListData(getGraphDisplayNames()); //Set the graphList
        graphList.setSelectedIndex(0);
        updateFields();
        setVisible(true);
    }

    public static void run(TrustGrapher trustGrapher, PropertyManager properties) {
        AlgorithmLoader algorithmLoader = new AlgorithmLoader(trustGrapher, properties);
        algorithmLoader.start();
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
        helpButton = new javax.swing.JButton();
        javax.swing.JPanel algorithmListPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        graphList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        graphPanel = new javax.swing.JPanel();
        displayField = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        baseField = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        propertiesField = new javax.swing.JTextField();
        choosePropertiesButton = new javax.swing.JButton();
        removePropertyButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        pathField = new javax.swing.JTextField();
        loadButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        classList = new javax.swing.JComboBox();
        addClassButton = new javax.swing.JButton();
        removeClassButton = new javax.swing.JButton();

        setTitle("Algorithm Configuration");
        setBackground(new java.awt.Color(39, 31, 24));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(new java.awt.Color(254, 254, 254));
        setResizable(false);

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

        helpButton.setText("Help");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        algorithmListPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph List", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N

        graphList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        graphList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                graphListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(graphList);

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

        javax.swing.GroupLayout algorithmListPanelLayout = new javax.swing.GroupLayout(algorithmListPanel);
        algorithmListPanel.setLayout(algorithmListPanelLayout);
        algorithmListPanelLayout.setHorizontalGroup(
            algorithmListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(algorithmListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addGroup(algorithmListPanelLayout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                        .addComponent(removeButton)))
                .addContainerGap())
        );
        algorithmListPanelLayout.setVerticalGroup(
            algorithmListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(algorithmListPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(algorithmListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        graphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 14))); // NOI18N

        displayField.setText("Display Graph");
        displayField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayFieldActionPerformed(evt);
            }
        });

        jLabel2.setBackground(new java.awt.Color(52, 52, 52));
        jLabel2.setText("Listens to:");

        baseField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Maximum 12 algorithms at any time.");

        jLabel5.setText("Maximum 6 graphs displayed at any time.");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithm Properties"));

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertiesField, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(choosePropertiesButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removePropertyButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(propertiesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(removePropertyButton)
                    .addComponent(choosePropertiesButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(graphPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(displayField)
                    .addGroup(graphPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(303, 303, 303))
                    .addGroup(graphPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addContainerGap(50, Short.MAX_VALUE))
                    .addGroup(graphPanelLayout.createSequentialGroup()
                        .addGroup(graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, graphPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(baseField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(graphPanelLayout.createSequentialGroup()
                .addComponent(displayField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(baseField, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel6.setText("Feedback Log:");

        pathField.setEditable(false);

        loadButton.setText("Choose Log");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Algorithms:");

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(classList, 0, 246, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addClassButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeClassButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathField, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(loadButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(classList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeClassButton)
                    .addComponent(addClassButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(helpButton)
                        .addGap(185, 185, 185)
                        .addComponent(applyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(algorithmListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(graphPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(algorithmListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(helpButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                    .addComponent(applyButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void graphListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_graphListValueChanged
        if (graphList.getSelectedIndex() != -1) {
            updateFields();
        }
    }//GEN-LAST:event_graphListValueChanged

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        //Generate array of graph types to load
        ArrayList<String> classPaths = new ArrayList<String>();
        for (int i = 0; i <= MAX_GRAPHS; i++) {
            if (config.containsKey(CLASS + i)) {
                classPaths.add(TrustClassLoader.formatClassName(i, config.getProperty(CLASS + i)));
            }
        }
        //Ask which type of graph to add
        if (graphConfigs.size() < MAX_GRAPHS) {
            String selectedClassName = ChatterBox.selectionPane("Question", "Which graph type would you like to load?", classPaths.toArray());
            if (selectedClassName != null) {
                int newKey = getNewKey(GRAPH);
                addNewGraph(newKey, false, classPaths.indexOf((Object) selectedClassName));
                config.setProperty(GRAPH + newKey, getGraphConfig(newKey).toString());

                //Add the info to the loader and window
                graphList.setListData(getGraphDisplayNames());
                graphList.setSelectedIndex(graphList.getLastVisibleIndex());
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int index = getSelectedGraph().getIndex();
        if (index == 0) {
            ChatterBox.alert("You cannot remove the FeedbackHistory.");
        } else if (index != -1) {
            //Check if this graph is being used by another
            for (GraphConfig graphConfig : graphConfigs) {
                if (graphConfig.getBase() != -1) {
                    if (graphConfig.getBase() == index) {
                        ChatterBox.alert("You cannot remove a graph that is used by another.");
                        return;
                    }
                }
            }

            config.remove(GRAPH + index); //Remove the GraphConfig from the proeprties
            for (int i = 0 ; i < graphConfigs.size() ; i++) { //remove the GraphConfig from the list
                if (index == graphConfigs.get(i).getIndex()) {
                    graphConfigs.remove(graphConfigs.get(i));
                }
            }
            graphList.setListData(getGraphDisplayNames()); //reset the graph list
            graphList.setSelectedIndex(graphList.getLastVisibleIndex());
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        config.save();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        config.save();
        dispose();
        if (getVisibleGraphCount() > 0) {
            trustGrapher.algorithmsLoaded(graphConfigs);
        } else {
            ChatterBox.alert("No graphs are displayed, so no action was done.");
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void displayFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayFieldActionPerformed
        if (graphList.getSelectedIndex() != -1 && isVisible()) {
            if (displayField.isSelected()) {
                if (getVisibleGraphCount() >= MAX_VISIBLE) {
                    displayField.setSelected(false);
                    ChatterBox.alert("You cannot have more than " + MAX_VISIBLE + " visible graphs at one time.");
                } else {
                    GraphConfig graphConfig = getSelectedGraph();
                    graphConfig.setDisplay(displayField.isSelected());
                    config.setProperty(graphConfig.getKey(), graphConfig.toString());
                }
            }
        }
    }//GEN-LAST:event_displayFieldActionPerformed

    private void baseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseFieldActionPerformed
        //This is called when the selected graph's base is changed from the options window
        if (baseField.isEnabled() && baseField.getSelectedIndex() != -1 && isVisible()) {
            GraphConfig graphConfig = getSelectedGraph();
            graphConfig.setBase(getBaseIndex());
            config.setProperty(graphConfig.getKey(), graphConfig.toString());
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

        //see if any graphs depend on this class.  If so, the class cannot be deleted
        for (GraphConfig graphCOnfig : graphConfigs) {
            if (graphCOnfig.getClassFile() != null) {
                if (graphCOnfig.getClassFile().getPath().equals(config.getProperty(CLASS + index))) {
                    ChatterBox.alert("This class cannot be removed since it has graphs that depend on it.");
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
            GraphConfig graphConfig = getSelectedGraph();
            graphConfig.setProperties(propertyFile);
            config.setProperty(graphConfig.getKey(), graphConfig.toString());
        }
    }//GEN-LAST:event_choosePropertiesButtonActionPerformed

    private void removePropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePropertyButtonActionPerformed
        propertiesField.setText("");
        getSelectedGraph().setProperties(null);
    }//GEN-LAST:event_removePropertyButtonActionPerformed

private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
    String message = "To use this configuration window:\n\n"
            + "You must first add a class path to the algorithms combo box.\nClick the 'Add' Button next to it to do so.\n\n"
            + "Then you can add a graph to the simulator with the 'Add' Button below the 'Graph list'.\n"
            + "You can only add graphs that have had their class path added already.\n\n"
            + "You can then select an individual graphs from the list and change its' properties in the panel to the right.\n"
            + "The 'Listens to' combo box means that the currently selected graph's algorithm will listen to that graph";
    ChatterBox.alert(message);
}//GEN-LAST:event_helpButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addClassButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox baseField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton choosePropertiesButton;
    private javax.swing.JComboBox classList;
    private javax.swing.JCheckBox displayField;
    private javax.swing.JList graphList;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField pathField;
    private javax.swing.JTextField propertiesField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeClassButton;
    private javax.swing.JButton removePropertyButton;
    // End of variables declaration//GEN-END:variables
}
////////////////////////////////////////////////////////////////////////////////

