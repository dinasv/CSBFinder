package Core;

import Core.Genomes.Gene;
import Core.PostProcess.Family;
import Core.PostProcess.FamilyClustering;

import Core.Genomes.GenomesInfo;
import Core.Patterns.Pattern;
import Core.Patterns.PatternScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 */
public class CSBFinderWorkflow {

    private Algorithm algorithm;

    private List<Pattern> patternsFromFile;

    private Parameters params;
    private GenomesInfo gi;
    private int patternsCount;

    public CSBFinderWorkflow(GenomesInfo gi){
        Objects.requireNonNull(gi);

        this.params = null;
        this.gi = gi;

        patternsCount = 0;

        this.algorithm = null;

        patternsFromFile = new ArrayList<>();
    }

    public void setPatternsFromFile(List<Pattern> patternsFromFile){
        this.patternsFromFile = patternsFromFile;
    }

    public void setAlgorithm(Algorithm algorithm){
        this.algorithm = algorithm;
        algorithm.setGenomesInfo(gi);
    }

    public List<Family> run(Parameters params){
        if (algorithm == null || params == null){
            return new ArrayList<>();
        }

        this.params = params;
        algorithm.setParameters(params);
        algorithm.setPatternsFromFile(patternsFromFile);

        algorithm.findPatterns();

        List<Pattern> patterns = algorithm.getPatterns();

        List<Family> families = processPatterns(patterns);

        return families;
    }

    private List<Family> processPatterns(List<Pattern> patterns){

        patternsCount = patterns.size();
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    private void computeScores(List<Pattern> patterns){

        PatternScore patternScore = new PatternScore(gi.getMaxGenomeSize(), gi.getNumberOfGenomes(), gi.getDatasetLengthSum(),
                gi.cogToContainingGenomes, gi.genomeToCogParalogCount);

        for (Pattern pattern : patterns) {
            List<Integer> patternLetters = new ArrayList<>();

            for (Gene gene: pattern.getPatternGenes()){
                patternLetters.add(gi.getLetter(gene));
            }

            double score = PatternScore.computePatternScore(patternScore, patternLetters, params.maxInsertion, params.maxError,
                    params.maxDeletion, pattern.getInstancesPerGenome());
            pattern.setScore(score);
        }
    }

    private List<Family> clusterToFamilies(List<Pattern> patterns){
        List<Family> families = FamilyClustering.Cluster(patterns, params.threshold, params.clusterBy, gi,
                params.nonDirectons);
        return families;
    }

    public int getPatternsCount() {
        return patternsCount;
    }
}
