package MVC.View.Listeners;

import MVC.View.Events.Event;

/**
 */
public interface Listener<T extends Event>{
    void eventOccurred(T event);
}
