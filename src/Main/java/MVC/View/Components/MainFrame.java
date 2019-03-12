package MVC.View.Components;

import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.Dialogs.ClusterDialog;
import MVC.View.Components.Dialogs.FilterDialog;
import MVC.View.Components.Dialogs.InputParametersDialog;
import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Events.*;
import MVC.View.Images.Icon;
import MVC.View.Listeners.*;
import MVC.View.Components.Panels.GenomePanel;
import MVC.View.Components.Panels.SummaryPanel;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;
import Model.PostProcess.Family;
import MVC.View.Models.Filters.FamiliesFilter;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final String PROGRAM_NAME = "CSBFinder";

    private CSBFinderController controller;

    private Menu menuBar;
    private Toolbar toolbar;

    private GenomePanel genomesPanel;
    private SummaryPanel summaryPanel;
    private ProgressBar progressBar;

    private InputParametersDialog inputParamsDialog;
    private ClusterDialog clusterDialog;
    private FilterDialog filterDialog;

    private FamiliesFilter familiesFilter;

    private JFileChooser fc;

    public MainFrame(CSBFinderController controller) {
        super(PROGRAM_NAME);

        fc = new JFileChooser(System.getProperty("user.dir"));
        familiesFilter = new FamiliesFilter();

        this.controller = controller;

        setLayout(new BorderLayout());
        initComponents();
        init();

        progressBar = new ProgressBar(this);

    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getLookAndFeelDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

    public void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(450, 350));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void clearPanels() {
        genomesPanel.clearPanel();
        summaryPanel.clearPanel();
        toolbar.disableClusterBtn();
    }

    public void initComponents() {
        Map<String, Color> colorsUsed = new HashMap<>();
        colorsUsed.put(controller.getUNKchar(), Color.lightGray);

        inputParamsDialog = new InputParametersDialog(fc);
        clusterDialog = new ClusterDialog();
        filterDialog = new FilterDialog();

        toolbar = new Toolbar();
        genomesPanel = new GenomePanel(colorsUsed);

        summaryPanel = new SummaryPanel(Icon.FILTER.getIcon());
        summaryPanel.disableFilterBtn();

        menuBar = new Menu(fc, this);
        menuBar.disableSaveFileBtn();

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
        setClusterListener();
        setPatternRowClickedListener();
        setFamilyRowClickedListener();
        setFilterTableListener();
        setApplyFilterListener();
        setNumOfNeighborsListener();
        setCSBFinderDoneListener();
        setGeneTooltipListener();

        setMenuListeners();
    }

    private void setMenuListeners() {
        setLoadButtonListener();
        setImportSessionButtonListener();
        setLoadCogInfoButtonListener();
        setSaveButtonListener();
    }

    public void displayFamilyTable(List<Family> familyList) {
        if (familyList.size() > 0) {
            menuBar.enableSaveFileBtn();
            summaryPanel.enableFilterBtn();
            toolbar.enableClusterBtn();

            summaryPanel.setFamilyData(familyList);
            familiesFilter.setFamilies(familyList);
        }
    }

    private void setGenomesData(String filePath){
        if (controller.getNumberOfGenomes() > 0) {
            inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
            genomesPanel.setGenomesInfo(controller.getGenomeInfo());
            toolbar.enableSelectParamsBtn();

            setTitle(String.format("%s - %s", PROGRAM_NAME, filePath));
        } else {
            toolbar.disableSelectParamsBtn();
            menuBar.disableSaveFileBtn();
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
        filterRequest.getPatternGenes().ifPresent(val -> familiesFilter.setGenes(val));

        familiesFilter.applyFilters();

        summaryPanel.setFilteredFamilies(familiesFilter.getFilteredFamilies());

    }

    private void setApplyFilterListener() {
        filterDialog.setApplyFilterListener(new RunListener<FilterRequest>() {
            public void runEventOccurred(RunEvent<FilterRequest> e) {
                FilterRequest request = e.getRequest();

                SwingUtilities.invokeLater(() -> {
                    filterDialog.setVisible(false);
                });

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() {
                        try {
                            genomesPanel.clearPanel();
                            setFilters(request);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                swingWorker.execute();
            }
        });
    }

    private void setRunClusteringListener() {
        clusterDialog.setRunListener(new RunListener<CSBFinderRequest>() {
            public void runEventOccurred(RunEvent<CSBFinderRequest> e) {

                SwingUtilities.invokeLater(() -> {
                    clusterDialog.setVisible(false);
                    progressBar.start("Running");
                });

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    CSBFinderRequest request = e.getRequest();
                    String msg = "";

                    @Override
                    protected Void doInBackground() throws Exception {
                        clearPanels();
                        msg = controller.clusterToFamilies(request.getFamilyClusterThreshold(),
                                request.getClusterType(), request.getClusterDenominator());
                        return null;
                    }

                    @Override
                    protected void done() {
                        progressBar.done("");
                        JOptionPane.showMessageDialog(MainFrame.this, msg);
                    }
                };
                swingWorker.execute();
            }
        });
    }



    private void setRunCSBFinderListener() {
        inputParamsDialog.setRunListener(new RunListener<CSBFinderRequest>() {
            public void runEventOccurred(RunEvent<CSBFinderRequest> e) {

                SwingUtilities.invokeLater(() -> {
                    inputParamsDialog.setVisible(false);
                    progressBar.start("Running");
                });

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    CSBFinderRequest request = e.getRequest();
                    String msg = "";

                    @Override
                    protected Void doInBackground() throws Exception {
                        clearPanels();
                        request.setInputGenomeFilesPath(controller.getInputGenomesPath());
                        msg = controller.findCSBs(request);
                        return null;
                    }

                    @Override
                    protected void done() {
                        progressBar.done("");
                        JOptionPane.showMessageDialog(MainFrame.this, msg);
                    }
                };
                swingWorker.execute();
            }
        });
    }

    private void setClusterListener() {
        toolbar.setClusterListener(new Listener<OpenDialogEvent>() {
            @Override
            public void eventOccurred(OpenDialogEvent e) {
                clusterDialog.setLocationRelativeTo(null);
                clusterDialog.setVisible(true);
            }
        });
    }

    private void setSelectParamsListener() {
        toolbar.setSelectParamsListener(new Listener<OpenDialogEvent>() {
            @Override
            public void eventOccurred(OpenDialogEvent e) {
                inputParamsDialog.setLocationRelativeTo(null);
                inputParamsDialog.setVisible(true);
            }
        });
    }

    private void setNumOfNeighborsListener() {
        toolbar.setSetNumOfNeighborsListener(e -> genomesPanel.setNumOfNeighbors(e.getNumOfNeighbors()));
    }

    private void setFilterTableListener() {
        summaryPanel.setFilterTableListener(new Listener<OpenDialogEvent>() {
            @Override
            public void eventOccurred(OpenDialogEvent e) {
                filterDialog.setLocationRelativeTo(null);
                filterDialog.setVisible(true);
            }
        });
    }

    /**
     * Load files listeners
     */
    private void setLoadButtonListener() {
        menuBar.setLoadGenomesListener(new Listener<LoadFileEvent>() {

            @Override
            public void eventOccurred(LoadFileEvent e) {

                File f = e.getFilePath();
                if (f.exists() && !f.isDirectory()) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Loading File");
                        }
                    });

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                        String msg = "";

                        @Override
                        protected Void doInBackground() throws Exception {
                            msg = controller.loadInputGenomesFile(f.getPath());
                            return null;
                        }

                        @Override
                        protected void done() {
                            clearPanels();

                            setGenomesData(f.getPath());

                            progressBar.done("");
                            JOptionPane.showMessageDialog(MainFrame.this, msg);
                        }
                    };
                    swingWorker.execute();
                }
            }
        });
    }

    private void setImportSessionButtonListener() {
        menuBar.setImportSessionListener(new Listener<LoadFileEvent>() {

            @Override
            public void eventOccurred(LoadFileEvent e) {

                File f = e.getFilePath();
                if (f.exists() && !f.isDirectory()) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Loading File");
                        }
                    });

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                        String msg = "";

                        @Override
                        protected Void doInBackground() throws Exception {
                            clearPanels();
                            msg = controller.loadSessionFile(f.getPath());
                            return null;
                        }

                        @Override
                        protected void done() {

                            setGenomesData(f.getPath());

                            progressBar.done("");
                            JOptionPane.showMessageDialog(MainFrame.this, msg);
                        }
                    };
                    swingWorker.execute();
                }
            }
        });
    }

    private void setLoadCogInfoButtonListener() {
        menuBar.setLoadCogInfoListener(new Listener<LoadFileEvent>() {

            @Override
            public void eventOccurred(LoadFileEvent e) {

                File f = e.getFilePath();
                if (f.exists() && !f.isDirectory()) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Loading File");
                        }
                    });

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                        String msg = "";

                        @Override
                        protected Void doInBackground() throws Exception {

                            msg = controller.loadCogInfo(f.getPath());
                            return null;
                        }

                        @Override
                        protected void done() {

                            progressBar.done("");
                            summaryPanel.fireTableDataChanged();
                            JOptionPane.showMessageDialog(MainFrame.this, msg);
                        }
                    };
                    swingWorker.execute();
                }
            }
        });
    }


    private void setSaveButtonListener() {
        menuBar.setSaveOutputListener(new Listener<SaveOutputEvent>() {
            String msg = "";

            @Override
            public void eventOccurred(SaveOutputEvent e) {

                if (e.getAction() == JFileChooser.APPROVE_OPTION) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Saving files...");
                        }
                    });


                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            msg = controller.saveOutputFiles(e.getOutputType(), e.getOutputDirectory(),
                                    e.getDatasetName(), familiesFilter.getFilteredFamilies());
                            return null;
                        }

                        @Override
                        protected void done() {
                            progressBar.done("");
                            JOptionPane.showMessageDialog(MainFrame.this, msg);
                        }
                    };

                    swingWorker.execute();
                }
            }
        });
    }

    /**
     * Tables click listeners
     */
    private void setPatternRowClickedListener() {
        summaryPanel.setPatternRowClickedListener(new RowClickedListener<Pattern>() {
            @Override
            public void rowClickedOccurred(RowClickedEvent<Pattern> event) {
                Pattern pattern = event.getRow();

                genomesPanel.clearPanel();
                genomesPanel.setNumOfNeighbors(toolbar.getNumOfNeighbors());
                genomesPanel.displayInstances(pattern);

                List<COG> patternCOGs = controller.getCogsInfo(pattern.getPatternGenes());
                Set<COG> insertedGenes = controller.getInsertedGenes(pattern, patternCOGs);
                summaryPanel.setCogInfo(patternCOGs, insertedGenes, genomesPanel.getColorsUsed());
            }
        });
    }

    private void setFamilyRowClickedListener() {
        summaryPanel.setFamilyRowClickedListener(new RowClickedListener<Family>() {
            @Override
            public void rowClickedOccurred(RowClickedEvent<Family> event) {
                Family family = event.getRow();
                summaryPanel.setFamilyPatternsData(family);

                genomesPanel.clearPanel();
                genomesPanel.displayPatterns(family.getPatterns());

                List<COG> patternCOGs = new ArrayList<>();
                for (Pattern pattern : family.getPatterns()) {
                    patternCOGs.addAll(controller.getCogsInfo(pattern.getPatternGenes()));
                }
                summaryPanel.setCogInfo(patternCOGs, new HashSet<>(), genomesPanel.getColorsUsed());
            }
        });
    }

    private void setGeneTooltipListener() {
        genomesPanel.setGeneTooltipListener(new Listener<GeneTooltipEvent>() {
            @Override
            public void eventOccurred(GeneTooltipEvent event) {
                COG cog = controller.getCogInfo(event.getCogId());
                if (cog != null){
                    event.getSrc().setToolTipText(String.format("<html>%s<br>%s</html>",
                            String.join("/", cog.getFunctionalCategories()), cog.getCogDesc()));
                }
            }
        });
    }

    private void setCSBFinderDoneListener() {
        controller.setCSBFinderDoneListener(e -> displayFamilyTable(e.getFamilyList()));
    }

}
