--------------------------------------------------------------------------------
------------------------------TrustGrapher ReadMe-------------------------------
----------------------------Written by Andrew O'Hara----------------------------
--------------------------------------------------------------------------------

-----------------------
0.0.0 Table of Contents
-----------------------

1.0.0 Description

2.0.0 Usage Instructions
    2.1.0 Running the program
    2.2.0 Start Screen
        2.2.1 The Menu Bar
    2.3.0 Loading Algorithms
        2.3.1 Loading a log file
        2.3.2 Loading a Class
        2.3.3 Removing a class
        2.3.4 Adding an Algorithm
        2.3.5 Remving an algorithm
        2.3.6 Configuring an Algorithm
        2.3.7 TrustGrapher Properties File
    2.4.0 Running the Simulator
        2.4.1 The Playback Panel
        2.4.2 Right-Clicking in a Viewer Window

3.0.0 Code Maintenance Explanation

4.0.0 ChangeLog

5.0.0 ToDo List
    4.1.0 High Priority
    4.2.0 Low Priority

--------------------------------------------------------------------------------

-----------------
1.0.0 Description
-----------------

This is a playable simulator that simulates a series of feedback events from an .arff log
The simulator supports a FeedbackHistory graph, reputation algorithm graphs, and trust algorithm graphs
Reputation and Trust algorithms can be loaded from the algorithm loader window

The main purpose of this simulator is to test trust algorithms, and see if and how they can be cheated or "broken"

--------------------------------------------------------------------------------

------------------------
2.0.0 Usage Instructions
------------------------

    -------------------------
    2.1.0 Running The Program
    -------------------------
    To run TrustGrapher, the contents of the zip folder must be extracted to any folder on the computer
    The .jar file and the lib directory and its contents are needed to run the program

    ------------------
    2.2.0 Start Screen
    ------------------
    You will start at a blank screen with no graph viewers.
    You will need to open the file menu, and click on "Load Algorithms" to begin loading algorithms

        ------------------
        2.2.1 The Menu Bar
        ------------------
        File > Load Algorithms
            Opens the Algorithm Loader Window.  See 2.3.0 for more details
        File > Exit
            Exits the program

        View > Tabbed View
            The default view.  Keeps all viewer windows in a tabbed pane
        View > Grid View
            Separates the graphs panel into a 2 x 3 grid
        View > Toggle Log Table
            Shows/hides a table which displays the current log events.  As the simulator plays, events that have occurred are highlighted
            

    ------------------------
    2.3.0 Loading Algorithms
    ------------------------
    The algorithm configurations screen allows you import .class and .jar files that contain compatible algorithmsm, as well as to configure algorithms
    It is important to know the difference between adding and removing a class and an algorithm
    A class is treated as a reference to the the file that contains an algorithm
    An algorithm is what is represented by the methods of a class file.  You must first load a class, before you can add an algorithm that uses that class
    Each algorithm can be configured by choosing a base algorithm, whether or not it is to be displayed in a graph, and what properties it has
    The base of an algorithm is another algorithm that notifies an algorithm of any changes made to it
    
    The feedback history algorithm is already there, and cannot be removed

        ------------------------
        2.3.1 Loading a Log File
        ------------------------
        To load a feedback log file into the simulator, click the choose log button on the top-right of the algorithm loader window.
        You can then choose a valid .arff file
        An .arff file is compatible providing the log events are after a @data tag, and there are no other characters after the last event line
        Each event line must be of the format <int assessorID>,<int assesseeID>,<double feedback>\n
        Due to limitations of the EigenTrust algorithm, the peer id's must start at 0, and have no gaps in between any of the id's

        ---------------------
        2.3.2 Loading A Class
        ---------------------
        To load a comptaible algorithm, click the Add button, next to the class combo box, and then choose a compatible .class or .jar file
        For an algorithm to be compatible, it must extend the ReputationAlgorithm or TrustAlgorithm class from cu.repsystestbed.algorithms
        It must also have a default constructor
        If you chose a .jar file, you must then manually type in fully qualified name of the algorithm that you want to load

        ----------------------
        2.3.3 Removing a Class
        ----------------------
        To remove a class file, select the class that you want to remove in the class combo box, then click the Remove button to the right
        You will have to confirm your choice
        For a class to be elegible for removal, no algorithms are allowed to use it

        -------------------------
        2.3.4 Adding an Algorithm
        -------------------------
        If you have loaded an algorithm's class file, you can then click the Add button below the algorithm list
        This will open a selection pane where you can select any algorithm that you have loaded
        You can only add an algorithm if you have previously added a valid base for it (ex. EigenTrust is a base for RankbasedTrustAlg)
        you can only have 12 algorithms (excluding the feedbackHistory) added at once

        ---------------------------
        2.3.5 Removing an Algorithm
        ---------------------------
        To remove an algorithm first select the one that you want to remove from the algorithm list, then click the remove button below
        You cannot remove an algorithm that is the base of another

        ------------------------------
        2.3.6 Configuring an Algorithm
        ------------------------------
        Once you select an algorithm from the algorithm list, the fields to the right will update to reflect the configurations of that algorithm
        You can change:
            Whether or not the algorithm is displayed as a graph
            The base of the algorithm
            The properties file that contains the instructions on how to use the algorithm and its default values
                An algorithm doesn't always need to have a properties file to work

        ----------------------------------
        2.3.7 TrustGrapher Properties File
        ----------------------------------
        This program saves TrustGrapher.properties to your home directory
        if you delete this file, it will be remade the next time you run the program or click the Ok and Apply buttons in the algorithm loader window
        if you delete the file while the program is running, your configurations will only be lost if you exit the program without first clicking the Ok and Apply buttons in the algorithm loader window
        It contains:
            the feedback log that you have loaded
            The classes that you have loaded and the last directory that you found a class in
            The algorithms that you have added and their configurations
            the view mode of the simulator.  You can only have 6 enabled at one time
        This properties file exists so that when you run the program again, all of your configurations will be loaded and so you don't have to navigate through the entire filesystem to get to the same directory that you were at before
        This properties file does not contain the properties contained in the algorithm properties files, only their file paths

    ---------------------------
    2.4.0 Running the Simulator
    ---------------------------

        ------------------------
        2.4.1 The Playback Panel
        ------------------------
        This panel is used to control the playback of the graph
        || Pauses playback
        |> Plays the graph forward
        <| PLays the graph backwards
        |>|> FastForwards the graph
        <|<| Rewinds the graph

        the Quick Playback Speed slider changes the speed that  the player fastForwards or rewinds at
        You can manually scrub the large slider at the bottom to change the place in the timeline

        ---------------------------------------
        2.4.2 Right-clicking in a Viewer Window
        ---------------------------------------
        Right clicking in a viewer window opens a popup menu.
        Mouse Mode: > Picking
            This allows you to select edges and vertices and drag to move vertices
        Mouse Mode: > Transforming
            The default mouse mode.  Dragging moves the graph around in the viewer window

        There are also 4 layouts that you can choose from:
            Circle
            FR
            ISO
            KK

    --------------------------------------
    2.5.0 Algorithm Types in the Simulator
    --------------------------------------

        ----------------------
        2.5.1 Feedback History
        ----------------------
        Each directed edge represents at least one feedback passing from one agent to the other
        Feedback values are displayed next to the line.  Multiple feedbacks are seperated by commas

        ----------------
        2.5.2 Reputation
        ----------------
        Each directed edge represents reputation one peer feels for the other
        The degree of the representation is displayed next to the edge

        -----------
        2.5.3 Trust
        -----------
        Each directed edge indicates that one peer trusts the other

--------------------------------------------------------------------------------

----------------------------------
3.0.0 Code Maintenance Explanation
----------------------------------

--------------------------------------------------------------------------------

---------------
3.0.0 ChangeLog
---------------

    Revision 26
Trust graph works

    Revision 27
Minor revisions before I add multiple view support

    Revision 28
Now supports multiple views on screen

    Revision 29
Added possible fix for mac right-clicking.  Needs to be tested
Finished Algorithm Configuration GUI.  Not yet fully integrated with TrustApplet
TrustApplet will load the visible algorithms specified by the configuration window.
TrustApplet will not yet load external class files

    Revision 30
Added file filters for configuration window
Integrated TrustGraphLoader with Configure and BitStylus

    Revision 31
Fixed a bug where the base was not being saved properly on the options window
The graphs specified by the options window are now loaded
Now loads external algorithms

    Revision 32
Now supports switching between tabbed and grid views
Now loads external jars.  This will require a lot of fine-tuning.  The implementation is very sloppy

    Revision 33
Added some idiot-proofing for the algorithm loader
Fixed remove algorithm button for algorithm loader
Refactoring and code optimization
An algorithm can now have a property file attached to it.  This is not yet implemented in the simulation however
The Property buttons and base field now disable when feedback history is selected
Remove class now works
Fixed a bug that did not allow you to load the maximum amount of algorithms at startup

    Revision 34
Removed the timestamps from the log window since they are irrelevant
The Log Window now toggles on and off
A class from a .jar will now display the same was a one from a .class
Removing a class will now remove the correct class from the properties file
This is as far as I can go with the major additions/changes until Parthy gives me the new algorithms

    Revision 35
Tweaked the Loading Bar and optimized some code
Removed Spring Layout since there was a bug where it invoked graph methods incompatible with the JGrapht graph
Commit prior to AlgorithmLoader property rewrite

    Revision 36
The PropertyManager class is now a Properties extension rather than a wrapper.  The constructor still takes care of loading an existing properties file for you
Updated My Java Library to make it more robust
Massive revision to algorithm loader to remove two heavily used fields in favor of the properties file
Fixed a bug where the loading bar would not dissappear if another graph was loaded, or the view was changed while the graph was playing
various code cleanups
Beginning to do thorough code documentation

    Revision 37
Fixed a bug where the class wasn't always removed, or the wrong one was being removed
The log table now persists after loading a new graph
The loading bar bug came back.  I have removed the loading bar from the log table builder to remove this
Now uses a new loading bar from My Java Library
The trustGrapher package is now in the cu package
ReadMe instructions done... I think
More documentation
Fixed a bug where the right-click menu items were not being highlighted

    Revision 38
Major Code cleanup and refactoring
Embedded loading bar into playback panel
The log reader now runs in the background thread to prevent the EDT thread from locking

    Revision 39
Changed the event list to an ArrayList rather than a Linked List.  This greatly increased playspeed of the original program, but it may not help as much here or at all
Massive optimizations to graphs for increased efficiency and minimization of "ripples"
Fixed a possible bug where feedback not being completely removed when rewinding may have adversely affected the reputation algorithm when going back forward

    Revision 40
The algorithms attached to the full graphs were unnecessary, and have been removed
Massive increase in log loading speed
Non-Feedback Graph Backward Events are now much more efficient
Made the tabbed view playback faster by only repainting the selected viewer
More major code refactoring.  This caused a bunch of awesome new bugs, but I fixed the ones I know about!
Fixed an annoyingly persistent bug that caused the graph to jerk for a few seconds when switching from grid view to tabbed view
More documentation (Makes my head explode!)

--------------------------------------------------------------------------------

---------------
4.0.0 ToDo List
---------------

    -------------------
    4.1.0 High Priority
    -------------------
    Implement speed hacks (maybe, If there are this many vertices, skip a few alg checks) (Add an option  for it)

    ------------------
    4.2.0 Low Priority
    ------------------
    Maybe make algorithm class for AlgorithmLoader
    Move the algorithm loader content pane to the graphsPanel when no graphs are loaded yet
    Have the properties file save next to the jar
    Add graphic buttons for the playback bar
    Integrate libraries into jar  (This is a NetBeans thing.  May have to make custom ant file.  dunno how to do that)

--------------------------------------------------------------------------------