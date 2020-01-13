package mvc.view.components;

import mvc.view.events.*;

import mvc.view.events.Event;
import mvc.view.graphics.Icon;
import mvc.view.listeners.Listener;

import javax.swing.*;
import java.awt.*;

public class Toolbar extends JPanel{

    private static final String SELECT_PARAMS_BTN_NAME = "Find CSBs";
    private static final String CLUSTER_BTN_NAME = "Cluster to families";
    private static final String SAVE_BTN_NAME = "Save";
    private static final String RANK_BTN_NAME = "Compute Score";
    private static final String ZOOM_OUT_BTN_NAME = "Zoom Out";
    private static final String ZOOM_IN_BTN_NAME = "Zoom In";

    private JButton selectParamsBtn;
    private JButton clusterBtn;
    private JButton saveBtn;
    private JButton rankBtn;
    private JButton zoomOutBtn;
    private JButton zoomInBtn;

    private final Integer[] NEIGHBORS_VALUES = {0, 1, 2, 3, 4, 5, 6,  7, 8, 9, 10};

    private JPanel rightPanel;
    private JComboBox selectNumOfNeighbors;
    private JLabel selectNumOfNeighborsLabel = new JLabel("Neighbors:");
    private JCheckBox showOnlyTables;
    private JLabel showOnlyTablesLabel = new JLabel("tables only:");

    private Listener<OpenDialogEvent> selectParamsListener;
    private Listener<OpenDialogEvent> clusterListener;
    private Listener<OpenDialogEvent> saveListener;
    private Listener<OpenDialogEvent> rankListener;
    private Listener<Event> zoomOutListener;
    private Listener<Event> zoomInListener;
    private Listener<numOfNeighborsEvent> numOfNeighborsListener;
    private Listener<ShowOnlyTablesEvent> showOnlyTablesListener;

    public Toolbar() {

        setBorder(BorderFactory.createEtchedBorder());

        JPanel buttons = new JPanel(new FlowLayout());

        selectParamsBtn = createToolbarButton(Icon.RUN.getIcon(), SELECT_PARAMS_BTN_NAME);
        clusterBtn = createToolbarButton(Icon.CLUSTER.getIcon(), CLUSTER_BTN_NAME);
        saveBtn = createToolbarButton(Icon.SAVE.getIcon(), SAVE_BTN_NAME);
        rankBtn = createToolbarButton(Icon.RANK.getIcon(), RANK_BTN_NAME);
        zoomOutBtn = createToolbarButton(Icon.ZOOM_OUT.getIcon(), ZOOM_OUT_BTN_NAME);
        zoomInBtn = createToolbarButton(Icon.ZOOM_IN.getIcon(), ZOOM_IN_BTN_NAME);

        buttons.add(selectParamsBtn);
        buttons.add(clusterBtn);
        buttons.add(rankBtn);

        buttons.add(new JSeparator(SwingConstants.VERTICAL));
        buttons.add(zoomOutBtn);
        buttons.add(zoomInBtn);

        buttons.add(new JSeparator(SwingConstants.VERTICAL));
        buttons.add(saveBtn);

        selectNumOfNeighbors = new JComboBox<>(NEIGHBORS_VALUES);
        selectNumOfNeighbors.setSelectedIndex(3);

        showOnlyTables = new JCheckBox();

        rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout());
        rightPanel.add(showOnlyTablesLabel);
        rightPanel.add(showOnlyTables);
        rightPanel.add(selectNumOfNeighborsLabel);
        rightPanel.add(selectNumOfNeighbors);

        setLayout(new BorderLayout());

        add(buttons, BorderLayout.LINE_START);
        add(rightPanel, BorderLayout.LINE_END);

        addListeners();

    }

    private void addListeners(){
        selectParamsBtn.addActionListener(e -> selectParamsListener.eventOccurred(new OpenDialogEvent()));
        clusterBtn.addActionListener(e -> clusterListener.eventOccurred(new OpenDialogEvent()));
        saveBtn.addActionListener(e -> saveListener.eventOccurred(new OpenDialogEvent()));
        selectNumOfNeighbors.addActionListener(e -> numOfNeighborsListener.eventOccurred(
                new numOfNeighborsEvent(getNumOfNeighbors())));
        showOnlyTables.addActionListener(e -> showOnlyTablesListener.eventOccurred(
                new ShowOnlyTablesEvent(showOnlyTables.isSelected())));

        rankBtn.addActionListener(e -> rankListener.eventOccurred(new OpenDialogEvent()));

        zoomOutBtn.addActionListener(e -> zoomOutListener.eventOccurred(new SimpleEvent()));
        zoomInBtn.addActionListener(e -> zoomInListener.eventOccurred(new SimpleEvent()));
    }

    private JButton createToolbarButton(ImageIcon icon, String btnName){
        JButton btn = new JButton(icon);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setEnabled(false);
        btn.setToolTipText(btnName);

        return btn;
    }


    public void setSelectParamsListener(Listener<OpenDialogEvent> selectParamsListener) {
        this.selectParamsListener = selectParamsListener;
    }

    public void setClusterListener(Listener<OpenDialogEvent> clusterListener) {
        this.clusterListener = clusterListener;
    }

    public void setSaveListener(Listener<OpenDialogEvent> saveListener) {
        this.saveListener = saveListener;
    }

    public void setRankListener(Listener<OpenDialogEvent> rankListener) {
        this.rankListener = rankListener;
    }

    public void setZoomOutListener(Listener<Event> zoomOutListener) {
        this.zoomOutListener = zoomOutListener;
    }

    public void setZoomInListener(Listener<Event> zoomInListener) {
        this.zoomInListener = zoomInListener;
    }

    public void setNumOfNeighborsListener(Listener<numOfNeighborsEvent> numOfNeighborsListener) {
        this.numOfNeighborsListener = numOfNeighborsListener;
    }

    public void setShowOnlyTablesListener(Listener<ShowOnlyTablesEvent> setshowOnlyTablesListener) {
        this.showOnlyTablesListener = setshowOnlyTablesListener;
    }

    public void enableSelectParamsBtn() {
        selectParamsBtn.setEnabled(true);
    }

    public void disableSelectParamsBtn() {
        selectParamsBtn.setEnabled(false);
    }

    public void enableClusterBtn() {
        clusterBtn.setEnabled(true);
    }

    public void disableClusterBtn() {
        clusterBtn.setEnabled(false);
    }

    public void enableSaveBtn() {
        saveBtn.setEnabled(true);
    }

    public void disableSaveBtn() {
        saveBtn.setEnabled(false);
    }

    public void enableRankBtn() {
        rankBtn.setEnabled(true);
    }

    public void disablRankeBtn() {
        rankBtn.setEnabled(false);
    }

    public void enableZoomOutBtn() {
        zoomOutBtn.setEnabled(true);
    }

    public void disabZoomOutBtn() {
        zoomOutBtn.setEnabled(false);
    }

    public void enableZoomInBtn() {
        zoomInBtn.setEnabled(true);
    }

    public void disabZoomInBtn() {
        zoomInBtn.setEnabled(false);
    }

    public int getNumOfNeighbors(){
       return selectNumOfNeighbors.getSelectedIndex();
    }

    public boolean isShowOnlyTables(){
        return showOnlyTables.isSelected();
    }

}




