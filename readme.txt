----------------------
TrustGrapher ChangeLog
----------------------

    Build 26
Trust graph works

    Build 27
Minor revisions before I add multiple view support

    Build 28
Now supports multiple views on screen

    Build 29
Added possible fix for mac right-clicking.  Needs to be tested
Finished Algorithm Configuration GUI.  Not yet fully integrated with TrustApplet
TrustApplet will load the visible algorithms specified by the configuration window.
TrustApplet will not yet load external class files

    Build 30
Added file filters for configuration window
Integrated TrustGraphLoader with Configure and BitStylus

    Build 31
Fixed a bug where the base was not being saved properly on the options window
The graphs specified by the options window are now loaded
Now loads external algorithms

    Build 32
Now supports switching between tabbed and grid views
Now loads external jars.  This will require a lot of fine-tuning.  The implementation is very sloppy

    Build 33
Added some idiot-proofing for the algorithm loader
Fixed remove algorithm button for algorithm loader
Refactoring and code optimization
An algorithm can now have a property file attached to it.  This is not yet implemented in the simulation however
The Property buttons and base field now disable when feedback history is selected
Remove class now works
Fixed a bug that did not allow you to load the maximum amount of algorithms at startup

    Build 34
Removed the timestamps from the log window since they are irrelevant
The Log Window now toggles on and off
A class from a .jar will now display the same was a one from a .class
Removing a class will now remove the correct class from the properties file
This is as far as I can go with the major additions/changes until Parthy gives me the new algorithms

    Build 35
Tweaked the Loading Bar and optimized some code
Removed Spring Layout since there was a bug where it invoked graph methods incompatible with the JGrapht graph
Commit prior to AlgorithmLoader property rewrite

    Build 36
The PropertyManager class is now a Properties extension rather than a wrapper.  The constructor still takes care of loading an exisitng properties file for you
Updated My Java Library to make it more robust
Massive revision to algorithm loader to remove two heavily used fields in favor of the properties file
Fixed a bug where the loading bar would not dissapear if another graph was loaded, or the view was changed while the graph was playing

-----
To Do
-----

    High Priority
JavaDocs!!!

    Low Priority
Add graphic buttons for the playback bar
Integrate libraries into jar  (This is a NetBeans thing.  May have to make custom ant file.  dunno how to do that)