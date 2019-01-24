package MVC.View.Listeners;

import MVC.View.Events.RunEvent;
import MVC.View.Requests.Request;

import java.util.EventListener;

public interface RunListener<T extends Request> extends EventListener {

    void runEventOccurred(RunEvent<T> e);
}
