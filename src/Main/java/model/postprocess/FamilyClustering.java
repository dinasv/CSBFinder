package model.postprocess;

import model.ClusterDenominator;
import model.genomes.Gene;
import model.genomes.GenomesInfo;
import model.genomes.Strand;
import model.patterns.Pattern;

import java.util.*;

import model.ClusterBy;

/**
 * Clusters patterns to families using a greedy method.
 */
public class FamilyClustering {

    public static List<Family> Cluster(List<Pattern> patterns, double threshold, ClusterBy clusterBy,
                                       ClusterDenominator clusterDenominator, GenomesInfo gi){

        List<Family> families = greedyClustering(patterns, threshold, clusterBy, clusterDenominator, gi);

        for (Family family: families){
            family.sortPatternsAndSetScore();
        }

        families.sort(new Family.ScoreComparator());
        return families;
    }

    private static boolean addToFamily(Family family, double threshold, Pattern currPattern,
                                       ClusterDenominator clusterDenominator, Set<Integer> currPatternGeneSet){

        Set<Integer> familySet = family.getGeneSet();

        int denominator = 1;
        if (clusterDenominator == ClusterDenominator.MIN_SET){
            denominator = Math.min(familySet.size(), currPatternGeneSet.size());

            threshold = denominator >= 1 && denominator <= 2 ? 1 : threshold;

        }else if (clusterDenominator == ClusterDenominator.MAX_SET){
            denominator = Math.max(familySet.size(), currPatternGeneSet.size());
        }else if (clusterDenominator == ClusterDenominator.UNION){
            Set<Integer> union = new HashSet<>(currPatternGeneSet);
            union.addAll(familySet);
            denominator = union.size();
        }

        Set<Integer> intersection = new HashSet<>(currPatternGeneSet);
        intersection.retainAll(familySet);

        if ((intersection.size() / (double) denominator) >= threshold) {
            family.addPattern(currPattern);
            return true;
        }
        return false;
    }

    private static List<Family> greedyClustering(List<Pattern> patterns, double threshold, ClusterBy clusterBy,
                                                 ClusterDenominator clusterDenominator, GenomesInfo gi){

        patterns.sort(clusterBy.patternComparator);

        List<Family> families = new ArrayList<>();
        Iterator<Pattern> it = patterns.iterator();
        if (it.hasNext()) {
            Pattern first_pattern = it.next();
            Family firstFamily = new Family(0, first_pattern, gi);

            families.add(firstFamily);

            while (it.hasNext()) {
                Pattern currPattern = it.next();

                Set<Integer> currPatternGeneSet = getGenesSet(currPattern.getPatternGenes(), gi);

                boolean addedPattern = false;
                for (Family family : families) {
                    addedPattern = addToFamily(family, threshold, currPattern, clusterDenominator, currPatternGeneSet);

                    if (addedPattern){
                        break;
                    }
                }

                if (!addedPattern) {
                    Family newFamily = new Family(families.size(), currPattern, gi);
                    families.add(newFamily);
                }
            }
        }

        return families;
    }

    private static Set<Integer> getGenesSet(Gene[] genes, GenomesInfo gi){
        Set<Integer> geneSet = new HashSet<>();
        for (Gene cog: genes) {
            int cogIndex = gi.getLetter(new Gene(cog.getCogId(), Strand.INVALID));
            if (cogIndex != -1) {
                geneSet.add(cogIndex);
            }
        }
        return geneSet;
    }

}
