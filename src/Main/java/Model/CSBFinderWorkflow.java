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
import java.util.stream.Collectors;

/**
 */
public class CSBFinderWorkflow {

    private Algorithm algorithm;

    private List<Pattern> patternsFromFile;

    private Parameters params;
    private GenomesInfo gi;
    private int patternsCount;

    private List<Pattern> patterns;
    private List<Family> families;

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

    public void run(Parameters params){
        if (algorithm == null || params == null){
            families = new ArrayList<>();
            params = new Parameters();
            algorithm = params.algorithmType.algorithm;
        }

        this.params = params;
        algorithm.setParameters(params);
        algorithm.setPatternsFromFile(patternsFromFile);

        int procCount = params.procCount == 0 ? Runtime.getRuntime().availableProcessors() : params.procCount;
        algorithm.setNumOfThreads(procCount);

        algorithm.findPatterns();

        patterns = algorithm.getPatterns();

        processPatterns();

    }

    private void processPatterns(){

        patternsCount = patterns.size();
        computeScores(patterns, params.delta);

        families = new ArrayList<>();
        if (params.skipClusterStep){
            families.add(new Family(0, gi, patterns));
        }else {
            clusterToFamilies(params.threshold, params.clusterBy, params.clusterDenominator);
        }
    }

    public void computeScores(double threshold){
        computeScores(patterns, threshold);
        families.forEach(Family::sortPatternsAndSetScore);
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

    public void setFamilies(List<Family> families){
        this.families = families;
        patterns = families.stream().map(Family::getPatterns).flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public void clusterToFamilies(double threshold, ClusterBy clusterBy, ClusterDenominator clusterDenominator){
        families = FamilyClustering.Cluster(patterns, threshold, clusterBy, clusterDenominator, gi);
    }

    public int getPatternsCount() {
        return patternsCount;
    }

    public List<Family> getFamilies(){
        return families;
    }
}
