package mvc.view.components.dialogs;

import javax.swing.*;

/**
 */
public class SaveAsDialog {

    private JFileChooser fileChooser;
    private JFrame mainFrame;

    private static final String SAVE_FILES_DIALOG_BTN_NAME = "Save";
    private static final String DIALOG_NAME = "Save As...";

    private String sessionFileExtension;

    public SaveAsDialog(JFileChooser fileChooser, JFrame mainFrame, String sessionFileExtension){
        this.fileChooser = fileChooser;
        this.mainFrame = mainFrame;
        this.sessionFileExtension = sessionFileExtension;
    }

    public int openDialog(){
        initOutputFileChooser();
        return fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_BTN_NAME);

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

}
