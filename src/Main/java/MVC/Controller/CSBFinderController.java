package MVC.Controller;

import MVC.Common.*;
import MVC.Model.CSBFinderModel;
import MVC.View.MainFrame;
import Utils.COG;
import Utils.Pattern;
import Utils.Gene;
import Utils.Replicon;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSBFinderController {

    private CSBFinderModel model;
    private MainFrame view;

    public CSBFinderController() {
        this.model = new CSBFinderModel();
        this.view = new MainFrame(this);

//        this.model.setGenomesLoadedListener(new GenomesLoadedListener() {
//            @Override
//            public void genomesLoadDone(GenomesLoadEvent e) {
//                view.displayInputPanel(model.getNumberOfGenomes());
//            }
//        });

        this.model.setCSBFinderDoneListener(new CSBFinderDoneListener() {
            @Override
            public void CSBFinderDoneOccurred(CSBFinderDoneEvent e) {
                view.displayFamilyTable(e.getFamilyList());
            }
        });
    }

    public void loadInputGenomesFile(String file_path) {
        this.model.loadInputGenomesFile(file_path);
    }

    public void saveOutputFiles(String outputFileType) { this.model.saveOutputFiles(outputFileType); }

    public void findCSBs(CSBFinderRequest request) {
        this.model.findCSBs(request);
    }

    public List<COG> getCogInfo(List<String> cogs) {
        return model.getCogInfo(cogs);
    }

    public Set<COG> getInsertedGenes(Map<String, Map<String, List<InstanceInfo>>> instances, List<COG> patternGenes){
        return model.getInsertedGenes(instances, patternGenes);
    }

    public Map<String, Map<String, List<InstanceInfo>>> getInstances(Pattern pattern) { return model.getInstances(pattern); }

    public Map<String, Map<String, Replicon>> getGenomeMap() { return model.getGenomeMap(); }

    public int getGenomesLoaded() {
        return model.getNumberOfGenomes();
    }

    public int getMaxGenomeSize(){
        return model.getMaxGenomeSize();
    }

    public int getNumberOfGenomes() {
        return model.getNumberOfGenomes();
    }

}


