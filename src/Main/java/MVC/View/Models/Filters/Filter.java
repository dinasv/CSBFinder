package MVC.View.Models.Filters;

/**
 */
public interface Filter<T> {

    boolean include(T pattern);
}
