package MVC.View.Components.Dialogs;

import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.Listener;

import javax.swing.*;

/**
 */
public class SaveDialog {

    private JFileChooser fileChooser;
    private OutputTypeChooser outputTypeChooser;
    private JFrame mainFrame;

    private Listener<SaveOutputEvent> saveOutputListener;

    private static final String SAVE_FILES_DIALOG_BTN_NAME = "Select Directory";
    private static final String SAVE_FILES_DIALOG_NAME = "Save";

    public SaveDialog(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
        outputTypeChooser = new OutputTypeChooser();
        this.mainFrame = mainFrame;
    }

    public void openDialog(){
        initOutputFileChooser();
        int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);
        saveOutputListener.eventOccurred(new SaveOutputEvent(outputTypeChooser.getChosenOutput(),
                outputTypeChooser.getDatasetName(), fileChooser.getSelectedFile().getAbsolutePath(), action));
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAccessory(outputTypeChooser);
        fileChooser.setDialogTitle(SAVE_FILES_DIALOG_NAME);
    }

    public void setSaveOutputListener(Listener<SaveOutputEvent> saveOutputListener) {
        this.saveOutputListener = saveOutputListener;
    }
}
