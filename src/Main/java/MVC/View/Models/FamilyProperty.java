package MVC.View.Models;

/**
 */

import Core.PostProcess.Family;

import java.util.function.Function;

public enum FamilyProperty implements ColumnProperty<Family> {

    FAMILY_ID(Family::getFamilyId, Integer.class),
    SCORE(Family::getScore, Double.class),
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
