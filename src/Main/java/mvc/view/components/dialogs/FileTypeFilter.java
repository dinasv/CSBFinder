package mvc.view.components.dialogs;


import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileTypeFilter extends FileFilter {

    private String[] allowedExtensions;
    //public final static String FASTA_SUFFIX = "fasta";
    //public final static String TXT_SUFFIX = "txt";

    public FileTypeFilter(String[] allowedExtensions){
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {

            for (String allowedExtension : allowedExtensions){
                if (extension.equals(allowedExtension)){
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    @Override
    public String getDescription() {
        return Arrays.stream(allowedExtensions).map(ext -> "*." + ext).collect(Collectors.joining(" or "));
        //return String.format("*.%s or *.%s", FASTA_SUFFIX, TXT_SUFFIX);
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
