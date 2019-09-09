package MVC.View.Components.Dialogs;

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

    private String sessionFileExtension;
    //private static final String SESSION_FILE_EXTENSION = "csb";

    public SaveAsDialog(JFileChooser fileChooser, JFrame mainFrame, String sessionFileExtension){
        this.fileChooser = fileChooser;
        this.mainFrame = mainFrame;
        this.sessionFileExtension = sessionFileExtension;
    }

    public void openDialog(){
        initOutputFileChooser();
        int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);
        if (action != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        String ext = FileTypeFilter.getExtension(file);

        if (!sessionFileExtension.equals(ext)){
            file = new File(file.toString() + "." + sessionFileExtension);
        }

        saveListener.eventOccurred(new FileEvent(this, file));
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        String[] extensions = {sessionFileExtension};
        fileChooser.addChoosableFileFilter(new FileTypeFilter(extensions));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(DIALOG_NAME);
    }

    public void setListener(Listener<FileEvent> listener) {
        this.saveListener = listener;
    }
}
