package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Events.RunEvent;
import MVC.View.Requests.Request;
import MVC.View.Workers.CustomSwingWorker;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RequestListener<T extends Request> implements RunListener<T> {

    private static final String RUNNING_MSG = "Running...";

    private Function<T, String> doInBackgroundFunc;
    private Consumer<T> doneFunc;
    private Component parent;
    private ProgressBar progressBar;

    public RequestListener(Function<T, String> doInBackgroundFunc, Consumer<T> doneFunc, Component parent,
                           ProgressBar progressBar){

        this.doInBackgroundFunc = doInBackgroundFunc;
        this.doneFunc = doneFunc;
        this.parent = parent;
        this.progressBar = progressBar;
    }


    @Override
    public void runEventOccurred(RunEvent<T> event) {
        SwingUtilities.invokeLater(() -> {
            progressBar.start(RUNNING_MSG);
        });

        T request = event.getRequest();

        SwingWorker<Void, Void> swingWorker = new CustomSwingWorker<>(doInBackgroundFunc, doneFunc,
                parent, request);

        swingWorker.execute();
    }
}
