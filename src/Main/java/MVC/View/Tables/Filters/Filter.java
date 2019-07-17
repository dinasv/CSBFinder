package MVC.View.Tables.Filters;

/**
 */
public interface Filter<T> {

    boolean include(T pattern);
}
