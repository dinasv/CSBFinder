package mvc.view.tables.filters;

/**
 */
public interface Filter<T> {

    boolean include(T pattern);
}
