package model;

/**
 * Defines the type of the printed output files
 */
public enum OutputType {
    TXT("txt"),
    XLSX("xlsx"),
    SESSION("csb");

    public final String extension;

    OutputType(String extension){
        this.extension = extension;
    }
}
