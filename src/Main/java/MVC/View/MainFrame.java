package MVC.View;

import MVC.Common.CSBFinderRequest;
import MVC.Controller.CSBFinderController;
import MVC.View.Events.FamilyRowClickedEvent;
import MVC.View.Events.LoadFileEvent;
import MVC.View.Events.RunEvent;
import MVC.View.Events.SaveOutputEvent;
import MVC.View.Listeners.FamilyRowClickedListener;
import MVC.View.Listeners.LoadFileListener;
import MVC.View.Listeners.RunListener;
import MVC.View.Listeners.SaveOutputListener;
import CLI.CommandLineArgs;
import Utils.COG;
import Utils.Pattern;
import PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private CSBFinderController controller;

    private Toolbar toolbar;
    private GenomePanel genomes;
    private InputPanel inputs;

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
        toolbar = new Toolbar();
        inputs = new InputPanel();
        genomes = new GenomePanel();
        summaryPanel = new SummaryPanel();

        setEventListeners();

        add(toolbar, BorderLayout.NORTH);
        add(inputs, BorderLayout.WEST);
        add(genomes, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
        inputs.setVisible(false);
    }

    private void setEventListeners() {
        setInputsListener();
        setToolbarListener();
        setFamilyRowClickedListener();
    }

    private void setInputsListener() {
        inputs.setRunListener(new RunListener() {
            public void runEventOccurred(RunEvent e) {
                CSBFinderRequest request = e.getRequest();

//                request.setQuorumWithoutInsertions(5);
//                request.setGeneInfoFilePath("E:\\Coding\\Java\\CSBFinderCore\\input\\cog_info.txt");

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
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
                                inputs.setVisible(true);
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

                JOptionPane option = new JOptionPane();
                String[] ops = new String[] {
                        String.valueOf(CommandLineArgs.OutputType.XLSX),
                        String.valueOf(CommandLineArgs.OutputType.TXT) };
                int type = option.showOptionDialog(
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

//    public void displayInputPanel(int numOfGenomesLoaded) {
//
//    }

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

//    public void displayGenomesTable(Map<String,List<Gene>> genomeToGeneListMap) {
//        //TODO: Implement genomes view
//        genomes.setData(genomeToGeneListMap);
//    }

    public void displayFamilyTable(List<Family> familyList) {
        toolbar.enableSaveFileBtn();
        summaryPanel.setFamilyData(familyList);
    }
}
