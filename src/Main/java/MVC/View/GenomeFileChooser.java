package MVC.View;


import javax.swing.filechooser.FileFilter;
import java.io.File;

public class GenomeFileChooser extends FileFilter {

    public final static String FASTA_SUFFIX = ".fasta";

    @Override
    public boolean accept(File f) {
        return f.getName().endsWith(FASTA_SUFFIX);
    }

    @Override
    public String getDescription() {
        return String.format("Genome files (%s)", FASTA_SUFFIX);
    }
}
