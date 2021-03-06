package mvc.view.tables;

import java.util.function.Function;

/**
 */
public interface ColumnProperty<T> {

    Function<T, ?> getFunction();

    Class<?> getFunctionReturnClass();
}
