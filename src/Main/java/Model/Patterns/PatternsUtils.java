package Model.Patterns;

import Model.Genomes.Gene;
import Model.Genomes.GenomesInfo;

import java.util.*;

public class PatternsUtils {
    public static void addSubPatternToRemoveList(Map<String, Pattern> patterns, String subPatternStr, Pattern pattern, HashSet<String> patternsToRemove) {
        Pattern subPattern = patterns.get(subPatternStr);

        if (subPattern != null) {
            int patternCount = pattern.getInstancesPerGenomeCount();
            int suffixCount = subPattern.getInstancesPerGenomeCount();
            if (suffixCount == patternCount) {
                patternsToRemove.add(subPatternStr);
            }
        }
    }

    public static void removeReverseCompliments(Map<String, Pattern> patterns, Pattern pattern, HashSet<String> patternsToRemove) {

        Gene[] reversePatternGenes = pattern.getReverseComplimentPattern();
        String reversedPatternStr = Pattern.toString(reversePatternGenes);
        Pattern reversedPattern = patterns.get(reversedPatternStr);

        String patternStr = pattern.toString();
        if (reversedPattern != null && !patternsToRemove.contains(patternStr)) {
            patternsToRemove.add(reversedPatternStr);
        }
    }

    public static List<Pattern> getLegalPatterns(List<Pattern> patterns, GenomesInfo gi){
        List<Pattern> legalPatterns = new ArrayList<>();
        for (Pattern pattern : patterns) {
            if (Arrays.stream(pattern.getPatternGenes()).allMatch(gene -> gi.getLetter(gene) != -1)){
                legalPatterns.add(pattern);
            }
        }
        return legalPatterns;
    }
}
