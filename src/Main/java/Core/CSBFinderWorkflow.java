package Core;

import Core.Genomes.Gene;
import Core.PostProcess.Family;
import Core.PostProcess.FamilyClustering;
import Core.SuffixTrees.DatasetTree;
import Core.SuffixTrees.PatternsTree;
import Core.Genomes.GenomesInfo;
import Core.Genomes.Pattern;
import Core.Genomes.PatternScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 */
public class CSBFinderWorkflow {

    //private DatasetTree datasetTree;
    //private PatternsTree patternsTree;

    Algorithm algorithm;

    List<Pattern> patternsFromFile;

    private Parameters params;
    private GenomesInfo gi;
    private int patternsCount;

    public CSBFinderWorkflow(GenomesInfo gi){
        Objects.requireNonNull(gi);

        this.params = null;
        this.gi = gi;

        patternsCount = 0;

        //datasetTree = null;
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

    public List<Family> run(Parameters params/*, List<Pattern> patternsFromFile*/){
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

    /*
    public List<Family> run(Parameters params){
        this.params = params;

        Algorithm mainAlgorithm = executeMainAlgorithm();
        if (mainAlgorithm == null){
            return new ArrayList<>();
        }

        List<Pattern> patterns = mainAlgorithm.getPatterns();

        List<Family> families = processPatterns(patterns);

        return families;
    }*/

    private List<Family> processPatterns(List<Pattern> patterns){

        patternsCount = patterns.size();
        computeScores(patterns);
        List<Family> families = clusterToFamilies(patterns);

        return families;
    }

    private Algorithm executeMainAlgorithm(){
        if (params == null || gi == null || algorithm == null){
            return null;
        }

        //if (params.nonDirectons != datasetTree.nonDirectons){
            //datasetTree = new DatasetTree(params.nonDirectons, gi);
            //algorithm.initialize(params, gi, patternsFromFile);
        //}
        algorithm.findPatterns();
        //SuffixTreeBasedAlgorithm mainAlgorithm = new SuffixTreeBasedAlgorithm(params, gi, patternsFromFile);

        return algorithm;

    }

    /*
    private void buildPatternsTree(List<Pattern> patternsFromFile){
        if (patternsFromFile.size() > 0){
            patternsTree = new PatternsTree(patternsFromFile, gi);
                    //new Trie(TreeType.STATIC);
            //datasetTree.buildPatternsTree(patternsFromFile, patternsTree, gi);
        }
    }*/

    private void computeScores(List<Pattern> patterns){

        PatternScore pattern_score = new PatternScore(gi.getMaxGenomeSize(), gi.getNumberOfGenomes(), gi.getDatasetLengthSum(),
                gi.cogToContainingGenomes, gi.genomeToCogParalogCount);

        for (Pattern pattern : patterns) {
            List<Integer> patternChars = new ArrayList<>();

            for (Gene gene: pattern.getPatternGenes()){
                patternChars.add(gi.getLetter(gene));
            }

            double score = PatternScore.computePatternScore(pattern_score, patternChars, params.maxInsertion, params.maxError,
                    params.maxDeletion, pattern.getInstanceCount());
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
