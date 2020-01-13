package mvc.view.tables;

/**
 */

import model.postprocess.Family;

import java.util.function.Function;

public enum FamilyProperty implements ColumnProperty<Family> {

    FAMILY_ID(Family::getFamilyId, Integer.class),
    SCORE(Family::getScore, Double.class),
    LONGEST_MEMBER(Family::getLongestPattern, Integer.class),
    MEMBERS(Family::size, Integer.class);

    private final Function<Family, ?> familyFunction;
    private final Class<?> returnType;

    FamilyProperty(Function<Family, ?> familyFunction, Class<?> returnType){
        this.familyFunction = familyFunction;
        this.returnType = returnType;
    }

    public Function<Family, ?> getFunction(){
        return familyFunction;
    }

    public Class<?> getFunctionReturnClass(){
        return returnType;
    }
}
