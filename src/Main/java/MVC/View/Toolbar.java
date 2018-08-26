package MVC.View;

import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.SaveOutputListener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel {

    private JButton loadFile;
    private JButton saveFile;
    private LoadFileListener loadFileListener;
    private SaveOutputListener saveOutputListener;

    public Toolbar() {
        setBorder(BorderFactory.createEtchedBorder());
        loadFile = new JButton("Load File");
        saveFile =  new JButton("Save File");
        saveFile.setEnabled(false);

        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(loadFile);
        add(saveFile);

        loadFile.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.addChoosableFileFilter(new GenomeFileChooser());

            int value = fc.showOpenDialog(this);

            if (value == JFileChooser.APPROVE_OPTION) {
                loadFileListener.loadFileEventOccurred(new LoadFileEvent(e, fc.getSelectedFile()));
            }
        });

        saveFile.addActionListener(e -> {
            saveOutputListener.saveOutputOccurred(new SaveOutputEvent());
        });

    }

    public void setLoadListener(LoadFileListener loadFileListener) {
        this.loadFileListener = loadFileListener;
    }

    public void setSaveOutputListener(SaveOutputListener saveOutputListener) {
        this.saveOutputListener = saveOutputListener;
    }

    public void enableSaveFileBtn() {
        saveFile.setEnabled(true);
    }
}
