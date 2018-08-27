package MVC.Controller;

import MVC.Common.*;
import MVC.Model.CSBFinderModel;
import MVC.View.MainFrame;
import Utils.Pattern;
import Utils.Gene;

import java.util.List;
import java.util.Map;

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

    public void loadFile(String file_path, boolean is_directon) {
        this.model.loadFile(file_path, is_directon);
    }

    public void saveOutputFiles(String outputFileType) { this.model.saveOutputFiles(outputFileType); }

    public void findCSBs(CSBFinderRequest request) {
        this.model.findCSBs(request);
    }

    public Map<String, String> getCogInfo(List<String> cogs) {
        return model.getCogInfo(cogs);
    }

    public Map<String, List<List<Gene>>> getInstances(Pattern pattern) { return model.getInstances(pattern); }

    public int getGenomesLoaded() {
        return model.getNumberOfGenomes();
    }

//    public static void main(String[] args) {
//        CSBFinderController controller = new CSBFinderController();
//        controller.loadFile("E:\\Coding\\java\\CSBFinderCore\\input\\plasmid_genomes.fasta", true);
//
//        CSBFinderRequest request = new CSBFinderRequest();
////        request.setCsb_pattern_file_name("e:\\Coding\\java\\CSBFinderCore\\input\\cog_info.txt");
//        controller.findCSBs(request);
//
//
//    }


}
