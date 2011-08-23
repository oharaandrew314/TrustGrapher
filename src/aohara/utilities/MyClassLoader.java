////////////////////////////////MyClassLoader//////////////////////////////////
package aohara.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A custom class loader.  Loads .class files directly, or from a .jar
 * @author Andrew O'Hara
 */
public class MyClassLoader {

////////////////////////////////Static Methods//////////////////////////////////
    public static Object newClass(String classPath) {
        if (classPath.contains(".jar")) { //If it is a jar
            if (classPath.contains("!")){//If it already has a name provided
                File file = new File(classPath.split("!")[0]);
                String name = classPath.split("!")[1];
                return classInstance((Class) loadJarClass(file, name));
            }else{
                return classInstance((Class) loadJarClass(new File(classPath)));
            }
        } else if (classPath.endsWith(".class")) {//If it is a class
            return classInstance(loadClass(new File(classPath)));
        } else {
            ChatterBox.debug("MyClassLoader", "newClass()", "Unsupported class\n" + classPath);
            new Exception().printStackTrace();
            return null;
        }
    }

    public static Class loadClass(File classFile) {
        if (classFile == null) {
            return null;
        }
        String className = classFile.getName().split(".class")[0];
        for (int i = 0; i < 1000; i++) {
            File dir = classFile.getParentFile(); // Create a File object on the root of the directory containing the class file
            try {
                ClassLoader cl = new URLClassLoader(new URL[]{dir.toURI().toURL()});
                return cl.loadClass(className);

            } catch (NoClassDefFoundError e) {
                className = dir.getPath().substring(dir.getPath().lastIndexOf(File.separator) + 1) + "." + className;
                classFile = classFile.getParentFile();
                continue;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static Object loadJarClass(File jarFile) {
        return loadJarClass(jarFile, "");
    }

    public static Object loadJarClass(File jarFile, String name) {
        if (jarFile == null || !jarFile.exists()) {
            ChatterBox.debug("Bitstylus", "loadJarClass()", "Invalid file");
            return null;
        }
        ClassLoader cl = null;
        try {
            cl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
            if (!name.equals("")) { //If a non-null name was passed, see if it will work
                return cl.loadClass(name);
            }
        } catch (MalformedURLException ex) {
            ChatterBox.debug("BitStylus", "loadJarClassFile()", "Invalid URL");
        } catch (ClassNotFoundException ex) {
            ChatterBox.alert("The fully qualified class name was incorrect.  Please try again.");
        }
        while (true) {
            try {
                name = ChatterBox.userInput("Please enter the fully qualified Class name that you wish to load.");
                return (name != null) ? cl.loadClass(name) : null;
            } catch (ClassNotFoundException ex) {
                ChatterBox.alert("The fully qualified class name was incorrect.  Please try again.");
                name = "";
            }
        }
    }

    public static Object classInstance(Class c) {
        try {
            Object o = c.newInstance();
            return o;
        } catch (InstantiationException ex) {
            ChatterBox.alert("This class must have a default constructor to be used.");
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
