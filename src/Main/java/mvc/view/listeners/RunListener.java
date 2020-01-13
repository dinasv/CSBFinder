package mvc.view.listeners;

import mvc.view.events.RunEvent;
import mvc.view.requests.Request;

import java.util.EventListener;

public interface RunListener<T extends Request> extends EventListener {

    void runEventOccurred(RunEvent<T> e);
}
