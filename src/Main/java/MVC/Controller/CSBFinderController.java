package MVC.Controller;

import MVC.View.Listeners.CSBFinderDoneListener;
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
import java.util.Properties;
import java.util.Set;

public class CSBFinderController {

    private static final String PROPERTIES_FILE = "config.properties";
    private Properties properties;

    public CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {
        this.model = new CSBFinderModel();
        this.view = new MainFrame(this);

        properties = new Properties();

        readProperties();

    }

    private void readProperties(){
        FileReader reader = null;
        try {
            reader = new FileReader(PROPERTIES_FILE);
        } catch (FileNotFoundException e) {
            //ignore
        }

        try {
            properties.load(reader);

            String path = properties.getProperty("session");
            if (path != null){
                view.invokeLoadSessionListener(path);
            }

            path = properties.getProperty("orthology");
            if (path != null){
                view.invokeLoadCogInfoListener(path);
            }

            path = properties.getProperty("taxonomy");
            if (path != null){
                view.invokeLoadTaxaListener(path);
            }

        } catch (IOException e) {
            System.out.println("A problem occurred while reading " + PROPERTIES_FILE);
        }
    }

    public void addProperty(String key, String value){
        properties.setProperty(key, value);

        try {
            properties.store(new FileWriter(PROPERTIES_FILE), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setCSBFinderDoneListener(CSBFinderDoneListener listener){
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

    public void saveSession(List<Family> families, File currentSession) {
        this.model.saveSession(families, currentSession);
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

}


