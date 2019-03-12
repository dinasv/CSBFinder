package MVC.View.Components;

import MVC.View.Components.Dialogs.InputFileChooser;
import MVC.View.Components.Dialogs.OutputTypeChooser;
import MVC.View.Components.Dialogs.SaveDialog;
import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.OpenDialogEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.Listener;


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
    private static final String SAVE_FILES = "Save";

    private Listener<LoadFileEvent> loadGenomesListener;
    private Listener<LoadFileEvent> importSessionListener;
    private Listener<LoadFileEvent> loadCogInfoListener;
    private Listener<OpenDialogEvent> saveOutputListener;

    private JMenuBar mainMenu;
    private JMenu menu;
    private JMenu submenuImport;
    private JMenuItem importGenomesMenuItem;
    private JMenuItem importSessionMenuItem;
    private JMenuItem importOrthologyInfoMenuItem;
    private JMenuItem saveItem;

    private JFileChooser fileChooser;

    private JFrame mainFrame;

    public Menu(JFileChooser fileChooser, JFrame mainFrame){
        this.fileChooser = fileChooser;
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
                initInputFileChooser(e.getActionCommand());
                loadEventOccured(e, loadGenomesListener);

                break;
            case LOAD_SESSION:
                initInputFileChooser(e.getActionCommand());
                loadEventOccured(e, importSessionListener);

                break;
            case LOAD_COG_INFO:
                initInputFileChooser(e.getActionCommand());
                loadEventOccured(e, loadCogInfoListener);

                break;
            case SAVE_FILES:

                saveOutputListener.eventOccurred(new OpenDialogEvent());

                break;
        }
    }

    private void initInputFileChooser(String action){

        fileChooser.addChoosableFileFilter(new InputFileChooser());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogTitle(action);

    }

    private void loadEventOccured(ActionEvent e, Listener<LoadFileEvent> listener){

        int action = fileChooser.showDialog(mainFrame, "Import");

        if (action == JFileChooser.APPROVE_OPTION) {
            listener.eventOccurred(new LoadFileEvent(e, fileChooser.getSelectedFile()));
        }
    }


    public void setImportSessionListener(Listener<LoadFileEvent> importSessionListener) {
        this.importSessionListener = importSessionListener;
    }

    public void setLoadGenomesListener(Listener<LoadFileEvent> loadGenomesListener) {
        this.loadGenomesListener = loadGenomesListener;
    }

    public void setLoadCogInfoListener(Listener<LoadFileEvent> loadCogInfoListener) {
        this.loadCogInfoListener = loadCogInfoListener;
    }

    public void setSaveOutputListener(Listener<OpenDialogEvent> saveOutputListener) {
        this.saveOutputListener = saveOutputListener;
    }



}
