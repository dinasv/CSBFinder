package MVC.View.Components;

import MVC.ProgramProperties;
import MVC.View.Graphics.GeneColors;
import MVC.View.Listeners.EventListener;
import MVC.View.Requests.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.Dialogs.*;
import MVC.View.Components.Panels.*;
import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Components.Shapes.Label;
import MVC.View.Events.*;
import MVC.View.Graphics.Icon;
import MVC.View.Listeners.*;
import Model.Genomes.Alphabet;
import Model.Genomes.Gene;
import Model.Genomes.GenomesInfo;
import Model.Genomes.Taxon;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Tables.Filters.FamiliesFilter;
import MVC.View.Requests.FilterRequest;
import com.beust.jcommander.ParameterException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final String PROGRAM_NAME = "CSBFinder";
    private static final String DEFAULT_SESSION_NAME = "Session1";
    private static final String RUNNING_MSG = "Running...";
    private static final String EXPORT_MSG = "Saving...";
    private static final String LOADING_MSG = "Loading File";
    private static final int MSG_WIDTH = 500;

    private static final String SESSION_FILE_EXTENSION = "csb";

    private File currentSessionFile;

    private static final int ZOOM_MIN = 6;
    private static final int ZOOM_MAX = 32;
    private static final int ZOOM_UNIT = 2;
    private int zoom;

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private CSBFinderController controller;

    private Menu menuBar;
    private Toolbar toolbar;
    private StatusBar statusBar;

    private MiddlePanel middlePanel;
    private SummaryPanel summaryPanel;
    private ProgressBar progressBar;

    private InputParametersDialog inputParamsDialog;
    private ClusterDialog clusterDialog;
    private RankDialog rankDialog;
    private FilterDialog filterDialog;
    private ExportDialog exportDialog;
    private SaveDialog saveDialog;
    private SaveAsDialog saveAsDialog;

    private FamiliesFilter familiesFilter;

    private TablesHistory tablesHistory;

    private JFileChooser fc;

    private Listener<FileEvent> loadSessionListener;
    private Listener<FileEvent> loadTaxaListener;
    private Listener<FileEvent> loadCogInfoListener;
    private Listener<FileEvent> saveListener;
    private Listener<OpenDialogEvent> saveAsListener;

    private ProgramProperties properties;
    private GeneColors colorsUsed;

    public MainFrame(CSBFinderController controller, ProgramProperties properties) {

        super(formatProgramTitle(DEFAULT_SESSION_NAME));

        this.controller = controller;
        this.properties = properties;

        progressBar = new ProgressBar(this);

        fc = new JFileChooser(System.getProperty("user.dir"));
        familiesFilter = new FamiliesFilter();
        tablesHistory = new TablesHistory();

        colorsUsed = new GeneColors();
        colorsUsed.setColor(controller.getUNKchar(), Color.lightGray);
        controller.setGeneColors(colorsUsed);

        setLayout(new BorderLayout());
        initComponents();
        init();
    }

    public void init() {
        currentSessionFile = null;
        zoom = Label.DEFAULT_FONT.getSize();

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(450, 350));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void clearPanels() {
        tablesHistory.clearAll();
        middlePanel.clearPanel();
        summaryPanel.clearPanel();

        toolbar.disablRankeBtn();
        toolbar.disableClusterBtn();

        menuBar.disableSaveAsBtn();
        menuBar.disableExportBtn();
        summaryPanel.disableFilterBtn();

        statusBar.clearText();

        disableSaveBtns();
    }

    private void disableSaveBtns(){
        toolbar.disableSaveBtn();
        menuBar.disableSaveBtn();
    }

    public void initComponents() {

        inputParamsDialog = new InputParametersDialog(fc);
        clusterDialog = new ClusterDialog();
        rankDialog = new RankDialog();
        filterDialog = new FilterDialog();
        exportDialog = new ExportDialog(fc, this);
        saveDialog = new SaveDialog(this);
        saveAsDialog = new SaveAsDialog(fc, this, SESSION_FILE_EXTENSION);

        toolbar = new Toolbar();
        statusBar = new StatusBar();
        middlePanel = new MiddlePanel(colorsUsed);

        summaryPanel = new SummaryPanel(Icon.FILTER.getIcon());

        menuBar = new Menu(fc, this, SESSION_FILE_EXTENSION);

        disableBtnsInit();

        setEventListeners();

        add(toolbar, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(middlePanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, top, summaryPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

    }

    private void setEventListeners() {
        setRunCSBFinderListener();
        setRunClusteringListener();
        setComputeScoreListener();
        setSelectParamsListener();
        setOpenExportDialogListener();
        setClusterListener();
        setRankListener();
        setPatternRowClickedListener();
        setFamilyRowClickedListener();
        setFilterTableListener();
        setApplyFilterListener();
        setNumOfNeighborsListener();
        setShowOnlyTablesListener();
        setCSBFinderDoneListener();
        setGeneTooltipListener();
        setGeneDoubleClickListener();
        setLoadButtonListener();
        setImportSessionButtonListener();
        setLoadCogInfoButtonListener();
        setLoadTaxaListener();
        setExportButtonListener();
        setSaveDialogListener();
        setSaveListener();
        setSaveAsDialogListener();
        setZoomOutListener();
        setZoomInListener();
        setShowSaveMsgListener();
    }

    private void displayFamilyTable(List<Family> familyList) {
        if (familyList.size() > 0) {
            enableBtnsResultsDisplay();

            controller.calculateMainFunctionalCategory();

            summaryPanel.setFamilyData(familyList);
            familiesFilter.setFamilies(familyList);

            statusBar.updateStatus(familyList);
        }
    }

    private void enableBtnsResultsDisplay(){
        menuBar.enableSaveAsFileBtn();
        menuBar.enableExportBtn();
        toolbar.enableRankBtn();
        toolbar.enableZoomOutBtn();
        toolbar.enableZoomInBtn();
        summaryPanel.enableFilterBtn();
        toolbar.enableClusterBtn();
        enableSaveBtns();
    }

    private void enableSaveBtns(){
        menuBar.enableSaveFileBtn();
        toolbar.enableSaveBtn();
    }


    private void disableBtnsInit(){
        toolbar.disableSelectParamsBtn();
        toolbar.disablRankeBtn();
        toolbar.disabZoomOutBtn();
        toolbar.disabZoomInBtn();

        menuBar.disableExportBtn();
        menuBar.disableSaveAsBtn();

        summaryPanel.disableFilterBtn();

        disableSaveBtns();
    }

    private void setGenomesData(){
        familiesFilter.clear();

        if (controller.getNumberOfGenomes() > 0) {
            inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            filterDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            middlePanel.setGenomesInfo(controller.getGenomeInfo());

            setGeneColors(controller.getGenomeInfo());
            toolbar.enableSelectParamsBtn();

        } else {
            disableBtnsInit();
        }
    }

    private void setGeneColors(GenomesInfo genomesInfo){
        genomesInfo.getAlphabetLetters().forEachRemaining(colorsUsed::setColor);
    }

    private void setFilters(FilterRequest filterRequest){
        familiesFilter.clear();

        filterRequest.getMinCSBLength().ifPresent(val -> familiesFilter.setPatternMinLength(val));
        filterRequest.getMaxCSBLength().ifPresent(val -> familiesFilter.setPatternMaxLength(val));
        filterRequest.getMinScore().ifPresent(val -> familiesFilter.setPatternMinScore(val));
        filterRequest.getMaxScore().ifPresent(val -> familiesFilter.setPatternMaxScore(val));
        filterRequest.getMinInstanceCount().ifPresent(val -> familiesFilter.setPatternMinCount(val));
        filterRequest.getMaxInstanceCount().ifPresent(val -> familiesFilter.setPatternMaxCount(val));
        filterRequest.getPatternIds().ifPresent(val -> familiesFilter.setPatternIds(val));
        filterRequest.getFamilyIds().ifPresent(val -> familiesFilter.setFamilyIds(val));
        filterRequest.getPatternStrand().ifPresent(val -> familiesFilter.setStrand(val));
        filterRequest.getPatternGenes().ifPresent(val -> familiesFilter.setGenes(val, filterRequest.getGenesOperator()));
        filterRequest.getMainFunctionalCategory().ifPresent(val -> familiesFilter.setFunctionalCategory(val,
                filterRequest.getFunctionalCategoryOption()));

        Function<Gene[], List<String>> genesToCogsDesc = (Gene[] genes) -> {
            List<COG> cogs = controller.getCogsInfo(genes);
            return cogs.stream().map(COG::toString).collect(Collectors.toList());
        };

        filterRequest.getGenesCategory().ifPresent(val -> familiesFilter.setGeneCategory(val,
                filterRequest.getGenesCategoryOperator(), genesToCogsDesc));

        filterRequest.getGenesCategoryExclude().ifPresent(val -> familiesFilter.setGeneCategoryExclude(val,
                genesToCogsDesc));

        familiesFilter.applyFilters();

    }


    private void tableRowClickFromHistory(){

        TableView tableView = tablesHistory.getTableView();
        if (tableView != TableView.NONE) {

            Family family = tablesHistory.getFamily();

            summaryPanel.selectFamily(family.getFamilyId());

            if (tableView == TableView.PATTERN) {
                summaryPanel.selectPattern(tablesHistory.getPattern().getPatternId());
            }
        }
    }

    private void setShowSaveMsgListener(){
        Listener<DontShowSaveMsgEvent> listener = event -> {
            properties.setShowSaveMsg(event.isShowSaveMsg());
        };

        saveDialog.setDontShowMsgListener(listener);
    }


    private void setApplyFilterListener() {

        Function<FilterRequest, String> doInBackgroundFunc = (FilterRequest request) -> {
            filterDialog.setVisible(false);
            middlePanel.clearPanel();
            setFilters(request);

            List<Family> filteredFamilies = familiesFilter.getFilteredFamilies();
            summaryPanel.setFilteredFamilies(filteredFamilies);
            statusBar.updateStatus(filteredFamilies);

            tableRowClickFromHistory();

            return null;
        };

        Consumer<FilterRequest> doneFunc = request -> {
            progressBar.done("");
            enableSaveBtns();
        };

        RequestListener<FilterRequest> listener = new RequestListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar);
        filterDialog.setApplyFilterListener(listener);

    }

    private void setRunClusteringListener() {

        Function<CSBFinderRequest, String> doInBackgroundFunc = (CSBFinderRequest request) -> {

            clusterDialog.setVisible(false);
            clearPanels();

            try {
                controller.clusterToFamilies(request.getFamilyClusterThreshold(),
                        request.getClusterType(), request.getClusterDenominator());
            } catch (ParameterException exception) {
                return exception.getMessage();
            }
            return null;
        };

        Consumer<CSBFinderRequest> doneFunc = request -> {
            progressBar.done("");
            enableSaveBtns();
        };

        RequestListener<CSBFinderRequest> listener = new RequestListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar);
        clusterDialog.setRunListener(listener);

    }

    private void setComputeScoreListener() {

        Function<CSBFinderRequest, String> doInBackgroundFunc = (CSBFinderRequest request) -> {
            rankDialog.setVisible(false);
            clearPanels();

            try {
                controller.computeScores(request.getGenomesDistanceThreshold());
            } catch (ParameterException exception) {
                return exception.getMessage();
            }
            return null;
        };

        Consumer<CSBFinderRequest> doneFunc = request -> {
            progressBar.done("");
            enableSaveBtns();
        };

        RequestListener<CSBFinderRequest> listener = new RequestListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar);
        rankDialog.setRunListener(listener);

    }


    private void setRunCSBFinderListener() {

        Function<CSBFinderRequest, String> doInBackgroundFunc = (CSBFinderRequest request) -> {
            inputParamsDialog.setVisible(false);
            clearPanels();
            request.setInputGenomeFilesPath(controller.getInputGenomesPath());
            try {
                controller.findCSBs(request);
            } catch (IOException exception) {
                return exception.getMessage();
            }
            return null;
        };

        Consumer<CSBFinderRequest> doneFunc = request -> {
            progressBar.done("");
        };

        RequestListener<CSBFinderRequest> listener = new RequestListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar);
        inputParamsDialog.setRunListener(listener);

    }

    private void setClusterListener() {
        toolbar.setClusterListener(e -> {
            clusterDialog.setLocationRelativeTo(null);
            clusterDialog.setVisible(true);
        });
    }

    private void setRankListener() {
        toolbar.setRankListener(e -> {
            rankDialog.setLocationRelativeTo(null);
            rankDialog.setVisible(true);
        });
    }

    private void setSelectParamsListener() {
        toolbar.setSelectParamsListener(e -> {
            inputParamsDialog.setLocationRelativeTo(null);
            inputParamsDialog.setVisible(true);
        });
    }

    private void setOpenExportDialogListener(){
        Listener<OpenExportDialogEvent> listener = e -> exportDialog.openDialog(e.getOutputType());

        menuBar.setExportListener(listener);
    }

    private void setSaveDialogListener(){
        Listener<OpenDialogEvent> listener = event -> {

            if (currentSessionFile == null) {
                saveAsListener.eventOccurred(new OpenDialogEvent());
            } else {
                int value;
                if (properties.showSaveMsg()) {
                    value = saveDialog.showDialog();
                }else{
                    value = JOptionPane.YES_OPTION; //save
                }

                if (value == JOptionPane.YES_OPTION) {
                    saveListener.eventOccurred(new FileEvent(this, currentSessionFile));
                }else if (value == JOptionPane.NO_OPTION){
                    saveAsListener.eventOccurred(new OpenDialogEvent());
                }
            }

        };
        menuBar.setSaveListener(listener);
        toolbar.setSaveListener(listener);
    }


    private void setSaveListener(){

        Function<FileEvent, String> doInBackgroundFunc = (FileEvent e) -> {

            controller.saveSession(familiesFilter.getFilteredFamilies(), e.getFile(), colorsUsed);
            setCurrentSessionFile(e.getFile());

            return null;
        };

        Consumer<FileEvent> doneFunc = request -> {
            progressBar.done("");
            disableSaveBtns();
        };

        saveListener = new EventListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar, EXPORT_MSG);

    }

    private void setSaveAsDialogListener(){
        saveAsListener = e -> {
            int action = saveAsDialog.openDialog();

            if (action != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fc.getSelectedFile();
            String ext = FileTypeFilter.getExtension(file);

            if (!SESSION_FILE_EXTENSION.equals(ext)){
                file = new File(file.toString() + "." + SESSION_FILE_EXTENSION);
            }

            if (file.exists()){
                int value = JOptionPane.showConfirmDialog(this,
                        "By clicking on \"OK\" you will overwrite the file " + file.toString(),
                        "Overwrite",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (value != JOptionPane.OK_OPTION){
                    return;
                }
            }

            saveListener.eventOccurred(new FileEvent(this, file));
        };

        menuBar.setSaveAsListener(saveAsListener);
    }


    private void setNumOfNeighborsListener() {
        toolbar.setNumOfNeighborsListener(e -> middlePanel.setNumOfNeighbors(e.getNumOfNeighbors()));
    }

    private void setShowOnlyTablesListener() {
        toolbar.setShowOnlyTablesListener(e -> {
            boolean isShowOnlyTables = e.isShowOnlyTables();
            if (isShowOnlyTables){
                middlePanel.clearPanel();
            }else{
                tableRowClickFromHistory();
            }
        });
    }

    private void setFilterTableListener() {
        summaryPanel.setFilterTableListener(e -> {
            filterDialog.setLocationRelativeTo(null);
            filterDialog.setVisible(true);
        });
    }


    private void setExportButtonListener() {

        Function<ExportEvent, String> doInBackgroundFunc = (ExportEvent e) -> {

            controller.exportFiles(e.getOutputType(), e.getOutputDirectory(),
                        e.getDatasetName(), familiesFilter.getFilteredFamilies());


            return null;
        };

        Consumer<ExportEvent> doneFunc = request -> {
            progressBar.done("");
        };

        EventListener<ExportEvent> listener = new EventListener<>(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar, EXPORT_MSG);
        exportDialog.setListener(listener);

    }

    private void setZoomOutListener(){
        toolbar.setZoomOutListener(event -> {
            if (zoom <= ZOOM_MIN) {
                toolbar.disabZoomOutBtn();
                return;
            }

            toolbar.enableZoomInBtn();
            zoom -= ZOOM_UNIT;
            SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    middlePanel.zoomOut(ZOOM_UNIT);
                    return null;
                }
            };

            swingWorker.execute();

        });
    }

    private void setZoomInListener(){
            toolbar.setZoomInListener(event -> {
                if (zoom >= ZOOM_MAX) {
                    toolbar.disabZoomInBtn();
                    return;
                }

                toolbar.enableZoomOutBtn();
                zoom += ZOOM_UNIT;
                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        middlePanel.zoomIn(ZOOM_UNIT);
                        return null;
                    }
                };

                swingWorker.execute();

            });
        }

    private String formatMsgWidth(String msg){
        String html = "<html><body width='%s'>%s";
        return String.format(html, MSG_WIDTH, msg);
    }

    public void invokeLoadSessionListener(String path){
        loadSessionListener.eventOccurred(new FileEvent(this, new File(path)));
    }

    public void invokeLoadCogInfoListener(String path){
        loadCogInfoListener.eventOccurred(new FileEvent(this, new File(path)));
    }

    public void invokeLoadTaxaListener(String path){
        loadTaxaListener.eventOccurred(new FileEvent(this, new File(path)));
    }

    /**
     * Load files listeners
     */
    private void setLoadButtonListener() {

        Function<FileEvent, String> doInBackgroundFunc = (FileEvent e) -> {
            clearPanels();
            try {
                File f = e.getFile();
                controller.loadInputGenomesFile(f.getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<FileEvent> doneFunc = (FileEvent e) -> {
            clearPanels();
            setGenomesData();
            setTitle(formatProgramTitle(DEFAULT_SESSION_NAME));
        };

        Listener<FileEvent> loadGenomesListener = new LoadFileListener(doInBackgroundFunc, doneFunc,
                MainFrame.this, progressBar);

        menuBar.setLoadGenomesListener(loadGenomesListener);
    }

    private void setImportSessionButtonListener() {

        Function<FileEvent, String> doInBackgroundFunc = (FileEvent e) -> {
            clearPanels();
            try {
                controller.loadSessionFile(e.getFile().getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<FileEvent> doneFunc = (FileEvent e) -> {
            setGenomesData();
            setCurrentSessionFile(e.getFile());
            disableSaveBtns();
        };

        loadSessionListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);

        menuBar.setImportSessionListener(loadSessionListener);
    }

    private void setCurrentSessionFile(File file){
        setTitle(formatProgramTitle(file.getName()));
        currentSessionFile = file;
    }

    private static String formatProgramTitle(String fileName){
        return String.format("%s - %s", PROGRAM_NAME, fileName);
    }

    private void setLoadCogInfoButtonListener() {
        Function<FileEvent, String> doInBackgroundFunc = (FileEvent e) -> {
            try {
                controller.loadCogInfo(e.getFile().getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<FileEvent> doneFunc = (FileEvent f) -> {
            tableRowClickFromHistory();
        };

        loadCogInfoListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);
        menuBar.setLoadCogInfoListener(loadCogInfoListener);
    }

    private void setLoadTaxaListener() {

        Function<FileEvent, String> doInBackgroundFunc = (FileEvent e) -> {
            try {
                controller.loadTaxa(e.getFile().getPath());
                Map<String, Taxon> genomeToTaxa = controller.getGenomeToTaxa();
                middlePanel.setGenomeToTaxa(genomeToTaxa);
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<FileEvent> doneFunc = (FileEvent e) -> {
            tableRowClickFromHistory();
        };

        loadTaxaListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);
        menuBar.setLoadTaxaListener(loadTaxaListener);
    }


    /**
     * Tables click listeners
     */
    private void setPatternRowClickedListener() {
        summaryPanel.setPatternRowClickedListener(event -> {
            Pattern pattern = event.getRow();

            tablesHistory.setPattern(pattern);
            middlePanel.clearPanel();
            middlePanel.setNumOfNeighbors(toolbar.getNumOfNeighbors());
            if (!toolbar.isShowOnlyTables()) {
                middlePanel.displayInstances(pattern);
            }

            List<COG> patternCOGs = controller.getCogsInfo(pattern.getPatternGenes());
            Set<COG> insertedGenes = controller.getInsertedGenes(pattern, patternCOGs);
            summaryPanel.setCogInfo(patternCOGs, insertedGenes, colorsUsed);
        });
    }

    private void setFamilyRowClickedListener() {
        summaryPanel.setFamilyRowClickedListener(event -> {
            Family family = event.getRow();

            tablesHistory.setFamily(family);
            summaryPanel.setFamilyPatternsData(family);
            middlePanel.clearPanel();

            if (!toolbar.isShowOnlyTables()) {
                middlePanel.displayPatterns(family.getPatterns());
            }

            List<COG> patternCOGs = new ArrayList<>();
            for (Pattern pattern : family.getPatterns()) {
                patternCOGs.addAll(controller.getCogsInfo(pattern.getPatternGenes()));
            }
            summaryPanel.setCogInfo(patternCOGs, new HashSet<>(), colorsUsed);
        });
    }

    private void setGeneTooltipListener() {
        middlePanel.setGeneTooltipListener(event -> {
            COG cog = controller.getCogInfo(event.getCogId());
            if (cog != null){
                event.getSrc().setToolTipText(String.format("<html>%s<br>%s | %s</html>",
                        String.join("/", cog.getFunctionalCategories()), cog.getCogDesc(), cog.getGeneName()));
            }
        });
    }


    /**
     * When gene is double clicked, align all identical genes in other instances on the same vertical line
     */
    private void setGeneDoubleClickListener() {
        middlePanel.setGeneDoubleClickListener(event -> {
            GeneShape anchorGene = event.getAnchorGene();
            if (anchorGene == null){
                return;
            }

            String cogId = anchorGene.getLabel().getText();
            if (!cogId.equals(Alphabet.UNK_CHAR)) {

                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, event.getSrc());
                if (viewPort != null) {
                    Rectangle view = viewPort.getViewRect();
                    middlePanel.alignGenes(anchorGene, event.getSrc(), view.x);
                }

            }
        });
    }

    private void setCSBFinderDoneListener() {
        controller.setCSBFinderDoneListener(e -> displayFamilyTable(e.getFamilyList()));
    }

}
