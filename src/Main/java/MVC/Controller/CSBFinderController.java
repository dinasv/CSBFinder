package MVC.Controller;

import Core.Genomes.Gene;
import Core.Genomes.Genome;
import MVC.Common.*;
import MVC.Model.CSBFinderModel;
import MVC.View.Components.MainFrame;
import Core.Genomes.COG;
import Core.Genomes.Pattern;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSBFinderController {

    private CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {
        this.model = new CSBFinderModel();
        this.view = new MainFrame(this);

        this.model.setCSBFinderDoneListener(new CSBFinderDoneListener() {
            @Override
            public void CSBFinderDoneOccurred(CSBFinderDoneEvent e) {
                view.displayFamilyTable(e.getFamilyList());
            }
        });
    }

    public String loadInputGenomesFile(String filePath) {

        return this.model.loadInputGenomesFile(filePath);
    }

    public String loadSessionFile(String filePath) throws IOException {
        return this.model.loadSessionFile(filePath);
    }

    public String loadCogInfo(String path){
        return this.model.loadCogInfo(path);
    }

    public void saveOutputFiles(String outputFileType) { this.model.saveOutputFiles(outputFileType); }

    public String findCSBs(CSBFinderRequest request) {
        return this.model.findCSBs(request);
    }

    public List<COG> getCogInfo(List<Gene> genes) {
        return model.getCogInfo(genes);
    }

    public Set<COG> getInsertedGenes(Map<String, Map<String, List<InstanceInfo>>> instances, List<COG> patternGenes){
        return model.getInsertedGenes(instances, patternGenes);
    }

    public Map<String, Map<String, List<InstanceInfo>>> getInstances(Pattern pattern) { return model.getInstances(pattern); }

    public Map<String, Genome> getGenomeMap() { return model.getGenomeMap(); }

    public int getMaxGenomeSize(){
        return model.getMaxGenomeSize();
    }

    public int getNumberOfGenomes() {
        return model.getNumberOfGenomes();
    }

    public String getUNKchar(){
        return model.getUNKchar();
    }

}


