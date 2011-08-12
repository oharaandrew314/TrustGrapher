////////////////////////////////TrustClassLoader//////////////////////////////////
package cu.trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import utilities.ChatterBox;

/**
 * An extension of MyClassLoader to include methods specific for TrustGrapher
 * @author Andrew O'Hara
 */
public class TrustClassLoader extends utilities.MyClassLoader {

////////////////////////////////Static Methods//////////////////////////////////
    /**
     *  Takes a path and tries to load an object from it.  If it is a valid algorithm, returns that object
     * @param classPath The path to the file
     * @return An object created from the file at the given path
     */
    public static Object newAlgorithm(String classPath) {
        Object o = newClass(classPath);
        if ((o instanceof ReputationAlgorithm) || (o instanceof TrustAlgorithm) || classPath.endsWith(".jar")) {
            return o;
        }
        ChatterBox.error("TrustClassLoader", "newAlgorithm()", "The file was invalid, no longer exists, or is not a recognized algorithm.\n" + classPath);
        return null;
    }

    /**
     * Takes a classPath and the class index and turns it into an easily readable name
     * @param index The class index
     * @param path The path to the class file
     * @return A display name for the class
     */
    public static String formatClassName(int index, String path) {
        //if it is a .jar, the name appears after the last '.'
        //Otherwise, it is a .class, and the name will appear after the last '/'
        char startChar = path.contains(".jar") ? '.' : '/';
        return "class" + index + "-" + path.substring(path.lastIndexOf(startChar) + 1).replace(".class", "");
    }
}
////////////////////////////////////////////////////////////////////////////////
