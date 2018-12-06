package MVC.View.Models;

/**
 */

import Core.Genomes.Pattern;

import java.util.Comparator;
import java.util.function.Function;

public enum PatternProperties {

    ID(Pattern::getPatternId, Integer.class),
    LENGTH(Pattern::getLength, Integer.class),
    SCORE(Pattern::getScore, Double.class),
    INSTANCE_COUNT(Pattern::getInstancesPerGenome, Integer.class),
    CSB(Pattern::toString, String.class),
    MAIN_CATEGORY(Pattern::getMainFunctionalCategory, String.class),
    FAMILY_ID(Pattern::getFamilyId, Integer.class);

    public final Function<Pattern, ?> patternFunction;
    public final Class<?> returnType;

    PatternProperties(Function<Pattern, ?> patternFunction, Class<?> returnType){
        this.patternFunction = patternFunction;
        this.returnType = returnType;
    }
}
