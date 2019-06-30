package MVC.View.Models;

/**
 */

import Model.Genomes.Gene;
import Model.Patterns.Pattern;

import java.util.function.Function;

public enum PatternProperty implements ColumnProperty<Pattern> {

    ID(Pattern::getPatternId, String.class),
    LENGTH(Pattern::getLength, Integer.class),
    SCORE(Pattern::getScore, Double.class),
    INSTANCE_COUNT(Pattern::getInstancesPerGenome, Integer.class),
    CSB(Pattern::toString, String.class),
    MAIN_CATEGORY(Pattern::getMainFunctionalCategory, String.class),
    FAMILY_ID(Pattern::getFamilyId, Integer.class),
    GENES(Pattern::getPatternGenes, Gene[].class);

    private final Function<Pattern, ?> patternFunction;
    private final Class<?> returnType;

    PatternProperty(Function<Pattern, ?> patternFunction, Class<?> returnType){
        this.patternFunction = patternFunction;
        this.returnType = returnType;
    }

    public Function<Pattern, ?> getFunction(){
        return patternFunction;
    }

    public Class<?> getFunctionReturnClass(){
        return returnType;
    }
}
