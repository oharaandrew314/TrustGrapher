////////////////////////////////LogPanel//////////////////////////////////
package cu.trustGrapher.eventplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;

import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import aohara.utilities.ChatterBox;

/**
 * The Log Panel always exists when graphs are loaded, but it is only displayed when the check box option for it is checked.
 * It displays the TrustlogEvents that are loaded into the graph, and highlights the current event.
 * You can click a row to have the simulator go to the event represented by that row, or scrub the list.
 * You can right-click on an event to add, modify, or remove an event.
 * 
 * This class is just the panel and handlers.  TrustGrapher handles the placing and removal of the Panel, 
 * and EventPlayer tells it when to update itself only if EventPlayer adds it as a listener.
 * @author Andrew O'Hara
 */
public final class LogPanel extends JPanel implements EventPlayerListener {

    private JTable logTable; //The JTable containing an array representation of the TrustLogEvents
    private JPopupMenu menu; //The popupMenu that is shown when a row is right-clicked
    private EventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    public LogPanel(EventPlayer eventThread) {
        this.eventThread = eventThread;
        initComponents();
        setVisible(eventThread.getTrustGrapher().getTrustMenuBar().toggleEventPanelButton.isSelected());
    }
//////////////////////////////////Accessors/////////////////////////////////////
    /**
     * @return returns the eventPlayer that this is attached to
     */
    public EventPlayer getEventPlayer(){
        return eventThread;
    }

///////////////////////////////////Methods//////////////////////////////////////
    /**
     * Creates all of the panel components and initializes the log table based on the events stored in the EventPlayer
     */
    private void initComponents() {
        List<TrustLogEvent> logEvents = eventThread.getEvents();
        Object[][] table = new Object[logEvents.size()][3];
        table[0] = new Object[]{"", "", ""};
        int i = 1;
        for (TrustLogEvent evt : logEvents) {
            if (evt != null) {
                table[i] = evt.toArray();
                i++;
            }
        }

        Object[] titles = {"Assessor", "Assessee", "Feedback"};
        logTable = new JTable(table, titles);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.setBackground(Color.LIGHT_GRAY);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        logTable.setColumnSelectionAllowed(false);
        logTable.setRowSelectionAllowed(true);
        ListListener listener = new ListListener();
        logTable.addMouseListener(listener);
        logTable.getSelectionModel().addListSelectionListener(listener);
        logTable.setVisible(true);

        JScrollPane listScroller = new JScrollPane(logTable);
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
        listScroller.setSize(logTable.getWidth(), logTable.getHeight());

        setLayout(new GridLayout(1, 1));
        add(listScroller);
        setBorder(BorderFactory.createTitledBorder("Log Event Panel"));
        Dimension listSize = logTable.getPreferredSize();
        setPreferredSize(new Dimension(listSize.width + 15, listSize.height));
        setMinimumSize(getPreferredSize());
        createPopupMenu(listener);
    }

    /**
     * Creates the popupMenu for the log table.  This is used to add, modify, and remove events
     * @param listener 
     */
    private void createPopupMenu(ActionListener listener) {
        JMenuItem insert = new JMenuItem("Insert Event After");
        insert.addActionListener(listener);
        JMenuItem remove = new JMenuItem("Remove Event");
        remove.addActionListener(listener);
        JMenuItem modify = new JMenuItem("Modify Event");
        modify.addActionListener(listener);

        menu = new JPopupMenu("Edit Events");
        menu.add(insert);
        menu.add(remove);
        menu.addSeparator();
        menu.add(modify);
    }

    /**
     * Highlights the row in the log table specified by the eventIndex.
     * This should only be called by the EventPlayer to notify it that the currentEventIndex has changed.
     * @param eventIndex The index of the row to change to
     */
    @Override
    public void goToIndex(int eventIndex) {
        logTable.setRowSelectionInterval(0, eventIndex);
        logTable.scrollRectToVisible(logTable.getCellRect(eventThread.getCurrentEventIndex() , 0, true));
    }

////////////////////////////////////Listeners///////////////////////////////////
    /**
     * This Listener handles button clicks onn the log table as well as button presses on the table popupMenu
     */
    private class ListListener extends MouseAdapter implements ActionListener, ListSelectionListener {

        private boolean scrubbing; //Whether or not the mouse button is pressed on the timeline

        /**
         * If the mouse is right-clicked on a row, highlight it, and have the EventPlayer move to that event, 
         * and then display the popup menu for that row.
         * Row left-clicking is already handled by valueChanged().
         * Also makes the listener aware that the mouse is pressed and potentially scrubbing the table
         * @param mouseEvent 
         */
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            scrubbing = true;
            if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                updateSelectedRow();
                menu.show(((JTable) mouseEvent.getSource()).getParent().getParent().getParent(), getMousePosition().x, getMousePosition().y);
            }
        }

        /**
         * Makes the listener aware that the mouse is no longer pressed or potentially scrubbing the table
         * @param mouseEvent 
         */
        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            scrubbing = false;
        }

        /**
         * Handles a popup menu button press
         * @param buttonEvent 
         */
        @Override
        public void actionPerformed(ActionEvent buttonEvent) {
            String buttonText = ((AbstractButton) buttonEvent.getSource()).getText();
            if (buttonText.equals("Insert Event After")) {
                EventInjector.getNewEvent(eventThread);
            } else if (buttonText.equals("Remove Event")) {
                eventThread.removeEvent();
            } else if (buttonText.equals("Modify Event")) {
                EventInjector.modifyEvent(eventThread);
            } else {
                ChatterBox.error(this, "actionPerformed()", "Uncaught button press");
            }
        }

        /**
         * If the selected row was changed, and it was because of the mouse selecting it (not the EventPlayer),
         * then have the EventPlayer go to that event.
         * @param selectionEvent 
         */
        public void valueChanged(ListSelectionEvent selectionEvent) {
            if (scrubbing) {
                updateSelectedRow();
            }
        }
        
        /**
         * Move to the row in the table that the mouse is currently hovering over,
         * and then have the EventPlayer move to that event
         */
        private void updateSelectedRow(){
            int newRow = 0;
            try{
                newRow = logTable.rowAtPoint(logTable.getMousePosition());
            }catch (NullPointerException ex){ //This catches the case when the mouse is released outside of the log table
                return;
            }
            logTable.getSelectionModel().setSelectionInterval(newRow, newRow);
            eventThread.goToEvent(newRow);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
