package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Workers.CustomSwingWorker;
import MVC.View.Workers.SwingWorkerCompletionWaiter;
import MVC.View.Events.LoadFileEvent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoadFileListener implements Listener<LoadFileEvent> {

    private static final String LOADING_MSG = "Loading File";

    private Function<File, String> doInBackgroundFunc;
    private Consumer<File> doneFunc;
    private Component parent;
    private ProgressBar progressBar;

    public LoadFileListener(Function<File, String> doInBackgroundFunc, Consumer<File> doneFunc, Component parent,
                            ProgressBar progressBar){

        this.doInBackgroundFunc = doInBackgroundFunc;
        this.doneFunc = doneFunc;
        this.parent = parent;
        this.progressBar = progressBar;
    }

    @Override
    public void eventOccurred(LoadFileEvent event) {
        File f = event.getFilePath();
        if (f.exists() && !f.isDirectory()) {

            SwingWorker<Void, Void> swingWorker = new CustomSwingWorker<>(doInBackgroundFunc, doneFunc,
                    parent, f);

            swingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(progressBar));
            swingWorker.execute();
            progressBar.start(LOADING_MSG);
        }
    }
}
