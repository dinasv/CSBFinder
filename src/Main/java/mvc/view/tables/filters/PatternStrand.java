package mvc.view.tables.filters;

/**
 */
public enum PatternStrand {

    MULTI_STRAND(true, "Multi-strand"),
    SINGLE_STRAND(false, "Single strand (directon)"),
    ALL(true, "All");

    public final boolean isMultiStrand;
    public final String description;

    PatternStrand(boolean isMultiStrand, String description){
        this.isMultiStrand = isMultiStrand;
        this.description = description;
    }
}
