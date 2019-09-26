package MVC.Controller;

import MVC.ProgramProperties;
import MVC.View.Graphics.GeneColors;
import MVC.View.Listeners.UpdateFamiliesListener;
import MVC.View.Requests.CSBFinderRequest;
import Model.ClusterBy;
import Model.ClusterDenominator;
import Model.Genomes.*;
import Model.OutputType;
import Model.PostProcess.Family;
import MVC.Model.CSBFinderModel;
import MVC.View.Components.MainFrame;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;
import com.beust.jcommander.ParameterException;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSBFinderController {

    private ProgramProperties properties;

    public CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {

        properties = new ProgramProperties();
        properties.loadProperties();

        this.model = new CSBFinderModel();
        this.view = new MainFrame(this, properties);

        invokeListeners();

    }

    private void invokeListeners(){
        String path = properties.getSessionPath();
        if (path != null){
            view.invokeLoadSessionListener(path);
        }

        path = properties.getCogInfoPath();
        if (path != null){
            view.invokeLoadCogInfoListener(path);
        }

        path = properties.getTaxaPath();
        if (path != null){
            view.invokeLoadTaxaListener(path);
        }
    }

    public void setCSBFinderDoneListener(UpdateFamiliesListener listener){
        model.setCSBFinderDoneListener(listener);
    }

    public void loadInputGenomesFile(String filePath) throws IOException {

        this.model.loadInputGenomesFile(filePath);
    }

    public void loadSessionFile(String filePath) throws IOException {
        this.model.loadSessionFile(filePath);
    }

    public void loadCogInfo(String path) throws IOException {
        this.model.loadCogInfo(path);
    }

    public void loadTaxa(String path) throws IOException {
        this.model.loadTaxa(path);
    }

    public void calculateMainFunctionalCategory(){
        this.model.calculateMainFunctionalCategory();
    }

    public void exportFiles(OutputType outputFileType, String outputDir, String datasetName,
                            List<Family> families) {
        this.model.exportFiles(outputFileType, outputDir, datasetName, families);
    }

    public void saveSession(List<Family> families, File currentSession, GeneColors colors) {
        this.model.saveSession(families, currentSession, colors);
    }

    public void findCSBs(CSBFinderRequest request) throws IOException, IllegalArgumentException {
        this.model.findCSBs(request);
    }

    public List<COG> getCogsInfo(Gene[] genes) {
        return model.getCogsInfo(genes);
    }

    public COG getCogInfo(String cogId) {
        return model.getCogInfo(cogId);
    }

    public Set<COG> getInsertedGenes(Pattern pattern, List<COG> patternCOGs){
        return model.getInsertedGenes(pattern, patternCOGs);
    }

    public int getMaxGenomeSize(){
        return model.getMaxGenomeSize();
    }

    public int getNumberOfGenomes() {
        return model.getNumberOfGenomes();
    }

    public GenomesInfo getGenomeInfo(){
        return model.getGenomeInfo();
    }

    public String getUNKchar(){
        return model.getUNKchar();
    }

    public String getInputGenomesPath(){
        return model.getInputGenomesPath();
    }

    public void clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator)
            throws ParameterException{
        model.clusterToFamilies(threshold, clusterBy, clusterDenominator);
    }

    public void computeScores(double threshold) throws ParameterException {
        model.computeScores(threshold);
    }

    public Map<String, Taxon> getGenomeToTaxa(){
        return model.getGenomeToTaxa();
    }

    public void setGeneColors(GeneColors colors){
        model.setGeneColors(colors);
    }

}


