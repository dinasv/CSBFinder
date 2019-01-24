package MVC.View.Components;

import MVC.View.Components.Dialogs.InputFileChooser;
import MVC.View.Components.Dialogs.OutputTypeChooser;
import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.SaveOutputListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 */
public class Menu implements ActionListener {

    private static final String LOAD_GENOMES = "Genomes File";
    private static final String LOAD_SESSION = "Session File";
    private static final String LOAD_COG_INFO = "Orthology Information File";
    private static final String SAVE_FILES_DIALOG_NAME = "Select Directory";
    private static final String SAVE_FILES = "Save";

    private LoadFileListener loadGenomesListener;
    private LoadFileListener importSessionListener;
    private LoadFileListener loadCogInfoListener;
    private SaveOutputListener saveOutputListener;

    private JMenuBar mainMenu;
    private JMenu menu;
    private JMenu submenuImport;
    private JMenuItem importGenomesMenuItem;
    private JMenuItem importSessionMenuItem;
    private JMenuItem importOrthologyInfoMenuItem;
    private JMenuItem saveItem;

    private JFileChooser fileChooser;
    private OutputTypeChooser outputTypeChooser;

    private JFrame mainFrame;

    public Menu(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
        outputTypeChooser = new OutputTypeChooser();
        mainMenu = new JMenuBar();
        this.mainFrame = mainFrame;
        this.mainFrame.setJMenuBar(mainMenu);

        createFileMenu();

        saveItem.addActionListener(this);
        importGenomesMenuItem.addActionListener(this);
        importSessionMenuItem.addActionListener(this);
        importOrthologyInfoMenuItem.addActionListener(this);
    }

    private void createFileMenu(){
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        mainMenu.add(menu);

        //Import
        submenuImport = new JMenu("Import");
        submenuImport.setMnemonic(KeyEvent.VK_I);

        importGenomesMenuItem = new JMenuItem(LOAD_GENOMES);
        importSessionMenuItem = new JMenuItem(LOAD_SESSION);
        importOrthologyInfoMenuItem = new JMenuItem(LOAD_COG_INFO);

        submenuImport.add(importGenomesMenuItem);
        submenuImport.add(importSessionMenuItem);
        submenuImport.add(importOrthologyInfoMenuItem);
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));

        //Save
        saveItem = new JMenuItem(SAVE_FILES);
        saveItem.setMnemonic(KeyEvent.VK_S);

        menu.add(saveItem);
        menu.add(submenuImport);
    }

    public void enableSaveFileBtn() {
        saveItem.setEnabled(true);
    }

    public void disableSaveFileBtn() {
        saveItem.setEnabled(false);
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
            case SAVE_FILES:
                initOutputFileChooser();
                int action = fileChooser.showDialog(mainFrame, SAVE_FILES_DIALOG_NAME);
                saveOutputListener.saveOutputOccurred(new SaveOutputEvent(e, outputTypeChooser.getChosenOutput(),
                        outputTypeChooser.getDatasetName(), fileChooser.getSelectedFile().getAbsolutePath(), action));

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
        int action = fileChooser.showDialog(mainFrame, e.getActionCommand());

        if (action == JFileChooser.APPROVE_OPTION) {
            listener.loadFileEventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
        }
    }

    private void initOutputFileChooser(){
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAccessory(outputTypeChooser);
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

}
