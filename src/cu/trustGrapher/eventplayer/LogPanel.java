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
import utilities.ChatterBox;

/**
 * Description
 * @author Andrew O'Hara
 */
public class LogPanel extends JPanel implements EventPlayerListener {

    private JTable logList;
    private JPopupMenu menu;
    private EventPlayer eventThread;

//////////////////////////////////Constructor///////////////////////////////////
    public LogPanel(List<TrustLogEvent> logEvents) {
        initComponents(logEvents);
    }
//////////////////////////////////Accessors/////////////////////////////////////
    public EventPlayer getEventPlayer(){
        return eventThread;
    }

///////////////////////////////////Methods//////////////////////////////////////
    private void initComponents(List<TrustLogEvent> logEvents) {
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
        logList = new JTable(table, titles);
        logList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logList.setBackground(Color.LIGHT_GRAY);
        logList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        logList.setColumnSelectionAllowed(false);
        logList.setRowSelectionAllowed(true);
        ListListener listener = new ListListener();
        logList.addMouseListener(listener);
        logList.getSelectionModel().addListSelectionListener(listener);
        logList.setVisible(true);

        JScrollPane listScroller = new JScrollPane(logList);
        listScroller.setWheelScrollingEnabled(true);
        listScroller.setBorder(BorderFactory.createLoweredBevelBorder());
        listScroller.setSize(logList.getWidth(), logList.getHeight());

        setLayout(new GridLayout(1, 1));
        add(listScroller);
        setBorder(BorderFactory.createTitledBorder("Log Event Panel"));
        Dimension listSize = logList.getPreferredSize();
        setPreferredSize(new Dimension(listSize.width + 15, listSize.height));
        setMinimumSize(getPreferredSize());
        createPopupMenu(listener);
    }

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

    @Override
    public void goToIndex(int eventIndex) {
        logList.setRowSelectionInterval(0, eventIndex);
        logList.scrollRectToVisible(logList.getCellRect(eventThread.getCurrentEventIndex() , 0, true));
    }

    @Override
    public void addEventPlayer(EventPlayer eventThread) {
        this.eventThread = eventThread;
    }

////////////////////////////////////Listeners///////////////////////////////////
    private class ListListener extends MouseAdapter implements ActionListener, ListSelectionListener {

        private boolean scrubbing;

        @Override
        public void mousePressed(MouseEvent e) {
            scrubbing = true;
            if (SwingUtilities.isRightMouseButton(e)) {
                updateSelectedRow();
                menu.show(((JTable) e.getSource()).getParent().getParent().getParent(), getMousePosition().x, getMousePosition().y);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            scrubbing = false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String buttonText = ((AbstractButton) e.getSource()).getText();
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

        public void valueChanged(ListSelectionEvent e) {
            if (scrubbing) {
                updateSelectedRow();
            }
        }
        
        private void updateSelectedRow(){
            int newRow = logList.rowAtPoint(logList.getMousePosition());
            logList.getSelectionModel().setSelectionInterval(newRow, newRow);
                eventThread.goToEvent(newRow);
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
