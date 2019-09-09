package MVC.View.Components.Dialogs;

import MVC.View.Events.ExportEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;

/**
 */
public class ExportDialog {

    private JFileChooser fileChooser;
    private OutputTypeChooser outputTypeChooser;
    private JFrame mainFrame;

    private Listener<ExportEvent> exportListener;

    private static final String SAVE_FILES_DIALOG_BTN_NAME = "Select Directory";
    private static final String SAVE_FILES_DIALOG_NAME = "Save";

    public ExportDialog(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
        outputTypeChooser = new OutputTypeChooser();
        this.mainFrame = mainFrame;
    }

    public void openDialog(){
        initOutputFileChooser();
        int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);
        exportListener.eventOccurred(new ExportEvent(outputTypeChooser.getChosenOutput(),
                outputTypeChooser.getDatasetName(), fileChooser.getSelectedFile().getAbsolutePath(), action));
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAccessory(outputTypeChooser);
        fileChooser.setDialogTitle(SAVE_FILES_DIALOG_NAME);
    }

    public void setListener(Listener<ExportEvent> listener) {
        this.exportListener = listener;
    }
}
