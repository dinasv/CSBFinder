package MVC.View.Components;

import MVC.View.Components.Dialogs.InputFileChooser;
import MVC.View.Components.Dialogs.OutputTypeChooser;
import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Events.SelectParamsEvent;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.SaveOutputListener;
import MVC.View.Listeners.SelectParamsListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Toolbar extends JPanel implements ActionListener{

    private static final String LOAD_GENOMES = "Load Input Genomes";
    private static final String LOAD_SESSION = "Import Session";
    private static final String LOAD_COG_INFO = "Load Orthology Information";
    private static final String SAVE_FILES_DIALOG_NAME = "Select Directory";
    private static final String SAVE_FILES_BTN_NAME = "Save";

    private JButton loadGenomes;
    private JButton importSession;
    private JButton loadCogInfo;
    private JButton saveFile;
    private JButton selectParams;
    private LoadFileListener loadGenomesListener;
    private LoadFileListener importSessionListener;
    private LoadFileListener loadCogInfoListener;
    private SaveOutputListener saveOutputListener;
    private SelectParamsListener selectParamsListener;

    private JFileChooser fileChooser;

    OutputTypeChooser outputTypeChooser;

    public Toolbar(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
        outputTypeChooser = new OutputTypeChooser();

        setBorder(BorderFactory.createEtchedBorder());
        loadGenomes = new JButton(LOAD_GENOMES);
        importSession = new JButton(LOAD_SESSION);
        loadCogInfo = new JButton(LOAD_COG_INFO);
        saveFile =  new JButton(SAVE_FILES_BTN_NAME);
        saveFile.setEnabled(false);
        selectParams =  new JButton("Run");
        selectParams.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(loadGenomes);
        add(importSession);
        add(loadCogInfo);
        add(selectParams);
        add(saveFile);

        loadGenomes.addActionListener(this);
        importSession.addActionListener(this);
        loadCogInfo.addActionListener(this);
        saveFile.addActionListener(this);

        selectParams.addActionListener(e -> {
            selectParamsListener.selectParamsOccurred(new SelectParamsEvent());
        });

    }

    public void setImportSessionListener(LoadFileListener importSessionListener) {
        this.importSessionListener = importSessionListener;
    }

    public void setLoadGenomesListener(LoadFileListener loadGenomesListener) {
        this.loadGenomesListener = loadGenomesListener;
    }

    public void setLoadCogInfoListener(LoadFileListener loadCogInfoListener) {
        this.loadCogInfoListener = loadCogInfoListener;
    }

    public void setSaveOutputListener(SaveOutputListener saveOutputListener) {
        this.saveOutputListener = saveOutputListener;
    }

    public void setSelectParamsListener(SelectParamsListener selectParamsListener) {
        this.selectParamsListener = selectParamsListener;
    }

    public void enableSaveFileBtn() {
        saveFile.setEnabled(true);
    }

    public void disableSaveFileBtn() {
        saveFile.setEnabled(false);
    }

    public void enableSelectParamsBtn() {
        selectParams.setEnabled(true);
    }
    public void disableSelectParamsBtn() {
        selectParams.setEnabled(false);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()){
            case LOAD_GENOMES:
                initInputFileChooser();
                loadEventOccured(e, loadGenomesListener);

                break;
            case LOAD_SESSION:
                initInputFileChooser();
                loadEventOccured(e, importSessionListener);

                break;
            case LOAD_COG_INFO:
                initInputFileChooser();
                loadEventOccured(e, loadCogInfoListener);

                break;
            case SAVE_FILES_BTN_NAME:
                initOutputFileChooser();
                int action = fileChooser.showDialog(this, SAVE_FILES_DIALOG_NAME);
                saveOutputListener.saveOutputOccurred(new SaveOutputEvent(e, outputTypeChooser.getChosenOutput(),
                        fileChooser.getSelectedFile().getAbsolutePath(), action));

                break;
        }
    }

    private void initInputFileChooser(){
        fileChooser.addChoosableFileFilter(new InputFileChooser());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void loadEventOccured(ActionEvent e, LoadFileListener listener){
        int action = fileChooser.showDialog(this, e.getActionCommand());

        if (action == JFileChooser.APPROVE_OPTION) {
            listener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
        }
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAccessory(outputTypeChooser);
    }
}




