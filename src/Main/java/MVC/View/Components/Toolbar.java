package MVC.View.Components;

import MVC.View.Components.Dialogs.GenomeFileChooser;
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

public class Toolbar extends JPanel {

    private JButton loadFile;
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

    public Toolbar(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;

        setBorder(BorderFactory.createEtchedBorder());
        loadFile = new JButton("Load Input Genomes");
        importSession = new JButton("Import Session");
        loadCogInfo = new JButton("Load Orthology Info");
        saveFile =  new JButton("Save");
        saveFile.setEnabled(false);
        selectParams =  new JButton("Run");
        selectParams.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(loadFile);
        add(importSession);
        add(loadCogInfo);
        add(selectParams);
        add(saveFile);

        loadFile.addActionListener(e -> {
            fileChooser.addChoosableFileFilter(new GenomeFileChooser());

            int value = fileChooser.showOpenDialog(this);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadGenomesListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        importSession.addActionListener(e -> {

            int value = fileChooser.showOpenDialog(this);

            if (value == JFileChooser.APPROVE_OPTION) {
                importSessionListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        loadCogInfo.addActionListener(e -> {

            int value = fileChooser.showOpenDialog(this);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadCogInfoListener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
            }
        });

        saveFile.addActionListener(e -> {
            saveOutputListener.saveOutputOccurred(new SaveOutputEvent());
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




