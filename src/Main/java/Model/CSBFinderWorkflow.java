package Model;

import Model.Genomes.Gene;
import Model.PostProcess.Family;
import Model.PostProcess.FamilyClustering;

import Model.Genomes.GenomesInfo;
import Model.Patterns.Pattern;
import Model.Patterns.PatternScore;

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

    private List<Pattern> patterns;

    public CSBFinderWorkflow(GenomesInfo gi){
        Objects.requireNonNull(gi);

        this.params = null;
        this.gi = gi;

        patternsCount = 0;

        this.algorithm = null;

        patternsFromFile = new ArrayList<>();
        patterns = new ArrayList<>();
    }

    public void clear(){
        patternsFromFile.clear();
        patterns.clear();
    }

    public void setPatternsFromFile(List<Pattern> patternsFromFile){
        this.patternsFromFile = patternsFromFile;
    }

    public void setAlgorithm(Algorithm algorithm){
        this.algorithm = algorithm;
        algorithm.setGenomesInfo(gi);
    }

    public void setParameters(Parameters parameters){
        this.params = parameters;
    }

    public List<Family> run(Parameters params){
        if (algorithm == null || params == null){
            return new ArrayList<>();
        }

        this.params = params;
        algorithm.setParameters(params);
        algorithm.setPatternsFromFile(patternsFromFile);

        int procCount = params.procCount == 0 ? Runtime.getRuntime().availableProcessors() : params.procCount;
        algorithm.setNumOfThreads(procCount);

        algorithm.findPatterns();

        patterns = algorithm.getPatterns();

        List<Family> families = processPatterns();

        return families;
    }

    private List<Family> processPatterns(){

        patternsCount = patterns.size();
        computeScores(patterns, params.delta);

        List<Family> families = new ArrayList<>();
        if (params.skipClusterStep){
            families.add(new Family(0, gi, patterns));
        }else {
            families = clusterToFamilies(params.threshold, params.clusterBy, params.clusterDenominator);
        }
        return families;
    }

    public void computeScores(double threshold){
        computeScores(patterns, threshold);
    }

    private void computeScores(List<Pattern> patterns, double threshold){

        if (threshold != 1) {
            gi.computeDistancesBetweenGenomesAllVsAll();
        }
        PatternScore patternScore = new PatternScore(gi, threshold);

        for (Pattern pattern : patterns) {
            List<Integer> patternLetters = new ArrayList<>();

            for (Gene gene: pattern.getPatternGenes()){
                patternLetters.add(gi.getLetter(gene));
            }

            double score = patternScore.computePatternScore(patternLetters, params.maxInsertion,
                    pattern.getInstanceGenomeIds());
            pattern.setScore(score);
        }
    }

    public void setPatterns(List<Pattern> patterns){
        this.patterns = patterns;
    }

    public List<Family> clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator){
        return FamilyClustering.Cluster(patterns, threshold, clusterBy, clusterDenominator, gi);
    }

    public int getPatternsCount() {
        return patternsCount;
    }
}
