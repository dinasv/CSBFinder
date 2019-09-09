package MVC.View.Components.Dialogs;

import MVC.View.Events.ExportEvent;
import MVC.View.Events.FileEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;
import java.io.File;

/**
 */
public class SaveAsDialog {

    private JFileChooser fileChooser;
    private JFrame mainFrame;

    private Listener<FileEvent> saveListener;

    private static final String SAVE_FILES_DIALOG_BTN_NAME = "Save";
    private static final String DIALOG_NAME = "Save As...";
    private static final String SESSION_FILE_EXTENSION = "txt";
    private static final String[] EXTENSIONS = {SESSION_FILE_EXTENSION};

    public SaveAsDialog(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
        this.mainFrame = mainFrame;
    }

    public void openDialog(){
        initOutputFileChooser();
        int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);
        if (action != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        String ext = FileTypeFilter.getExtension(file);

        if (!SESSION_FILE_EXTENSION.equals(ext)){
            file = new File(file.toString() + "." + SESSION_FILE_EXTENSION);
        }

        saveListener.eventOccurred(new FileEvent(this, file));
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new FileTypeFilter(EXTENSIONS));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(DIALOG_NAME);
    }

    public void setListener(Listener<FileEvent> listener) {
        this.saveListener = listener;
    }
}
