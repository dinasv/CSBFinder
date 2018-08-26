package MVC.Common;

import PostProcess.Family;

import java.util.List;

public class CSBFinderDoneEvent {

    private List<Family> familyList;

    public CSBFinderDoneEvent(List<Family> familyList) {
        this.familyList = familyList;
    }

    public List<Family> getFamilyList() {
        return familyList;
    }
}
