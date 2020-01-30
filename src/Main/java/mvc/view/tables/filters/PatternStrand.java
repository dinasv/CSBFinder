package mvc.view.tables.filters;

/**
 */
public enum PatternStrand {

    CROSS_STRAND(true, "Cross-strand"),
    SINGLE_STRAND(false, "Single strand (directon)"),
    ALL(true, "All");

    public final boolean isCrossStrand;
    public final String description;

    PatternStrand(boolean isCrossStrand, String description){
        this.isCrossStrand = isCrossStrand;
        this.description = description;
    }
}
