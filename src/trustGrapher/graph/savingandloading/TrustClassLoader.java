////////////////////////////////TrustClassLoader//////////////////////////////////
package trustGrapher.graph.savingandloading;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import utilities.ChatterBox;

/**
 * An extension of MyClassLoader to include methods specific for TrustGrapher
 * @author Andrew O'Hara
 */
public class TrustClassLoader extends utilities.MyClassLoader {

////////////////////////////////Static Methods//////////////////////////////////
    public static Object newAlgorithm(String classPath) {
        Object o = newClass(classPath);
        if ((o instanceof ReputationAlgorithm) || (o instanceof TrustAlgorithm) || classPath.endsWith(".jar")) {
            return o;
        }
        ChatterBox.error("TrustClassLoader", "newAlgorithm()", "The file was not a recognized algorithm.\n" + classPath);
        return null;
    }

    public static String formatClassName(String path) {
        //if it is a .jar, the name appears after the last '.'
        //Otherwise, it is a .class, and the name will appear after the last '/'
        char startChar = path.contains(".jar") ? '.' : '/';
        return path.substring(path.lastIndexOf(startChar) + 1);
    }
}
////////////////////////////////////////////////////////////////////////////////
