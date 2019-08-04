package MVC.View.Components;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LoadFileSwingWorker extends SwingWorker<Void, Void> {

    private static final int MSG_WIDTH = 500;

    private String msg;
    private Function<File, String> doInBackgroundFunc;
    private Consumer<File> doneFunc;
    private File file;
    private Component parent;

    public LoadFileSwingWorker(Function<File, String> doInBackgroundFunc, Consumer<File> doneFunc, File file,
                               Component parent){

        this.doInBackgroundFunc = doInBackgroundFunc;
        this.doneFunc = doneFunc;
        this.file = file;
        this.parent = parent;
        msg = null;
    }

    @Override
    protected Void doInBackground() throws Exception {
        msg = doInBackgroundFunc.apply(file);
        return null;
    }

    @Override
    protected void done() {

        doneFunc.accept(file);

        if (msg != null) {
            JOptionPane.showMessageDialog(parent, formatMsgWidth(msg));
        }
    }

    private static String formatMsgWidth(String msg){
        String html = "<html><body width='%s'>%s";
        return String.format(html, MSG_WIDTH, msg);
    }
}
