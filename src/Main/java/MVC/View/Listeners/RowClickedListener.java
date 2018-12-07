package MVC.View.Listeners;

import MVC.View.Events.RowClickedEvent;

public interface RowClickedListener<T> {

    void rowClickedOccurred(RowClickedEvent<T> e);
}
