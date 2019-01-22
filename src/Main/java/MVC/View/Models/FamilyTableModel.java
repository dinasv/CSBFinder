package MVC.View.Models;

import Core.PostProcess.Family;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FamilyTableModel extends TableModel<Integer, Family> {

    public FamilyTableModel(ColumnProperty[] columns){
        super(columns);
    }

    public void setFamilies(List<Family> families) {

        setData(families);
        setIdToRow(families.stream()
                .collect(Collectors.toMap(Family::getFamilyId, Function.identity())));
    }

}
