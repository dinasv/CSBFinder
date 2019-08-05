package MVC.View.Workers;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomSwingWorker<T> extends SwingWorker<Void, Void>{

    private static final int MSG_WIDTH = 500;

    private String msg;
    private Function<T, String> doInBackgroundFunc;
    private Consumer<T> doneFunc;
    private Component parent;
    private T arg;

    public CustomSwingWorker(Function<T, String> doInBackgroundFunc, Consumer<T> doneFunc, Component parent,
                             T arg){
        this.doInBackgroundFunc = doInBackgroundFunc;
        this.doneFunc = doneFunc;
        this.parent = parent;
        this.arg = arg;
        msg = null;
    }


    @Override
    protected Void doInBackground() {
        msg = doInBackgroundFunc.apply(arg);
        return null;
    }

    @Override
    protected void done() {

        doneFunc.accept(arg);

        //An exception occurred
        if (msg != null) {
            JOptionPane.showMessageDialog(parent, formatMsgWidth(msg));
        }
    }

    private static String formatMsgWidth(String msg){
        String html = "<html><body width='%s'>%s";
        return String.format(html, MSG_WIDTH, msg);
    }
}
