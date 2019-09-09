package MVC.View.Listeners;

import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Events.ExportEvent;
import MVC.View.Workers.CustomSwingWorker;
import MVC.View.Workers.SwingWorkerCompletionWaiter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public class SaveFileListener extends EventListener<ExportEvent> {

    private static final String EXPORT_MSG = "Saving...";

    public SaveFileListener(Function<ExportEvent, String> doInBackgroundFunc, Consumer<ExportEvent> doneFunc, Component parent,
                            ProgressBar progressBar){

        super(doInBackgroundFunc, doneFunc, parent, progressBar, EXPORT_MSG);
    }

    @Override
    public void eventOccurred(ExportEvent event) {

        if (event.getAction() != JFileChooser.APPROVE_OPTION) {
            return;
        }

        super.eventOccurred(event);

    }
}
