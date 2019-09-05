package MVC.View.Components;

import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.Dialogs.*;
import MVC.View.Components.Panels.*;
import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Components.Shapes.Label;
import MVC.View.Events.*;
import MVC.View.Images.Icon;
import MVC.View.Listeners.*;
import MVC.View.Requests.Request;
import MVC.View.Workers.CustomSwingWorker;
import Model.Genomes.Alphabet;
import Model.Genomes.Gene;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final String PROGRAM_NAME = "CSBFinder";
    private static final String RUNNING_MSG = "Running...";
    private static final String SAVING_MSG = "Saving Files...";
    private static final String LOADING_MSG = "Loading File";
    private static final int MSG_WIDTH = 500;

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
    private SaveDialog saveDialog;

    private FamiliesFilter familiesFilter;

    private TablesHistory tablesHistory;

    private JFileChooser fc;

    private Listener<LoadFileEvent> loadSessionListener;
    private Listener<LoadFileEvent> loadTaxaListener;
    private Listener<LoadFileEvent> loadCogInfoListener;

    public MainFrame(CSBFinderController controller) {
        super(PROGRAM_NAME);

        progressBar = new ProgressBar(this);

        fc = new JFileChooser(System.getProperty("user.dir"));
        familiesFilter = new FamiliesFilter();
        tablesHistory = new TablesHistory();

        this.controller = controller;

        setLayout(new BorderLayout());
        initComponents();
        init();



    }

    public void init() {
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
        toolbar.disableSaveBtn();
        menuBar.disableSaveBtn();
        summaryPanel.disableFilterBtn();

        statusBar.clearText();
    }

    public void initComponents() {
        Map<String, Color> colorsUsed = new HashMap<>();
        colorsUsed.put(controller.getUNKchar(), Color.lightGray);

        inputParamsDialog = new InputParametersDialog(fc);
        clusterDialog = new ClusterDialog();
        rankDialog = new RankDialog();
        filterDialog = new FilterDialog();
        saveDialog = new SaveDialog(fc, this);

        toolbar = new Toolbar();
        statusBar = new StatusBar();
        middlePanel = new MiddlePanel(colorsUsed);

        summaryPanel = new SummaryPanel(Icon.FILTER.getIcon());

        menuBar = new Menu(fc, this);

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
        setOpenSaveDialogListener();
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
        setSaveButtonListener();
        setZoomOutListener();
        setZoomInListener();
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
        menuBar.enableSaveFileBtn();
        toolbar.enableSaveBtn();
        toolbar.enableRankBtn();
        toolbar.enableZoomOutBtn();
        toolbar.enableZoomInBtn();
        summaryPanel.enableFilterBtn();
        toolbar.enableClusterBtn();
    }

    private void disableBtnsInit(){
        toolbar.disableSelectParamsBtn();
        toolbar.disablRankeBtn();
        toolbar.disabZoomOutBtn();
        toolbar.disabZoomInBtn();
        toolbar.disableSaveBtn();
        menuBar.disableSaveBtn();

        summaryPanel.disableFilterBtn();
    }

    private void setGenomesData(String filePath){
        familiesFilter.clear();

        if (controller.getNumberOfGenomes() > 0) {
            inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            filterDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            middlePanel.setGenomesInfo(controller.getGenomeInfo());

            toolbar.enableSelectParamsBtn();

            setTitle(String.format("%s - %s", PROGRAM_NAME, filePath));
        } else {
            disableBtnsInit();
        }
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

        Function<Gene[], String> genesToCogsDesc = (Gene[] genes) -> {
            List<COG> cogs = controller.getCogsInfo(genes);
            return cogs.stream().map(COG::toString).collect(Collectors.joining(" "));
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

    private void setOpenSaveDialogListener(){
        Listener<OpenDialogEvent> listener = e -> saveDialog.openDialog();

        menuBar.setSaveOutputListener(listener);
        toolbar.setSaveListener(listener);
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


    private void setSaveButtonListener() {

        Listener<SaveOutputEvent> listener = new Listener<SaveOutputEvent>() {
            String msg = "";

            @Override
            public void eventOccurred(SaveOutputEvent e) {

                if (e.getAction() == JFileChooser.APPROVE_OPTION) {

                    SwingUtilities.invokeLater(() -> progressBar.start(SAVING_MSG));

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            controller.saveOutputFiles(e.getOutputType(), e.getOutputDirectory(),
                                    e.getDatasetName(), familiesFilter.getFilteredFamilies());
                            return null;
                        }

                        @Override
                        protected void done() {
                            progressBar.done("");
                            //JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                        }
                    };

                    swingWorker.execute();
                }
            }
        };

        saveDialog.setSaveOutputListener(listener);

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
        loadSessionListener.eventOccurred(new LoadFileEvent(this, new File(path)));
    }

    public void invokeLoadCogInfoListener(String path){
        loadCogInfoListener.eventOccurred(new LoadFileEvent(this, new File(path)));
    }

    public void invokeLoadTaxaListener(String path){
        loadTaxaListener.eventOccurred(new LoadFileEvent(this, new File(path)));
    }

    /**
     * Load files listeners
     */
    private void setLoadButtonListener() {

        Function<File, String> doInBackgroundFunc = (File f) -> {
            clearPanels();
            try {
                controller.loadInputGenomesFile(f.getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<File> doneFunc = (File f) -> {
            clearPanels();
            setGenomesData(f.getPath());
        };

        Listener<LoadFileEvent> loadGenomesListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);

        menuBar.setLoadGenomesListener(loadGenomesListener);
    }

    private void setImportSessionButtonListener() {

        Function<File, String> doInBackgroundFunc = (File f) -> {
            clearPanels();
            try {
                controller.loadSessionFile(f.getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<File> doneFunc = (File f) -> {
            setGenomesData(f.getPath());
        };

        loadSessionListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);

        menuBar.setImportSessionListener(loadSessionListener);
    }

    private void setLoadCogInfoButtonListener() {
        Function<File, String> doInBackgroundFunc = (File f) -> {
            try {
                controller.loadCogInfo(f.getPath());
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<File> doneFunc = (File f) -> {
            tableRowClickFromHistory();
        };

        loadCogInfoListener = new LoadFileListener(doInBackgroundFunc, doneFunc, MainFrame.this, progressBar);
        menuBar.setLoadCogInfoListener(loadCogInfoListener);
    }

    private void setLoadTaxaListener() {

        Function<File, String> doInBackgroundFunc = (File f) -> {
            try {
                controller.loadTaxa(f.getPath());
                Map<String, Taxon> genomeToTaxa = controller.getGenomeToTaxa();
                middlePanel.setGenomeToTaxa(genomeToTaxa);
            }catch (IOException exception){
                return exception.getMessage();
            }
            return null;
        };

        Consumer<File> doneFunc = (File f) -> {
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
            summaryPanel.setCogInfo(patternCOGs, insertedGenes, middlePanel.getColorsUsed());
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
            summaryPanel.setCogInfo(patternCOGs, new HashSet<>(), middlePanel.getColorsUsed());
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
                    middlePanel.alignGenes(anchorGene, view.x);
                }

            }
        });
    }

    private void setCSBFinderDoneListener() {
        controller.setCSBFinderDoneListener(e -> displayFamilyTable(e.getFamilyList()));
    }

}
