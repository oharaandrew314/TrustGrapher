///////////////////////////////////ChatterBox///////////////////////////////////
package aohara.utilities;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * User I/O API for Swing frameworks
 * @author Andrew O'Hara
 */
public class ChatterBox {

//////////////////////////////Password Pane Input///////////////////////////////
    /**
     * Opens a dialog prompting the user for a password.
     * Calls getPassword("Please enter the password.");
     * @return The password that the user entered
     */
    public static String getPassword() {
        return getPassword("Please enter the password.");
    }

    /**
     * Opens a dialog prompting the user for a password with the given JDialog title.
     * @param title The text to display as the JDialog title
     * @return The password that the user entered
     */
    public static String getPassword(String title) {
        JPasswordField pwd = new JPasswordField(title.length()/5*4);
        int returnVal = JOptionPane.showConfirmDialog(null, pwd, title, JOptionPane.OK_CANCEL_OPTION);
        if (returnVal == 0) {
            return extractPW(pwd);
        }
        return null;
    }

    /**
     * Uses a JDialog to check that the password that the user entered is equal to the correct password.
     * If the password is incorrect, displays an incorrect message and prompts the user to try again.
     * If the user gets the password correct, returns true.  If the user cancels, returns false.
     * Calls checkPassword("Please enter the password", password);
     * @param password The correct password
     * @return Whether or not the user entered the correct password
     */
    public static boolean checkPassword(String password) {
        return checkPassword("Please enter the password", password);
    }

    /**
     * Uses a JDialog to check that the password that the user entered is equal to the correct password.
     * If the password is incorrect, displays an incorrect message and prompts the user to try again.
     * If the user gets the password correct, returns true.  If the user cancels, returns false.
     * @param customMessage The message to display in the JDialog title.
     * @param password The correct password
     * @return Whether or not the user entered the correct password.
     */
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

    /**
     * First checks to see if the user knows the old password.  If so, proceeds, otherwise, returns null.
     * Then prompts the user to enter a new password twice into separate JDialogs, and checks if they are equal.
     * If them passwords do not match, the user is promted to try again.  If the user cancels, returns null.
     * @param oldPassword
     * @return The new password
     */
    public static String getNewPassword(String oldPassword) {
        if (oldPassword != null) {
            if (!checkPassword("Please enter the old password", oldPassword)) {
                return null;
            }
        }
        return getNewPassword();
    }

    /**
     * Prompts the user to enter a new password twice into separate JDialogs, and checks if they are equal.
     * If them passwords do not match, the user is promted to try again.
     * @return The new password
     */
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

    /**
     * Takes a JPasswordField and returns the password as a String
     * @param field The JPasswordField to extract the password from
     * @return the password
     */
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
     * Calls alert(null, s);
     * @param s the String to display
     */
    public static void alert(String s) {
        alert(null, s);
    }

    /**
     * Displays the given String in a dialog box above the given JComponent
     * @param parent The Component to display the alert box over
     * @param s the String to display
     */
    public static void alert(JComponent parent, String s) {
        JOptionPane.showMessageDialog(parent, s);
    }

/////////////////////////////////Input Panes////////////////////////////////////
    /**
     * Displayes a Yes/No dialog box and returns the result.
     * @param message The text to display in the dialog box body.
     * @return returns true if user clicks yes, otherwise false
     */
    public static boolean yesNoDialog(Object message) {
        if (JOptionPane.showConfirmDialog(null, message, "Question", JOptionPane.YES_NO_OPTION) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Displays an Ok/Cancel dialog box and returns the result.
     * @param title The title of the JDialog
     * @param message The message to display in the body of the JDialog
     * @return true if the user clicks ok, false otherwise
     */
    public static boolean okCancelDialog(String title, Object message) {
        if (JOptionPane.showConfirmDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Displays a String input dialog box
     * @param message The message to display in the JDialog body
     * @return The String that the user entered
     */
    public static String userInput(String message) {
        return JOptionPane.showInputDialog(null, message);
    }

    /**
     * Displays a JDialog asking the user to select an option from a combo box and returns the result as an int
     * @param title The title of the JDialog
     * @param message The message to display in the JDialog body
     * @param options The String array of options for the user to select
     * @return Returns the index of the options that the user selected. -1 if the user cancelled
     */
    public static int questionPane(String title, String message, String[] options){
        return JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    /**
     * Displays a JDialog asking the user to select an option from a combo box and returns the result as a String
     * @param title The title of the JDialog
     * @param message The message to display in the JDialog body
     * @param options The String array of options for the user to select
     * @return Returns the options that the user selected as a Stirng.  Null if the user clicked cancel
     */
    public static String selectionPane(String title, String message, Object[] options){
        return (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

///////////////////////////////Debug Messages///////////////////////////////////

    /**
     * Displays an error JDialog.
     * @param origin The object that this is being called from
     * @param method The method that this is being called from
     * @param s A description of what went wrong
     */
    public static void error(Object origin, String method, String s) {
        error(origin.getClass().getSimpleName(), method, s);
    }

    /**
     * Displays an error JDialog.  It is best to use this when calling from a static method.
     * @param origin The name of the object that this is being called from
     * @param method The method that this is being called from
     * @param s A description of what went wrong
     */
    public static void error(String origin, String method, String s){
        alert("Error: " + origin + "." + method + ": " + s);
    }

    /**
     * Prints a message to the console to assist in debugging.
     * @param origin The object that this is being called from
     * @param method The method that this is being called from
     * @param s A message of what's going on here
     */
    public static void debug(Object origin, String method, String s) {
        debug(origin.getClass().getSimpleName(), method, s);
    }

    /**
     *Displays an error JDialog.  It is best to use this when calling from a static method.
     * @param origin The name of the object that this is being called from
     * @param method The method that this is being called from
     * @param s A message of what's going on here
     */
    public static void debug(String origin, String method, String s){
        System.out.println("Debug: " + origin + "." + method + ": " + s);
    }

    /**
     * Displays a dialog box detailing an criticalError and then exits the program.
     * origin The object that this is being called from
     * @param method The method that this is being called from
     * @param s A description of what went wrong
     */
    public static void criticalError(Object origin, String method, String s) {
        criticalError(origin.getClass().getSimpleName(), method, s);
    }

    /**
     * Displays a dialog box detailing an criticalError and then exits the program.  It is best to use this when calling from a static method.
     * @param origin The name of the object that this is being called from
     * @param method The method that this is being called from
     * @param s A description of what went wrong
     */
    public static void criticalError(String parent, String method, String s) {
        String exception = "Critical error: ";
        if (parent != null) {
            exception = exception + parent + ".";
        }
        exception = exception + method + ":  " + s + ".\nCannot continue.  Exiting...";
        alert(exception);
        System.exit(1);
    }
}
////////////////////////////////////////////////////////////////////////////////

