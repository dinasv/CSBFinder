package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;

import MVC.View.Events.LoadFileEvent;

import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoadFileListener extends EventListener<LoadFileEvent> {

    private static final String LOADING_MSG = "Loading File";

    public LoadFileListener(Function<LoadFileEvent, String> doInBackgroundFunc, Consumer<LoadFileEvent> doneFunc,
                            Component parent,
                            ProgressBar progressBar){

        super(doInBackgroundFunc, doneFunc, parent, progressBar, LOADING_MSG);
    }

    @Override
    public void eventOccurred(LoadFileEvent event) {
        File f = event.getFile();
        if (!f.exists() || f.isDirectory()) {
            return;
        }
        super.eventOccurred(event);
    }
}
