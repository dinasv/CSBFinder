package MVC.View.Events;

import Model.PostProcess.Family;

import java.util.List;

public class UpdateFamiliesEvent {

    private List<Family> familyList;

    public UpdateFamiliesEvent(List<Family> familyList) {
        this.familyList = familyList;
    }

    public List<Family> getFamilyList() {
        return familyList;
    }
}
