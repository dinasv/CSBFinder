package MVC.View;

import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Events.SelectParamsEvent;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.SaveOutputListener;
import MVC.View.Listeners.SelectParamsListener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel {

    private JButton loadFile;
    private JButton saveFile;
    private JButton selectParams;
    private LoadFileListener loadFileListener;
    private SaveOutputListener saveOutputListener;
    private SelectParamsListener selectParamsListener;

    private JFileChooser fc;

    public Toolbar(JFileChooser fc) {
        this.fc = fc;

        setBorder(BorderFactory.createEtchedBorder());
        loadFile = new JButton("Load Input Genomes");
        saveFile =  new JButton("Save");
        saveFile.setEnabled(false);
        selectParams =  new JButton("Run");
        selectParams.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(loadFile);
        add(selectParams);
        add(saveFile);

        loadFile.addActionListener(e -> {
            fc.addChoosableFileFilter(new GenomeFileChooser());

            int value = fc.showOpenDialog(this);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadFileListener.loadFileEventOccurred(new LoadFileEvent(e, fc.getSelectedFile()));
            }
        });

        saveFile.addActionListener(e -> {
            saveOutputListener.saveOutputOccurred(new SaveOutputEvent());
        });

        selectParams.addActionListener(e -> {
            selectParamsListener.selectParamsOccurred(new SelectParamsEvent());
        });

    }

    public void setLoadListener(LoadFileListener loadFileListener) {
        this.loadFileListener = loadFileListener;
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
}
