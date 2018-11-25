package MVC.View;

import Core.OutputType;
import MVC.Common.CSBFinderRequest;
import MVC.Common.InstanceInfo;
import MVC.Controller.CSBFinderController;
import MVC.View.Events.*;
import MVC.View.Listeners.*;
import MVC.View.Panels.GenomePanel;
import MVC.View.Panels.SummaryPanel;
import Core.Genomes.COG;
import Core.Genomes.Pattern;
import Core.PostProcess.Family;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {

    private CSBFinderController controller;

    private Toolbar toolbar;
    private GenomePanel genomes;

    InputParametersDialog inputParamsDialog;

    private SummaryPanel summaryPanel;

    private ProgressBar progressBar;

    private JFileChooser fc;

    public MainFrame(CSBFinderController controller) {
        super("CSBFinder");

        setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.PLAIN,16));

        fc = new JFileChooser(System.getProperty("user.dir"));

        this.controller = controller;

        setLayout(new BorderLayout());
        initComponents();
        init();

        progressBar = new ProgressBar(this);


    }

    public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getLookAndFeelDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }

    public void init() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(450, 350));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public void clearPanels(){
        genomes.clearPanel();
        summaryPanel.clearPanel();
    }

    public void initComponents() {
        Map<String, Color> colorsUsed = new HashMap<>();
        colorsUsed.put(controller.getUNKchar(), Color.lightGray);

        inputParamsDialog = new InputParametersDialog(fc);

        toolbar = new Toolbar(fc);
        genomes = new GenomePanel(colorsUsed);
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

                    String msg = "";

                    @Override
                    protected Void doInBackground() throws Exception {
                        clearPanels();
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

    private void setToolbarListener() {
        setLoadButtonListener();
        setImportSessionButtonListener();
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
                            toolbar.disableSaveFileBtn();

                            if (controller.getNumberOfGenomes() > 0) {
                                inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
                                toolbar.enableSelectParamsBtn();
                            }else{
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
        toolbar.setImportSessionListener(new LoadFileListener() {

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

                            toolbar.disableSaveFileBtn();
                            toolbar.enableSelectParamsBtn();

                            if (controller.getNumberOfGenomes() > 0) {
                                inputParamsDialog.setGenomeData(controller.getNumberOfGenomes(), controller.getMaxGenomeSize());
                                toolbar.enableSelectParamsBtn();
                            }else{
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

    private void setSaveButtonListener() {
        toolbar.setSaveOutputListener(new SaveOutputListener() {
            @Override
            public void saveOutputOccurred(SaveOutputEvent e) {

                //JOptionPane option = new JOptionPane();
                String[] ops = new String[] {
                        String.valueOf(OutputType.XLSX),
                        String.valueOf(OutputType.TXT) };
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
            public void rowClickedOccurred(FamilyRowClickedEvent event) {
                Pattern pattern = event.getPattern();
                Map<String, Map<String, List<InstanceInfo>>> instances = controller.getInstances(pattern);
                genomes.clearPanel();
                genomes.displayInstances(pattern.getPatternGenes(), instances);

                List<COG> patternCOGs = controller.getCogInfo(pattern.getPatternGenes());
                Set<COG> insertedGenes = controller.getInsertedGenes(instances, patternCOGs);
                summaryPanel.setCogInfo(patternCOGs, insertedGenes, genomes.getColorsUsed());
            }
        });
    }

    public void displayFamilyTable(List<Family> familyList) {
        toolbar.enableSaveFileBtn();
        summaryPanel.setFamilyData(familyList);
    }
}
