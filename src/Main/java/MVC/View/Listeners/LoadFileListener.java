package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;

import MVC.View.Events.FileEvent;

import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoadFileListener extends EventListener<FileEvent> {

    private static final String LOADING_MSG = "Loading File";

    public LoadFileListener(Function<FileEvent, String> doInBackgroundFunc, Consumer<FileEvent> doneFunc,
                            Component parent,
                            ProgressBar progressBar){

        super(doInBackgroundFunc, doneFunc, parent, progressBar, LOADING_MSG);
    }

    @Override
    public void eventOccurred(FileEvent event) {
        File f = event.getFile();
        if (!f.exists() || f.isDirectory()) {
            return;
        }
        super.eventOccurred(event);
    }
}
