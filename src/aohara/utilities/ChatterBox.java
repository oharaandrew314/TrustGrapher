///////////////////////////////////ChatterBox///////////////////////////////////
package aohara.utilities;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * UserI/O API for Swing frameworks
 * @author Andrew O'Hara
 */
public class ChatterBox {

//////////////////////////////Password Pane Input///////////////////////////////
    public static String getPassword() {
        return getPassword("Please enter the password.");
    }

    public static String getPassword(String title) {
        JPasswordField pwd = new JPasswordField(title.length()/5*4);
        int returnVal = JOptionPane.showConfirmDialog(null, pwd, title, JOptionPane.OK_CANCEL_OPTION);
        if (returnVal == 0) {
            return extractPW(pwd);
        }
        return null;
    }

    public static boolean checkPassword(String password) {
        return checkPassword("Please enter the password", password);
    }

    public static boolean checkPassword(String customMessage, String password) {
        String message = customMessage;
        while (true) {
            String temp = getPassword(message);
            if (temp == null) {
                return false;
            } else if (temp.equals(password)) {
                return true;
            }
            message = "Incorrect.  Please try again.";
        }
    }

    public static String getNewPassword(String oldPassword) {
        if (oldPassword != null) {
            if (!checkPassword("Please enter the old password", oldPassword)) {
                return null;
            }
        }
        return getNewPassword();
    }

    public static String getNewPassword(){
        String pw1 = "";
        String pw2 = " ";
        while (!pw1.equals(pw2)) {
            pw1 = getPassword("Please enter a new password");
            if (pw1 == null){
                return null;
            }
            pw2 = getPassword("Please confirm the password.");
            if (pw2 == null){
                return null;
            }
        }
        return pw1;
    }

    public static String extractPW(JPasswordField field) {
        String pw = "";
        char[] temp = field.getPassword();
        for (int i = 0; i < temp.length; i++) {
            pw = pw + temp[i];
            temp[0] = '0';
        }
        field.setText("");
        return pw;
    }

/////////////////////////////////Alert Panes////////////////////////////////////
    /**
     * Displays the given String in a dialog box.
     * @param s
     */
    public static void alert(String s) {
        alert(null, s);
    }

    /**
     * Displays the given String in a dialog box above the given JComponent
     * @param parent
     * @param s
     */
    public static void alert(JComponent parent, String s) {
        JOptionPane.showMessageDialog(parent, s);
    }

/////////////////////////////////Input Panes////////////////////////////////////
    /**
     * Displayes a Yes/No dialog box
     * @param message
     * @return returns an integer of 0 if user clicks yes, 1 if no.
     */
    public static boolean yesNoDialog(Object message) {
        if (JOptionPane.showConfirmDialog(null, message, "Question", JOptionPane.YES_NO_OPTION) == 0) {
            return true;
        }
        return false;
    }

    public static boolean okCancelDialog(String title, Object message) {
        if (JOptionPane.showConfirmDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Displays a String input dialog box
     * @param message
     * @return The String that the user entered
     */
    public static String userInput(String message) {
        return JOptionPane.showInputDialog(null, message);
    }

    public static int questionPane(String title, String message, String[] options){
        return JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    public static String selectionPane(String title, String message, Object[] options){
        return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

///////////////////////////////Debug Messages///////////////////////////////////

    public static void uselessMethod(Object parent, String method){
        debug(parent, method, "This is a useless method.  The code has been commented out.");
    }

    public static void redundantMethod(Object parent, String method){
        debug(parent, method, "TThe logic paths of this redundant method have been simplified or removed.");
    }

    /**
     * Prints a method not implemented message to the console
     * @param parent
     * @param method
     */
    public static void notImplemented(Object parent, String method) {
        notImplemented(parent, method, "");
    }

    public static void notImplemented(Object parent, String method, String note){
        note = "  " + note;
        print("Error: " + parent.getClass().getSimpleName() + "." + method + ": Not yet implemented." + note);
    }

    /**
     * Displays an error dialog box
     * @param parent
     * @param method
     * @param s
     */
    public static void error(Object parent, String method, String s) {
        error(parent.getClass().getSimpleName(), method, s);
    }
    
    public static void error(String parent, String method, String s){
        alert("Error: " + parent + "." + method + ": " + s);
    }

    /**
     * Prints a message to the console to assist in debugging.
     * @param parent
     * @param method
     * @param s
     */
    public static void debug(Object parent, String method, String s) {
        debug(parent.getClass().getSimpleName(), method, s);
    }

    public static void debug(String parent, String method, String s){
        print("Debug: " + parent + "." + method + ": " + s);
    }
    
    public static void debugAlert(Object parent, String method, String message){
        alert("Debug: " + parent.getClass().getSimpleName() + "." + method + ": " + message);
    }

    /**
     * Displays a dialog box detailing an criticalError and then closes the gui if one is given.
     * @param ui
     * @param parent
     * @param method
     * @param s
     */
    public static void criticalError(Object parent, String method, String s) {
        criticalError(parent.getClass().getSimpleName(), method, s);
    }

    /**
     * Displays a dialog box detailing an criticalError and then closes the gui if one is given.  Use this one if you cannot give a parent reference
     * @param ui
     * @param parent
     * @param method
     * @param s
     */
    public static void criticalError(String parent, String method, String s) {
        String exception = "Critical error: ";
        if (parent != null) {
            exception = exception + parent + ".";
        }
        exception = exception + method + ":  " + s + ".\nCannot continue.  Exiting...";
        alert(exception);
        System.exit(0);
    }

////////////////////////////////Console Panes///////////////////////////////////
    /**
     * Prints a message to the console
     * @param s
     */
    public static void print(String s) {
        System.out.println(s);
    }
}
////////////////////////////////////////////////////////////////////////////////

