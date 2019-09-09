package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Events.Event;
import MVC.View.Workers.CustomSwingWorker;
import MVC.View.Workers.SwingWorkerCompletionWaiter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class EventListener<T extends Event> implements Listener<T> {

    private Function<T, String> doInBackgroundFunc;
    private Consumer<T> doneFunc;
    private Component parent;
    private ProgressBar progressBar;
    private String msg;

    public EventListener(Function<T, String> doInBackgroundFunc, Consumer<T> doneFunc, Component parent,
                            ProgressBar progressBar, String msg){

        this.doInBackgroundFunc = doInBackgroundFunc;
        this.doneFunc = doneFunc;
        this.parent = parent;
        this.progressBar = progressBar;
        this.msg = msg;
    }

    @Override
    public void eventOccurred(T event) {
        SwingWorker<Void, Void> swingWorker = new CustomSwingWorker<T>(doInBackgroundFunc, doneFunc,
                parent, event);

        swingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(progressBar));
        swingWorker.execute();
        progressBar.start(msg);
    }


}
