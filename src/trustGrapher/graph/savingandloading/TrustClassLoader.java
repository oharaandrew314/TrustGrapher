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
        if ((o instanceof ReputationAlgorithm) || (o instanceof TrustAlgorithm)) {
            return o;
        }
        ChatterBox.alert("The file was not a recognized algorithm.\n" + classPath);
        return null;
    }

    public static String formatClassName(String path) {
        if (path.contains("!")) { //Replace the long jar path with "<algName> from <jarName>"
            path = path.substring(path.lastIndexOf('.') + 1, path.length()) + " from " + path.substring(path.lastIndexOf('/') + 1, path.indexOf('!'));
        } else {
            path = path.substring(path.lastIndexOf('/') + 1).replace(".class", "");
        }
        return path;
    }
}
////////////////////////////////////////////////////////////////////////////////
