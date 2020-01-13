package mvc.view.tables;

import model.patterns.Pattern;
import model.postprocess.Family;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatternsTableModel extends CSBFinderTableModel<String, Pattern> {

    public PatternsTableModel(ColumnProperty[] columns){
        super(columns);
    }

    public void setFamilies(List<Family> families){
        families.forEach(family -> family.getPatterns()
                .forEach(pattern -> pattern.setFamilyId(family.getFamilyId())));

        setIdToObject(families.stream()
                .map(Family::getPatterns)
                .flatMap(List::stream)
                .collect(Collectors.toMap(Pattern::getPatternId, Function.identity())));

        setData(families.stream()
                .map(Family::getPatterns)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

}
