package MVC.Controller;

import Model.ClusterBy;
import Model.ClusterDenominator;
import Model.Genomes.Gene;
import Model.Genomes.GenomesInfo;
import Model.OutputType;
import Model.PostProcess.Family;
import MVC.Common.*;
import MVC.Model.CSBFinderModel;
import MVC.View.Components.MainFrame;
import Model.OrthologyGroups.COG;
import Model.Patterns.Pattern;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class CSBFinderController {

    public CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {
        this.model = new CSBFinderModel();
        this.view = new MainFrame(this);
    }

    public void setCSBFinderDoneListener(CSBFinderDoneListener listener){
        model.setCSBFinderDoneListener(listener);
    }

    public String loadInputGenomesFile(String filePath) {

        return this.model.loadInputGenomesFile(filePath);
    }

    public String loadSessionFile(String filePath) {
        return this.model.loadSessionFile(filePath);
    }

    public String loadCogInfo(String path){
        return this.model.loadCogInfo(path);
    }

    public String saveOutputFiles(OutputType outputFileType, String outputDir, String datasetName,
                                  List<Family> families) {
        return this.model.saveOutputFiles(outputFileType, outputDir, datasetName, families);
    }

    public String findCSBs(CSBFinderRequest request) {
        return this.model.findCSBs(request);
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

    public String clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator){
        return model.clusterToFamilies(threshold, clusterBy, clusterDenominator);
    }

}


