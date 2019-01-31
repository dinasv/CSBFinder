package MVC.View.Models;

import Core.PostProcess.Family;

import java.util.function.Function;

/**
 */
public interface ColumnProperty<T> {

    Function<T, ?> getFunction();

    Class<?> getFunctionReturnClass();
}
