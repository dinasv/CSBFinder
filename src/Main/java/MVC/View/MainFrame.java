package MVC.View;

import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Components.HiddenPanel;
import MVC.View.Events.*;
import MVC.View.Listeners.*;
import CLI.CommandLineArgs;
import Utils.COG;
import Utils.Pattern;
import PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends JFrame {

    private CSBFinderController controller;

    private Toolbar toolbar;
    private GenomePanel genomes;

    InputParametersDialog inputParamsDialog;

    private SummaryPanel summaryPanel;

    private ProgressBar progressBar;

    public MainFrame(CSBFinderController controller) {
        super("CSB Finder");
        this.controller = controller;

        setLayout(new BorderLayout());
        initComponents();
        init();

        progressBar = new ProgressBar(this);
    }

    public void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(450, 350));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void initComponents() {

        inputParamsDialog = new InputParametersDialog();
        inputParamsDialog.pack();

        toolbar = new Toolbar();
        genomes = new GenomePanel();
        summaryPanel = new SummaryPanel();

        setEventListeners();

        add(toolbar, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout());
        top.add(genomes, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, top, summaryPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

    }

    private void setEventListeners() {
        setInputsListener();
        setToolbarListener();
        setFamilyRowClickedListener();
    }

    private void setInputsListener() {
        inputParamsDialog.setRunListener(new RunListener() {
            public void runEventOccurred(RunEvent e) {
                CSBFinderRequest request = e.getRequest();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        inputParamsDialog.setVisible(false);
                        progressBar.start("Running");
                    }
                });

                SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        controller.findCSBs(request);
                        return null;
                    }

                    @Override
                    protected void done() {
                        progressBar.done("");
                    }
                };
                swingWorker.execute();
            }
        });
    }

    private void setToolbarListener() {
        setLoadButtonListener();
        setSaveButtonListener();
        setSelectParamsListener();
    }

    private void setSelectParamsListener(){
        toolbar.setSelectParamsListener(new SelectParamsListener() {
            @Override
            public void selectParamsOccurred(SelectParamsEvent e) {

                inputParamsDialog.setLocationRelativeTo(null);
                inputParamsDialog.setVisible(true);

            }
        });
    }

    private void setLoadButtonListener() {
        toolbar.setLoadListener(new LoadFileListener() {

            @Override
            public void loadFileEventOccurred(LoadFileEvent e) {

                File f = e.getFilePath();
                if (f.exists() && !f.isDirectory()) {
                    // open option dialog for user to choose if he wants to run with directon option
                    boolean isDirecton = true;

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Loading Files");
                        }
                    });

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            controller.loadInputGenomesFile(f.getPath());
                            return null;
                        }

                        @Override
                        protected void done() {
                            progressBar.done("File Loaded Successfully");
                            if (controller.getGenomesLoaded() == -1) {
                                JOptionPane.showMessageDialog(MainFrame.this, "An error occurred while loading file");
                            } else {

                                inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
                                toolbar.enableSelectParamsBtn();
                            }
                        }
                    };
                    swingWorker.execute();
                }
            }
        });
    }

    private void setSaveButtonListener() {
        toolbar.setSaveOutputListener(new SaveOutputListener() {
            @Override
            public void saveOutputOccurred(SaveOutputEvent e) {

                //JOptionPane option = new JOptionPane();
                String[] ops = new String[] {
                        String.valueOf(CommandLineArgs.OutputType.XLSX),
                        String.valueOf(CommandLineArgs.OutputType.TXT) };
                int type = JOptionPane.showOptionDialog(
                        MainFrame.this,
                        "Please choose the desired type for the output files",
                        "Choose Output Type", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        ops,
                        true);

                String strType = type == JOptionPane.CLOSED_OPTION ? "" : ops[type];

                if (!"".equals(strType)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.start("Saving files...");
                        }
                    });

                    SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            controller.saveOutputFiles(strType);
                            return null;
                        }

                        @Override
                        protected void done() {
                            progressBar.done("");
                        }
                    };

                    swingWorker.execute();
                }
            }
        });
    }

    private void setFamilyRowClickedListener() {
        summaryPanel.setFamilyRowClickedListener(new FamilyRowClickedListener() {
            @Override
            public void rowClickedOccurred(FamilyRowClickedEvent e) {
                Pattern p = e.getPattern();
                List<COG> patternCOGs = controller.getCogInfo(Arrays.asList(p.getPatternArr()));
                genomes.displayInstances(p.getPatternArr(), controller.getInstances(p));
                summaryPanel.setCogInfo(patternCOGs, genomes.getColorsUsed());
            }
        });
    }

    public void displayFamilyTable(List<Family> familyList) {
        toolbar.enableSaveFileBtn();
        summaryPanel.setFamilyData(familyList);
    }
}
