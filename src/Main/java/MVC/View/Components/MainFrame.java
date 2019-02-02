package MVC.View.Components;

import Core.Genomes.GenomesInfo;
import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.Dialogs.FilterDialog;
import MVC.View.Components.Dialogs.InputParametersDialog;
import MVC.View.Components.Dialogs.ProgressBar;
import MVC.View.Events.*;
import MVC.View.Images.Icon;
import MVC.View.Listeners.*;
import MVC.View.Components.Panels.GenomePanel;
import MVC.View.Components.Panels.SummaryPanel;
import Core.OrthologyGroups.COG;
import Core.Patterns.Pattern;
import Core.PostProcess.Family;
import MVC.View.Requests.FilterRequest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private CSBFinderController controller;

    private Toolbar toolbar;
    private GenomePanel genomes;

    private InputParametersDialog inputParamsDialog;
    private FilterDialog filterDialog;

    private SummaryPanel summaryPanel;

    private ProgressBar progressBar;

    private Menu menuBar;

    private JFileChooser fc;

    public MainFrame(CSBFinderController controller) {
        super("CSBFinder");

        fc = new JFileChooser(System.getProperty("user.dir"));

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
        genomes.clearPanel();
        summaryPanel.clearPanel();
    }

    public void initComponents() {
        Map<String, Color> colorsUsed = new HashMap<>();
        colorsUsed.put(controller.getUNKchar(), Color.lightGray);

        inputParamsDialog = new InputParametersDialog(fc, Icon.QUESTION_MARK.getIcon());
        filterDialog = new FilterDialog();

        toolbar = new Toolbar(Icon.RUN.getIcon());
        genomes = new GenomePanel(colorsUsed);
        summaryPanel = new SummaryPanel(Icon.FILTER.getIcon());

        menuBar = new Menu(fc, this);
        menuBar.disableSaveFileBtn();

        setEventListeners();

        add(toolbar, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(genomes, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, top, summaryPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

    }

    private void setEventListeners() {
        setRunCSBFinderListener();
        setMenuListeners();
        setToolbarListener();
        setPatternRowClickedListener();
        setFamilyRowClickedListener();
        setFilterTableListener();
        setApplyFilterListener();
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
                    protected Void doInBackground() throws Exception {
                        summaryPanel.setFilterRequest(request);
                        return null;
                    }

                    /*
                    @Override
                    protected void done() {
                        progressBar.done("");
                        JOptionPane.showMessageDialog(MainFrame.this, msg);
                    }*/
                };
                swingWorker.execute();
            }
        });
    }


    private void setRunCSBFinderListener() {
        inputParamsDialog.setRunListener(new RunListener<CSBFinderRequest>() {
            public void runEventOccurred(RunEvent<CSBFinderRequest> e) {
                CSBFinderRequest request = e.getRequest();

                SwingUtilities.invokeLater(() -> {
                    inputParamsDialog.setVisible(false);
                    progressBar.start("Running");
                });

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

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

    private void setMenuListeners(){
        setLoadButtonListener();
        setImportSessionButtonListener();
        setLoadCogInfoButtonListener();
        setSaveButtonListener();
    }
    private void setToolbarListener() {
        setSelectParamsListener();
    }

    private void setSelectParamsListener() {
        toolbar.setSelectParamsListener(new SelectParamsListener() {
            @Override
            public void selectParamsOccurred(SelectParamsEvent e) {

                inputParamsDialog.setLocationRelativeTo(null);
                inputParamsDialog.setVisible(true);

            }
        });
    }

    private void setFilterTableListener() {
        summaryPanel.setFilterTableListener(new FilterTableListener() {
            @Override
            public void filterTableOccurred(FilterTableEvent e) {
                filterDialog.setLocationRelativeTo(null);
                filterDialog.setVisible(true);
            }
        });
    }


    private void setLoadButtonListener() {
        menuBar.setLoadGenomesListener(new LoadFileListener() {

            @Override
            public void loadFileEventOccurred(LoadFileEvent e) {

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
                            menuBar.disableSaveFileBtn();

                            if (controller.getNumberOfGenomes() > 0) {
                                inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
                                toolbar.enableSelectParamsBtn();
                            } else {
                                toolbar.disableSelectParamsBtn();
                            }
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
        menuBar.setImportSessionListener(new LoadFileListener() {

            @Override
            public void loadFileEventOccurred(LoadFileEvent e) {

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

                            menuBar.enableSaveFileBtn();
                            toolbar.enableSelectParamsBtn();

                            if (controller.getNumberOfGenomes() > 0) {
                                inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
                                toolbar.enableSelectParamsBtn();
                            } else {
                                toolbar.disableSelectParamsBtn();
                            }
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
        menuBar.setLoadCogInfoListener(new LoadFileListener() {

            @Override
            public void loadFileEventOccurred(LoadFileEvent e) {

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
        menuBar.setSaveOutputListener(new SaveOutputListener() {
            String msg = "";

            @Override
            public void saveOutputOccurred(SaveOutputEvent e) {

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
                                    e.getDatasetName());
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

    private void setPatternRowClickedListener() {
        summaryPanel.setPatternRowClickedListener(new RowClickedListener<Pattern>() {
            @Override
            public void rowClickedOccurred(RowClickedEvent<Pattern> event) {
                Pattern pattern = event.getRow();

                GenomesInfo genomesInfo = controller.getGenomeInfo();
                genomes.clearPanel();
                genomes.displayInstances(pattern, genomesInfo, 3);

                List<COG> patternCOGs = controller.getCogInfo(pattern.getPatternGenes());
                Set<COG> insertedGenes = controller.getInsertedGenes(pattern, patternCOGs);
                summaryPanel.setCogInfo(patternCOGs, insertedGenes, genomes.getColorsUsed());
            }
        });
    }

    private void setFamilyRowClickedListener() {
        summaryPanel.setFamilyRowClickedListener(new RowClickedListener<Family>() {
            @Override
            public void rowClickedOccurred(RowClickedEvent<Family> event) {
                Family family = event.getRow();
                summaryPanel.setFamilyPatternsData(family);

                genomes.clearPanel();
                genomes.displayPatterns(family.getPatterns());

                List<COG> patternCOGs = new ArrayList<>();
                for (Pattern pattern : family.getPatterns()) {
                    patternCOGs.addAll(controller.getCogInfo(pattern.getPatternGenes()));
                }
                summaryPanel.setCogInfo(patternCOGs, new HashSet<>(), genomes.getColorsUsed());
            }
        });
    }

    public void displayFamilyTable(List<Family> familyList) {
        menuBar.enableSaveFileBtn();
        summaryPanel.setFamilyData(familyList);
    }
}
