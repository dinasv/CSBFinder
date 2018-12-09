package MVC.View.Components.Dialogs;


import javax.swing.filechooser.FileFilter;
import java.io.File;

public class InputFileChooser extends FileFilter {

    public final static String FASTA_SUFFIX = "fasta";
    public final static String TXT_SUFFIX = "txt";

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(FASTA_SUFFIX) ||
                    extension.equals(TXT_SUFFIX)){
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return String.format("*.%s or *.%s", FASTA_SUFFIX, TXT_SUFFIX);
    }

    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
