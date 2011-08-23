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
        2.2.1 The File Menu
		2.2.2 The Edit Menu
		2.2.3 The View Menu
    2.3.0 Loading Graphs
        2.3.1 Loading a log file
        2.3.2 Adding an Algorithm Path
        2.3.3 Removing an Algorithm Path
        2.3.4 Adding a Graph
        2.3.5 Remving a Graph
        2.3.6 Configuring a Graph
    2.4.0 Running the Simulator
        2.4.1 The Playback Panel
		2.4.2 The Event Panel
        2.4.3 Right-Clicking in a Viewer Window

3.0.0 ChangeLog

4.0.0 ToDo List
    4.1.0 High Priority
    4.2.0 Low Priority

--------------------------------------------------------------------------------

-----------------
1.0.0 Description
-----------------

This is a playable simulator that simulates a series of feedback events from an .arff log file.
The simulator supports a FeedbackHistory graph, reputation algorithm graphs, and trust algorithm graphs.
Reputation and Trust algorithms can be loaded from the algorithm loader window

The main purpose of this simulator is to test trust algorithms, and see if and how they can be cheated or "broken".

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
    You will start at a blank screen, and the AlgorithmLoader Windows will pop up.
	You can then configure the algorithms and graph that you wish to include in the simulation.
	Your last configuration will automatically be loaded.

        ------------------
        2.2.1 The File Menu
        ------------------
        File > Load Algorithms
            Opens the Algorithm Loader Window.  See 2.3.0 for more details
		File > Save Log File
			Saves the events currently loaded into the Event Player to an .arff log file
		File > Export Results
			This is not yet implemented.  Go to cu.trustGrapher.TrustMenuBar, then search 'Implement export results'.
        File > Exit
            Exits the program

		-------------------
		2.2.2 The Edit Menu
		-------------------
		Edit > Insert Event After
			Opens a Dialog Box which allows you to create a new TrustLogEvent and place it after the currently selected event in the timeline
		Edit > Modify Event
			Opens a Dialog Box which allows you to modify the currently selected event in the timeline
		Edit > Remove Event
			Removes the currently seelected event in the timeline from the EventPlayer
		Edit > Options
			Opens a window which allows you to set the timelineSlider scrub mode and delay between EventPlayer ticks

		-------------------
		2.2.3 The View Menu
		-------------------
        View > Tabbed View
            The default view.  Keeps all viewer windows in a tabbed pane
        View > Grid View
            Separates the graphs panel into a 2 x 3 grid
        View > Display Playback Panel
            Shows/hides a panel which displays the EventPlayer controls and timelineSlider.
		View > Display Event Panel
            Shows/hides a panel which displays the events currently in the EventPlayer.  As the simulator plays, the selected event is highlighted

    --------------------
    2.3.0 Loading Graphs
    --------------------
    The algorithm configurations screen allows you import .class and .jar files that contain compatible algorithms, as well as to configure graphs.
    It is important to know the difference between adding and removing an algorithm and a graph.
    An algorithm is treated as a reference to the the file that contains the algorithm
    A graph is what contains the algorithm and all of the entities, and may be displayed in a GraphViewer in the Simulator.
	You must first load an algorithm before you can add a graph that uses that algortihm.
    Each graph can be configured by choosing a base graph, whether or not it is displayed in the simulator, and by choosing its configuration properties.
    The base of a graph is held by the graph's algorithm.  When the algortihm runs, it updates its own graph based on the entities in the base graph.
    The feedback history algorithm is already there, and cannot be removed.

        ------------------------
        2.3.1 Loading a Log File
        ------------------------
        To load a feedback log file into the simulator, click the choose log button on the top-right of the AlgorithmLoader window.
        You can then choose a valid .arff file.
        An .arff file is compatible providing the log events are after a @data tag, and there are no other characters after the last event line
        Each event line must be of the format <int assessorID>,<int assesseeID>,<double feedback>\n
        Due to limitations of the EigenTrust algorithm, the peer id's must start at 0, and have no gaps in between any of the id's

        -------------------------------
        2.3.2 Loading an Algorithm Path
        -------------------------------
        To load a comptaible algorithm, click the Add button next to the Algorithm combo box, and then choose a compatible .class or .jar file
        For an algorithm to be compatible, it must extend the ReputationAlgorithm or TrustAlgorithm class from cu.repsystestbed.algorithms.
        It must also have a default constructor.
        If you chose a .jar file, you must then manually type in the fully qualified name of the algorithm that you want to load.

        --------------------------------
        2.3.3 Removing an Algorithm Path
        --------------------------------
        To remove an algorithm path, select the algorithm that you want to remove in the algorithm combo box, then click the Remove button to the right
        You will have to confirm your choice.
        For a class to be elegible for removal, it cannot be used by any loaded graphs.

        --------------------
        2.3.4 Adding a Graph
        --------------------
        If you have loaded an algorithm, you can then click the Add button below the graph list.
        This will open a selection pane where you can select any algorithm that you have loaded.
        You can only add a graph if you have previously added a valid base for it (ex. EigenTrust is a base for RankbasedTrustAlg)
        you can only have 13 algorithms (including the feedbackHistory) added at once.

        ----------------------
        2.3.5 Removing a Graph
        ----------------------
        To remove a graph, first select the one that you want to remove from the graph list, then click the remove button below.
        You cannot remove a graph that is the base of another.

        -------------------------
        2.3.6 Configuring a Graph
        -------------------------
        Once you select a graph from the graph list, the fields to the right will update to reflect the configurations of that algorithm
        You can change:
            Whether or not the graph is displayed
            The base of the algorithm
            The properties file that contains the instructions on how to use the algorithm and its default values
                An algorithm doesn't always need to have a properties file to work

    ---------------------------
    2.4.0 Running the Simulator
    ---------------------------

        ------------------------
        2.4.1 The Playback Panel
        ------------------------
        This panel is used to control the playback of the graph
        || Pauses playback
        |> Plays the graph forward
        <| PLays the graph backward

        The Events per Tick Slider changes the number of events that are processed after each EventPlayer tick.
		This is the most efficient method of increasing the playback speed as opposed to decreasing the tick delay.
        You can manually scrub or Drag & drop the large slider at the bottom to change the place in the timeline 
		depending on the options selected in the OptionsWindow.

		---------------------
		2.4.2 The Event Panel
		---------------------
		This panel can be used to navigate through the events, and to modify them.
		As the events are played, the row pertainig to the current event is highlighted.
		You can click on a row to instantly go to that event, or scrub it to play across the events at your own time.
		You can also right-click a row to get a popup menu similar to the edit menu.  See 2.2.2 for more info

        ---------------------------------------
        2.4.3 Right-clicking in a Viewer Window
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

    Revision 41
Right-click works on Windows platforms again
Massive Algorithm Loader rewrite to use Algorithm objects to keep track of algorithm configurations.  This has created several bugs.  Not all have been found and fixed yet.
The simulator properties file is now read every time the algorithm configuration window is opened
Added a small help box in the Algorithm Configuration Window and renamed the "Base Algorithm" label to "Depends on"

    Revision 42
Rewrote TrustLogEvent and TrustEventPlayer for increased stability and compatability with new and intended features
TrustGraphViewer Repaint is now much faster after optimizing the Entity toString methods
You can now select an event on the log table to move to that event in the simulator
    The event injector is a work in progress
Increased stability of rewritten algorithm loader
Fixed a new bug where the reputation values were incorrect
Fixed a bug where algorithm graphs no longer rewound properly
Fixed a bug where the graph would play in fast forward every time after you scrubbed the playback slider

    Revision 43
Added GraphManager class
Fixed a bug where the loading bar would not dissapear that was made much more common after optimizing log reader speed
Event injector almost working

    Revision 44
Event injection and removal works
The log table now shows by default, and its setting is saved in the TrustGrapher properties file
PlaybackPanel buttons now update properly after selecting event in log table
Increased stability of log table
Simulation is paused after the graph is started or reset
You can no longer remove the start event from the log table
Fixed a new bug where the playbackPanel would disappear after changing viewer tabs

    Revision 45
The log list panel is now sized appropriately
Fixed a bug where a new event was not added to the correct spot in the timeline
Fixed a bug where the algortihm check box did not work as intended.  This was only noticable when existing algorithms were loaded
FastForward works again
Scrubbing the playbackSlider works again
The playbackSlider now shows tick marks and labels

    Revision 46
Added 'Modify Event' button to log panel right-click menu
Added custom event delay option to file menu
You can now scrub the log panel
Removed fast forwarding and replaced it with "events per tick"
Removed tick marks and labels from playbackSlider
You can now edit events from the new edit menu
Added user confirmation for event removal
Added 'about' menu item
The event Panel now has a minimum size
The highlighted event in the event panel scroll pane is now always kept in view
Got rid of some unnecessary code and did some refactoring
Added some more documentation

    Revision 47
A new bug where the graph had no layout when it was loaded should be fixed
The events per tick slider value is now saved as a property
Added revision number to frame title

	Revision 48
Replaced the GraphManager with a list of SimGraphPairs
Added option to TrustGrapher Options window to change playback slider mode between scrub and drag & drop
Moved the entity display predicates to the GraphPair class
The Algorithm Loader window now runs on startup and the menu bar is disabled until it is loaded
When the view mode is changed, the log no longer needs to be reread
The playbackPanel buttons will now update after the playbackSlider has been released
If the program is run from a jar, it will now save the properties file next to it.  Otherwise, is will just be saved in the project root directory
Removed several unnecessary fields from TrustGrapher and simplified the initialization of the EventPlayerListeners
Javadocs

	Revision 49
Fixed a bug where a NullPointerException was thrown if the mouse was scrubbing the log table, and was released outside of the table
More Documentation

	Revision 50
New bug: I do not think the reputation values are accurate anymore.  I don't know how long this has been for
New Feature: You can now save the simulator's current event list as an .arff file.  This includes changes from inside the simulator
New Feature: There is not an option to toggle the playback panel in the view menu
Feature fix: Should now show that Agents no longer trust eachother due to new low feeedback values (untested)
Feature fix: You will no longer see Agents with no Edges in Trust Graphs.  An Agents will now only appear when it trusts another one
Bug Fix: If you add a new Agent to the graph through the event injector, the simulator will no longer crash when it attempts to add it
Refactor: The Simulator menu bar is now it's own class
Refactor: Merged forward and backward events with graphEvent method for SimGraphs.  They were too similar to keep separate
More Documentation

	Revision 51
New Bug Fix: The simulator view can be changed from grid to tabbed again
Bug Fix: Removing a graph from the AlgorithmLoader will now always remove the correct GraphConfig
New Feature: Updated the look of the options and algorithm loader windows to be cleaner
New Feature: When the otions button is pressed, if there is a simulation running, it will pause it
Refactor: Removed GraphPair class, added the fullGraph as a JungAdapterGraph parameter to the SimAbstractGraph
Refactor: Integrated the GraphConfigManager with the AlgorithmLoader.  It isn't as clean this way, but the other way wasn't necessary
More Documentation

	Revision 52
Finished documentation
Feature Change: If a new feedbackHistory graph is added to the properties file, its visibility will now be false by default
Refactor: Removed the TrustClassLoader class

	Release Version 1.0
Fixed a bug in the AlgorithmLoader where the display graph check box did not always work
Useless graphs are no longer created by the GraphLoader
The aohara.utilities package is now in the TrustGrapher project src directory.  It is no longer an external library
Added a Doxygen javaDoc to the Documentation directory.  Each class has its own graphical class diagram
Added screencast videos to the Documentation directory
The 'about' menu button is back
Updated readme instructions

--------------------------------------------------------------------------------

---------------
4.0.0 ToDo List
---------------

    -------------------
    4.1.0 High Priority
    -------------------
	None

    ------------------
    4.2.0 Low Priority
    ------------------
	None

--------------------------------------------------------------------------------
