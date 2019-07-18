package MVC.View.Components;

import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.Dialogs.*;
import MVC.View.Components.Panels.TableView;
import MVC.View.Components.Panels.TablesHistory;
import MVC.View.Components.Shapes.GeneShape;
import MVC.View.Events.*;
import MVC.View.Images.Icon;
import MVC.View.Listeners.*;
import MVC.View.Components.Panels.GenomePanel;
import MVC.View.Components.Panels.SummaryPanel;
import Model.Genomes.Alphabet;
import Model.Genomes.Gene;
import Model.Genomes.Strand;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Tables.Filters.FamiliesFilter;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private static final String PROGRAM_NAME = "CSBFinder";
    private static final String RUNNING_MSG = "Running...";
    private static final String SAVING_MSG = "Saving Files...";
    private static final String LOADING_MSG = "Loading File";
    private static final int MSG_WIDTH = 500;

    private ExecutorService threadPool = Executors.newFixedThreadPool(1);

    private CSBFinderController controller;

    private Menu menuBar;
    private Toolbar toolbar;

    private GenomePanel genomesPanel;
    private SummaryPanel summaryPanel;
    private ProgressBar progressBar;

    private InputParametersDialog inputParamsDialog;
    private ClusterDialog clusterDialog;
    private FilterDialog filterDialog;
    private SaveDialog saveDialog;

    private FamiliesFilter familiesFilter;

    private TablesHistory tablesHistory;

    private JFileChooser fc;

    public MainFrame(CSBFinderController controller) {
        super(PROGRAM_NAME);

        fc = new JFileChooser(System.getProperty("user.dir"));
        familiesFilter = new FamiliesFilter();
        tablesHistory = new TablesHistory();

        this.controller = controller;

        setLayout(new BorderLayout());
        initComponents();
        init();

        progressBar = new ProgressBar(this);

    }

    public void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(450, 350));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void clearPanels() {
        tablesHistory.clearAll();
        genomesPanel.clearPanel();
        summaryPanel.clearPanel();

        toolbar.disableClusterBtn();
        toolbar.disableSaveBtn();
        menuBar.disableSaveBtn();
        summaryPanel.disableFilterBtn();
    }

    public void initComponents() {
        Map<String, Color> colorsUsed = new HashMap<>();
        colorsUsed.put(controller.getUNKchar(), Color.lightGray);

        inputParamsDialog = new InputParametersDialog(fc);
        clusterDialog = new ClusterDialog();
        filterDialog = new FilterDialog();
        saveDialog = new SaveDialog(fc, this);

        toolbar = new Toolbar();
        genomesPanel = new GenomePanel(colorsUsed);

        summaryPanel = new SummaryPanel(Icon.FILTER.getIcon());

        menuBar = new Menu(fc, this);

        disableBtnsInit();

        setEventListeners();

        add(toolbar, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(genomesPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, top, summaryPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

    }

    private void setEventListeners() {
        setRunCSBFinderListener();
        setRunClusteringListener();
        setSelectParamsListener();
        setOpenSaveDialogListener();
        setClusterListener();
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
        setSaveButtonListener();
    }

    private void displayFamilyTable(List<Family> familyList) {
        if (familyList.size() > 0) {
            enableBtnsResultsDisplay();

            controller.calculateMainFunctionalCategory();

            summaryPanel.setFamilyData(familyList);
            familiesFilter.setFamilies(familyList);
        }
    }

    private void enableBtnsResultsDisplay(){
        menuBar.enableSaveFileBtn();
        toolbar.enableSaveBtn();
        summaryPanel.enableFilterBtn();
        toolbar.enableClusterBtn();
    }

    private void disableBtnsInit(){
        toolbar.disableSelectParamsBtn();

        toolbar.disableSaveBtn();
        menuBar.disableSaveBtn();

        summaryPanel.disableFilterBtn();
    }

    private void setGenomesData(String filePath){
        familiesFilter.clear();

        if (controller.getNumberOfGenomes() > 0) {
            inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            filterDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            genomesPanel.setGenomesInfo(controller.getGenomeInfo());

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

        familiesFilter.applyFilters();
        List<Family> filteredFamilies = familiesFilter.getFilteredFamilies();
        summaryPanel.setFilteredFamilies(filteredFamilies);

        tableRowClickFromHistory();

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
        filterDialog.setApplyFilterListener(e -> {
            FilterRequest request = e.getRequest();

            SwingUtilities.invokeLater(() -> filterDialog.setVisible(false));

            SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() {

                    genomesPanel.clearPanel();
                    setFilters(request);

                    return null;
                }
            };
            swingWorker.execute();
        });
    }

    private void setRunClusteringListener() {
        clusterDialog.setRunListener(e -> {

            SwingUtilities.invokeLater(() -> {
                clusterDialog.setVisible(false);
                progressBar.start(RUNNING_MSG);
            });

            SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                CSBFinderRequest request = e.getRequest();
                String msg = "";

                @Override
                protected Void doInBackground() {
                    clearPanels();
                    msg = controller.clusterToFamilies(request.getFamilyClusterThreshold(),
                            request.getClusterType(), request.getClusterDenominator());
                    return null;
                }

                @Override
                protected void done() {
                    progressBar.done("");
                    JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                }
            };
            swingWorker.execute();
        });
    }


    private void setRunCSBFinderListener() {
        inputParamsDialog.setRunListener(e -> {

            SwingUtilities.invokeLater(() -> {
                inputParamsDialog.setVisible(false);
                progressBar.start(RUNNING_MSG);
            });

            SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                CSBFinderRequest request = e.getRequest();
                String msg = "";

                @Override
                protected Void doInBackground() {
                    clearPanels();
                    request.setInputGenomeFilesPath(controller.getInputGenomesPath());
                    msg = controller.findCSBs(request);
                    return null;
                }

                @Override
                protected void done() {
                    progressBar.done("");
                    JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                }
            };
            swingWorker.execute();
        });
    }

    private void setClusterListener() {
        toolbar.setClusterListener(e -> {
            clusterDialog.setLocationRelativeTo(null);
            clusterDialog.setVisible(true);
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
        toolbar.setNumOfNeighborsListener(e -> genomesPanel.setNumOfNeighbors(e.getNumOfNeighbors()));
    }

    private void setShowOnlyTablesListener() {
        toolbar.setShowOnlyTablesListener(e -> {
            boolean isShowOnlyTables = e.isShowOnlyTables();
            if (isShowOnlyTables){
                genomesPanel.clearPanel();
            }else{
                tableRowClickFromHistory();
                //summaryPanel.fireTableDataChanged();
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
                            msg = controller.saveOutputFiles(e.getOutputType(), e.getOutputDirectory(),
                                    e.getDatasetName(), familiesFilter.getFilteredFamilies());
                            return null;
                        }

                        @Override
                        protected void done() {
                            progressBar.done("");
                            JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                        }
                    };

                    swingWorker.execute();
                }
            }
        };

        saveDialog.setSaveOutputListener(listener);

    }

    private String formatMsgWidth(String msg){
        String html = "<html><body width='%s'>%s";
        return String.format(html, MSG_WIDTH, msg);
    }

    /**
     * Load files listeners
     */
    private void setLoadButtonListener() {
        menuBar.setLoadGenomesListener(e -> {

            File f = e.getFilePath();
            if (f.exists() && !f.isDirectory()) {

                SwingUtilities.invokeLater(() -> progressBar.start(LOADING_MSG));

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    String msg = "";

                    @Override
                    protected Void doInBackground() {
                        msg = controller.loadInputGenomesFile(f.getPath());
                        return null;
                    }

                    @Override
                    protected void done() {
                        clearPanels();

                        setGenomesData(f.getPath());

                        progressBar.done("");

                        JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                    }
                };
                swingWorker.execute();
            }
        });
    }

    private void setImportSessionButtonListener() {
        menuBar.setImportSessionListener(e -> {

            File f = e.getFilePath();
            if (f.exists() && !f.isDirectory()) {

                SwingUtilities.invokeLater(() -> progressBar.start(LOADING_MSG));

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    String msg = "";

                    @Override
                    protected Void doInBackground() {
                        clearPanels();
                        msg = controller.loadSessionFile(f.getPath());
                        return null;
                    }

                    @Override
                    protected void done() {

                        setGenomesData(f.getPath());

                        progressBar.done("");
                        JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                    }
                };

                threadPool.submit(swingWorker);
            }
        });
    }

    private void setLoadCogInfoButtonListener() {
        menuBar.setLoadCogInfoListener(e -> {

            File f = e.getFilePath();
            if (f.exists() && !f.isDirectory()) {

                SwingUtilities.invokeLater(() -> progressBar.start(LOADING_MSG));

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    String msg = "";

                    @Override
                    protected Void doInBackground() {

                        msg = controller.loadCogInfo(f.getPath());
                        return null;
                    }

                    @Override
                    protected void done() {

                        progressBar.done("");
                        tableRowClickFromHistory();
                        JOptionPane.showMessageDialog(MainFrame.this, formatMsgWidth(msg));
                    }
                };
                swingWorker.execute();
            }
        });
    }


    /**
     * Tables click listeners
     */
    private void setPatternRowClickedListener() {
        summaryPanel.setPatternRowClickedListener(event -> {
            Pattern pattern = event.getRow();

            tablesHistory.setPattern(pattern);
            genomesPanel.clearPanel();
            genomesPanel.setNumOfNeighbors(toolbar.getNumOfNeighbors());
            if (!toolbar.isShowOnlyTables()) {
                genomesPanel.displayInstances(pattern);
            }

            List<COG> patternCOGs = controller.getCogsInfo(pattern.getPatternGenes());
            Set<COG> insertedGenes = controller.getInsertedGenes(pattern, patternCOGs);
            summaryPanel.setCogInfo(patternCOGs, insertedGenes, genomesPanel.getColorsUsed());
        });
    }

    private void setFamilyRowClickedListener() {
        summaryPanel.setFamilyRowClickedListener(event -> {
            Family family = event.getRow();

            tablesHistory.setFamily(family);
            summaryPanel.setFamilyPatternsData(family);
            genomesPanel.clearPanel();

            if (!toolbar.isShowOnlyTables()) {
                genomesPanel.displayPatterns(family.getPatterns());
            }

            List<COG> patternCOGs = new ArrayList<>();
            for (Pattern pattern : family.getPatterns()) {
                patternCOGs.addAll(controller.getCogsInfo(pattern.getPatternGenes()));
            }
            summaryPanel.setCogInfo(patternCOGs, new HashSet<>(), genomesPanel.getColorsUsed());
        });
    }

    private void setGeneTooltipListener() {
        genomesPanel.setGeneTooltipListener(event -> {
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
        genomesPanel.setGeneDoubleClickListener(event -> {
            GeneShape anchorGene = event.getAnchorGene();
            if (anchorGene == null){
                return;
            }

            String cogId = anchorGene.getLabel().getText();
            if (!cogId.equals(Alphabet.UNK_CHAR)) {

                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, event.getSrc());
                if (viewPort != null) {
                    Rectangle view = viewPort.getViewRect();
                    //int x = event.getGeneX() - view.x;
                    genomesPanel.alignGenes(anchorGene, view.x);
                }

            }
        });
    }

    private void setCSBFinderDoneListener() {
        controller.setCSBFinderDoneListener(e -> displayFamilyTable(e.getFamilyList()));
    }

}
