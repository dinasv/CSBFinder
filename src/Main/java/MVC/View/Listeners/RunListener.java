package MVC.View.Listeners;

import MVC.View.Events.RunEvent;

import java.util.EventListener;

public interface RunListener extends EventListener {

    void runEventOccurred(RunEvent e);
}
