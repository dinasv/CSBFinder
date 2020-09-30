package mvc.view.components;

import mvc.view.components.dialogs.FileTypeFilter;
import mvc.view.events.*;
import mvc.view.listeners.Listener;
import model.OutputType;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 */
public class Menu implements ActionListener {

    private static final String LOAD_GENOMES = "Genomes File";

    private static final String LOAD_COG_INFO = "Orthology Information File";
    private static final String LOAD_TAXA = "Taxonomy File";
    private static final String LOAD_METADATA = "Genome Metadata File";
    private static final String SAVE_FILES = "Save";
    private static final String SAVE_AS_FILES = "Save As...";
    private static final String EXPORT_FILES = "Export";
    private static final String EXPORT_TXT = "*.txt";
    private static final String EXPORT_XLS = "*.xlsx";
    private static final String OPEN = "Open...";
    private static final String SETTINGS = "Settings";
    private static final String[] LOAD_EXTENSIONS = {"fasta", "txt"};
    private static final String[] CSV_FILE_EXTENSION = {"csv"};

    private String sessionFileExtension;

    private Listener<FileEvent> loadGenomesListener;
    private Listener<FileEvent> importSessionListener;
    private Listener<FileEvent> loadCogInfoListener;
    private Listener<FileEvent> loadTaxaListener;
    private Listener<FileEvent> loadMetadataListener;
    private Listener<OpenExportDialogEvent> exportListener;
    private Listener<OpenDialogEvent> saveListener;
    private Listener<OpenDialogEvent> saveAsListener;
    private Listener<OpenDialogEvent> settingsListener;

    private JMenuBar mainMenu;
    private JMenu menu;
    private JMenu submenuImport;
    private JMenuItem importGenomesMenuItem;

    private JMenuItem importOrthologyInfoMenuItem;
    private JMenuItem importTaxaMenuItem;
    private JMenuItem importMetadataMenuItem;
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JMenu submenuExport;
    private JMenuItem exportItemTxt;
    private JMenuItem exportItemXsl;
    private JMenuItem openItem;
    private JMenuItem settingsItem;

    private JFileChooser fileChooser;

    private JFrame mainFrame;

    public Menu(JFileChooser fileChooser, JFrame mainFrame, String sessionFileExtension){
        this.fileChooser = fileChooser;
        this.mainFrame = mainFrame;
        this.sessionFileExtension = sessionFileExtension;

        mainMenu = new JMenuBar();
        this.mainFrame.setJMenuBar(mainMenu);

        createFileMenu();

        saveItem.addActionListener(this);
        importGenomesMenuItem.addActionListener(this);

        importOrthologyInfoMenuItem.addActionListener(this);
        importTaxaMenuItem.addActionListener(this);
        importMetadataMenuItem.addActionListener(this);
        openItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        exportItemTxt.addActionListener(this);
        exportItemXsl.addActionListener(this);
        settingsItem.addActionListener(this);
    }

    private void createFileMenu(){
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        mainMenu.add(menu);

        //Import
        submenuImport = new JMenu("Import");
        submenuImport.setMnemonic(KeyEvent.VK_I);

        importGenomesMenuItem = new JMenuItem(LOAD_GENOMES);
        importGenomesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));

        importOrthologyInfoMenuItem = new JMenuItem(LOAD_COG_INFO);
        importTaxaMenuItem = new JMenuItem(LOAD_TAXA);
        importMetadataMenuItem = new JMenuItem(LOAD_METADATA);

        submenuImport.add(importGenomesMenuItem);
        submenuImport.add(importOrthologyInfoMenuItem);
        submenuImport.add(importTaxaMenuItem);
        submenuImport.add(importMetadataMenuItem);

        //Save
        saveItem = new JMenuItem(SAVE_FILES);
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

        //Save
        saveAsItem = new JMenuItem(SAVE_AS_FILES);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK+InputEvent.CTRL_MASK));

        submenuExport = new JMenu(EXPORT_FILES);
        submenuExport.setMnemonic(KeyEvent.VK_E);

        exportItemTxt = new JMenuItem(EXPORT_TXT);
        exportItemTxt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        exportItemXsl = new JMenuItem(EXPORT_XLS);
        exportItemXsl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        submenuExport.add(exportItemTxt);
        submenuExport.add(exportItemXsl);

        openItem = new JMenuItem(OPEN);

        menu.add(openItem);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));

        menu.add(submenuImport);

        menu.addSeparator();

        menu.add(saveItem);
        menu.add(saveAsItem);

        menu.addSeparator();

        settingsItem = new JMenuItem(SETTINGS);
        menu.add(settingsItem);

        menu.addSeparator();

        menu.add(submenuExport);

    }

    public void enableSaveFileBtn() {
        saveItem.setEnabled(true);
    }

    public void disableSaveBtn() {
        saveItem.setEnabled(false);
    }

    public void enableSaveAsFileBtn() {
        saveAsItem.setEnabled(true);
    }

    public void disableSaveAsBtn() {
        saveAsItem.setEnabled(false);
    }

    public void enableExportBtn() {
        exportItemTxt.setEnabled(true);
        exportItemXsl.setEnabled(true);
    }

    public void disableExportBtn() {
        exportItemTxt.setEnabled(false);
        exportItemXsl.setEnabled(false);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()){
            case LOAD_GENOMES:
                initInputFileChooser(e.getActionCommand(), LOAD_EXTENSIONS);
                loadEventOccured(e, loadGenomesListener);

                break;
            case OPEN:
                String[] extensions = {sessionFileExtension};
                initInputFileChooser(e.getActionCommand(), extensions);
                loadEventOccured(e, importSessionListener);

                break;
            case LOAD_COG_INFO:
                initInputFileChooser(e.getActionCommand(), LOAD_EXTENSIONS);
                loadEventOccured(e, loadCogInfoListener);

                break;
            case LOAD_TAXA:

                initInputFileChooser(e.getActionCommand(), LOAD_EXTENSIONS);
                loadEventOccured(e, loadTaxaListener);

                break;
            case LOAD_METADATA:

                initInputFileChooser(e.getActionCommand(), CSV_FILE_EXTENSION);
                loadEventOccured(e, loadMetadataListener);

                break;
            case SAVE_FILES:
                saveListener.eventOccurred(new OpenDialogEvent());
                break;
            case SAVE_AS_FILES:
                saveAsListener.eventOccurred(new OpenDialogEvent());
                break;

            case EXPORT_TXT:
                exportListener.eventOccurred(new OpenExportDialogEvent(OutputType.TXT));
                break;

            case EXPORT_XLS:
                exportListener.eventOccurred(new OpenExportDialogEvent(OutputType.XLSX));
                break;

            case SETTINGS:
                settingsListener.eventOccurred(new OpenDialogEvent());
                break;
        }
    }

    private void initInputFileChooser(String action, String[] extensions){

        fileChooser.resetChoosableFileFilters();
        fileChooser.addChoosableFileFilter(new FileTypeFilter(extensions));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(action);

    }

    private void loadEventOccured(ActionEvent e, Listener<FileEvent> listener){

        int action = fileChooser.showDialog(mainFrame, "Import");

        if (action == JFileChooser.APPROVE_OPTION) {
            listener.eventOccurred(new FileEvent(e, fileChooser.getSelectedFile()));
        }
    }


    public void setImportSessionListener(Listener<FileEvent> importSessionListener) {
        this.importSessionListener = importSessionListener;
    }

    public void setLoadGenomesListener(Listener<FileEvent> loadGenomesListener) {
        this.loadGenomesListener = loadGenomesListener;
    }

    public void setLoadCogInfoListener(Listener<FileEvent> loadCogInfoListener) {
        this.loadCogInfoListener = loadCogInfoListener;
    }

    public void setLoadTaxaListener(Listener<FileEvent> loadTaxaListener) {
        this.loadTaxaListener = loadTaxaListener;
    }

    public void setLoadMetadataListener(Listener<FileEvent> loadMetadataListener) {
        this.loadMetadataListener = loadMetadataListener;
    }

    public void setExportListener(Listener<OpenExportDialogEvent> exportListener) {
        this.exportListener = exportListener;
    }

    public void setSaveListener(Listener<OpenDialogEvent> listener) {
        this.saveListener = listener;
    }

    public void setSaveAsListener(Listener<OpenDialogEvent> listener) {
        this.saveAsListener = listener;
    }

    public void setSettingsListener(Listener<OpenDialogEvent> listener) {
        this.settingsListener = listener;
    }



}
