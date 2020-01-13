package mvc.view.components.dialogs;

import mvc.view.events.ExportEvent;
import mvc.view.listeners.Listener;
import model.OutputType;

import javax.swing.*;

/**
 */
public class ExportDialog {

    private JFileChooser fileChooser;
    private OutputTypeChooser outputTypeChooser;
    private JFrame mainFrame;

    private Listener<ExportEvent> exportListener;

    private static final String SAVE_FILES_DIALOG_BTN_NAME = "Select Folder";
    private static final String SAVE_FILES_DIALOG_NAME = "Choose a folder for the exported files";

    public ExportDialog(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
        outputTypeChooser = new OutputTypeChooser();
        this.mainFrame = mainFrame;
    }

    public void openDialog(OutputType outputType){
        initOutputFileChooser();
        int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);
        if (action != JFileChooser.APPROVE_OPTION) {
            return;
        }
        exportListener.eventOccurred(new ExportEvent(outputType,
                outputTypeChooser.getFileNameField(), fileChooser.getSelectedFile().getAbsolutePath(), action));
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
