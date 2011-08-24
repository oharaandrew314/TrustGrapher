package aohara.utilities;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * This is a file filter for a JFileChooser.
 * @author Andrew O'Hara
 */
public class ExtensionFileFilter extends FileFilter {

    String description;
    String extensions[];

    /**
     * Creates a file filter.
     * @param extension The single file extension to accept.  Do not include the '.'
     */
    public ExtensionFileFilter(String extension) {
        this(new String[]{extension});
    }

    /**
     * Creates a file filter.
     * @param extensions The array of file extensions to accept.  Do not include the ".' in the extensions.
     */
    public ExtensionFileFilter(String extensions[]) {
        description = "." + extensions[0];
        for (int i = 1; i < extensions.length - 1; i++) { //Don't add the last extension yet
            description = description + ", ." + extensions[i];
        }
        if (extensions.length - 1 != 0) { //If the last extension isn't the first extension, add the last extension
            description = description + " and ." + extensions[extensions.length - 1];
        }
        description = description + " files only";
        this.extensions = (String[]) extensions.clone();
        toLower(this.extensions);
    }

    private void toLower(String array[]) {
        for (int i = 0, n = array.length; i < n; i++) {
            array[i] = array[i].toLowerCase();
        }
    }

    public String getDescription() {
        return description;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        } else {
            String path = file.getAbsolutePath().toLowerCase();
            for (int i = 0, n = extensions.length; i < n; i++) {
                String extension = extensions[i];
                if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
                    return true;
                }
            }
        }
        return false;
    }
}
