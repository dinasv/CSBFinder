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

public class Toolbar extends JPanel {

    private static String LOAD_GENOMES = "Load Input Genomes";
    private static String LOAD_SESSION = "Import Session";
    private static String LOAD_COG_INFO = "Load Orthology Information";

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
        saveFile =  new JButton("Save");
        saveFile.setEnabled(false);
        selectParams =  new JButton("Run");
        selectParams.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(loadGenomes);
        add(importSession);
        add(loadCogInfo);
        add(selectParams);
        add(saveFile);

        loadGenomes.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(new InputFileChooser());
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setAccessory(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int value = fileChooser.showDialog(this, LOAD_GENOMES);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadGenomesListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        importSession.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(new InputFileChooser());
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setAccessory(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int value = fileChooser.showDialog(this, LOAD_SESSION);

            if (value == JFileChooser.APPROVE_OPTION) {
                importSessionListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        loadCogInfo.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.addChoosableFileFilter(new InputFileChooser());
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setAccessory(null);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            int value = fileChooser.showDialog(this, LOAD_COG_INFO);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadCogInfoListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        saveFile.addActionListener(e -> {
            fileChooser.resetChoosableFileFilters();
            fileChooser.setAcceptAllFileFilterUsed(true);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAccessory(outputTypeChooser);

            int action = fileChooser.showDialog(this, "Select Directory");

            saveOutputListener.saveOutputOccurred(new SaveOutputEvent(e, outputTypeChooser.getChosenOutput(),
                    fileChooser.getSelectedFile().getAbsolutePath(), action));

        });

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


}




