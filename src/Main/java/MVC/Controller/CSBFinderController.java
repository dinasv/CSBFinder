package MVC.Controller;

import Model.ClusterBy;
import Model.ClusterDenominator;
import Model.Genomes.*;
import Model.OutputType;
import Model.PostProcess.Family;
import MVC.Common.*;
import MVC.Model.CSBFinderModel;
import MVC.View.Components.MainFrame;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;
import com.beust.jcommander.ParameterException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSBFinderController {

    public CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {
        this.model = new CSBFinderModel();
        this.view = new MainFrame(this);
        parseConfigFile();
    }

    private void parseConfigFile(){
        try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
            String line = br.readLine();
            while (line != null) {
                String[] splitLine= line.trim().split(":");

                String type = splitLine[0];
                String path = splitLine.length > 1 ? splitLine[1] : "";

                if (path.length() > 0) {
                    switch (type) {
                        case "session":
                            view.invokeLoadSessionListener(path);
                            break;
                        case "orthology":
                            view.invokeLoadCogInfoListener(path);
                            break;
                        case "taxonomy":
                            view.invokeLoadTaxaListener(path);
                            break;
                    }
                }

                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            //no config file
            System.out.println("No config.txt file");
        } catch (IOException e) {
            System.out.println("Problem reading config.txt file");
            //skip
        } catch (Exception e){
            System.out.println(e.getMessage());
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

    public void saveOutputFiles(OutputType outputFileType, String outputDir, String datasetName,
                                  List<Family> families) {
        this.model.saveOutputFiles(outputFileType, outputDir, datasetName, families);
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


