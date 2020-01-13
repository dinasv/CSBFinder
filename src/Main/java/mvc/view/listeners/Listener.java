package mvc.view.listeners;

import mvc.view.events.Event;

/**
 */
public interface Listener<T extends Event>{
    void eventOccurred(T event);
}
