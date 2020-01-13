package mvc.view.listeners;

import mvc.view.events.RowClickedEvent;

public interface RowClickedListener<T> {

    void rowClickedOccurred(RowClickedEvent<T> e);
}
